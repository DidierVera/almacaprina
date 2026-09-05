package com.didiprogrammer.almacaprina.data.remote

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest

/**
 * Funciones genéricas reutilizables por todos los repositorios.
 * Cada repositorio concreto (ver domain/repository + data/repository) solo
 * necesita indicar la tabla y el tipo — esta capa no vuelve a repetirse.
 */

suspend inline fun <reified T : Any> fetchAll(table: String): List<T> =
    SupabaseClientProvider.client.postgrest
        .from(table)
        .select()
        .decodeList()

suspend inline fun <reified T : Any> fetchById(table: String, id: String): T? =
    SupabaseClientProvider.client.postgrest
        .from(table)
        .select {
            filter { eq("id", id) }
        }
        .decodeSingleOrNull()

suspend inline fun <reified T : Any> insertRow(table: String, value: T): T =
    SupabaseClientProvider.client.postgrest
        .from(table)
        .insert(value) { select() }
        .decodeSingle()

suspend inline fun <reified T : Any> updateRow(table: String, id: String, value: T): T =
    SupabaseClientProvider.client.postgrest
        .from(table)
        .update(value) {
            filter { eq("id", id) }
            select()
        }
        .decodeSingle()

suspend fun deleteRow(table: String, id: String) {
    SupabaseClientProvider.client.postgrest
        .from(table)
        .delete {
            filter { eq("id", id) }
        }
}
