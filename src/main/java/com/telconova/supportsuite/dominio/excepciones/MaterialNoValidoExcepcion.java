package com.telconova.supportsuite.dominio.excepciones;

public class MaterialNoValidoExcepcion extends DominioExcepcion {

    public MaterialNoValidoExcepcion(String mensaje) {
        super(mensaje);
    }

    public MaterialNoValidoExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public static MaterialNoValidoExcepcion porId(Long id) {
        return new MaterialNoValidoExcepcion(
                String.format("No se encontró el material con ID: %d", id)
        );
    }

    public static MaterialNoValidoExcepcion porCodigo(String codigo) {
        return new MaterialNoValidoExcepcion(
                String.format("No se encontró el material con código: %s", codigo)
        );
    }

    public static MaterialNoValidoExcepcion materialInactivo(String codigo) {
        return new MaterialNoValidoExcepcion(
                String.format("El material con código '%s' está inactivo", codigo)
        );
    }

    public static MaterialNoValidoExcepcion stockInsuficiente(String codigo, int disponible, int solicitado) {
        return new MaterialNoValidoExcepcion(
                String.format("Stock insuficiente para el material '%s'. Disponible: %d, Solicitado: %d",
                        codigo, disponible, solicitado)
        );
    }

    public static MaterialNoValidoExcepcion cantidadInvalida(int cantidad) {
        return new MaterialNoValidoExcepcion(
                String.format("La cantidad debe ser mayor a cero. Cantidad proporcionada: %d", cantidad)
        );
    }
}
