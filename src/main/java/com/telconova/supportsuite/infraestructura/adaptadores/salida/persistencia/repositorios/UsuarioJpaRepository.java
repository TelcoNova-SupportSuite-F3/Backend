package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios;

import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.UsuarioEntity;
import com.telconova.supportsuite.dominio.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para Usuario
 */
@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long>{

    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UsuarioEntity> findByActivoTrue();

    List<UsuarioEntity> findByRol(RolUsuario rol);

    @Query("SELECT u FROM UsuarioEntity u WHERE u.activo = true AND u.rol = :rol")
    List<UsuarioEntity> findByRolAndActivoTrue(@Param("rol") RolUsuario rol);
}
