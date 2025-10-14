package com.telconova.supportsuite.aplicacion.servicios;

import com.telconova.supportsuite.aplicacion.puertos.entrada.IEvidenciaService;
import com.telconova.supportsuite.aplicacion.puertos.salida.IEvidenciaRepository;
import com.telconova.supportsuite.aplicacion.puertos.salida.IOrdenTrabajoRepository;
import com.telconova.supportsuite.aplicacion.puertos.salida.IUsuarioRepository;
import com.telconova.supportsuite.aplicacion.puertos.salida.IAlmacenamientoArchivos;
import com.telconova.supportsuite.aplicacion.dto.request.RegistrarEvidenciaRequest;
import com.telconova.supportsuite.aplicacion.dto.response.EvidenciaResponse;
import com.telconova.supportsuite.dominio.entidades.Evidencia;
import com.telconova.supportsuite.dominio.entidades.OrdenTrabajo;
import com.telconova.supportsuite.dominio.entidades.Usuario;
import com.telconova.supportsuite.dominio.enums.EstadoOrden;
import com.telconova.supportsuite.dominio.excepciones.OrdenFinalizadaExcepcion;
import com.telconova.supportsuite.dominio.excepciones.OrdenNoEncontradaExcepcion;
import com.telconova.supportsuite.dominio.excepciones.AccesoNoAutorizadoExcepcion;
import com.telconova.supportsuite.dominio.excepciones.EvidenciaNoValidaExcepcion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para operaciones con evidencias
 *
 * @author TelcoNova Development Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvidenciaService implements IEvidenciaService {

    private final IEvidenciaRepository evidenciaRepository;
    private final IOrdenTrabajoRepository ordenTrabajoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IAlmacenamientoArchivos almacenamientoArchivos;

    @Override
    @Transactional
    public EvidenciaResponse registrarComentario(Long ordenId, String comentario, String emailUsuario) {
        log.info("Registrando comentario para orden {} por usuario: {}", ordenId, emailUsuario);

        // Validar acceso a la orden y que no esté finalizada
        validarAccesoYEstadoOrden(ordenId, emailUsuario);

        // Obtener usuario
        Usuario usuario = usuarioRepository.buscarPorEmail(emailUsuario)
                .orElseThrow(() -> new OrdenNoEncontradaExcepcion("Usuario no encontrado: " + emailUsuario));

        // Crear evidencia de comentario
        Evidencia evidencia = Evidencia.crearComentario(ordenId, comentario, usuario.getId());

        // Guardar evidencia
        Evidencia evidenciaGuardada = evidenciaRepository.guardar(evidencia);

        // Agregar evidencia a la orden
        OrdenTrabajo orden = ordenTrabajoRepository.buscarPorId(ordenId)
                .orElseThrow(() -> OrdenNoEncontradaExcepcion.porId(ordenId));
        orden.agregarEvidencia(evidenciaGuardada);
        ordenTrabajoRepository.guardar(orden);

        log.info("Comentario registrado exitosamente para orden {}", ordenId);

        return mapearAResponse(evidenciaGuardada, usuario.getNombreCompleto());
    }

    @Override
    @Transactional
    public EvidenciaResponse registrarFoto(Long ordenId, MultipartFile archivo, String emailUsuario) {
        log.info("Registrando foto para orden {} por usuario: {}", ordenId, emailUsuario);

        // Validar acceso a la orden
        validarAccesoYEstadoOrden(ordenId, emailUsuario);

        // Validar archivo
        validarArchivo(archivo);

        // Obtener usuario
        Usuario usuario = usuarioRepository.buscarPorEmail(emailUsuario)
                .orElseThrow(() -> new OrdenNoEncontradaExcepcion("Usuario no encontrado: " + emailUsuario));

        // Guardar archivo
        String rutaArchivo = almacenamientoArchivos.guardarArchivo(archivo, "evidencias");

        try {
            // Crear evidencia de foto
            Evidencia evidencia = Evidencia.crearFoto(
                    ordenId,
                    rutaArchivo,
                    archivo.getOriginalFilename(),
                    archivo.getContentType(),
                    archivo.getSize(),
                    usuario.getId()
            );

            // Guardar evidencia
            Evidencia evidenciaGuardada = evidenciaRepository.guardar(evidencia);

            // Agregar evidencia a la orden
            OrdenTrabajo orden = ordenTrabajoRepository.buscarPorId(ordenId)
                    .orElseThrow(() -> OrdenNoEncontradaExcepcion.porId(ordenId));
            orden.agregarEvidencia(evidenciaGuardada);
            ordenTrabajoRepository.guardar(orden);

            log.info("Foto registrada exitosamente para orden {}", ordenId);

            return mapearAResponse(evidenciaGuardada, usuario.getNombreCompleto());

        } catch (Exception e) {
            // Si hay error, eliminar el archivo guardado
            almacenamientoArchivos.eliminarArchivo(rutaArchivo);
            throw e;
        }
    }

    @Override
    @Transactional
    public EvidenciaResponse registrarEvidenciaMixta(Long ordenId, RegistrarEvidenciaRequest request, String emailUsuario) {
        log.info("Registrando evidencia mixta para orden {} por usuario: {}", ordenId, emailUsuario);

        // Validar acceso a la orden y que no esté finalizada
        validarAccesoYEstadoOrden(ordenId, emailUsuario);

        // Validar que hay al menos comentario o foto
        boolean tieneComentario = request.getComentario() != null && !request.getComentario().trim().isEmpty();
        boolean tieneFoto = request.getFoto() != null && !request.getFoto().isEmpty();

        if (!tieneComentario && !tieneFoto) {
            throw EvidenciaNoValidaExcepcion.evidenciaRequerida();
        }

        // Si solo hay comentario
        if (!tieneFoto) {
            return registrarComentario(ordenId, request.getComentario(), emailUsuario);
        }

        // Si solo hay foto
        if (!tieneComentario) {
            return registrarFoto(ordenId, request.getFoto(), emailUsuario);
        }

        // Si hay ambos, registrar primero la foto con comentario como descripción adicional
        EvidenciaResponse fotoResponse = registrarFoto(ordenId, request.getFoto(), emailUsuario);
        registrarComentario(ordenId, request.getComentario(), emailUsuario);

        return fotoResponse; // Retornar la respuesta de la foto como principal
    }

    @Override
    public List<EvidenciaResponse> obtenerEvidenciasPorOrden(Long ordenId, String emailUsuario) {
        log.info("Obteniendo evidencias para orden {} solicitado por: {}", ordenId, emailUsuario);

        // Validar acceso a la orden
        validarAccesoOrden(ordenId, emailUsuario);

        List<Evidencia> evidencias = evidenciaRepository.obtenerEvidenciasPorOrden(ordenId);

        return evidencias.stream()
                .map(evidencia -> {
                    String nombreCreador = usuarioRepository.buscarPorId(evidencia.getCreadoPor())
                            .map(Usuario::getNombreCompleto)
                            .orElse("Usuario desconocido");
                    return mapearAResponse(evidencia, nombreCreador);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarEvidencia(Long evidenciaId, String emailUsuario) {
        log.info("Eliminando evidencia {} solicitado por: {}", evidenciaId, emailUsuario);

        Evidencia evidencia = evidenciaRepository.buscarPorId(evidenciaId)
                .orElseThrow(() -> new OrdenNoEncontradaExcepcion("Evidencia no encontrada: " + evidenciaId));

        Usuario usuario = usuarioRepository.buscarPorEmail(emailUsuario)
                .orElseThrow(() -> new OrdenNoEncontradaExcepcion("Usuario no encontrado: " + emailUsuario));

        // Verificar que el usuario puede eliminar la evidencia
        if (!usuario.esAdministrador() && !evidencia.getCreadoPor().equals(usuario.getId())) {
            throw new AccesoNoAutorizadoExcepcion("No tiene permisos para eliminar esta evidencia");
        }

        // Si es una foto, eliminar el archivo
        if (evidencia.esFoto() && evidencia.getRutaArchivo() != null) {
            almacenamientoArchivos.eliminarArchivo(evidencia.getRutaArchivo());
        }

        evidenciaRepository.eliminar(evidenciaId);

        log.info("Evidencia {} eliminada exitosamente", evidenciaId);
    }

    @Override
    public EvidenciaResponse obtenerEvidenciaPorId(Long evidenciaId, String emailUsuario) {
        log.info("Obteniendo evidencia {} solicitado por: {}", evidenciaId, emailUsuario);

        Evidencia evidencia = evidenciaRepository.buscarPorId(evidenciaId)
                .orElseThrow(() -> new OrdenNoEncontradaExcepcion("Evidencia no encontrada: " + evidenciaId));

        // Validar acceso a la orden de la evidencia
        validarAccesoOrden(evidencia.getOrdenTrabajoId(), emailUsuario);

        String nombreCreador = usuarioRepository.buscarPorId(evidencia.getCreadoPor())
                .map(Usuario::getNombreCompleto)
                .orElse("Usuario desconocido");

        return mapearAResponse(evidencia, nombreCreador);
    }

    private void validarAccesoOrden(Long ordenId, String emailUsuario) {
        OrdenTrabajo orden = ordenTrabajoRepository.buscarPorId(ordenId)
                .orElseThrow(() -> OrdenNoEncontradaExcepcion.porId(ordenId));

        Usuario usuario = usuarioRepository.buscarPorEmail(emailUsuario)
                .orElseThrow(() -> new OrdenNoEncontradaExcepcion("Usuario no encontrado: " + emailUsuario));

        // Verificar acceso según el rol
        if (!usuario.esAdministrador() && !orden.estaAsignadaATecnico(usuario.getId())) {
            throw AccesoNoAutorizadoExcepcion.paraOrden(ordenId, emailUsuario);
        }
    }

    /**
     * Valida acceso a la orden Y que no esté finalizada (para modificaciones)
     */
    private void validarAccesoYEstadoOrden(Long ordenId, String emailUsuario) {
        OrdenTrabajo orden = ordenTrabajoRepository.buscarPorId(ordenId)
                .orElseThrow(() -> OrdenNoEncontradaExcepcion.porId(ordenId));

        Usuario usuario = usuarioRepository.buscarPorEmail(emailUsuario)
                .orElseThrow(() -> new OrdenNoEncontradaExcepcion("Usuario no encontrado: " + emailUsuario));

        // Verificar acceso según el rol
        if (!usuario.esAdministrador() && !orden.estaAsignadaATecnico(usuario.getId())) {
            throw AccesoNoAutorizadoExcepcion.paraOrden(ordenId, emailUsuario);
        }

        // Verificar que la orden no esté finalizada
        if (orden.getEstado() == EstadoOrden.FINALIZADA || orden.getEstado() == EstadoOrden.CANCELADA) {
            throw OrdenFinalizadaExcepcion.noSePuedeAgregarEvidencia(ordenId);
        }

        log.debug("Acceso y estado de orden {} validados para usuario: {}", ordenId, emailUsuario);
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw EvidenciaNoValidaExcepcion.archivoVacio();
        }

        // Validar tipo MIME
        if (!almacenamientoArchivos.esTipoMimePermitido(archivo.getContentType())) {
            throw EvidenciaNoValidaExcepcion.tipoArchivoNoPermitido(archivo.getContentType());
        }

        // Validar tamaño
        long tamaanoMaximo = 10 * 1024 * 1024; // 10MB
        if (archivo.getSize() > tamaanoMaximo) {
            throw EvidenciaNoValidaExcepcion.archivoMuyGrande(archivo.getSize(), tamaanoMaximo);
        }
    }

    private EvidenciaResponse mapearAResponse(Evidencia evidencia, String nombreCreador) {
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
