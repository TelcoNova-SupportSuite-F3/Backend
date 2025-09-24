package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios;

import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.MaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para Material
 */
@Repository
public interface MaterialJpaRepository extends JpaRepository<MaterialEntity, Long> {

    Optional<MaterialEntity> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<MaterialEntity> findByActivoTrue();

    @Query("SELECT m FROM MaterialEntity m WHERE m.activo = true AND UPPER(m.nombre) LIKE UPPER(CONCAT('%', :nombre, '%')) ORDER BY m.nombre")
    List<MaterialEntity> findByNombreContainingIgnoreCaseAndActivoTrue(@Param("nombre") String nombre);

    @Query("SELECT m FROM MaterialEntity m WHERE m.activo = true AND UPPER(m.nombre) LIKE UPPER(CONCAT('%', :nombre, '%')) ORDER BY m.nombre")
    List<MaterialEntity> buscarPorNombreConLimite(@Param("nombre") String nombre, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT m FROM MaterialEntity m WHERE m.stockDisponible <= :stockMinimo AND m.activo = true")
    List<MaterialEntity> findMaterialesConStockBajo(@Param("stockMinimo") Integer stockMinimo);
}
