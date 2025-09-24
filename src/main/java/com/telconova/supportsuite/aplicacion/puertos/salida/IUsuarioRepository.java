package com.telconova.supportsuite.aplicacion.puertos.salida;

import com.telconova.supportsuite.dominio.entidades.Usuario;
import java.util.Optional;

public interface IUsuarioRepository {

    /**
     * Busca un usuario por email
     */
    Optional<Usuario> buscarPorEmail(String email);

    /**
     * Busca un usuario por ID
     */
    Optional<Usuario> buscarPorId(Long id);

    /**
     * Guarda un usuario
     */
    Usuario guardar(Usuario usuario);

    /**
     * Verifica si existe un usuario con el email dado
     */
    boolean existePorEmail(String email);

    /**
     * Obtiene todos los usuarios activos
     */
    java.util.List<Usuario> obtenerUsuariosActivos();

    /**
     * Obtiene usuarios por rol
     */
    java.util.List<Usuario> obtenerUsuariosPorRol(com.telconova.supportsuite.dominio.enums.RolUsuario rol);
}
