package com.telconova.supportsuite.aplicacion.puertos.salida;

import com.telconova.supportsuite.dominio.entidades.OrdenTrabajo;
import com.telconova.supportsuite.dominio.enums.EstadoOrden;
import java.util.Optional;
import java.util.List;

public interface IOrdenTrabajoRepository {

    /**
     * Busca una orden por ID
     */
    Optional<OrdenTrabajo> buscarPorId(Long id);

    /**
     * Busca una orden por número de orden
     */
    Optional<OrdenTrabajo> buscarPorNumeroOrden(String numeroOrden);

    /**
     * Guarda una orden de trabajo
     */
    OrdenTrabajo guardar(OrdenTrabajo ordenTrabajo);

    /**
     * Obtiene órdenes asignadas a un técnico
     */
    List<OrdenTrabajo> obtenerOrdenesPorTecnico(Long tecnicoId);

    /**
     * Obtiene todas las órdenes del sistema
     */
    List<OrdenTrabajo> obtenerTodasLasOrdenes();

    /**
     * Obtiene órdenes por estado
     */
    List<OrdenTrabajo> obtenerOrdenesPorEstado(EstadoOrden estado);

    /**
     * Obtiene órdenes por técnico y estado
     */
    List<OrdenTrabajo> obtenerOrdenesPorTecnicoYEstado(Long tecnicoId, EstadoOrden estado);

    /**
     * Verifica si existe una orden por número
     */
    boolean existePorNumeroOrden(String numeroOrden);

    /**
     * Cuenta órdenes por estado
     */
    long contarOrdenesPorEstado(EstadoOrden estado);

    /**
     * Obtiene órdenes vencidas (más de X días)
     */
    List<OrdenTrabajo> obtenerOrdenesVencidas(int diasLimite);
}
