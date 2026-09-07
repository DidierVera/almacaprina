package com.didiprogrammer.almacaprina.util

import kotlin.uuid.Uuid

/**
 * Genera un UUID client-side para nuevos registros antes de enviarlos a Supabase.
 * Se prefiere sobre depender del default de Postgres (gen_random_uuid()) porque el modelo
 * de dominio exige `id: String` no nulo al construir el objeto a insertar, y porque deja
 * el código listo para una futura caché local offline (SQLDelight, ver CLAUDE.md).
 */
fun newId(): String = Uuid.random().toString()
