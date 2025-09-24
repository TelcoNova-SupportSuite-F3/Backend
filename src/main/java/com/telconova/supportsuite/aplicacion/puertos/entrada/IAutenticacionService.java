package com.telconova.supportsuite.aplicacion.puertos.entrada;

import com.telconova.supportsuite.aplicacion.dto.request.LoginRequest;
import com.telconova.supportsuite.aplicacion.dto.response.LoginResponse;

public interface IAutenticacionService {

    /**
     * Autentica un usuario con sus credenciales
     */
    LoginResponse iniciarSesion(LoginRequest request);

    /**
     * Valida un token JWT
     */
    boolean validarToken(String token);

    /**
     * Obtiene el email del usuario desde un token
     */
    String obtenerEmailDeToken(String token);

    /**
     * Verifica si un token ha expirado
     */
    boolean tokenHaExpirado(String token);

    /**
     * Genera un nuevo token de acceso
     */
    String generarNuevoToken(String email);
}
