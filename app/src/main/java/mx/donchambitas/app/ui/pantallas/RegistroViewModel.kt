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
import mx.donchambitas.app.dominio.validacion.LimitesRegistro
import mx.donchambitas.app.dominio.validacion.ValidacionesAuth
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

/**
 * ViewModel de la pantalla de registro (P-03).
 * Eventos de la seccion 3.4 y resultados de la 3.5 de
 * docs/producto/DISENO-AUTENTICACION.md.
 *
 * Por DEC-25 el registro deja la sesion abierta, asi que un alta exitosa
 * entra directo al inicio del rol elegido.
 */
@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val repositorioAuth: RepositorioAuth
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoRegistro())
    val estado: StateFlow<EstadoRegistro> = _estado.asStateFlow()

    // Solo se valida al salir de un campo en el que ya se escribio: pasar por
    // uno vacio camino a otro no es un error todavia (1.5).
    private val tocados = mutableSetOf<CampoRegistro>()

    fun alElegirRol(rol: RolUsuario) {
        _estado.update { it.copy(rol = rol, errorRol = null) }
    }

    fun alCambiarNombre(valor: String) {
        tocados += CampoRegistro.NOMBRE
        _estado.update {
            it.copy(nombre = valor.take(LimitesRegistro.LARGO_MAXIMO_NOMBRE), errorNombre = null)
        }
    }

    fun alCambiarApellidos(valor: String) {
        tocados += CampoRegistro.APELLIDOS
        _estado.update {
            it.copy(apellidos = valor.take(LimitesRegistro.LARGO_MAXIMO_APELLIDOS), errorApellidos = null)
        }
    }

    fun alCambiarCorreo(valor: String) {
        tocados += CampoRegistro.CORREO
        _estado.update {
            it.copy(correo = valor.take(LimitesRegistro.LARGO_MAXIMO_CORREO), errorCorreo = null)
        }
    }

    fun alCambiarContrasena(valor: String) {
        tocados += CampoRegistro.CONTRASENA
        _estado.update { it.copy(contrasena = valor, errorContrasena = null) }
    }

    fun alCambiarTelefono(valor: String) {
        tocados += CampoRegistro.TELEFONO
        // Se filtra al escribir en vez de validarse despues: no es una regla
        // de negocio, es no dejar teclear lo que el campo no admite.
        _estado.update {
            it.copy(
                telefono = valor.filter(Char::isDigit).take(LimitesRegistro.LARGO_TELEFONO),
                errorTelefono = null
            )
        }
    }

    fun alPerderFoco(campo: CampoRegistro) {
        if (campo in tocados) _estado.update { it.validado(campo) }
    }

    fun alRegistrar() {
        if (_estado.value.cargando) return
        val validado = CampoRegistro.entries.fold(
            _estado.value.copy(errorRol = ValidacionesAuth.validarRol(_estado.value.rol)?.mensaje())
        ) { parcial, campo -> parcial.validado(campo) }
        val rol = validado.rol
        if (rol == null || !validado.sinErrores()) {
            _estado.value = validado
            return
        }
        _estado.value = validado.copy(cargando = true, errorPantalla = null, mensajePantalla = null)

        viewModelScope.launch {
            // Normalizacion de 1.6, antes de que la base la exija.
            val resultado = repositorioAuth.registrar(
                correo = validado.correo.trim().lowercase(),
                contrasena = validado.contrasena,
                nombre = validado.nombre.trim(),
                apellidos = validado.apellidos.trim(),
                telefono = validado.telefono.trim(),
                rol = rol
            )
            _estado.update { actual ->
                when (resultado) {
                    is Resultado.Exito -> actual.copy(
                        cargando = false,
                        destino = inicioDelRol(resultado.dato.rol)
                    )
                    is Resultado.Error -> actual.copy(
                        cargando = false,
                        errorPantalla = resultado.tipo.paraPantalla(),
                        // CONTRATOS-API.md: en VALIDACION el mensaje ya viene en
                        // espanol desde la base y se muestra tal cual (3.3).
                        mensajePantalla = resultado.mensaje.takeIf {
                            resultado.tipo == TipoError.VALIDACION
                        }
                    )
                }
            }
        }
    }

    fun alReintentar() {
        _estado.update { it.copy(errorPantalla = null, mensajePantalla = null) }
        alRegistrar()
    }

    fun alConsumirDestino() {
        _estado.update { it.copy(destino = null) }
    }
}

private fun EstadoRegistro.validado(campo: CampoRegistro): EstadoRegistro = when (campo) {
    CampoRegistro.NOMBRE ->
        copy(errorNombre = ValidacionesAuth.validarNombre(nombre)?.mensaje())
    CampoRegistro.APELLIDOS ->
        copy(errorApellidos = ValidacionesAuth.validarApellidos(apellidos)?.mensaje())
    CampoRegistro.CORREO ->
        copy(errorCorreo = ValidacionesAuth.validarCorreo(correo)?.mensaje())
    CampoRegistro.CONTRASENA ->
        copy(errorContrasena = ValidacionesAuth.validarContrasenaRegistro(contrasena)?.mensaje())
    CampoRegistro.TELEFONO ->
        copy(errorTelefono = ValidacionesAuth.validarTelefono(telefono)?.mensaje())
}

private fun EstadoRegistro.sinErrores(): Boolean =
    listOf(errorRol, errorNombre, errorApellidos, errorCorreo, errorContrasena, errorTelefono)
        .all { it == null }
