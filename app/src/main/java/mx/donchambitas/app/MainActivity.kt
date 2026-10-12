package mx.donchambitas.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject
import kotlinx.coroutines.launch
import mx.donchambitas.app.datos.remoto.supabase.esEnlaceConSesion
import mx.donchambitas.app.datos.remoto.supabase.esEnlaceDeRecuperacion
import mx.donchambitas.app.datos.remoto.supabase.importarSesionDeEnlace
import mx.donchambitas.app.ui.navegacion.AvisosEnlace
import mx.donchambitas.app.ui.navegacion.GrafoNavegacion
import mx.donchambitas.app.ui.tema.DonChambitasTema

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Perezoso: el cliente solo se crea si llega un enlace de Auth, asi que
    // la aplicacion arranca aunque falten las llaves en local.properties.
    @Inject
    lateinit var supabase: Lazy<SupabaseClient>

    @Inject
    lateinit var avisosEnlace: AvisosEnlace

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        procesarEnlace(intent)
        setContent {
            DonChambitasTema {
                GrafoNavegacion()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        procesarEnlace(intent)
    }

    /**
     * Pasos 3 y 4 del recorrido de recuperacion (DEC-27): deja la sesion
     * importada y, si el enlace es de recuperacion, avisa para que la
     * navegacion lleve a P-18 (paso 5).
     */
    private fun procesarEnlace(intent: Intent?) {
        val datos = intent?.data ?: return
        val fragmento = datos.fragment
        if (!esEnlaceConSesion(datos.scheme, datos.host, fragmento) || fragmento == null) return
        // Sin llaves el cliente no se puede crear; un enlace no debe cerrar la aplicacion por eso.
        val cliente = runCatching { supabase.get() }.getOrNull() ?: return
        lifecycleScope.launch {
            val importada = cliente.importarSesionDeEnlace(fragmento)
            if (importada && esEnlaceDeRecuperacion(fragmento)) avisosEnlace.avisarRecuperacion()
        }
    }
}
