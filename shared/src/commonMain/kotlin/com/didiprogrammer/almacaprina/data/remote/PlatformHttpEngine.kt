package com.didiprogrammer.almacaprina.data.remote

import io.ktor.client.engine.HttpClientEngine

/**
 * Motor HTTP por plataforma para el cliente de Supabase — ver [SupabaseClientProvider].
 * Android usa OkHttp en vez del motor CIO por defecto: OkHttp valida cada conexión antes
 * de reusarla y reintenta sola con una nueva si la anterior murió en silencio (ej. tras
 * minimizar la app varios minutos — Doze/ahorro de batería puede cortar el socket sin
 * avisar al cliente), que es justo el síntoma reportado ("todas las consultas fallan al
 * volver a la app después de un rato en segundo plano").
 */
expect fun platformHttpEngine(): HttpClientEngine
