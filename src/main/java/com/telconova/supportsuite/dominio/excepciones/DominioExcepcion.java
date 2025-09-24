package com.telconova.supportsuite.dominio.excepciones;

public class DominioExcepcion extends RuntimeException {

    public DominioExcepcion(String mensaje) {
        super(mensaje);
    }

    public DominioExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
