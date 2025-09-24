package com.telconova.supportsuite.aplicacion.puertos.entrada;

import com.telconova.supportsuite.aplicacion.dto.response.OrdenTrabajoResponse;
import com.telconova.supportsuite.aplicacion.dto.request.ActualizarEstadoRequest;
import com.telconova.supportsuite.dominio.enums.EstadoOrden;

import java.util.List;

public interface IOrdenTrabajoService {

    /**
     * Obtiene todas las órdenes asignadas a un técnico
     */
    List<OrdenTrabajoResponse> obtenerOrdenesPorTecnico(String emailTecnico);

    /**
     * Obtiene todas las órdenes del sistema (solo para administradores)
     */
    List<OrdenTrabajoResponse> obtenerTodasLasOrdenes();

    /**
     * Obtiene una orden específica por ID
     */
    OrdenTrabajoResponse obtenerOrdenPorId(Long ordenId, String emailUsuario);

    /**
     * Obtiene órdenes filtradas por estado
     */
    List<OrdenTrabajoResponse> obtenerOrdenesPorEstado(EstadoOrden estado, String emailUsuario);

    /**
     * Actualiza el estado de una orden
     */
    OrdenTrabajoResponse actualizarEstadoOrden(Long ordenId, ActualizarEstadoRequest request, String emailUsuario);

    /**
     * Finaliza una orden de trabajo
     */
    OrdenTrabajoResponse finalizarOrden(Long ordenId, ActualizarEstadoRequest request, String emailUsuario);

    /**
     * Verifica si un usuario puede acceder a una orden
     */
    boolean puedeAccederOrden(Long ordenId, String emailUsuario);
}
