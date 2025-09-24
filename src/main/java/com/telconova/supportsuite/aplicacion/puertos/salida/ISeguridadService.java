package com.telconova.supportsuite.aplicacion.puertos.salida;

public interface ISeguridadService {

    /**
     * Encripta una contraseña
     */
    String encriptarContrasena(String contrasenaPlana);

    /**
     * Verifica si una contraseña coincide con su hash
     */
    boolean verificarContrasena(String contrasenaPlana, String contrasenaEncriptada);

    /**
     * Genera un token JWT
     */
    String generarTokenJwt(String email, String rol);

    /**
     * Valida un token JWT
     */
    boolean validarTokenJwt(String token);

    /**
     * Extrae el email de un token JWT
     */
    String extraerEmailDeToken(String token);

    /**
     * Extrae el rol de un token JWT
     */
    String extraerRolDeToken(String token);

    /**
     * Verifica si un token ha expirado
     */
    boolean tokenHaExpirado(String token);

    /**
     * Obtiene el tiempo de expiración de un token
     */
    java.util.Date obtenerExpiracionToken(String token);
}
