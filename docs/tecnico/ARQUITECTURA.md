# Arquitectura

## Stack

| Pieza | Elección |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | Jetpack Compose con Material 3 |
| Patrón | MVVM con estado inmutable |
| Inyección | Hilt |
| Navegación | Navigation Compose |
| Asincronía | Coroutines y Flow |
| Backend | Supabase (DEC-16) |
| Red | `supabase-kt`: Postgrest, Auth, Storage, Realtime y Functions (DEC-17) |
| Serialización | kotlinx.serialization |
| Local | DataStore para preferencias. La sesión de Supabase Auth va en DataStore cifrada con una llave del Android Keystore (`DEC-30`, `S2-T08`) |
| Imágenes | Coil |
| Pruebas | JUnit, MockK y Compose UI Test |
| minSdk | 26 |
| targetSdk | 35 |

No se cambia nada de esta tabla sin una decisión registrada en
`docs/control/DECISIONES.md`.

## Las tres capas

```
ui  ──────>  dominio  <──────  datos
```

`ui` y `datos` dependen de `dominio`. `dominio` no depende de nadie. Una
pantalla nunca importa nada de `datos`.

### dominio

Los modelos del negocio y las interfaces de los repositorios. Kotlin puro, sin
Android, sin Supabase, sin Compose. Si aquí aparece un `import android.*` o un
`import io.github.jan.supabase.*`, algo se hizo mal.

### datos

Las implementaciones. Cada repositorio tiene dos: la falsa y la real.

### ui

Pantallas en Compose y sus ViewModels. Una pantalla por archivo. La pantalla no
sabe de dónde salen los datos.

## Estructura de carpetas

```
app/src/main/java/mx/donchambitas/app/
├── DonChambitasApp.kt
├── MainActivity.kt
├── di/
│   ├── ModuloSupabase.kt
│   └── ModuloRepositorios.kt
├── dominio/
│   ├── modelo/         Usuario, PerfilTrabajador, Servicio, Solicitud...
│   └── repositorio/    Interfaces: RepositorioAuth, RepositorioServicios...
├── datos/
│   ├── remoto/
│   │   ├── supabase/   Cliente de Supabase y consultas
│   │   └── dto/        Objetos de transferencia
│   ├── local/          DataStore
│   ├── falso/          FuenteDatosFalsa y las implementaciones en memoria
│   └── repositorio/    Implementaciones reales
├── ui/
│   ├── tema/           Color.kt, Tipografia.kt, Formas.kt, Tema.kt
│   ├── componentes/    Botones, campos, tarjetas, estados
│   ├── navegacion/     Rutas.kt, GrafoNavegacion.kt
│   └── pantallas/
│       ├── autenticacion/
│       ├── cliente/
│       ├── trabajador/
│       └── compartidas/
└── util/
```

## La regla del repositorio

PEND-01 se cerró el 2026-09-14: el backend es **Supabase** (DEC-16). Eso **no**
deroga esta regla, la mantiene. Es lo que permite que cada implementación real
entre en su turno sin tocar una sola pantalla.

**1.** El repositorio se define como interfaz en `dominio/repositorio/`.

```kotlin
interface RepositorioServicios {
    suspend fun obtenerPorTrabajador(trabajadorId: String): Resultado<List<Servicio>>
    suspend fun crear(servicio: Servicio): Resultado<Servicio>
}
```

**2.** La implementación falsa vive en `datos/falso/` y devuelve datos en
memoria, con un retraso artificial de unos 300 ms para que los estados de carga
se puedan probar de verdad. **Sigue siendo la implementación activa** hasta que
la tarea real de cada módulo aterrice.

**3.** La implementación real vive en `datos/repositorio/`, habla con Supabase a
través de `supabase-kt` y **nunca** se filtra hacia arriba: el dominio no sabe
que Supabase existe.

