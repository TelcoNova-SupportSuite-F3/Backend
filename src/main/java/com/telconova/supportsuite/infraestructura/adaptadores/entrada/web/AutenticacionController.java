package com.telconova.supportsuite.infraestructura.adaptadores.entrada.web;

import com.telconova.supportsuite.aplicacion.puertos.entrada.IAutenticacionService;
import com.telconova.supportsuite.aplicacion.dto.request.LoginRequest;
import com.telconova.supportsuite.aplicacion.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de autenticación
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación de usuarios")
public class AutenticacionController {

    private final IAutenticacionService autenticacionService;

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario con email @telconova.com y contraseña",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login exitoso"),
                    @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Intento de login para usuario: {}", request.getEmail());

        LoginResponse response = autenticacionService.iniciarSesion(request);

        log.info("Login exitoso para usuario: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Validar token",
            description = "Valida si un token JWT es válido y no ha expirado"
    )
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validarToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(false);
        }

        String token = authHeader.substring(7);
        boolean isValid = autenticacionService.validarToken(token);

        return ResponseEntity.ok(isValid);
    }
}
