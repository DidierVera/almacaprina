package com.didiprogrammer.almacaprina.data.remote

import com.didiprogrammer.almacaprina.util.newId
import io.github.jan.supabase.storage.storage

/**
 * Sube fotos al bucket público "goat-photos" de Supabase Storage (ver
 * supabase/migrations/0003_goat_photos_storage.sql) y devuelve la URL pública
 * para guardarla en Goat.photoUrl. El bucket es de solo-lectura pública;
 * solo el rol admin puede escribir (política RLS de storage.objects).
 */
object PhotoUploadService {
    private const val BUCKET = "goat-photos"

    suspend fun uploadGoatPhoto(goatId: String, bytes: ByteArray): String {
        val path = "$goatId/${newId()}.jpg"
        val bucket = SupabaseClientProvider.client.storage.from(BUCKET)
        bucket.upload(path, bytes) { upsert = true }
        return bucket.publicUrl(path)
    }
}
