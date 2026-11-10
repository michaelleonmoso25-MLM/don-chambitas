package mx.donchambitas.app.util

import java.math.BigDecimal
import java.time.Instant
import mx.donchambitas.app.dominio.modelo.CalificacionTrabajador
import mx.donchambitas.app.dominio.modelo.Categoria
import mx.donchambitas.app.dominio.modelo.Conversacion
import mx.donchambitas.app.dominio.modelo.DetalleSolicitud
import mx.donchambitas.app.dominio.modelo.Estado
import mx.donchambitas.app.dominio.modelo.EstadoPostulacion
import mx.donchambitas.app.dominio.modelo.EstadoSolicitud
import mx.donchambitas.app.dominio.modelo.Mensaje
import mx.donchambitas.app.dominio.modelo.Municipio
import mx.donchambitas.app.dominio.modelo.PerfilHabilidad
import mx.donchambitas.app.dominio.modelo.PerfilPublicoTrabajador
import mx.donchambitas.app.dominio.modelo.PerfilTrabajador
import mx.donchambitas.app.dominio.modelo.Postulacion
import mx.donchambitas.app.dominio.modelo.Resena
import mx.donchambitas.app.dominio.modelo.ResenaPublica
import mx.donchambitas.app.dominio.modelo.ResumenTrabajadorBusqueda
import mx.donchambitas.app.dominio.modelo.RolUsuario
import mx.donchambitas.app.dominio.modelo.Servicio
import mx.donchambitas.app.dominio.modelo.ServicioFoto
import mx.donchambitas.app.dominio.modelo.ServicioPublico
import mx.donchambitas.app.dominio.modelo.Sesion
import mx.donchambitas.app.dominio.modelo.Solicitud
import mx.donchambitas.app.dominio.modelo.Usuario

/**
 * Fabrica de objetos de prueba con valores por defecto y argumentos con nombre.
 * Permite que cada prueba solo especifique los campos relevantes para su caso.
 */
object DatosPrueba {

    val INSTANTE_BASE: Instant = Instant.parse("2026-09-01T12:00:00Z")

    fun crearUsuario(
        id: String = "usr-prueba-001",
        correo: String = "usuario.prueba@donchambitas.mx",
        nombre: String = "Juan",
        apellidos: String = "Pérez Gómez",
        telefono: String? = "5551234567",
        rol: RolUsuario = RolUsuario.CLIENTE,
        fotoUrl: String? = null,
        activo: Boolean = true,
        creadoEn: Instant = INSTANTE_BASE,
        actualizadoEn: Instant = INSTANTE_BASE
    ): Usuario = Usuario(
        id = id,
        correo = correo,
        nombre = nombre,
        apellidos = apellidos,
        telefono = telefono,
        rol = rol,
        fotoUrl = fotoUrl,
        activo = activo,
        creadoEn = creadoEn,
        actualizadoEn = actualizadoEn
    )

    fun crearPerfilTrabajador(
        usuarioId: String = "usr-trab-001",
        rol: RolUsuario = RolUsuario.TRABAJADOR,
        titulo: String = "Plomero y electricista",
        descripcion: String? = "10 años de experiencia en mantenimiento general e instalaciones.",
        experienciaAnios: Int = 10,
        estadoId: Int? = 9,
        municipioId: Int? = 1,
        disponible: Boolean = true,
        creadoEn: Instant = INSTANTE_BASE,
        actualizadoEn: Instant = INSTANTE_BASE
    ): PerfilTrabajador = PerfilTrabajador(
        usuarioId = usuarioId,
        rol = rol,
        titulo = titulo,
        descripcion = descripcion,
        experienciaAnios = experienciaAnios,
        estadoId = estadoId,
        municipioId = municipioId,
        disponible = disponible,
        creadoEn = creadoEn,
        actualizadoEn = actualizadoEn
    )

    fun crearPerfilHabilidad(
        perfilId: String = "usr-trab-001",
        habilidad: String = "Plomería general"
    ): PerfilHabilidad = PerfilHabilidad(
        perfilId = perfilId,
        habilidad = habilidad
    )

    fun crearServicio(
        id: String = "srv-prueba-001",
        perfilId: String = "usr-trab-001",
        categoriaId: Int = 1,
        titulo: String = "Reparación de tuberías",
        descripcion: String = "Reparación de fugas y desazolve.",
        precioDesde: BigDecimal = BigDecimal("350.00"),
        precioHasta: BigDecimal? = BigDecimal("1200.00"),
        unidadPrecio: String? = "por trabajo",
        activo: Boolean = true,
        creadoEn: Instant = INSTANTE_BASE,
        actualizadoEn: Instant = INSTANTE_BASE
    ): Servicio = Servicio(
        id = id,
        perfilId = perfilId,
        categoriaId = categoriaId,
        titulo = titulo,
        descripcion = descripcion,
        precioDesde = precioDesde,
        precioHasta = precioHasta,
        unidadPrecio = unidadPrecio,
        activo = activo,
        creadoEn = creadoEn,
        actualizadoEn = actualizadoEn
    )

