package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers;

import com.telconova.supportsuite.dominio.entidades.Material;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.MaterialEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Material (dominio) y MaterialEntity (JPA)
 */
@Component
public class MaterialMapper {

    public Material toDomain(MaterialEntity entity) {
        if (entity == null) {
            return null;
        }

        return Material.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .unidadMedida(entity.getUnidadMedida())
                .precioUnitario(entity.getPrecioUnitario())
                .stockDisponible(entity.getStockDisponible())
                .activo(entity.getActivo())
                .fechaCreacion(entity.getFechaCreacion())
                .build();
    }

    public MaterialEntity toEntity(Material domain) {
        if (domain == null) {
            return null;
        }

        return MaterialEntity.builder()
                .id(domain.getId())
                .codigo(domain.getCodigo())
                .nombre(domain.getNombre())
                .descripcion(domain.getDescripcion())
                .unidadMedida(domain.getUnidadMedida())
                .precioUnitario(domain.getPrecioUnitario())
                .stockDisponible(domain.getStockDisponible())
                .activo(domain.estaActivo())
                .fechaCreacion(domain.getFechaCreacion())
                .build();
    }
}
