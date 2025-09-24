package com.telconova.supportsuite.dominio.excepciones;

public class OrdenFinalizadaExcepcion extends DominioExcepcion{

    public OrdenFinalizadaExcepcion(String mensaje) {
        super(mensaje);
    }

    public OrdenFinalizadaExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public static OrdenFinalizadaExcepcion noSePuedeAgregarEvidencia(Long ordenId) {
        return new OrdenFinalizadaExcepcion(
                String.format("No se puede agregar evidencia a la orden %d porque ya está finalizada", ordenId)
        );
    }

    public static OrdenFinalizadaExcepcion noSePuedeModificar(Long ordenId, String accion) {
        return new OrdenFinalizadaExcepcion(
                String.format("No se puede %s en la orden %d porque ya está finalizada", accion, ordenId)
        );
    }
}
