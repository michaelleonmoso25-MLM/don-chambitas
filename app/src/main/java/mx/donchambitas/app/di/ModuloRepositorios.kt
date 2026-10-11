package mx.donchambitas.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import mx.donchambitas.app.datos.falso.RepositorioCatalogosFalso
import mx.donchambitas.app.datos.falso.RepositorioChatFalso
import mx.donchambitas.app.datos.falso.RepositorioIaFalso
import mx.donchambitas.app.datos.falso.RepositorioPostulacionesFalso
import mx.donchambitas.app.datos.falso.RepositorioResenasFalso
import mx.donchambitas.app.datos.falso.RepositorioServiciosFalso
import mx.donchambitas.app.datos.falso.RepositorioSolicitudesFalso
import mx.donchambitas.app.datos.falso.RepositorioTrabajadorFalso
import mx.donchambitas.app.datos.repositorio.RepositorioAuthReal
import mx.donchambitas.app.datos.repositorio.RepositorioUsuarioReal
import mx.donchambitas.app.dominio.repositorio.RepositorioAuth
import mx.donchambitas.app.dominio.repositorio.RepositorioCatalogos
import mx.donchambitas.app.dominio.repositorio.RepositorioChat
import mx.donchambitas.app.dominio.repositorio.RepositorioIa
import mx.donchambitas.app.dominio.repositorio.RepositorioPostulaciones
import mx.donchambitas.app.dominio.repositorio.RepositorioResenas
import mx.donchambitas.app.dominio.repositorio.RepositorioServicios
import mx.donchambitas.app.dominio.repositorio.RepositorioSolicitudes
import mx.donchambitas.app.dominio.repositorio.RepositorioTrabajador
import mx.donchambitas.app.dominio.repositorio.RepositorioUsuario

/**
 * Modulo de Hilt que enlaza las diez interfaces de repositorio del dominio
 * con sus implementaciones activas.
 *
 * RepositorioAuth y RepositorioUsuario ya enlazan a su implementacion real;
 * los otros ocho, a la falsa en memoria (FuenteDatosFalsa).
 * Cuando cada tarea real aterriza (S2-T07, S2-T12, S2-T14, S3-T09, S4-T09, S5-T07),
 * este archivo es el unico que cambia su enlace.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ModuloRepositorios {

    @Binds
    @Singleton
    abstract fun enlazarRepositorioAuth(
        impl: RepositorioAuthReal
    ): RepositorioAuth

    @Binds
    @Singleton
    abstract fun enlazarRepositorioUsuario(
        impl: RepositorioUsuarioReal
    ): RepositorioUsuario

    @Binds
    @Singleton
    abstract fun enlazarRepositorioTrabajador(
        impl: RepositorioTrabajadorFalso
    ): RepositorioTrabajador

    @Binds
    @Singleton
    abstract fun enlazarRepositorioServicios(
        impl: RepositorioServiciosFalso
    ): RepositorioServicios

    @Binds
    @Singleton
    abstract fun enlazarRepositorioSolicitudes(
        impl: RepositorioSolicitudesFalso
    ): RepositorioSolicitudes

    @Binds
    @Singleton
    abstract fun enlazarRepositorioPostulaciones(
        impl: RepositorioPostulacionesFalso
    ): RepositorioPostulaciones

    @Binds
    @Singleton
    abstract fun enlazarRepositorioChat(
        impl: RepositorioChatFalso
    ): RepositorioChat

    @Binds
    @Singleton
    abstract fun enlazarRepositorioResenas(
        impl: RepositorioResenasFalso
    ): RepositorioResenas

    @Binds
    @Singleton
    abstract fun enlazarRepositorioCatalogos(
        impl: RepositorioCatalogosFalso
    ): RepositorioCatalogos

    @Binds
    @Singleton
    abstract fun enlazarRepositorioIa(
        impl: RepositorioIaFalso
    ): RepositorioIa
}
