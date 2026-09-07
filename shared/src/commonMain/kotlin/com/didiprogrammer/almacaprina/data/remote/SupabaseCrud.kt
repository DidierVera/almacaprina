package com.didiprogrammer.almacaprina.data.remote

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Funciones genéricas reutilizables por todos los repositorios.
 * Cada repositorio concreto (ver domain/repository + data/repository) solo
 * necesita indicar la tabla y el tipo — esta capa no vuelve a repetirse.
 */

/**
 * Cuántas consultas a Supabase pueden estar en vuelo al mismo tiempo, en toda la app.
 *
 * Varias pantallas piden varias tablas en paralelo con `coroutineScope { async { ... } }`
 * (Inicio pide 15 a la vez, por ejemplo). En una conexión rápida eso reduce el tiempo total
 * al de la consulta más lenta — pero en internet rural o intermitente, disparar 15 conexiones
 * HTTPS a la vez reparte el ancho de banda disponible entre todas, y ninguna alcanza a
 * responder dentro del timeout (por eso el error "Request/Connect timeout has expired" salía
 * incluso después de subir el timeout a 30s). Con este límite, el resto simplemente espera su
 * turno en la cola — cada consulta que sí corre tiene el ancho de banda para ella sola y su
 * propio timeout completo desde que arranca.
 */
private const val MAX_CONCURRENT_SUPABASE_REQUESTS = 4

@PublishedApi
internal val networkSemaphore = Semaphore(MAX_CONCURRENT_SUPABASE_REQUESTS)

suspend inline fun <reified T : Any> fetchAll(table: String): List<T> = networkSemaphore.withPermit {
    SupabaseClientProvider.client.postgrest
        .from(table)
        .select()
        .decodeList()
}

suspend inline fun <reified T : Any> fetchById(table: String, id: String): T? = networkSemaphore.withPermit {
    SupabaseClientProvider.client.postgrest
        .from(table)
        .select {
            filter { eq("id", id) }
        }
        .decodeSingleOrNull()
}

suspend inline fun <reified T : Any> insertRow(table: String, value: T): T = networkSemaphore.withPermit {
    SupabaseClientProvider.client.postgrest
        .from(table)
        .insert(value) { select() }
        .decodeSingle()
}

suspend inline fun <reified T : Any> updateRow(table: String, id: String, value: T): T = networkSemaphore.withPermit {
    SupabaseClientProvider.client.postgrest
        .from(table)
        .update(value) {
            filter { eq("id", id) }
            select()
        }
        .decodeSingle()
}

suspend fun deleteRow(table: String, id: String) {
    networkSemaphore.withPermit {
        SupabaseClientProvider.client.postgrest
            .from(table)
            .delete {
                filter { eq("id", id) }
            }
    }
}
