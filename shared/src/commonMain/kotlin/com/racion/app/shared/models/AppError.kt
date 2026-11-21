package com.racion.app.shared.models

/**
 * Taxonomía de fallos que la interfaz debe distinguir.
 *
 * Ningún error de red sin traducir llega a la UI: cada caso tiene su propio mensaje
 * porque un "algo salió mal" compartido no indica si reintentar sirve o no.
 */
sealed class AppError(val userMessage: String) {
    data object Network : AppError("Sin conexión. Revisá tu internet e intentá de nuevo.")
    data object NotFound : AppError("No encontramos ese producto en el catálogo.")
    data object RateLimited : AppError("Demasiadas consultas. Esperá un momento e intentá otra vez.")
    data object Decoding : AppError("Recibimos datos que no pudimos interpretar.")
    data class Server(val code: Int?, val detail: String? = null) :
        AppError("El servidor no respondió bien. Intentá de nuevo.")
    data class Unauthorized(val message: String) : AppError(message)
    data class Unknown(val detail: String? = null) :
        AppError("Ocurrió un error inesperado. Intentá de nuevo.")
}