    fun crearServicioFoto(
        id: String = "foto-prueba-001",
        servicioId: String = "srv-prueba-001",
        url: String = "https://ejemplo.com/fotos/1.jpg",
        posicion: Int = 1,
        creadoEn: Instant = INSTANTE_BASE
    ): ServicioFoto = ServicioFoto(
        id = id,
        servicioId = servicioId,
        url = url,
        posicion = posicion,
        creadoEn = creadoEn
    )

    fun crearSolicitud(
        id: String = "sol-prueba-001",
        clienteId: String = "usr-prueba-001",
        rolCliente: RolUsuario = RolUsuario.CLIENTE,
        categoriaId: Int = 1,
        titulo: String = "Fuga en baño",
        descripcion: String = "Se requiere plomero urgente para cambio de empaques.",
        presupuesto: BigDecimal? = BigDecimal("500.00"),
        estadoId: Int? = 9,
        municipioId: Int? = 1,
        estatus: EstadoSolicitud = EstadoSolicitud.ABIERTA,
        trabajadorId: String? = null,
        categorizadaPorIa: Boolean = false,
        creadoEn: Instant = INSTANTE_BASE,
        actualizadoEn: Instant = INSTANTE_BASE,
        cerradaEn: Instant? = null
    ): Solicitud = Solicitud(
        id = id,
        clienteId = clienteId,
        rolCliente = rolCliente,
        categoriaId = categoriaId,
        titulo = titulo,
        descripcion = descripcion,
        presupuesto = presupuesto,
        estadoId = estadoId,
        municipioId = municipioId,
        estatus = estatus,
        trabajadorId = trabajadorId,
        categorizadaPorIa = categorizadaPorIa,
        creadoEn = creadoEn,
        actualizadoEn = actualizadoEn,
        cerradaEn = cerradaEn
    )

    fun crearPostulacion(
        id: String = "post-prueba-001",
        solicitudId: String = "sol-prueba-001",
        trabajadorId: String = "usr-trab-001",
        mensaje: String? = "Puedo acudir hoy por la tarde.",
        precioPropuesto: BigDecimal? = BigDecimal("450.00"),
        estatus: EstadoPostulacion = EstadoPostulacion.ENVIADA,
        creadoEn: Instant = INSTANTE_BASE,
        actualizadoEn: Instant = INSTANTE_BASE
    ): Postulacion = Postulacion(
        id = id,
        solicitudId = solicitudId,
        trabajadorId = trabajadorId,
        mensaje = mensaje,
        precioPropuesto = precioPropuesto,
        estatus = estatus,
        creadoEn = creadoEn,
        actualizadoEn = actualizadoEn
    )

    fun crearConversacion(
        id: String = "conv-prueba-001",
        clienteId: String = "usr-prueba-001",
        trabajadorId: String = "usr-trab-001",
        creadoEn: Instant = INSTANTE_BASE
    ): Conversacion = Conversacion(
        id = id,
        clienteId = clienteId,
        trabajadorId = trabajadorId,
        creadoEn = creadoEn
    )

    fun crearMensaje(
        id: String = "msg-prueba-001",
        conversacionId: String = "conv-prueba-001",
        emisorId: String = "usr-prueba-001",
        contenido: String = "Hola, ¿sigue disponible?",
        creadoEn: Instant = INSTANTE_BASE,
        leidoEn: Instant? = null
    ): Mensaje = Mensaje(
        id = id,
        conversacionId = conversacionId,
        emisorId = emisorId,
        contenido = contenido,
        creadoEn = creadoEn,
        leidoEn = leidoEn
    )

    fun crearResena(
        id: String = "res-prueba-001",
        solicitudId: String = "sol-prueba-001",
        clienteId: String = "usr-prueba-001",
        trabajadorId: String = "usr-trab-001",
        calificacion: Int = 5,
        comentario: String? = "Trabajo impecable y muy profesional.",
        creadoEn: Instant = INSTANTE_BASE
    ): Resena = Resena(
        id = id,
        solicitudId = solicitudId,
        clienteId = clienteId,
        trabajadorId = trabajadorId,
        calificacion = calificacion,
        comentario = comentario,
        creadoEn = creadoEn
    )

    fun crearCategoria(
        id: Int = 1,
        nombre: String = "Plomería",
        descripcion: String? = "Instalaciones de agua, gas y drenaje",
        icono: String? = "plumbing",
        activa: Boolean = true,
        orden: Int = 1
    ): Categoria = Categoria(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        icono = icono,
        activa = activa,
        orden = orden
    )

