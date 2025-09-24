package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers;

import com.telconova.supportsuite.dominio.entidades.MaterialUtilizado;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.MaterialUtilizadoEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre MaterialUtilizado (dominio) y MaterialUtilizadoEntity (JPA)
 */
@Component
public class MaterialUtilizadoMapper {

    public MaterialUtilizado toDomain(MaterialUtilizadoEntity entity) {
        if (entity == null) {
            return null;
        }

        return MaterialUtilizado.builder()
                .id(entity.getId())
                .ordenTrabajoId(entity.getOrdenTrabajoId())
                .materialId(entity.getMaterialId())
                .cantidadUtilizada(entity.getCantidadUtilizada())
                .precioUnitario(entity.getPrecioUnitario())
                .fechaRegistro(entity.getFechaRegistro())
                .registradoPor(entity.getRegistradoPor())
                .nombreMaterial(entity.getNombreMaterial())
                .unidadMedida(entity.getUnidadMedida())
                .codigoMaterial(entity.getCodigoMaterial())
                .build();
    }

    public MaterialUtilizadoEntity toEntity(MaterialUtilizado domain) {
        if (domain == null) {
            return null;
        }

        return MaterialUtilizadoEntity.builder()
                .id(domain.getId())
                .ordenTrabajoId(domain.getOrdenTrabajoId())
                .materialId(domain.getMaterialId())
                .cantidadUtilizada(domain.getCantidadUtilizada())
                .precioUnitario(domain.getPrecioUnitario())
                .fechaRegistro(domain.getFechaRegistro())
                .registradoPor(domain.getRegistradoPor())
                .codigoMaterial(domain.getCodigoMaterial())
                .nombreMaterial(domain.getNombreMaterial())
                .unidadMedida(domain.getUnidadMedida())
                .build();
    }
}
