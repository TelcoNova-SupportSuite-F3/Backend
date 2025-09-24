package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios;

import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.MaterialUtilizadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para Material Utilizado
 */
@Repository
public interface MaterialUtilizadoJpaRepository extends JpaRepository<MaterialUtilizadoEntity, Long> {

    List<MaterialUtilizadoEntity> findByOrdenTrabajoIdOrderByFechaRegistroDesc(Long ordenTrabajoId);

    Optional<MaterialUtilizadoEntity> findByOrdenTrabajoIdAndMaterialId(Long ordenTrabajoId, Long materialId);

    @Query("SELECT mu FROM MaterialUtilizadoEntity mu WHERE mu.ordenTrabajoId = :ordenId ORDER BY mu.fechaRegistro DESC")
    List<MaterialUtilizadoEntity> findMaterialesUtilizadosPorOrden(@Param("ordenId") Long ordenId);

    @Query("SELECT SUM(mu.cantidadUtilizada * mu.precioUnitario) FROM MaterialUtilizadoEntity mu WHERE mu.ordenTrabajoId = :ordenId")
    Double calcularCostoTotalMateriales(@Param("ordenId") Long ordenId);
}
