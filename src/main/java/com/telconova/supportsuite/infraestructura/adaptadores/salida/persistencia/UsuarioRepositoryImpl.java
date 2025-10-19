package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia;

import com.telconova.supportsuite.aplicacion.puertos.salida.IUsuarioRepository;
import com.telconova.supportsuite.dominio.entidades.Usuario;
import com.telconova.supportsuite.dominio.enums.RolUsuario;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.UsuarioEntity;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers.UsuarioMapper;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del repositorio de usuarios usando JPA
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UsuarioRepositoryImpl implements IUsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;
    private final UsuarioMapper mapper;

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        log.debug("Buscando usuario por email: {}", email);
        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        log.debug("Buscando usuario por ID: {}", id);
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        log.debug("Guardando usuario: {}", usuario.getEmail().getValor());
        UsuarioEntity entity = mapper.toEntity(usuario);
        UsuarioEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public boolean existePorEmail(String email) {
        log.debug("Verificando existencia de usuario por email: {}", email);
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public List<Usuario> obtenerUsuariosActivos() {
        log.debug("Obteniendo usuarios activos");
        return jpaRepository.findByActivoTrue()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Usuario> obtenerUsuariosPorRol(RolUsuario rol) {
        log.debug("Obteniendo usuarios por rol: {}", rol);
        return jpaRepository.findByRolAndActivoTrue(rol)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
