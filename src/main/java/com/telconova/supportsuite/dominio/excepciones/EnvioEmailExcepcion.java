package com.telconova.supportsuite.dominio.excepciones;

public class EnvioEmailExcepcion extends RuntimeException {

    public EnvioEmailExcepcion(String mensaje) {
        super(mensaje);
    }

    public EnvioEmailExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
