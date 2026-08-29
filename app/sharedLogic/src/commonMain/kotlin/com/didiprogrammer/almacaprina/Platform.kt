package com.didiprogrammer.almacaprina

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform