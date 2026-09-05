package com.didiprogrammer.almacaprina.data.remote

import com.didiprogrammer.almacaprina.domain.model.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.from

object AuthService {

    suspend fun signIn(email: String, password: String) {
        SupabaseClientProvider.client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        SupabaseClientProvider.client.auth.signOut()
    }

    fun currentUserId(): String? =
        SupabaseClientProvider.client.auth.currentUserOrNull()?.id

    /**
     * Trae el perfil (y por lo tanto el rol) del usuario que acaba de iniciar sesión.
     * Se usa justo después del login para decidir a qué Home mandarlo.
     */
    suspend fun fetchOwnProfile(): Profile? {
        val userId = currentUserId() ?: return null
        return SupabaseClientProvider.client.postgrest
            .from(SupabaseTables.PROFILES)
            .select {
                filter { eq("id", userId) }
            }
            .decodeSingleOrNull()
    }
}
