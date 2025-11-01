package com.telconova.supportsuite.aplicacion.servicios;

import com.telconova.supportsuite.aplicacion.dto.request.ActualizarEstadoRequest;
import com.telconova.supportsuite.aplicacion.dto.response.*;
import com.telconova.supportsuite.aplicacion.puertos.entrada.IEvidenciaService;
import com.telconova.supportsuite.aplicacion.puertos.entrada.IMaterialService;
import com.telconova.supportsuite.aplicacion.puertos.entrada.IOrdenTrabajoService;
import com.telconova.supportsuite.aplicacion.puertos.salida.*;
import com.telconova.supportsuite.dominio.entidades.Evidencia;
import com.telconova.supportsuite.dominio.entidades.OrdenTrabajo;
import com.telconova.supportsuite.dominio.entidades.Usuario;
import com.telconova.supportsuite.dominio.enums.EstadoOrden;
import com.telconova.supportsuite.dominio.excepciones.AccesoNoAutorizadoExcepcion;
import com.telconova.supportsuite.dominio.excepciones.DominioExcepcion;
import com.telconova.supportsuite.dominio.excepciones.EstadoOrdenInvalidoExcepcion;
import com.telconova.supportsuite.dominio.excepciones.OrdenNoEncontradaExcepcion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de aplicación para operaciones con órdenes de trabajo
 *
 * @author TelcoNova Development Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenTrabajoService implements IOrdenTrabajoService {

    private final IOrdenTrabajoRepository ordenTrabajoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IEvidenciaRepository evidenciaRepository;
    private final IEvidenciaService evidenciaService;
    private final IMaterialService materialService;
    private final IAlmacenamientoArchivos almacenamientoArchivos;
    private final INotificacionService notificacionService;
    private static final String MENSAJE_USUARIO_NO_ENCONTRADO = "Usuario no encontrado: ";

    private IOrdenTrabajoService self;

    @Autowired
    public void setSelf(@Lazy IOrdenTrabajoService self) {
        this.self = self;
    }

    @Override
    public List<OrdenTrabajoResponse> obtenerOrdenesPorTecnico(String emailTecnico) {
        log.info("Obteniendo órdenes para técnico: {}", emailTecnico);

        Usuario tecnico = usuarioRepository.buscarPorEmail(emailTecnico)
                .orElseThrow(() -> new OrdenNoEncontradaExcepcion(MENSAJE_USUARIO_NO_ENCONTRADO + emailTecnico));

        if (!tecnico.esTecnico()) {
            throw AccesoNoAutorizadoExcepcion.porRolInsuficiente("TECNICO", tecnico.getRol().name());
        }

        List<OrdenTrabajo> ordenes = ordenTrabajoRepository.obtenerOrdenesPorTecnico(tecnico.getId());

        log.info("Encontradas {} órdenes para técnico: {}", ordenes.size(), emailTecnico);

        return ordenes.stream()
                .map(orden -> mapearAResponseCompleto(orden, emailTecnico))
                .toList();
    }

    @Override
    public List<OrdenTrabajoResponse> obtenerTodasLasOrdenes() {
        log.info("Obteniendo todas las órdenes del sistema");

        List<OrdenTrabajo> ordenes = ordenTrabajoRepository.obtenerTodasLasOrdenes();

        log.info("Encontradas {} órdenes en el sistema", ordenes.size());

        return ordenes.stream()
                .map(orden -> mapearAResponseCompleto(orden, null)) // null indica admin
                .toList();
    }

    @Override
    public OrdenTrabajoResponse obtenerOrdenPorId(Long ordenId, String emailUsuario) {
        log.info("Obteniendo orden {} para usuario: {}", ordenId, emailUsuario);

        OrdenTrabajo orden = ordenTrabajoRepository.buscarPorId(ordenId)
                .orElseThrow(() -> OrdenNoEncontradaExcepcion.porId(ordenId));

        // Verificar acceso
        if (!puedeAccederOrden(ordenId, emailUsuario)) {
            throw AccesoNoAutorizadoExcepcion.paraOrden(ordenId, emailUsuario);
        }

        return mapearAResponseCompleto(orden, emailUsuario);
    }

    @Override
    public List<OrdenTrabajoResponse> obtenerOrdenesPorEstado(EstadoOrden estado, String emailUsuario) {
        log.info("Obteniendo órdenes en estado {} para usuario: {}", estado, emailUsuario);

        Usuario usuario = usuarioRepository.buscarPorEmail(emailUsuario)
                .orElseThrow(() -> new OrdenNoEncontradaExcepcion(MENSAJE_USUARIO_NO_ENCONTRADO + emailUsuario));

        List<OrdenTrabajo> ordenes;

        if (usuario.esAdministrador()) {
            ordenes = ordenTrabajoRepository.obtenerOrdenesPorEstado(estado);
        } else if (usuario.esTecnico()) {
            ordenes = ordenTrabajoRepository.obtenerOrdenesPorTecnicoYEstado(usuario.getId(), estado);
        } else {
            throw AccesoNoAutorizadoExcepcion.porRolInsuficiente("TECNICO o ADMIN", usuario.getRol().name());
        }

        return ordenes.stream()
                .map(orden -> mapearAResponseCompleto(orden, emailUsuario))
                .toList();
    }

    @Override
    @Transactional
    public OrdenTrabajoResponse actualizarEstadoOrden(Long ordenId, ActualizarEstadoRequest request, String emailUsuario) {
        log.info("Actualizando estado de orden {} a {} por usuario: {}",
                ordenId, request.getNuevoEstado(), emailUsuario);

        OrdenTrabajo orden = ordenTrabajoRepository.buscarPorId(ordenId)
                .orElseThrow(() -> OrdenNoEncontradaExcepcion.porId(ordenId));

        Usuario usuario = usuarioRepository.buscarPorEmail(emailUsuario)
                .orElseThrow(() -> new OrdenNoEncontradaExcepcion(MENSAJE_USUARIO_NO_ENCONTRADO + emailUsuario));

        // Verificar acceso
        if (!puedeAccederOrden(ordenId, emailUsuario)) {
            throw AccesoNoAutorizadoExcepcion.paraOrden(ordenId, emailUsuario);
        }

        // Validar que no se intente modificar una orden CANCELADA o FINALIZADA
        if (orden.getEstado() == EstadoOrden.CANCELADA || orden.getEstado() == EstadoOrden.FINALIZADA) {
            throw new EstadoOrdenInvalidoExcepcion("No se puede modificar una orden cancelada o finalizada");
        }

        // Validar transiciones permitidas usando el enum
        if (!orden.getEstado().puedeTransicionarA(request.getNuevoEstado())) {
            throw new EstadoOrdenInvalidoExcepcion(
                    String.format("No se puede cambiar de estado %s a %s",
                            orden.getEstado(), request.getNuevoEstado()));
        }

        // GUARDAR ESTADO ANTERIOR PARA NOTIFICACIÓN
        EstadoOrden estadoAnterior = orden.getEstado();
        LocalDateTime fechaHoraCambio = LocalDateTime.now();

        aplicarCambioEstado(orden, request.getNuevoEstado(), ordenId);

        OrdenTrabajo ordenActualizada = ordenTrabajoRepository.guardar(orden);

        enviarNotificacionesCambioEstado(ordenActualizada, usuario, estadoAnterior, fechaHoraCambio);

        log.info("Estado de orden {} actualizado exitosamente a {}", ordenId, request.getNuevoEstado());

        return mapearAResponseCompleto(ordenActualizada, emailUsuario);
    }

    /**
     * Aplica el cambio de estado según las reglas de negocio
     */
    private void aplicarCambioEstado(OrdenTrabajo orden, EstadoOrden nuevoEstado, Long ordenId) {
        switch (nuevoEstado) {
            case EN_PROCESO -> procesarTransicionAEnProceso(orden, ordenId);
            case PAUSADA -> procesarTransicionAPausada(orden, ordenId);
            case CANCELADA -> procesarTransicionACancelada(orden, ordenId);
            case FINALIZADA -> procesarTransicionAFinalizada(orden, ordenId);
            case ASIGNADA -> orden.reanudar();
        }
    }

    /**
     * Procesa la transición al estado EN_PROCESO
     */
    private void procesarTransicionAEnProceso(OrdenTrabajo orden, Long ordenId) {
        if (orden.getEstado() == EstadoOrden.PAUSADA) {
            orden.reanudar();
            log.info("Orden {} reanudada desde estado PAUSADA", ordenId);
        } else if (orden.getEstado() == EstadoOrden.ASIGNADA) {
            orden.iniciarTrabajo();
            log.info("Orden {} iniciada desde estado ASIGNADA", ordenId);
        }
    }

    /**
     * Procesa la transición al estado PAUSADA
     */
    private void procesarTransicionAPausada(OrdenTrabajo orden, Long ordenId) {
        orden.pausar();
        log.info("Orden {} pausada", ordenId);
    }

    /**
     * Procesa la transición al estado CANCELADA
     */
    private void procesarTransicionACancelada(OrdenTrabajo orden, Long ordenId) {
        orden.setEstado(EstadoOrden.CANCELADA);
        orden.setFechaFinTrabajo(LocalDateTime.now());
        orden.setFechaActualizacion(LocalDateTime.now());

        try {
            materialService.devolverMaterialesDeOrden(ordenId);
            log.info("Materiales devueltos exitosamente al cancelar orden {}", ordenId);
        } catch (Exception e) {
            log.error("Error al devolver materiales de la orden {}: {}", ordenId, e.getMessage());
            throw new DominioExcepcion("Error al devolver materiales: " + e.getMessage());
        }

        log.info("Orden {} cancelada", ordenId);
    }

    /**
     * Procesa la transición al estado FINALIZADA
     */
    private void procesarTransicionAFinalizada(OrdenTrabajo orden, Long ordenId) {
        long cantidadEvidencias = evidenciaRepository.contarEvidenciasPorOrden(ordenId);
        if (cantidadEvidencias == 0) {
            throw new DominioExcepcion("Se requiere al menos un comentario o foto para finalizar la orden");
        }

        if (!EstadoOrden.estadosParaFinalizar().contains(orden.getEstado())) {
            throw EstadoOrdenInvalidoExcepcion.paraFinalizar(orden.getEstado());
        }

        orden.setEstado(EstadoOrden.FINALIZADA);
        orden.setFechaFinTrabajo(LocalDateTime.now());
        orden.setFechaActualizacion(LocalDateTime.now());
    }

    /**
     * Envía notificaciones del cambio de estado
     */
    private void enviarNotificacionesCambioEstado(OrdenTrabajo orden, Usuario usuario,
                                                  EstadoOrden estadoAnterior, LocalDateTime fechaHoraCambio) {
        try {
            CambioEstadoOrdenDTO cambioEstado = CambioEstadoOrdenDTO.builder()
                    .numeroOrden(orden.getNumeroOrden().getValor())
                    .nombreTecnico(usuario.getNombreCompleto())
                    .estadoAnterior(estadoAnterior.getDescripcion())
                    .estadoNuevo(orden.getEstado().getDescripcion())
                    .fechaHoraCambio(fechaHoraCambio)
                    .clienteNombre(orden.getClienteNombre())
                    .clienteTelefono(orden.getClienteTelefono() != null
                            ? orden.getClienteTelefono().toString() : null)
                    .build();

            notificacionService.notificarCambioEstadoASupervisor(cambioEstado);
            notificacionService.notificarCambioEstadoACliente(cambioEstado);

            log.info("Notificaciones enviadas para orden {}", orden.getId());
        } catch (Exception e) {
            log.error("Error al enviar notificaciones para orden {}: {}", orden.getId(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public OrdenTrabajoResponse finalizarOrden(Long ordenId, ActualizarEstadoRequest request, String emailUsuario) {
        log.info("Finalizando orden {} por usuario: {}", ordenId, emailUsuario);

        // Validar que las fechas estén presentes
        if (request.getFechaInicioTrabajo() == null || request.getFechaFinTrabajo() == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias para finalizar una orden");
        }
        // Usar el metodo de actualización con estado FINALIZADA
        request.setNuevoEstado(EstadoOrden.FINALIZADA);
        return self.actualizarEstadoOrden(ordenId, request, emailUsuario);
    }

    @Override
    public boolean puedeAccederOrden(Long ordenId, String emailUsuario) {
        Usuario usuario = usuarioRepository.buscarPorEmail(emailUsuario)
                .orElse(null);

        if (usuario == null) {
            return false;
        }
        // Los administradores pueden acceder a cualquier orden
        if (usuario.esAdministrador()) {
            return true;
        }
        // Los técnicos solo pueden acceder a sus órdenes asignadas
        if (usuario.esTecnico()) {
            OrdenTrabajo orden = ordenTrabajoRepository.buscarPorId(ordenId).orElse(null);
            return orden != null && orden.estaAsignadaATecnico(usuario.getId());
        }
        return false;
    }

    /**
     * Mapea una orden completa incluyendo evidencias y materiales
     */
    private OrdenTrabajoResponse mapearAResponseCompleto(OrdenTrabajo orden, String emailUsuario) {
        TecnicoResponse tecnico = obtenerTecnicoResponse(orden);
        double duracionHoras = calcularDuracionEnHoras(orden);
        List<EvidenciaResponse> evidencias = cargarEvidencias(orden, emailUsuario);
        DatosMateriales datosMateriales = cargarMateriales(orden, emailUsuario);

        return OrdenTrabajoResponse.builder()
                .id(orden.getId())
                .numeroOrden(orden.getNumeroOrden().getValor())
                .titulo(orden.getTitulo())
                .descripcion(orden.getDescripcion())
                .estado(orden.getEstado().name())
                .prioridad(orden.getPrioridad().name())
                .tipoServicio(orden.getTipoServicio().name())
                .clienteNombre(orden.getClienteNombre())
                .clienteTelefono(orden.getClienteTelefono() != null ? orden.getClienteTelefono().toString() : null)
                .direccion(orden.getDireccion())
                .tecnicoAsignado(tecnico)
                .fechaAsignacion(orden.getFechaAsignacion())
                .fechaInicioTrabajo(orden.getFechaInicioTrabajo())
                .fechaFinTrabajo(orden.getFechaFinTrabajo())
                .fechaCreacion(orden.getFechaCreacion())
                .evidencias(evidencias)
                .materialesUtilizados(datosMateriales.materiales())
                .costoTotalMateriales(datosMateriales.costoTotal())
                .duracionTrabajoHoras(duracionHoras)
                .estaVencida(orden.estaVencida())
                .build();
    }

    /**
     * Obtiene la información del técnico asignado
     */
    private TecnicoResponse obtenerTecnicoResponse(OrdenTrabajo orden) {
        if (orden.getTecnicoAsignadoId() == null) {
            return null;
        }

        return usuarioRepository.buscarPorId(orden.getTecnicoAsignadoId())
                .map(usuarioTecnico -> TecnicoResponse.builder()
                        .id(usuarioTecnico.getId())
                        .email(usuarioTecnico.getEmail().getValor())
                        .nombreCompleto(usuarioTecnico.getNombreCompleto())
                        .activo(usuarioTecnico.estaActivo())
                        .build())
                .orElse(null);
    }

    /**
     * Calcula la duración del trabajo en horas
     */
    private double calcularDuracionEnHoras(OrdenTrabajo orden) {
        if (orden.getDuracionTrabajo().compareTo(Duration.ZERO) <= 0) {
            return 0.0;
        }
        return Math.round((orden.getDuracionTrabajo().toMinutes() / 60.0) * 100.0) / 100.0;
    }

    /**
     * Carga las evidencias según el tipo de usuario
     */
    private List<EvidenciaResponse> cargarEvidencias(OrdenTrabajo orden, String emailUsuario) {
        if (emailUsuario != null && puedeAccederOrden(orden.getId(), emailUsuario)) {
            return cargarEvidenciasParaUsuario(orden, emailUsuario);
        } else if (emailUsuario == null) {
            return cargarEvidenciasParaAdmin(orden);
        }
        return List.of();
    }

    /**
     * Carga evidencias para un usuario específico
     */
    private List<EvidenciaResponse> cargarEvidenciasParaUsuario(OrdenTrabajo orden, String emailUsuario) {
        try {
            return evidenciaService.obtenerEvidenciasPorOrden(orden.getId(), emailUsuario);
        } catch (Exception e) {
            log.warn("Error cargando evidencias para orden {}: {}", orden.getId(), e.getMessage());
            return List.of();
        }
    }

    /**
     * Carga evidencias para administrador usando el repositorio directamente
     */
    private List<EvidenciaResponse> cargarEvidenciasParaAdmin(OrdenTrabajo orden) {
        try {
            return evidenciaRepository.obtenerEvidenciasPorOrden(orden.getId())
                    .stream()
                    .map(evidencia -> {
                        String nombreCreador = usuarioRepository.buscarPorId(evidencia.getCreadoPor())
                                .map(Usuario::getNombreCompleto)
                                .orElse("Usuario desconocido");
                        return mapearEvidenciaAResponse(evidencia, nombreCreador);
                    })
                    .toList();
        } catch (Exception e) {
            log.warn("Error cargando evidencias para orden {} (admin): {}", orden.getId(), e.getMessage());
            return List.of();
        }
    }

    /**
     * Carga los materiales utilizados según el tipo de usuario
     */
    private DatosMateriales cargarMateriales(OrdenTrabajo orden, String emailUsuario) {
        if (emailUsuario != null && puedeAccederOrden(orden.getId(), emailUsuario)) {
            return cargarMaterialesParaUsuario(orden, emailUsuario);
        } else if (emailUsuario == null) {
            return cargarMaterialesParaAdmin(orden);
        }
        return new DatosMateriales(List.of(), 0.0);
    }

    /**
     * Carga materiales para un usuario específico
     */
    private DatosMateriales cargarMaterialesParaUsuario(OrdenTrabajo orden, String emailUsuario) {
        try {
            List<MaterialUtilizadoResponse> materiales =
                    materialService.obtenerMaterialesUtilizadosPorOrden(orden.getId(), emailUsuario);
            double costoTotal = materiales.stream()
                    .mapToDouble(MaterialUtilizadoResponse::getCostoTotal)
                    .sum();
            return new DatosMateriales(materiales, costoTotal);
        } catch (Exception e) {
            log.warn("Error cargando materiales para orden {}: {}", orden.getId(), e.getMessage());
            return new DatosMateriales(List.of(), 0.0);
        }
    }

    /**
     * Carga materiales para administrador
     */
    private DatosMateriales cargarMaterialesParaAdmin(OrdenTrabajo orden) {
        try {
            List<MaterialUtilizadoResponse> materiales =
                    materialService.obtenerMaterialesUtilizadosPorOrden(orden.getId(), "admin@dummy.com");
            double costoTotal = materiales.stream()
                    .mapToDouble(MaterialUtilizadoResponse::getCostoTotal)
                    .sum();
            return new DatosMateriales(materiales, costoTotal);
        } catch (Exception e) {
            log.warn("Error cargando materiales para orden {} (admin): {}", orden.getId(), e.getMessage());
            return new DatosMateriales(List.of(), 0.0);
        }
    }

    /**
     * Mapea evidencia a response (para uso interno cuando no se puede usar el service)
     */
    private EvidenciaResponse mapearEvidenciaAResponse(Evidencia evidencia, String nombreCreador) {
        String urlFoto = null;
        if (evidencia.esFoto() && evidencia.getRutaArchivo() != null) {
            urlFoto = almacenamientoArchivos.obtenerUrlPublica(evidencia.getRutaArchivo());
        }

        return EvidenciaResponse.builder()
                .id(evidencia.getId())
                .tipo(evidencia.getTipo().name())
                .contenido(evidencia.getContenido())
                .urlFoto(urlFoto)
                .nombreArchivo(evidencia.getNombreArchivoOriginal())
                .tamanoArchivo(evidencia.getTamanoArchivoFormateado())
                .fechaCreacion(evidencia.getFechaCreacion())
                .creadoPor(nombreCreador)
                .build();
    }

    /**
     * Record para encapsular los datos de materiales
     */
    private record DatosMateriales(List<MaterialUtilizadoResponse> materiales, double costoTotal) {}
}