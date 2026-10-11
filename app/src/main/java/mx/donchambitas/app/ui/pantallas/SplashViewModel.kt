package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.TipoError

/**
 * ViewModel para la pantalla de bienvenida (P-01 Splash).
 * Lee la sesión real de RepositorioAuth y resuelve la ruta de destino (HU-03),
 * garantizando un tiempo mínimo de permanencia en pantalla de 800 ms para
 * evitar parpadeos.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repositorioAuth: RepositorioAuth
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoSplash())
    val estado: StateFlow<EstadoSplash> = _estado.asStateFlow()

    private var verificacion: Job? = null

    init {
        verificarSesion()
    }

    fun alReintentar() {
        verificarSesion()
    }

    private fun verificarSesion() {
        if (verificacion?.isActive == true) return
        _estado.value = EstadoSplash()
        verificacion = viewModelScope.launch {
            // Corre a la par de la lectura, no después: si la sesión tarda
            // 300 ms, P-01 dura 800, no 1100.
            val tiempoMinimo = launch { delay(TIEMPO_MINIMO_SPLASH_MS) }
            val destino = try {
                // sesionActual() no emite mientras no sepa si hay sesión, y
                // tampoco si la hay pero no pudo leer la ficha (sin red). Las
                // dos son silencio, así que el tope es lo único que las corta.
                withTimeoutOrNull(TIEMPO_MAXIMO_ESPERA_MS) {
                    destinoDe(repositorioAuth.sesionActual().first())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sin llaves en local.properties el cliente no se puede crear
                // y el flujo lanza. La aplicación abre igual y llega a P-02.
                Ruta.IniciarSesion
            }
            if (destino == null) {
                _estado.value = EstadoSplash(cargando = false, error = TipoError.RED)
                return@launch
            }
            tiempoMinimo.join()
            _estado.value = EstadoSplash(cargando = false, destino = destino)
        }
    }

    private fun destinoDe(sesion: Sesion?): Ruta =
        sesion?.let { inicioDelRol(it.usuario.rol) } ?: Ruta.IniciarSesion

    companion object {
        const val TIEMPO_MINIMO_SPLASH_MS: Long = 800L
        const val TIEMPO_MAXIMO_ESPERA_MS: Long = 8_000L
    }
}
