package com.didiprogrammer.almacaprina.security

import android.content.Context

/** Se inicializa una sola vez desde MainActivity.onCreate, antes de setContent. */
object AndroidAppContext {
    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
        }
    }
}
