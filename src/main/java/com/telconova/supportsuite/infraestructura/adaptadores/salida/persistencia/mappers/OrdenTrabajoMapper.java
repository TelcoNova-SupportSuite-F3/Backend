package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers;

import com.telconova.supportsuite.dominio.entidades.OrdenTrabajo;
import com.telconova.supportsuite.dominio.valueobjects.NumeroOrden;
import com.telconova.supportsuite.dominio.valueobjects.Telefono;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.OrdenTrabajoEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre OrdenTrabajo (dominio) y OrdenTrabajoEntity (JPA)
 */
@Component
public class OrdenTrabajoMapper {

    public OrdenTrabajo toDomain(OrdenTrabajoEntity entity) {
        if (entity == null) {
            return null;
        }

        return OrdenTrabajo.builder()
                .id(entity.getId())
                .numeroOrden(NumeroOrden.de(entity.getNumeroOrden()))
                .titulo(entity.getTitulo())
                .descripcion(entity.getDescripcion())
                .estado(entity.getEstado())
                .prioridad(entity.getPrioridad())
                .tipoServicio(entity.getTipoServicio())
                .clienteNombre(entity.getClienteNombre())
                .clienteTelefono(entity.getClienteTelefono() != null ? Telefono.de(entity.getClienteTelefono()) : null)
                .direccion(entity.getDireccion())
                .tecnicoAsignadoId(entity.getTecnicoAsignadoId())
                .fechaAsignacion(entity.getFechaAsignacion())
                .fechaInicioTrabajo(entity.getFechaInicioTrabajo())
                .fechaFinTrabajo(entity.getFechaFinTrabajo())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    public OrdenTrabajoEntity toEntity(OrdenTrabajo domain) {
        if (domain == null) {
            return null;
        }

        return OrdenTrabajoEntity.builder()
                .id(domain.getId())
                .numeroOrden(domain.getNumeroOrden().getValor())
                .titulo(domain.getTitulo())
                .descripcion(domain.getDescripcion())
                .estado(domain.getEstado())
                .prioridad(domain.getPrioridad())
                .tipoServicio(domain.getTipoServicio())
                .clienteNombre(domain.getClienteNombre())
                .clienteTelefono(domain.getClienteTelefono() != null ? domain.getClienteTelefono().getValor() : null)
                .direccion(domain.getDireccion())
                .tecnicoAsignadoId(domain.getTecnicoAsignadoId())
                .fechaAsignacion(domain.getFechaAsignacion())
                .fechaInicioTrabajo(domain.getFechaInicioTrabajo())
                .fechaFinTrabajo(domain.getFechaFinTrabajo())
                .fechaCreacion(domain.getFechaCreacion())
                .fechaActualizacion(domain.getFechaActualizacion())
                .build();
    }
}
