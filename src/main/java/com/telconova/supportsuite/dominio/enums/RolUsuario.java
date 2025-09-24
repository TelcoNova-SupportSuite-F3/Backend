package com.telconova.supportsuite.dominio.enums;

public enum RolUsuario {
    TECNICO("Técnico"),
    ADMIN("Administrador");

    private final String descripcion;

    RolUsuario(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
