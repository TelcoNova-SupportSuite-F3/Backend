package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios;

import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.OrdenTrabajoEntity;
import com.telconova.supportsuite.dominio.enums.EstadoOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para Orden de Trabajo
 */
@Repository
public interface OrdenTrabajoJpaRepository extends JpaRepository<OrdenTrabajoEntity, Long>{

    Optional<OrdenTrabajoEntity> findByNumeroOrden(String numeroOrden);

    boolean existsByNumeroOrden(String numeroOrden);

    List<OrdenTrabajoEntity> findByTecnicoAsignadoId(Long tecnicoId);

    List<OrdenTrabajoEntity> findByEstado(EstadoOrden estado);

    List<OrdenTrabajoEntity> findByTecnicoAsignadoIdAndEstado(Long tecnicoId, EstadoOrden estado);

    long countByEstado(EstadoOrden estado);

    @Query("SELECT o FROM OrdenTrabajoEntity o WHERE o.estado IN :estados ORDER BY o.prioridad DESC, o.fechaCreacion ASC")
    List<OrdenTrabajoEntity> findByEstadoInOrderByPrioridadDesc(@Param("estados") List<EstadoOrden> estados);

    @Query("SELECT o FROM OrdenTrabajoEntity o WHERE o.fechaCreacion < :fechaLimite AND o.estado != 'FINALIZADA'")
    List<OrdenTrabajoEntity> findOrdenesVencidas(@Param("fechaLimite") LocalDateTime fechaLimite);

    @Query("SELECT o FROM OrdenTrabajoEntity o WHERE o.tecnicoAsignadoId = :tecnicoId AND o.estado IN ('ASIGNADA', 'EN_PROCESO', 'PAUSADA') ORDER BY o.prioridad DESC, o.fechaCreacion ASC")
    List<OrdenTrabajoEntity> findOrdenesActivasPorTecnico(@Param("tecnicoId") Long tecnicoId);
}
