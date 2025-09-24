package com.telconova.supportsuite.aplicacion.puertos.entrada;

import com.telconova.supportsuite.aplicacion.dto.request.RegistrarEvidenciaRequest;
import com.telconova.supportsuite.aplicacion.dto.response.EvidenciaResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IEvidenciaService {
    /**
     * Registra un comentario como evidencia
     */
    EvidenciaResponse registrarComentario(Long ordenId, String comentario, String emailUsuario);

    /**
     * Registra una foto como evidencia
     */
    EvidenciaResponse registrarFoto(Long ordenId, MultipartFile archivo, String emailUsuario);

    /**
     * Registra evidencia mixta (comentario y foto)
     */
    EvidenciaResponse registrarEvidenciaMixta(Long ordenId, RegistrarEvidenciaRequest request, String emailUsuario);

    /**
     * Obtiene todas las evidencias de una orden
     */
    List<EvidenciaResponse> obtenerEvidenciasPorOrden(Long ordenId, String emailUsuario);

    /**
     * Elimina una evidencia (solo el creador o admin)
     */
    void eliminarEvidencia(Long evidenciaId, String emailUsuario);

    /**
     * Obtiene una evidencia específica
     */
    EvidenciaResponse obtenerEvidenciaPorId(Long evidenciaId, String emailUsuario);
}
