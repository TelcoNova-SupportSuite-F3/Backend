package com.telconova.supportsuite.dominio.excepciones;

public class UsuarioNoValidoExcepcion extends DominioExcepcion {

    public UsuarioNoValidoExcepcion(String mensaje) {
        super(mensaje);
    }

    public UsuarioNoValidoExcepcion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public static UsuarioNoValidoExcepcion porEmailInvalido(String email) {
        return new UsuarioNoValidoExcepcion(
                String.format("El email '%s' no es válido o no pertenece al dominio @telconova.com", email)
        );
    }

    public static UsuarioNoValidoExcepcion porUsuarioInactivo(String email) {
        return new UsuarioNoValidoExcepcion(
                String.format("El usuario con email '%s' está inactivo", email)
        );
    }

    public static UsuarioNoValidoExcepcion porCredencialesInvalidas() {
        return new UsuarioNoValidoExcepcion("Las credenciales proporcionadas son inválidas");
    }
}
