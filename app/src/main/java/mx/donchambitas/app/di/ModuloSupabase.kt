package mx.donchambitas.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton
import mx.donchambitas.app.BuildConfig
import mx.donchambitas.app.datos.local.crearAlmacenSesionCifrada
import mx.donchambitas.app.datos.remoto.supabase.ConfiguracionSupabase

@Module
@InstallIn(SingletonComponent::class)
object ModuloSupabase {

    /**
     * Solo la URL del proyecto y la anon key, que es publica por diseño: lo
     * que protege los datos son las politicas RLS. La llave de servicio del
     * proyecto no entra al APK por ningun motivo (AGENTS.md §6).
     */
    @Provides
    @Singleton
    fun proveerClienteSupabase(@ApplicationContext contexto: Context): SupabaseClient {
        check(BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) {
            "Faltan SUPABASE_URL y SUPABASE_ANON_KEY en local.properties. Nunca se suben al repositorio."
        }
        return createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY) {
            install(Auth) {
                // El flujo se queda en IMPLICIT, el de omision: con PKCE el
                // enlace de recuperacion pierde type=recovery (CONTRATOS-API.md).
                scheme = ConfiguracionSupabase.ESQUEMA_ENLACE
                host = ConfiguracionSupabase.HOST_ENLACE
                // Por omision supabase-kt guarda la sesion en texto plano en
                // las SharedPreferences (DEC-30).
                sessionManager = crearAlmacenSesionCifrada(contexto)
            }
            install(Postgrest)
            // Fotos de perfil, cubeta perfiles (S2-T14).
            install(Storage)
        }
    }
}
