// PRUEBA TEMPORAL — borra esto cuando el login real del Paso 6 esté listo.
// Pégalo dentro de tu App() composable, reemplazando el contenido de ejemplo del wizard.

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.data.remote.AuthService
import com.didiprogrammer.almacaprina.domain.repository.GoatRepository
import org.koin.compose.koinInject

@Composable
fun ConnectionTestScreen(goatRepository: GoatRepository = koinInject()) {
    var result by remember { mutableStateOf("Probando conexión...") }

    LaunchedEffect(Unit) {
        result = try {
            // Reemplaza con el correo/contraseña reales de tu usuario Admin
            AuthService.signIn("german@almacaprina.com", "Cabra2026")
            val profile = AuthService.fetchOwnProfile()
            val goats = goatRepository.getAll()

            """
            ✅ Login exitoso
            Rol detectado: ${profile?.role}
            Cabras en la tabla: ${goats.size}
            """.trimIndent()
        } catch (e: Exception) {
            e.printStackTrace()
            "❌ Error: ${e.message}"
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(result)
    }
}