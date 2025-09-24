package com.telconova.supportsuite.dominio.enums;

public enum Prioridad {

    BAJA(1, "Baja", "Prioridad baja - No urgente"),
    MEDIA(2, "Media", "Prioridad media - Atención normal"),
    ALTA(3, "Alta", "Prioridad alta - Requiere atención pronta"),
    CRITICA(4, "Crítica", "Prioridad crítica - Atención inmediata");

    private final int nivel;
    private final String descripcion;
    private final String detalle;

    Prioridad(int nivel, String descripcion, String detalle) {
        this.nivel = nivel;
        this.descripcion = descripcion;
        this.detalle = detalle;
    }

    public int getNivel() {
        return nivel;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getDetalle() {
        return detalle;
    }

    /**
     * Verifica si esta prioridad es mayor que otra
     */
    public boolean esMayorQue(Prioridad otra) {
        return this.nivel > otra.nivel;
    }

    /**
     * Verifica si esta prioridad es crítica o alta
     */
    public boolean esUrgente() {
        return this == CRITICA || this == ALTA;
    }
}
