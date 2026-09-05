package com.didiprogrammer.almacaprina.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    @SerialName("campo") CAMPO,
    @SerialName("ventas") VENTAS,
    @SerialName("admin") ADMIN
}

@Serializable
data class Profile(
    val id: String,
    val role: UserRole,
    @SerialName("full_name") val fullName: String? = null
)
