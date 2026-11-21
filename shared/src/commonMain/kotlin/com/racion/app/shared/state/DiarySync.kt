package com.racion.app.shared.state

import com.racion.app.shared.data.AppFormat
import com.racion.app.shared.data.SeedData
import com.racion.app.shared.models.AppError
import com.racion.app.shared.models.AppResult
import com.racion.app.shared.models.DiaryEntry
import com.racion.app.shared.network.FirebaseAuthService
import com.racion.app.shared.network.FirestoreDiaryService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import com.racion.app.shared.models.NutritionGoals
import com.racion.app.shared.models.UserProfile
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Mantiene el diario local y Firestore alineados para quien inició sesión con cuenta.
 *
 * - Al iniciar sesión baja los días guardados: cada día remoto reemplaza al local.
 * - Después sube cada día cuyo contenido cambió (alta, borrado o porciones).
 *
 * El diario local sigue siendo la fuente de verdad de la UI; la nube es un respaldo
 * por usuario. Los invitados no sincronizan.
 */
class DiarySync(
    private val store: AppStore,
    private val auth: FirebaseAuthService,
    private val cloud: FirestoreDiaryService
) {
    /** Última versión de cada día que se sabe guardada en Firestore. */
    private val synced = mutableMapOf<String, List<DiaryEntry>>()

    fun start(scope: CoroutineScope): Job = scope.launch {
        store.state
            .map { state -> state.session?.takeIf { it.canSync }?.userId }
            .distinctUntilChanged()
            .collectLatest { uid ->
                synced.clear()
                if (uid == null) return@collectLatest

                when (val remote = authorized { cloud.fetchDays(uid, it) }) {
                    is AppResult.Success -> {
                        val local = store.state.value.entries.filterNot { dayKey(it) in remote.value }
                        // Días subidos por versiones que sembraban el ejemplo: se limpian y se vuelven a subir abajo.
                        val cloud = remote.value.values.flatten().filterNot { SeedData.isLegacyDemo(it) }
                        store.replaceEntries(local + cloud)
                        synced.putAll(remote.value)
                    }
                    is AppResult.Failure -> println("RacionSync no se pudo bajar el diario: ${remote.error}")
                }
                // Los días que ya estaban en el dispositivo no se suben solos: solo lo que cambie desde ahora.
                // Así el diario de demo o de otra cuenta no termina en esta cuenta.
                groupByDay(store.state.value.entries).forEach { (day, entries) -> synced.putIfAbsent(day, entries) }

                coroutineScope {
                    launch { syncMeta(uid) }
                    syncDiary(uid)
                }
            }
    }

    private suspend fun syncDiary(uid: String) {
        store.state.map { it.entries }.distinctUntilChanged().collect { entries ->
            val local = groupByDay(entries)
            for (day in local.keys + synced.keys) {
                val current = local[day].orEmpty()
                if (current == synced[day].orEmpty()) continue
                // shortcut: un día que falla al subir se reintenta solo con el próximo cambio; agregar cola offline si hace falta.
                if (authorized { cloud.saveDay(uid, it, day, current) } is AppResult.Success) {
                    synced[day] = current
                }
            }
        }
    }

    /**
     * Metas y perfil: lo de la nube gana al iniciar sesión; si la cuenta aún no tiene, se
     * sube lo del dispositivo. Después se sube cada cambio.
     */
    private suspend fun syncMeta(uid: String) {
        val goals = authorized { cloud.fetchDoc(uid, it, GOALS, NutritionGoals.serializer()) }
        val profile = authorized { cloud.fetchDoc(uid, it, PROFILE, UserProfile.serializer()) }
        if (goals is AppResult.Success && profile is AppResult.Success) {
            store.applyRemoteMeta(goals.value, profile.value)
        }
        // Nube vacía: se sube lo del dispositivo. Lectura fallida: no se pisa la nube hasta el próximo cambio.
        var lastGoals = if (goals is AppResult.Success) goals.value else store.state.value.goals
        var lastProfile = if (profile is AppResult.Success) profile.value else store.state.value.profile

        store.state.map { it.goals to it.profile }.distinctUntilChanged().collect { (g, p) ->
            if (g != lastGoals && authorized { cloud.saveDoc(uid, it, GOALS, NutritionGoals.serializer(), g) } is AppResult.Success) {
                lastGoals = g
            }
            if (p != lastProfile && authorized { cloud.saveDoc(uid, it, PROFILE, UserProfile.serializer(), p) } is AppResult.Success) {
                lastProfile = p
            }
        }
    }

    /** Ejecuta con el ID token actual; si venció, lo refresca una vez, lo guarda y reintenta. */
    private suspend fun <T> authorized(block: suspend (idToken: String) -> AppResult<T>): AppResult<T> {
        val session = store.state.value.session ?: return AppResult.Failure(AppError.Unauthorized("Sin sesión."))
        val first = session.idToken?.let { block(it) }
        if (first != null && !(first is AppResult.Failure && first.error is AppError.Unauthorized)) return first

        return when (val refreshed = auth.refresh(session)) {
            is AppResult.Success -> {
                store.setSession(refreshed.value)
                block(refreshed.value.idToken.orEmpty())
            }
            is AppResult.Failure -> refreshed
        }
    }

    private fun groupByDay(entries: List<DiaryEntry>) = entries.groupBy(::dayKey)

    companion object {
        private const val GOALS = "goals/main"
        private const val PROFILE = "profile/main"

        /** Clave del documento: fecha local `yyyy-MM-dd`. */
        fun dayKey(entry: DiaryEntry): String = AppFormat.localDate(entry.loggedAtEpochMillis).toString()
    }
}
