package com.telconova.supportsuite.aplicacion.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para el inicio de sesión
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta del inicio de sesión exitoso")
public class LoginResponse {

    @Schema(description = "Token JWT de acceso", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Tipo de token", example = "Bearer")
    private String tipoToken;

    @Schema(description = "Email del usuario autenticado", example = "juan.perez@telconova.com")
    private String email;

    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez González")
    private String nombreCompleto;

    @Schema(description = "Rol del usuario", example = "TECNICO")
    private String rol;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Bogota")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Schema(description = "Fecha y hora de expiración del token", example = "2025-09-21 11:45:28")
    private LocalDateTime expiracion;

    @Schema(description = "Indica si el usuario está activo", example = "true")
    private boolean activo;
}
