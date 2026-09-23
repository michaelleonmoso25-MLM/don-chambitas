package mx.donchambitas.app.ui.pantallas

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.modelo.Usuario
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.util.DatosPrueba
import mx.donchambitas.app.util.Resultado

/**
 * RepositorioAuth que anota lo que recibe, para comprobar que los ViewModels
 * normalizan antes de llamar (1.6 de DISENO-AUTENTICACION.md). La fuente falsa
 * compara el correo sin distinguir mayusculas, asi que ahi no se nota.
 */
class RepositorioAuthEspia : RepositorioAuth {

    var ultimoCorreo: String? = null
        private set
    var ultimoNombre: String? = null
        private set
    var ultimosApellidos: String? = null
        private set
    var ultimoTelefono: String? = null
        private set

    override suspend fun registrar(
        correo: String,
        contrasena: String,
        nombre: String,
        apellidos: String,
        telefono: String?,
        rol: RolUsuario
    ): Resultado<Usuario> {
        ultimoCorreo = correo
        ultimoNombre = nombre
        ultimosApellidos = apellidos
        ultimoTelefono = telefono
        return Resultado.Exito(DatosPrueba.crearUsuario(rol = rol))
    }

    override suspend fun iniciarSesion(correo: String, contrasena: String): Resultado<Sesion> {
        ultimoCorreo = correo
        return Resultado.Exito(Sesion(DatosPrueba.crearUsuario()))
    }

    override suspend fun recuperarContrasena(correo: String): Resultado<Unit> = Resultado.Exito(Unit)

    override suspend fun cambiarContrasena(nueva: String): Resultado<Unit> = Resultado.Exito(Unit)

    override suspend fun cerrarSesion(): Resultado<Unit> = Resultado.Exito(Unit)

    override fun sesionActual(): Flow<Sesion?> = flowOf(null)
}
