package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios;

import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.EvidenciaEntity;
import com.telconova.supportsuite.dominio.enums.TipoEvidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para Evidencia
 */
@Repository
public interface EvidenciaJpaRepository extends JpaRepository<EvidenciaEntity, Long> {

    List<EvidenciaEntity> findByOrdenTrabajoIdOrderByFechaCreacionDesc(Long ordenTrabajoId);

    List<EvidenciaEntity> findByOrdenTrabajoIdAndTipo(Long ordenTrabajoId, TipoEvidencia tipo);

    long countByOrdenTrabajoId(Long ordenTrabajoId);

    List<EvidenciaEntity> findByCreadoPor(Long creadoPor);

    @Query("SELECT e FROM EvidenciaEntity e WHERE e.ordenTrabajoId = :ordenId ORDER BY e.fechaCreacion DESC")
    List<EvidenciaEntity> findEvidenciasPorOrdenOrdenadas(@Param("ordenId") Long ordenId);
}
