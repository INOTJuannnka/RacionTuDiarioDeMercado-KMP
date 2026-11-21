package com.racion.app.shared.network

import com.racion.app.shared.models.AppError
import com.racion.app.shared.models.AppResult
import com.racion.app.shared.models.AppResult.Failure
import com.racion.app.shared.models.AppResult.Success
import com.racion.app.shared.models.FoodProduct
import com.racion.app.shared.network.dto.OffProductResponse
import com.racion.app.shared.network.dto.OffSearchResponse
import com.racion.app.shared.network.dto.toFoodProduct
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException

/**
 * Implementación real del catálogo con Ktor Client.
 *
 * Reemplaza `URLSession` (iOS) y Retrofit (Android original). La app no conoce Ktor:
 * consume [FoodCatalogService] y recibe `AppResult`.
 *
 * ⚠️ Un código inexistente devuelve **HTTP 200** con `status = 0` y `product = null`.
 * Mirar solo el código HTTP trataría un "no existe" como si existiera, por eso el
 * chequeo de `status` es obligatorio.
 */
class OpenFoodFactsService(
    private val client: HttpClient = defaultHttpClient(),
    private val baseUrl: String = BASE_URL,
    private val searchUrl: String = SEARCH_URL
) : FoodCatalogService {

    override suspend fun search(query: String): AppResult<List<FoodProduct>> {
        val clean = query.trim()
        if (clean.isEmpty()) return Success(emptyList())

        // `cgi/search.pl` corta con 503 tras ~1 consulta por ráfaga; el buscador nuevo de OFF no.
        val response = execute {
            client.get("$searchUrl/search") {
                parameter("q", clean)
                parameter("page_size", "20")
                parameter("fields", FIELDS)
            }
        } ?: return Failure(AppError.Network)

        response.errorOrNull()?.let { return Failure(it) }

        val dto = decode<OffSearchResponse>(response) ?: return Failure(AppError.Decoding)
        val products = dto.hits.orEmpty().mapNotNull { it.toFoodProduct() }
        return Success(products)
    }

    override suspend fun product(barcode: String): AppResult<FoodProduct> {
        val clean = barcode.trim()
        if (clean.isEmpty()) return Failure(AppError.NotFound)

        val response = execute {
            client.get("$baseUrl/api/v2/product/$clean.json") {
                parameter("fields", FIELDS)
            }
        } ?: return Failure(AppError.Network)

        response.errorOrNull()?.let { return Failure(it) }

        val dto = decode<OffProductResponse>(response) ?: return Failure(AppError.Decoding)
        if (dto.status != 1) return Failure(AppError.NotFound)

        val product = dto.product?.toFoodProduct() ?: return Failure(AppError.NotFound)
        return Success(product)
    }

    // MARK: - Transporte

    /**
     * Ejecuta la petición y colapsa cualquier fallo de conectividad en
     * [AppError.Network]: la acción que puede tomar la persona es la misma (revisar
     * la conexión), así que no tiene sentido exponer seis mensajes distintos.
     */
    private suspend inline fun execute(
        block: suspend () -> HttpResponse
    ): HttpResponse? =
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }

    private suspend inline fun <reified T> decode(response: HttpResponse): T? =
        try {
            response.body<T>()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }

    private fun HttpResponse.errorOrNull(): AppError? {
        val code = status
        if (code.isSuccess()) return null
        return when (code.value) {
            404 -> AppError.NotFound
            // Open Food Facts usa 503 para señalar el límite de peticiones y 429 el
            // estándar: los dos significan "esperá".
            429, 503 -> AppError.RateLimited
            else -> AppError.Server(code.value)
        }
    }

    companion object {
        const val BASE_URL = "https://world.openfoodfacts.org"
        const val SEARCH_URL = "https://search.openfoodfacts.org"

        /**
         * Solo se piden los campos que la app usa: el objeto completo trae decenas
         * de claves que nunca se leen.
         */
        private const val FIELDS =
            "code,product_name,brands,quantity,serving_quantity,categories," +
                "ingredients_text,nutrition_grades,image_front_url,nutriments"
    }
}
