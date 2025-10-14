package com.telconova.supportsuite.dominio.enums;

import java.util.Set;

public enum EstadoOrden {

    ASIGNADA("Asignada", "Orden asignada a un técnico pero aún no iniciada"),
    EN_PROCESO("En Proceso", "Orden siendo trabajada por el técnico"),
    PAUSADA("Pausada", "Orden temporalmente pausada"),
    CANCELADA("Cancelada", "Orden cancelada y no se completará"),
    FINALIZADA("Finalizada", "Orden completada exitosamente");

    private final String descripcion;
    private final String detalle;

    EstadoOrden(String descripcion, String detalle) {
        this.descripcion = descripcion;
        this.detalle = detalle;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getDetalle() {
        return detalle;
    }

    /**
     * Estados que permiten agregar materiales
     */
    public static Set<EstadoOrden> estadosParaAgregarMateriales() {
        return Set.of(EN_PROCESO);
    }

    /**
     * Estados desde los cuales se puede finalizar una orden
     */
    public static Set<EstadoOrden> estadosParaFinalizar() {
        return Set.of(EN_PROCESO, PAUSADA);
    }

    /**
     * Estados activos (no finalizados)
     */
    public static Set<EstadoOrden> estadosActivos() {
        return Set.of(ASIGNADA, EN_PROCESO, PAUSADA);
    }

    /**
     * Verifica si desde este estado se puede transicionar al estado destino
     */
    public boolean puedeTransicionarA(EstadoOrden estadoDestino) {
        return switch (this) {
            case ASIGNADA -> Set.of(EN_PROCESO, PAUSADA, CANCELADA).contains(estadoDestino);
            case EN_PROCESO -> Set.of(PAUSADA, CANCELADA, FINALIZADA).contains(estadoDestino);
            case PAUSADA -> Set.of(EN_PROCESO, CANCELADA, FINALIZADA).contains(estadoDestino);
            case CANCELADA -> false; // No se puede cambiar desde cancelada
            case FINALIZADA -> false; // No se puede cambiar desde finalizada
        };
    }
}
