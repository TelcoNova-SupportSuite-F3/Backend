package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers;

import com.telconova.supportsuite.dominio.entidades.Usuario;
import com.telconova.supportsuite.dominio.valueobjects.Email;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.UsuarioEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Usuario (dominio) y UsuarioEntity (JPA)
 */
@Component
public class UsuarioMapper {

    public Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) {
            return null;
        }

        return Usuario.builder()
                .id(entity.getId())
                .email(Email.de(entity.getEmail()))
                .contrasenaEncriptada(entity.getContrasena())
                .nombreCompleto(entity.getNombreCompleto())
                .rol(entity.getRol())
                .activo(entity.getActivo())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    public UsuarioEntity toEntity(Usuario domain) {
        if (domain == null) {
            return null;
        }

        return UsuarioEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail().getValor())
                .contrasena(domain.getContrasenaEncriptada())
                .nombreCompleto(domain.getNombreCompleto())
                .rol(domain.getRol())
                .activo(domain.estaActivo())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaActualizacion(domain.getFechaActualizacion())
                .build();
    }
}
