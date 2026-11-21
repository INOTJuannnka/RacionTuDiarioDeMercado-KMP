package com.racion.app.shared.di

import com.racion.app.shared.data.LocalCatalog
import com.racion.app.shared.network.FirebaseAuthService
import com.racion.app.shared.network.FirestoreDiaryService
import com.racion.app.shared.network.FoodCatalogService
import com.racion.app.shared.network.OpenFoodFactsService
import com.racion.app.shared.network.defaultHttpClient
import com.racion.app.shared.persistence.RacionStorage
import com.racion.app.shared.state.AppStore
import com.racion.app.shared.state.DiarySync
import com.racion.app.shared.viewmodel.AddFoodViewModel
import com.racion.app.shared.viewmodel.ConfirmViewModel
import com.racion.app.shared.viewmodel.CreditsViewModel
import com.racion.app.shared.viewmodel.DiaryViewModel
import com.racion.app.shared.viewmodel.GoalsViewModel
import com.racion.app.shared.viewmodel.HomeViewModel
import com.racion.app.shared.viewmodel.LoginViewModel
import com.racion.app.shared.viewmodel.ReportViewModel
import com.racion.app.shared.viewmodel.ScannerViewModel
import com.racion.app.shared.viewmodel.SportProfileViewModel
import com.racion.app.shared.models.FoodProduct
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Composición raíz de dependencias (el `AppContainer` del proyecto Android).
 *
 * La plataforma solo aporta [Settings] (SharedPreferences / NSUserDefaults /
 * Preferences); todo lo demás se construye aquí, una sola vez, y se comparte entre
 * las pantallas.
 */
class AppContainer(
    settings: Settings,
    private val client: HttpClient = defaultHttpClient()
) {

    val storage: RacionStorage = RacionStorage(settings)
    val localCatalog: LocalCatalog = LocalCatalog()
    val catalog: FoodCatalogService = OpenFoodFactsService(client)
    val store: AppStore = AppStore(storage)
    val auth: FirebaseAuthService = FirebaseAuthService(client)

    /** Vive lo mismo que la app: sincroniza el diario con Firestore en segundo plano. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        DiarySync(store, auth, FirestoreDiaryService(client)).start(appScope)
    }

    // MARK: - ViewModels

    fun loginViewModel(): LoginViewModel = LoginViewModel(store, auth)

    fun homeViewModel(): HomeViewModel = HomeViewModel(store)

    fun diaryViewModel(): DiaryViewModel = DiaryViewModel(store)

    fun goalsViewModel(): GoalsViewModel = GoalsViewModel(store)

    fun reportViewModel(): ReportViewModel = ReportViewModel(store)

    fun creditsViewModel(): CreditsViewModel = CreditsViewModel(store)

    fun addFoodViewModel(): AddFoodViewModel = AddFoodViewModel(catalog, localCatalog, store)

    fun scannerViewModel(): ScannerViewModel = ScannerViewModel(store, catalog, localCatalog)

    fun confirmViewModel(product: FoodProduct): ConfirmViewModel =
        ConfirmViewModel(product, store)

    fun sportProfileViewModel(): SportProfileViewModel = SportProfileViewModel(store)

    /**
     * Libera el cliente HTTP. Las pantallas comparten el contenedor, así que solo se
     * llama cuando muere la aplicación (o la actividad raíz en Android).
     */
    fun close() {
        appScope.cancel()
        client.close()
    }
}
