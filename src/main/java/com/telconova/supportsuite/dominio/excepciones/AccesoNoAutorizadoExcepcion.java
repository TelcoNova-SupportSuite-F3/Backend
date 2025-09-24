package com.telconova.supportsuite.dominio.excepciones;

public class AccesoNoAutorizadoExcepcion extends DominioExcepcion {

    public AccesoNoAutorizadoExcepcion(String mensaje) {
        super(mensaje);
    }

    public AccesoNoAutorizadoExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public static AccesoNoAutorizadoExcepcion paraOrden(Long ordenId, String usuarioEmail) {
        return new AccesoNoAutorizadoExcepcion(
                String.format("El usuario '%s' no tiene autorización para acceder a la orden %d",
                        usuarioEmail, ordenId)
        );
    }

    public static AccesoNoAutorizadoExcepcion porRolInsuficiente(String rolRequerido, String rolActual) {
        return new AccesoNoAutorizadoExcepcion(
                String.format("Se requiere el rol '%s' para esta operación. Rol actual: '%s'",
                        rolRequerido, rolActual)
        );
    }

    public static AccesoNoAutorizadoExcepcion tokenInvalido() {
        return new AccesoNoAutorizadoExcepcion("Token de autenticación inválido o expirado");
    }
}
