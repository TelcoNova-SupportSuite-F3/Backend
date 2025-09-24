package com.telconova.supportsuite.dominio.enums;

public enum TipoServicio {

    INSTALACION("Instalación", "Instalación de nuevos servicios"),
    REPARACION("Reparación", "Reparación de servicios existentes"),
    MANTENIMIENTO("Mantenimiento", "Mantenimiento preventivo o correctivo"),
    UPGRADE("Upgrade", "Actualización o mejora de servicios"),
    DESCONEXION("Desconexión", "Desconexión de servicios"),
    RECONEXION("Reconexión", "Reconexión de servicios suspendidos"),
    REVISION_TECNICA("Revisión Técnica", "Revisión técnica especializada");

    private final String descripcion;
    private final String detalle;

    TipoServicio(String descripcion, String detalle) {
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
     * Tipos de servicio que requieren materiales típicamente
     */
    public boolean requiereMaterialesGeneralmente() {
        return this == INSTALACION || this == REPARACION || this == UPGRADE;
    }
}
