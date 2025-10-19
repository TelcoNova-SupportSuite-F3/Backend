package com.telconova.supportsuite.dominio.excepciones;

public class AlmacenamientoArchivoExcepcion extends RuntimeException {

    public AlmacenamientoArchivoExcepcion(String mensaje) {
        super(mensaje);
    }

    public AlmacenamientoArchivoExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
