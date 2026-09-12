package com.didiprogrammer.almacaprina.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout

/**
 * Timeout prudente para conexiones lentas o intermitentes (ej. internet rural de la finca).
 * El default de Ktor es muy corto para ese escenario y produce "Connect timeout has expired"
 * en consultas normales — no es una señal de que la consulta esté mal, solo de que la red
 * tardó más de lo esperado en responder.
 */
private const val NETWORK_TIMEOUT_MILLIS = 30_000L

/**
 * Reintentos automáticos para fallas transitorias (ej. 504 Gateway Timeout del lado de
 * Supabase, cortes intermitentes de la red rural). Sin esto, cualquier hipo de red hace
 * fallar la operación de una sola vez y el usuario (ej. Campo registrando ordeño) tiene que
 * notarlo y reintentar a mano. Es seguro reintentar inserts/updates/deletes aquí porque:
 * - Insert: el id (UUID) lo genera el cliente ANTES de la petición y se reutiliza igual en
 *   el reintento — si el intento original en realidad sí se guardó (solo se perdió la
 *   respuesta), el reintento choca con la restricción de llave primaria (23505) en vez de
 *   crear una fila duplicada.
 * - Update/Delete: son idempotentes por diseño (filtran por id), repetirlos no cambia el
 *   resultado.
 */
private const val MAX_NETWORK_RETRIES = 3

object SupabaseClientProvider {
    @OptIn(SupabaseInternal::class)
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SupabaseConfig.SUPABASE_URL,
        supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
        // Realtime no se instala todavía — se agrega cuando de verdad necesitemos
        // sincronización en vivo entre dispositivos (ninguna pantalla lo requiere aún)

        // Motor HTTP explícito por plataforma — ver PlatformHttpEngine.kt. En Android usa
        // OkHttp en vez del CIO por defecto: valida las conexiones reusadas del pool y
        // evita que una conexión muerta tras minimizar la app tumbe todas las consultas.
        httpEngine = platformHttpEngine()

        httpConfig {
            install(HttpTimeout) {
                connectTimeoutMillis = NETWORK_TIMEOUT_MILLIS
                requestTimeoutMillis = NETWORK_TIMEOUT_MILLIS
                socketTimeoutMillis = NETWORK_TIMEOUT_MILLIS
            }
            install(HttpRequestRetry) {
                retryOnExceptionOrServerErrors(maxRetries = MAX_NETWORK_RETRIES)
                exponentialDelay()
            }
        }
    }
}
