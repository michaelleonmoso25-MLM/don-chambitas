package mx.donchambitas.app.ui.pantallas

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.donchambitas.app.R
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.dominio.repositorio.RepositorioUsuario
import mx.donchambitas.app.dominio.validacion.LimitesRegistro
import mx.donchambitas.app.dominio.validacion.ValidacionesAuth
import mx.donchambitas.app.ui.navegacion.AvisosEnlace
import mx.donchambitas.app.util.Resultado
import mx.donchambitas.app.util.TipoError

/**
 * ViewModel de Mi cuenta (P-18), HU-05.
 * Especificado en docs/tareas/S2-T11.md.
 *
 * La ficha se pide a RepositorioUsuario y no a la Sesion, que no se entera de
 * la edicion (CONTRATOS-API.md, "Usuario"). El repositorio no valida ni
 * recorta: eso pasa aqui, con las mismas reglas que P-03.
 */
@HiltViewModel
class MiCuentaViewModel @Inject constructor(
    private val repositorioUsuario: RepositorioUsuario,
    private val repositorioAuth: RepositorioAuth,
    avisosEnlace: AvisosEnlace
) : ViewModel() {

    private val _estado = MutableStateFlow(
        EstadoMiCuenta(
            // Se llego por el enlace del correo: la seccion de contrasena ya abierta (DEC-27).
            seccion = if (avisosEnlace.tomarContrasenaPendiente()) {
                SeccionMiCuenta.CONTRASENA
            } else {
                SeccionMiCuenta.NINGUNA
            }
        )
    )
    val estado: StateFlow<EstadoMiCuenta> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun alReintentar() {
        cargar()
    }

    private fun cargar() {
        _estado.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            val resultado = repositorioUsuario.obtenerMiUsuario()
            _estado.update {
                when (resultado) {
                    is Resultado.Exito -> it.copy(usuario = resultado.dato, cargando = false)
                    is Resultado.Error -> it.copy(errorCarga = resultado.tipo, cargando = false)
                }
            }
        }
    }

    /** Abrir una seccion cierra la otra y descarta lo que tuviera escrito. */
    fun alAbrirSeccion(seccion: SeccionMiCuenta) {
        if (_estado.value.guardando) return
        val usuario = _estado.value.usuario
        _estado.update {
            it.limpio().copy(
                seccion = seccion,
                nombre = usuario?.nombre.orEmpty(),
                apellidos = usuario?.apellidos.orEmpty(),
                telefono = usuario?.telefono.orEmpty()
            )
        }
    }

    fun alCancelar() {
        if (_estado.value.guardando) return
        _estado.update { it.limpio() }
    }

    fun alCambiarNombre(valor: String) {
        _estado.update {
            it.copy(nombre = valor.take(LimitesRegistro.LARGO_MAXIMO_NOMBRE), errorNombre = null)
        }
    }

    fun alCambiarApellidos(valor: String) {
        _estado.update {
            it.copy(apellidos = valor.take(LimitesRegistro.LARGO_MAXIMO_APELLIDOS), errorApellidos = null)
        }
    }

    /** Igual que en P-03: no se deja teclear lo que el campo no admite. */
    fun alCambiarTelefono(valor: String) {
        _estado.update {
            it.copy(
                telefono = valor.filter(Char::isDigit).take(LimitesRegistro.LARGO_TELEFONO),
                errorTelefono = null
            )
        }
    }

    fun alCambiarContrasena(valor: String) {
        _estado.update { it.copy(contrasenaNueva = valor, errorContrasena = null) }
    }

    /**
     * El telefono es obligatorio, como en P-03 (DISENO-AUTENTICACION.md 5.3):
     * lo decidio el lider el 2026-10-11 al tomar S2-T11.
     */
    fun alGuardarDatos() {
        val actual = _estado.value
        if (actual.guardando) return
        val validado = actual.copy(
            errorNombre = ValidacionesAuth.validarNombre(actual.nombre)?.mensaje(),
            errorApellidos = ValidacionesAuth.validarApellidos(actual.apellidos)?.mensaje(),
            errorTelefono = ValidacionesAuth.validarTelefono(actual.telefono)?.mensaje()
        )
        if (listOf(validado.errorNombre, validado.errorApellidos, validado.errorTelefono).any { it != null }) {
            _estado.value = validado
            return
        }
        _estado.value = validado.copy(guardando = true)

        viewModelScope.launch {
            val resultado = repositorioUsuario.actualizarMiUsuario(
                nombre = validado.nombre.trim(),
                apellidos = validado.apellidos.trim(),
                telefono = validado.telefono.trim()
            )
            _estado.update {
                when (resultado) {
                    // La fila que devuelve el repositorio ya es la actualizada:
                    // no hace falta volver a leerla.
                    is Resultado.Exito -> it.limpio().copy(
                        usuario = resultado.dato,
                        mensaje = MensajeMiCuenta.Texto(R.string.mi_cuenta_datos_guardados)
                    )
                    is Resultado.Error -> it.copy(
                        guardando = false,
                        mensaje = MensajeMiCuenta.Error(resultado.tipo)
                    )
                }
            }
        }
    }

    /**
     * Solo se pide la nueva (decision 2 de S2-T11). Con el minimo de 8 en la
     * interfaz, weak_password no deberia llegar, asi que VALIDACION de
     * cambiarContrasena es same_password (CONTRATOS-API.md, errores de Auth).
     */
    fun alGuardarContrasena() {
        val actual = _estado.value
        if (actual.guardando) return
        val error = ValidacionesAuth.validarContrasenaRegistro(actual.contrasenaNueva)?.mensaje()
        if (error != null) {
            _estado.value = actual.copy(errorContrasena = error)
            return
        }
        _estado.value = actual.copy(guardando = true)

        viewModelScope.launch {
            val resultado = repositorioAuth.cambiarContrasena(actual.contrasenaNueva)
            _estado.update {
                when {
                    resultado is Resultado.Exito -> it.limpio().copy(
                        mensaje = MensajeMiCuenta.Texto(R.string.mi_cuenta_contrasena_actualizada)
                    )
                    resultado is Resultado.Error && resultado.tipo == TipoError.VALIDACION -> it.copy(
                        guardando = false,
                        errorContrasena = R.string.mi_cuenta_contrasena_igual
                    )
                    resultado is Resultado.Error -> it.copy(
                        guardando = false,
                        mensaje = MensajeMiCuenta.Error(resultado.tipo)
                    )
                    else -> it
                }
            }
        }
    }

    /**
     * No navega: si sale bien, sesionActual() emite null y SesionViewModel
     * lleva a P-02. Si falla (sin red), la sesion sigue abierta y se avisa.
     */
    fun alCerrarSesion() {
        if (_estado.value.cerrandoSesion) return
        _estado.update { it.copy(cerrandoSesion = true) }
        viewModelScope.launch {
            val resultado = repositorioAuth.cerrarSesion()
            if (resultado is Resultado.Error) {
                _estado.update {
                    it.copy(cerrandoSesion = false, mensaje = MensajeMiCuenta.Error(resultado.tipo))
                }
            }
        }
    }

    /**
     * Decisión 3 de S2-T13: se llama al repositorio aunque el real devuelva
     * error hasta S2-T14. Así S2-T14 solo cambia el repositorio.
     */
    fun alConfirmarFoto(bytes: ByteArray) {
        if (_estado.value.subiendoFoto) return
        _estado.update { it.copy(subiendoFoto = true) }
        viewModelScope.launch {
            val resultado = repositorioUsuario.subirFotoPerfil(bytes)
            _estado.update {
                when (resultado) {
                    is Resultado.Exito -> it.copy(
                        subiendoFoto = false,
                        usuario = it.usuario?.copy(fotoUrl = resultado.dato),
                        mensaje = MensajeMiCuenta.Texto(R.string.mi_cuenta_foto_actualizada)
                    )
                    is Resultado.Error -> it.copy(
                        subiendoFoto = false,
                        mensaje = MensajeMiCuenta.Error(resultado.tipo)
                    )
                }
            }
        }
    }

    /** La imagen no se pudo usar (pesada, de otro tipo, ilegible o sin cámara): solo se avisa. */
    fun alRechazarFoto(@StringRes motivo: Int) {
        _estado.update { it.copy(mensaje = MensajeMiCuenta.Texto(motivo)) }
    }

    fun alConsumirMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }
}

/** Sin seccion abierta, sin campos escritos y sin errores; la ficha y el mensaje se quedan. */
private fun EstadoMiCuenta.limpio(): EstadoMiCuenta = copy(
    seccion = SeccionMiCuenta.NINGUNA,
    nombre = "",
    apellidos = "",
    telefono = "",
    errorNombre = null,
    errorApellidos = null,
    errorTelefono = null,
    contrasenaNueva = "",
    errorContrasena = null,
    guardando = false
)
