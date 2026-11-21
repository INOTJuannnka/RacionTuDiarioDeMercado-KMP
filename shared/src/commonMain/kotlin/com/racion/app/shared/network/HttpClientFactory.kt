package com.racion.app.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Fábrica del cliente HTTP compartido.
 *
 * El motor (OkHttp / Darwin / CIO) se elige con [httpClientEngine], que cada target
 * implementa. El resto de la configuración vive aquí: una sola política de timeouts,
 * un único `User-Agent` y un JSON tolerante.
 */
fun defaultHttpClient(
    engine: HttpClientEngine = httpClientEngine(),
    configure: HttpClientConfig<*>.() -> Unit = {}
): HttpClient = HttpClient(engine) {
    // La app traduce los códigos HTTP a `AppError`; Ktor no debe lanzar antes.
    expectSuccess = false

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
                // Los cuerpos de Firebase Auth llevan valores fijos por defecto (requestUri, requestType).
                encodeDefaults = true
            }
        )
    }

    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MS
        connectTimeoutMillis = CONNECT_TIMEOUT_MS
        socketTimeoutMillis = SOCKET_TIMEOUT_MS
    }

    defaultRequest {
        // Open Food Facts bloquea agentes genéricos: este header es obligatorio.
        header(HttpHeaders.UserAgent, USER_AGENT)
        header(HttpHeaders.Accept, "application/json")
    }

    configure()
}

/** Motor de red de cada plataforma. */
expect fun httpClientEngine(): HttpClientEngine

const val USER_AGENT: String = "RacionTuDiarioDeMercado/1.0 (migracion academica KMP)"
internal const val REQUEST_TIMEOUT_MS: Long = 12_000
internal const val CONNECT_TIMEOUT_MS: Long = 12_000
internal const val SOCKET_TIMEOUT_MS: Long = 20_000
