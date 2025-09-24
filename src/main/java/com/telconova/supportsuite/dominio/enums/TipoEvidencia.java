package com.telconova.supportsuite.dominio.enums;

public enum TipoEvidencia {
    COMENTARIO("Comentario", "Comentario de texto del técnico", 500),
    FOTO("Foto", "Fotografía como evidencia visual", 10485760); // 10MB en bytes

    private final String descripcion;
    private final String detalle;
    private final long tamaanoMaximo;

    TipoEvidencia(String descripcion, String detalle, long tamaanoMaximo) {
        this.descripcion = descripcion;
        this.detalle = detalle;
        this.tamaanoMaximo = tamaanoMaximo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getDetalle() {
        return detalle;
    }

    public long getTamaanoMaximo() {
        return tamaanoMaximo;
    }

    /**
     * Verifica si el tipo es una foto
     */
    public boolean esFoto() {
        return this == FOTO;
    }

    /**
     * Verifica si el tipo es un comentario
     */
    public boolean esComentario() {
        return this == COMENTARIO;
    }

    /**
     * Obtiene los tipos MIME permitidos para fotos
     */
    public static String[] tiposMimePermitidos() {
        return new String[]{"image/jpeg", "image/jpg", "image/png", "image/svg"};
    }
}
