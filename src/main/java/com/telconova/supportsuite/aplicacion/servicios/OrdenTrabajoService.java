package com.telconova.supportsuite.aplicacion.servicios;

import com.telconova.supportsuite.aplicacion.dto.response.*;
import com.telconova.supportsuite.aplicacion.puertos.entrada.IEvidenciaService;
import com.telconova.supportsuite.aplicacion.puertos.entrada.IMaterialService;
import com.telconova.supportsuite.aplicacion.puertos.entrada.IOrdenTrabajoService;
import com.telconova.supportsuite.aplicacion.puertos.salida.*;
import com.telconova.supportsuite.aplicacion.dto.request.ActualizarEstadoRequest;
import com.telconova.supportsuite.dominio.entidades.Evidencia;
import com.telconova.supportsuite.dominio.entidades.OrdenTrabajo;
import com.telconova.supportsuite.dominio.entidades.Usuario;
import com.telconova.supportsuite.dominio.enums.EstadoOrden;
import com.telconova.supportsuite.dominio.excepciones.DominioExcepcion;
import com.telconova.supportsuite.dominio.excepciones.EstadoOrdenInvalidoExcepcion;
import com.telconova.supportsuite.dominio.excepciones.OrdenNoEncontradaExcepcion;
import com.telconova.supportsuite.dominio.excepciones.AccesoNoAutorizadoExcepcion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<OrdenTrabajoResponse> obtenerOrdenesPorTecnico(String emailTecnico) {
        log.info("Obteniendo órdenes para técnico: {}", emailTecnico);

        Usuario tecnico = usuarioRepository.buscarPorEmail(emailTecnico)
                .orElseThrow(() -> new OrdenNoEncontradaExcepcion("Usuario no encontrado: " + emailTecnico));

        if (!tecnico.esTecnico()) {
            throw AccesoNoAutorizadoExcepcion.porRolInsuficiente("TECNICO", tecnico.getRol().name());
        }

        List<OrdenTrabajo> ordenes = ordenTrabajoRepository.obtenerOrdenesPorTecnico(tecnico.getId());

        log.info("Encontradas {} órdenes para técnico: {}", ordenes.size(), emailTecnico);

        return ordenes.stream()
                .map(orden -> mapearAResponseCompleto(orden, emailTecnico))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrdenTrabajoResponse> obtenerTodasLasOrdenes() {
        log.info("Obteniendo todas las órdenes del sistema");

        List<OrdenTrabajo> ordenes = ordenTrabajoRepository.obtenerTodasLasOrdenes();

        log.info("Encontradas {} órdenes en el sistema", ordenes.size());

        return ordenes.stream()
                .map(orden -> mapearAResponseCompleto(orden, null)) // null indica admin
                .collect(Collectors.toList());
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
                .orElseThrow(() -> new OrdenNoEncontradaExcepcion("Usuario no encontrado: " + emailUsuario));

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
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrdenTrabajoResponse actualizarEstadoOrden(Long ordenId, ActualizarEstadoRequest request, String emailUsuario) {
        log.info("Actualizando estado de orden {} a {} por usuario: {}",
                ordenId, request.getNuevoEstado(), emailUsuario);

        OrdenTrabajo orden = ordenTrabajoRepository.buscarPorId(ordenId)
                .orElseThrow(() -> OrdenNoEncontradaExcepcion.porId(ordenId));

        Usuario usuario = usuarioRepository.buscarPorEmail(emailUsuario)
                .orElseThrow(() -> new OrdenNoEncontradaExcepcion("Usuario no encontrado: " + emailUsuario));

        // Verificar acceso
        if (!puedeAccederOrden(ordenId, emailUsuario)) {
            throw AccesoNoAutorizadoExcepcion.paraOrden(ordenId, emailUsuario);
        }

        // Validar que no se intente modificar una orden CANCELADA o FINALIZADA
        if (orden.getEstado() == EstadoOrden.CANCELADA) {
            throw new EstadoOrdenInvalidoExcepcion("No se puede modificar una orden cancelada");
        }

        if (orden.getEstado() == EstadoOrden.FINALIZADA && request.getNuevoEstado() == EstadoOrden.CANCELADA) {
            throw new EstadoOrdenInvalidoExcepcion("No se puede cancelar una orden que ya está finalizada");
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

        // Aplicar el cambio de estado según las reglas de negocio
        switch (request.getNuevoEstado()) {
            case EN_PROCESO -> {
                // Si viene de PAUSADA, reanudar; si viene de ASIGNADA, iniciar
                if (orden.getEstado() == EstadoOrden.PAUSADA) {
                    orden.reanudar();
                    log.info("Orden {} reanudada desde estado PAUSADA", ordenId);
                } else if (orden.getEstado() == EstadoOrden.ASIGNADA) {
                    orden.iniciarTrabajo();
                    log.info("Orden {} iniciada desde estado ASIGNADA", ordenId);
                }
            }
            case PAUSADA -> {
                orden.pausar();
                log.info("Orden {} pausada", ordenId);
            }
            case CANCELADA -> {
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
                log.info("Orden {} cancelada por usuario: {}", ordenId, emailUsuario);
            }
            case FINALIZADA -> {
                if (request.getFechaInicioTrabajo() == null || request.getFechaFinTrabajo() == null) {
                    throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias para finalizar");
                }
                long cantidadEvidencias = evidenciaRepository.contarEvidenciasPorOrden(ordenId);
                if (cantidadEvidencias == 0) {
                    throw new DominioExcepcion("Se requiere al menos un comentario o foto para finalizar la orden");
                }
                if (!EstadoOrden.estadosParaFinalizar().contains(orden.getEstado())) {
                    throw EstadoOrdenInvalidoExcepcion.paraFinalizar(orden.getEstado());
                }
                orden.setEstado(EstadoOrden.FINALIZADA);
                orden.setFechaInicioTrabajo(request.getFechaInicioTrabajo());
                orden.setFechaFinTrabajo(request.getFechaFinTrabajo());
                orden.setFechaActualizacion(LocalDateTime.now());
            }
            case ASIGNADA -> orden.reanudar();
        }

        OrdenTrabajo ordenActualizada = ordenTrabajoRepository.guardar(orden);

        try {
            CambioEstadoOrdenDTO cambioEstado = CambioEstadoOrdenDTO.builder()
                    .numeroOrden(ordenActualizada.getNumeroOrden().getValor())
                    .nombreTecnico(usuario.getNombreCompleto())
                    .estadoAnterior(estadoAnterior.getDescripcion())
                    .estadoNuevo(ordenActualizada.getEstado().getDescripcion())
                    .fechaHoraCambio(fechaHoraCambio)
                    .clienteNombre(ordenActualizada.getClienteNombre())
                    .clienteTelefono(ordenActualizada.getClienteTelefono() != null
                            ? ordenActualizada.getClienteTelefono().toString() : null)
                    .build();

            // Notificar al supervisor
            notificacionService.notificarCambioEstadoASupervisor(cambioEstado);

            // Notificar al cliente
            notificacionService.notificarCambioEstadoACliente(cambioEstado);

            log.info("Notificaciones enviadas para orden {}", ordenId);
        } catch (Exception e) {
            log.error("Error al enviar notificaciones para orden {}: {}", ordenId, e.getMessage());
        }

        log.info("Estado de orden {} actualizado exitosamente a {}", ordenId, request.getNuevoEstado());

        return mapearAResponseCompleto(ordenActualizada, emailUsuario);
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
        return actualizarEstadoOrden(ordenId, request, emailUsuario);
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
        TecnicoResponse tecnico = null;

        if (orden.getTecnicoAsignadoId() != null) {
            Usuario usuarioTecnico = usuarioRepository.buscarPorId(orden.getTecnicoAsignadoId())
                    .orElse(null);

            if (usuarioTecnico != null) {
                tecnico = TecnicoResponse.builder()
                        .id(usuarioTecnico.getId())
                        .email(usuarioTecnico.getEmail().getValor())
                        .nombreCompleto(usuarioTecnico.getNombreCompleto())
                        .activo(usuarioTecnico.estaActivo())
                        .build();
            }
        }

        double duracionHoras = 0.0;
        if (orden.getDuracionTrabajo().compareTo(Duration.ZERO) > 0) {
            duracionHoras = Math.round((orden.getDuracionTrabajo().toMinutes() / 60.0) * 100.0) / 100.0;
        }

        // **CARGAR EVIDENCIAS** - Solo si el usuario tiene acceso
        List<EvidenciaResponse> evidencias = List.of();
        if (emailUsuario != null && puedeAccederOrden(orden.getId(), emailUsuario)) {
            try {
                evidencias = evidenciaService.obtenerEvidenciasPorOrden(orden.getId(), emailUsuario);
            } catch (Exception e) {
                log.warn("Error cargando evidencias para orden {}: {}", orden.getId(), e.getMessage());
                evidencias = List.of();
            }
        } else if (emailUsuario == null) { // Es admin consultando todas las órdenes
            try {
                // Para admin, usar directamente el repositorio
                evidencias = evidenciaRepository.obtenerEvidenciasPorOrden(orden.getId())
                        .stream()
                        .map(evidencia -> {
                            String nombreCreador = usuarioRepository.buscarPorId(evidencia.getCreadoPor())
                                    .map(Usuario::getNombreCompleto)
                                    .orElse("Usuario desconocido");
                            return mapearEvidenciaAResponse(evidencia, nombreCreador);
                        })
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.warn("Error cargando evidencias para orden {} (admin): {}", orden.getId(), e.getMessage());
                evidencias = List.of();
            }
        }

        // **CARGAR MATERIALES UTILIZADOS** - Solo si el usuario tiene acceso
        List<MaterialUtilizadoResponse> materialesUtilizados = List.of();
        double costoTotalMateriales = 0.0;
        if (emailUsuario != null && puedeAccederOrden(orden.getId(), emailUsuario)) {
            try {
                materialesUtilizados = materialService.obtenerMaterialesUtilizadosPorOrden(orden.getId(), emailUsuario);
                costoTotalMateriales = materialesUtilizados.stream()
                        .mapToDouble(MaterialUtilizadoResponse::getCostoTotal)
                        .sum();
            } catch (Exception e) {
                log.warn("Error cargando materiales para orden {}: {}", orden.getId(), e.getMessage());
                materialesUtilizados = List.of();
                costoTotalMateriales = 0.0;
            }
        } else if (emailUsuario == null) { // Es admin consultando todas las órdenes
            try {
                materialesUtilizados = materialService.obtenerMaterialesUtilizadosPorOrden(orden.getId(), "admin@dummy.com");
                // Calcular costo total de materiales
                costoTotalMateriales = materialesUtilizados.stream()
                        .mapToDouble(MaterialUtilizadoResponse::getCostoTotal)
                        .sum();
            } catch (Exception e) {
                log.warn("Error cargando materiales para orden {} (admin): {}", orden.getId(), e.getMessage());
                materialesUtilizados = List.of();
                costoTotalMateriales = 0.0;
            }
        }

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
                .materialesUtilizados(materialesUtilizados)
                .costoTotalMateriales(costoTotalMateriales)
                .duracionTrabajoHoras(duracionHoras)
                .estaVencida(orden.estaVencida())
                .build();
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
}