**4.** Hilt decide cuál se usa, en un solo lugar:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class ModuloRepositorios {
    @Binds
    abstract fun enlazarRepositorioServicios(
        impl: RepositorioServiciosFalso
    ): RepositorioServicios
}
```

El día que S3-T09 termine, se cambia **esa línea** por `RepositorioServiciosReal`.
Ni una pantalla, ni un ViewModel, ni una prueba se tocan. Esa es toda la razón
de ser de este diseño.

## Qué pone Supabase y qué ponemos nosotros

| Pieza | Quién la resuelve | Tarea |
|---|---|---|
| Registro, sesión, refresco de token, recuperación | Supabase Auth | S2-T07 |
| Base de datos y consultas | Postgrest sobre el esquema de `basedatos/` | S3-T09, S4-T09 |
| Operaciones atómicas | Funciones RPC, ya escritas en `01_esquema.sql` | S3-T09, S5-T07 |
| Foto de perfil | Supabase Storage, cubeta `perfiles` | S2-T14 |
| Fotos de servicio | Supabase Storage, cubeta `servicios` | S3-T09 (`H-16`) |
| Chat en vivo | Supabase Realtime | S5-T07 |
| Proxy de IA con la llave de OpenAI | Edge Function | S4-T10 |
| Control de acceso | Políticas RLS, ya escritas en `basedatos/02_politicas_rls.sql` | las valida S1-T03 |

**Las llaves de Supabase que viven en la aplicación son la URL del proyecto y la
`anon key`, y nada más.** La `anon key` es pública por diseño: lo que protege
los datos son las políticas RLS, no el secreto de esa llave. La `service_role`
key **nunca** entra al repositorio ni al APK, por ningún motivo. Igual que la
llave de OpenAI, que vive solo en la Edge Function. Ver `docs/producto/IA.md`.

## Estado de la interfaz

Cada pantalla tiene una `data class` de estado inmutable y el ViewModel la
expone como `StateFlow`.

```kotlin
data class EstadoIniciarSesion(
    val correo: String = "",
    val contrasena: String = "",
    val cargando: Boolean = false,
    val errorCorreo: String? = null,
    val errorGeneral: String? = null,
    val sesionIniciada: Boolean = false
)
```

Nunca se exponen varios `StateFlow` sueltos por pantalla. Uno solo, con todo
dentro.

## Manejo de errores

Todo lo que puede fallar devuelve `Resultado<T>`:

```kotlin
sealed interface Resultado<out T> {
    data class Exito<T>(val dato: T) : Resultado<T>
    data class Error(val tipo: TipoError, val mensaje: String) : Resultado<Nothing>
}

enum class TipoError { RED, AUTENTICACION, VALIDACION, CORREO_DUPLICADO, LIMITE_IA, SERVIDOR, DESCONOCIDO }
```

Nada de excepciones cruzando capas. Nada de `null` como señal de error.

## Navegación

Rutas en un objeto sellado, sin cadenas sueltas repartidas por el código.

```kotlin
sealed class Ruta(val plantilla: String) {
    data object Splash : Ruta("splash")
    data object IniciarSesion : Ruta("iniciar_sesion")
    data object InicioCliente : Ruta("inicio_cliente")

    // Las rutas con argumento declaran la plantilla que registra el grafo y,
    // aparte, como construir el destino concreto. Si la misma cadena hace las
    // dos cosas, nunca puedes navegar: te queda el literal "{id}".
    data object PerfilTrabajador : Ruta("perfil_trabajador/{id}") {
        const val ARG_ID = "id"
        fun destino(id: String) = "perfil_trabajador/$id"
    }
}
```

Hay guardas por sesión y por rol: un cliente no llega a las pantallas del
trabajador ni al revés.

## Lo que no se hace

- Lógica de negocio dentro de un `@Composable`.
- Llamadas a red desde la interfaz.
- `LiveData` (se usa Flow).
- Singletons a mano (se usa Hilt).
- La llave de OpenAI en cualquier parte del proyecto.
- La `service_role` key de Supabase en cualquier parte del proyecto.
- Consultas a Supabase desde un ViewModel o un `@Composable`. Siempre por repositorio.
