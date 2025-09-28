package com.telconova.supportsuite.aplicacion.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para solicitud de inicio de sesión
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos requeridos para el inicio de sesión")
public class LoginRequest {
    @Schema(description = "Email del usuario", example = "juan.perez@telconova.com")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Pattern(regexp = ".*@telconova\\.com$", message = "Solo se permiten emails del dominio @telconova.com")
    private String email;

    @Schema(description = "Contraseña del usuario", example = "miContrasena123")
    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;
}
