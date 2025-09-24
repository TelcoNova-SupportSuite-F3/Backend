package com.telconova.supportsuite.dominio.excepciones;

import com.telconova.supportsuite.dominio.enums.EstadoOrden;

public class EstadoOrdenInvalidoExcepcion extends DominioExcepcion {

    public EstadoOrdenInvalidoExcepcion(String mensaje) {
        super(mensaje);
    }

    public EstadoOrdenInvalidoExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public static EstadoOrdenInvalidoExcepcion transicionInvalida(EstadoOrden estadoActual, EstadoOrden estadoDestino) {
        return new EstadoOrdenInvalidoExcepcion(
                String.format("No se puede cambiar del estado '%s' al estado '%s'",
                        estadoActual.getDescripcion(), estadoDestino.getDescripcion())
        );
    }

    public static EstadoOrdenInvalidoExcepcion paraAgregarMateriales(EstadoOrden estadoActual) {
        return new EstadoOrdenInvalidoExcepcion(
                String.format("No se pueden agregar materiales cuando la orden está en estado '%s'. " +
                        "La orden debe estar EN_PROCESO.", estadoActual.getDescripcion())
        );
    }

    public static EstadoOrdenInvalidoExcepcion paraFinalizar(EstadoOrden estadoActual) {
        return new EstadoOrdenInvalidoExcepcion(
                String.format("No se puede finalizar una orden en estado '%s'. " +
                        "La orden debe estar EN_PROCESO o PAUSADA.", estadoActual.getDescripcion())
        );
    }
}
