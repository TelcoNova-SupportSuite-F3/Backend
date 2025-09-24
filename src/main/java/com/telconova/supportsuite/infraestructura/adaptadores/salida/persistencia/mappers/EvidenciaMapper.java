package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers;

import com.telconova.supportsuite.dominio.entidades.Evidencia;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.EvidenciaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Evidencia (dominio) y EvidenciaEntity (JPA)
 */
@Component
public class EvidenciaMapper {

    public Evidencia toDomain(EvidenciaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Evidencia.builder()
                .id(entity.getId())
                .ordenTrabajoId(entity.getOrdenTrabajoId())
                .tipo(entity.getTipo())
                .contenido(entity.getContenido())
                .rutaArchivo(entity.getRutaArchivo())
                .nombreArchivoOriginal(entity.getNombreArchivoOriginal())
                .tipoMime(entity.getTipoMime())
                .tamanoArchivo(entity.getTamanoArchivo())
                .fechaCreacion(entity.getFechaCreacion())
                .creadoPor(entity.getCreadoPor())
                .build();
    }

    public EvidenciaEntity toEntity(Evidencia domain) {
        if (domain == null) {
            return null;
        }

        return EvidenciaEntity.builder()
                .id(domain.getId())
                .ordenTrabajoId(domain.getOrdenTrabajoId())
                .tipo(domain.getTipo())
                .contenido(domain.getContenido())
                .rutaArchivo(domain.getRutaArchivo())
                .nombreArchivoOriginal(domain.getNombreArchivoOriginal())
                .tipoMime(domain.getTipoMime())
                .tamanoArchivo(domain.getTamanoArchivo())
                .fechaCreacion(domain.getFechaCreacion())
                .creadoPor(domain.getCreadoPor())
                .build();
    }
}
