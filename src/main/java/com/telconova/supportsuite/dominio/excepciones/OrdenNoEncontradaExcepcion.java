package com.telconova.supportsuite.dominio.excepciones;

public class OrdenNoEncontradaExcepcion extends DominioExcepcion {

    public OrdenNoEncontradaExcepcion(String mensaje) {
        super(mensaje);
    }

    public OrdenNoEncontradaExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public static OrdenNoEncontradaExcepcion porId(Long id) {
        return new OrdenNoEncontradaExcepcion(
                String.format("No se encontró la orden de trabajo con ID: %d", id)
        );
    }

    public static OrdenNoEncontradaExcepcion porNumeroOrden(String numeroOrden) {
        return new OrdenNoEncontradaExcepcion(
                String.format("No se encontró la orden de trabajo con número: %s", numeroOrden)
        );
    }
}
