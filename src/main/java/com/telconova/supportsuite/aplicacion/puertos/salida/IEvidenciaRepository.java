package com.telconova.supportsuite.aplicacion.puertos.salida;

import com.telconova.supportsuite.dominio.entidades.Evidencia;
import java.util.Optional;
import java.util.List;

public interface IEvidenciaRepository {

    /**
     * Guarda una evidencia
     */
    Evidencia guardar(Evidencia evidencia);

    /**
     * Busca una evidencia por ID
     */
    Optional<Evidencia> buscarPorId(Long id);

    /**
     * Obtiene evidencias de una orden de trabajo
     */
    List<Evidencia> obtenerEvidenciasPorOrden(Long ordenTrabajoId);

    /**
     * Obtiene evidencias por tipo
     */
    List<Evidencia> obtenerEvidenciasPorTipo(Long ordenTrabajoId, com.telconova.supportsuite.dominio.enums.TipoEvidencia tipo);

    /**
     * Elimina una evidencia
     */
    void eliminar(Long evidenciaId);

    /**
     * Cuenta evidencias de una orden
     */
    long contarEvidenciasPorOrden(Long ordenTrabajoId);

    /**
     * Obtiene evidencias creadas por un usuario
     */
    List<Evidencia> obtenerEvidenciasPorUsuario(Long usuarioId);
}