    fun crearEstado(
        id: Int = 9,
        nombre: String = "Ciudad de México",
        clave: String = "CDMX"
    ): Estado = Estado(
        id = id,
        nombre = nombre,
        clave = clave
    )

    fun crearMunicipio(
        id: Int = 1,
        estadoId: Int = 9,
        nombre: String = "Cuauhtémoc"
    ): Municipio = Municipio(
        id = id,
        estadoId = estadoId,
        nombre = nombre
    )

    fun crearSesion(
        usuario: Usuario = crearUsuario()
    ): Sesion = Sesion(
        usuario = usuario
    )

    fun crearServicioPublico(
        id: String = "srv-pub-001",
        titulo: String = "Instalación de tuberías",
        descripcion: String = "Servicio con garantía por escrito",
        categoria: String = "Plomería",
        categoriaId: Int = 1,
        precioDesde: BigDecimal? = BigDecimal("300.00"),
        precioHasta: BigDecimal? = BigDecimal("1000.00"),
        unidadPrecio: String? = "por visita",
        fotos: List<String> = emptyList()
    ): ServicioPublico = ServicioPublico(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        categoria = categoria,
        categoriaId = categoriaId,
        precioDesde = precioDesde,
        precioHasta = precioHasta,
        unidadPrecio = unidadPrecio,
        fotos = fotos
    )

    fun crearResenaPublica(
        calificacion: Int = 5,
        comentario: String? = "Excelente trato y precio justo.",
        clienteNombre: String = "María G.",
        creadoEn: Instant = INSTANTE_BASE
    ): ResenaPublica = ResenaPublica(
        calificacion = calificacion,
        comentario = comentario,
        clienteNombre = clienteNombre,
        creadoEn = creadoEn
    )

    fun crearPerfilPublicoTrabajador(
        id: String = "usr-trab-001",
        nombre: String = "Roberto",
        apellidos: String = "Ramírez",
        fotoUrl: String? = null,
        titulo: String = "Plomero certificado",
        descripcion: String? = "Especialista en fugas y calentadores.",
        experienciaAnios: Int = 8,
        telefonoContacto: String? = "5559876543",
        disponible: Boolean = true,
        estado: String? = "Ciudad de México",
        municipio: String? = "Cuauhtémoc",
        habilidades: List<String> = listOf("Plomería"),
        calificacion: CalificacionTrabajador = CalificacionTrabajador(4.8, 12),
        servicios: List<ServicioPublico> = emptyList(),
        resenas: List<ResenaPublica> = emptyList()
    ): PerfilPublicoTrabajador = PerfilPublicoTrabajador(
        id = id,
        nombre = nombre,
        apellidos = apellidos,
        fotoUrl = fotoUrl,
        titulo = titulo,
        descripcion = descripcion,
        experienciaAnios = experienciaAnios,
        telefonoContacto = telefonoContacto,
        disponible = disponible,
        estado = estado,
        municipio = municipio,
        habilidades = habilidades,
        calificacion = calificacion,
        servicios = servicios,
        resenas = resenas
    )

    fun crearResumenTrabajadorBusqueda(
        trabajadorId: String = "usr-trab-001",
        nombre: String = "Roberto",
        apellidos: String = "Ramírez",
        fotoUrl: String? = null,
        titulo: String = "Plomero certificado",
        disponible: Boolean = true,
        estadoId: Int? = 9,
        municipioId: Int? = 1,
        estado: String? = "Ciudad de México",
        municipio: String? = "Cuauhtémoc",
        promedio: Double = 4.8,
        totalResenas: Int = 12,
        serviciosActivos: Int = 2,
        precioDesde: BigDecimal? = BigDecimal("300.00"),
        categorias: List<Int> = listOf(1),
        creadoEn: Instant = INSTANTE_BASE
    ): ResumenTrabajadorBusqueda = ResumenTrabajadorBusqueda(
        trabajadorId = trabajadorId,
        nombre = nombre,
        apellidos = apellidos,
        fotoUrl = fotoUrl,
        titulo = titulo,
        disponible = disponible,
        estadoId = estadoId,
        municipioId = municipioId,
        estado = estado,
        municipio = municipio,
        promedio = promedio,
        totalResenas = totalResenas,
        serviciosActivos = serviciosActivos,
        precioDesde = precioDesde,
        categorias = categorias,
        creadoEn = creadoEn
    )

    fun crearDetalleSolicitud(
        solicitud: Solicitud = crearSolicitud(),
        postulaciones: List<Postulacion> = emptyList()
    ): DetalleSolicitud = DetalleSolicitud(
        solicitud = solicitud,
        postulaciones = postulaciones
    )
}
