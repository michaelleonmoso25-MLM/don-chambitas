package mx.donchambitas.app.ui.pantallas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.dominio.validacion.ValidacionesAuth
import mx.donchambitas.app.ui.navegacion.Ruta
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

/**
 * ViewModel de la pantalla de inicio de sesion (P-02).
 * Eventos de la seccion 2.3 y resultados de la 2.4 de
 * docs/producto/DISENO-AUTENTICACION.md.
 */
@HiltViewModel
class IniciarSesionViewModel @Inject constructor(
    private val repositorioAuth: RepositorioAuth
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoIniciarSesion())
    val estado: StateFlow<EstadoIniciarSesion> = _estado.asStateFlow()

    // Solo se valida al salir de un campo en el que ya se escribio: pasar por
    // uno vacio camino a otro no es un error todavia (1.5).
    private val tocados = mutableSetOf<CampoIniciarSesion>()

    fun alCambiarCorreo(valor: String) {
        tocados += CampoIniciarSesion.CORREO
        _estado.update { it.copy(correo = valor, errorCorreo = null) }
    }

    fun alCambiarContrasena(valor: String) {
        tocados += CampoIniciarSesion.CONTRASENA
        _estado.update { it.copy(contrasena = valor, errorContrasena = null) }
    }

    fun alPerderFoco(campo: CampoIniciarSesion) {
        if (campo in tocados) _estado.update { it.validado(campo) }
    }

    fun alIniciarSesion() {
        if (_estado.value.cargando) return
        val validado = CampoIniciarSesion.entries.fold(_estado.value) { parcial, campo ->
            parcial.validado(campo)
        }
        if (validado.errorCorreo != null || validado.errorContrasena != null) {
            _estado.value = validado
            return
        }
        _estado.value = validado.copy(cargando = true, errorPantalla = null)

        viewModelScope.launch {
            // Normalizacion de 1.6: ck_usuario_correo_minusculas rechaza el
            // correo tal como se escribio. En pantalla se sigue viendo lo tecleado.
            val resultado = repositorioAuth.iniciarSesion(
                correo = validado.correo.trim().lowercase(),
                contrasena = validado.contrasena
            )
            _estado.update { actual ->
                when (resultado) {
                    is Resultado.Exito -> actual.copy(
                        cargando = false,
                        destino = inicioDelRol(resultado.dato.usuario.rol)
                    )
                    is Resultado.Error -> actual.copy(
                        cargando = false,
                        errorPantalla = resultado.tipo.paraPantalla()
                    )
                }
            }
        }
    }

    fun alReintentar() {
        _estado.update { it.copy(errorPantalla = null) }
        alIniciarSesion()
    }

    fun alConsumirDestino() {
        _estado.update { it.copy(destino = null) }
    }
}

private fun EstadoIniciarSesion.validado(campo: CampoIniciarSesion): EstadoIniciarSesion =
    when (campo) {
        CampoIniciarSesion.CORREO ->
            copy(errorCorreo = ValidacionesAuth.validarCorreo(correo)?.mensaje())
        CampoIniciarSesion.CONTRASENA ->
            copy(errorContrasena = ValidacionesAuth.validarContrasenaInicioSesion(contrasena)?.mensaje())
    }

/** Pantalla de inicio de cada rol, a donde se entra despues de autenticarse. */
internal fun inicioDelRol(rol: RolUsuario): Ruta = when (rol) {
    RolUsuario.CLIENTE -> Ruta.InicioCliente
    RolUsuario.TRABAJADOR -> Ruta.InicioTrabajador
}

/** LIMITE_IA no aplica a la autenticacion: si llega, se pinta como DESCONOCIDO (seccion 6). */
internal fun TipoError.paraPantalla(): TipoError =
    if (this == TipoError.LIMITE_IA) TipoError.DESCONOCIDO else this
