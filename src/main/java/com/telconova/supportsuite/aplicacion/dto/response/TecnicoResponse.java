package com.telconova.supportsuite.aplicacion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de respuesta para información del técnico
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información básica del técnico")
public class TecnicoResponse {

    @Schema(description = "ID del técnico", example = "2")
    private Long id;

    @Schema(description = "Email del técnico", example = "juan.perez@telconova.com")
    private String email;

    @Schema(description = "Nombre completo", example = "Juan Pérez González")
    private String nombreCompleto;

    @Schema(description = "Estado activo", example = "true")
    private boolean activo;
}
