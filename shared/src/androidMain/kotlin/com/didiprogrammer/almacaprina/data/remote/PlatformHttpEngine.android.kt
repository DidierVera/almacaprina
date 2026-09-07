package com.didiprogrammer.almacaprina.data.remote

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.ConnectionPool
import java.util.concurrent.TimeUnit

actual fun platformHttpEngine(): HttpClientEngine = OkHttp.create {
    config {
        // Ya es el default de OkHttp, pero explícito: reintenta con una conexión nueva
        // si la que tenía en el pool murió mientras la app estaba en segundo plano.
        retryOnConnectionFailure(true)
        // Recicla conexiones inactivas más agresivo que el default (5 min) para reducir
        // la ventana en la que una conexión "viva" en el pool en realidad ya está muerta.
        connectionPool(ConnectionPool(5, 1, TimeUnit.MINUTES))
    }
}
