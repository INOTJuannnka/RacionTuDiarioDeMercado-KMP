package com.racion.app.shared.network

import com.racion.app.shared.models.AppError
import com.racion.app.shared.models.AppResult
import com.racion.app.shared.models.DiaryEntry
import com.racion.app.shared.models.Nutrition
import com.racion.app.shared.persistence.RacionStorage
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

/**
 * Diario en Cloud Firestore por su API REST: un documento por día y por usuario.
 *
 *     users/{uid}/days/{yyyy-MM-dd}
 *       date, entryCount, totalNutrition, updatedAt, entries[]
 *
 * Las reglas desplegadas solo dejan leer/escribir esa ruta a su dueño
 * (`request.auth.uid == uid`), por eso cada llamada lleva el ID token del usuario.
 */
class FirestoreDiaryService(
    private val client: HttpClient,
    projectId: String = PROJECT_ID
) {
    private val base = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"
    private val json = RacionStorage.defaultJson()

    /** Todos los días guardados del usuario, por clave `yyyy-MM-dd`. */
    suspend fun fetchDays(uid: String, idToken: String): AppResult<Map<String, List<DiaryEntry>>> =
        request {
            // shortcut: una sola página de 300 días, sin nextPageToken; paginar cuando haya historiales más largos.
            client.get("$base/users/$uid/days") {
                bearerAuth(idToken)
                parameter("pageSize", 300)
            }
        }.map { body ->
            val documents = json.parseToJsonElement(body).jsonObject["documents"]?.jsonArray.orEmpty()
            documents.associate { document ->
                val day = document.jsonObject.getValue("name").jsonPrimitive.content.substringAfterLast('/')
                val fields = fromFirestoreFields(document.jsonObject["fields"]?.jsonObject ?: JsonObject(emptyMap()))
                day to json.decodeFromJsonElement<DayDocument>(fields).entries
            }
        }

    /** Reemplaza el documento del día. Un día sin entradas se borra. */
    suspend fun saveDay(uid: String, idToken: String, day: String, entries: List<DiaryEntry>): AppResult<Unit> {
        val url = "$base/users/$uid/days/$day"
        if (entries.isEmpty()) {
            return request { client.delete(url) { bearerAuth(idToken) } }.map { }
        }
        val document = DayDocument(
            date = day,
            entryCount = entries.size,
            totalNutrition = Nutrition.total(entries.map { it.totalNutrition }),
            entries = entries
        )
        val fields = toFirestoreFields(json.encodeToJsonElement(document).jsonObject).let {
            JsonObject(it + ("updatedAt" to buildJsonObject { put("timestampValue", Clock.System.now().toString()) }))
        }
        return request {
            client.patch(url) {
                bearerAuth(idToken)
                contentType(ContentType.Application.Json)
                setBody(JsonObject(mapOf("fields" to fields)).toString())
            }
        }.map { }
    }

    /** 401/403 = token vencido o ajeno: quien llama refresca el token y reintenta. */
    private suspend inline fun request(block: () -> HttpResponse): AppResult<String> = try {
        val response = block()
        val body = response.bodyAsText()
        when {
            response.status.isSuccess() || response.status.value == 404 -> AppResult.Success(body)
            response.status.value == 401 || response.status.value == 403 ->
                AppResult.Failure(AppError.Unauthorized("Tu sesión venció. Iniciá sesión de nuevo."))
            else -> {
                println("RacionFirestore HTTP ${response.status.value}: $body")
                AppResult.Failure(AppError.Server(response.status.value, body))
            }
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        AppResult.Failure(AppError.Network)
    }

    @Serializable
    internal data class DayDocument(
        val date: String,
        val entryCount: Int,
        val totalNutrition: Nutrition,
        val entries: List<DiaryEntry>
    )

    companion object {
        const val PROJECT_ID = "racion-tu-diario-de-mercado"
    }
}

// MARK: - JSON <-> valores tipados de Firestore ({"stringValue": …}, {"mapValue": …})

internal fun toFirestoreFields(obj: JsonObject): JsonObject =
    JsonObject(obj.mapValues { (_, value) -> toFirestoreValue(value) })

internal fun fromFirestoreFields(fields: JsonObject): JsonObject =
    JsonObject(fields.mapValues { (_, value) -> fromFirestoreValue(value.jsonObject) })

private fun toFirestoreValue(element: JsonElement): JsonObject = when (element) {
    is JsonNull -> buildJsonObject { put("nullValue", JsonNull) }
    is JsonObject -> buildJsonObject { put("mapValue", JsonObject(mapOf("fields" to toFirestoreFields(element)))) }
    is JsonArray -> buildJsonObject {
        put("arrayValue", JsonObject(mapOf("values" to JsonArray(element.map(::toFirestoreValue)))))
    }
    is JsonPrimitive -> when {
        element.isString -> buildJsonObject { put("stringValue", element.content) }
        element.booleanOrNull != null -> buildJsonObject { put("booleanValue", element.booleanOrNull) }
        // Firestore manda los enteros como texto ("integerValue": "539").
        element.longOrNull != null -> buildJsonObject { put("integerValue", element.content) }
        else -> buildJsonObject { put("doubleValue", element) }
    }
}

private fun fromFirestoreValue(value: JsonObject): JsonElement {
    val (type, raw) = value.entries.first()
    return when (type) {
        "mapValue" -> fromFirestoreFields(raw.jsonObject["fields"]?.jsonObject ?: JsonObject(emptyMap()))
        "arrayValue" -> JsonArray(raw.jsonObject["values"]?.jsonArray.orEmpty().map { fromFirestoreValue(it.jsonObject) })
        "integerValue" -> JsonPrimitive(raw.jsonPrimitive.content.toLong())
        "nullValue" -> JsonNull
        else -> raw // stringValue, booleanValue, doubleValue, timestampValue: ya son primitivos JSON.
    }
}
