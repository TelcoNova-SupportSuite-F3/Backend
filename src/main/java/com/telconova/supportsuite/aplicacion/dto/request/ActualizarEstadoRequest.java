package com.telconova.supportsuite.aplicacion.dto.request;

import com.telconova.supportsuite.dominio.enums.EstadoOrden;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO para actualizar el estado de una orden
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para actualizar el estado de una orden de trabajo")
public class ActualizarEstadoRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    @Schema(description = "Nuevo estado de la orden",
            example = "EN_PROCESO",
            required = true)
    private EstadoOrden nuevoEstado;

    @Schema(description = "Observaciones sobre el cambio de estado",
            example = "Iniciando trabajo en sitio del cliente")
    private String observaciones;

    @Schema(description = "Fecha de inicio del trabajo (requerida para finalización)",
            example = "2025-01-15T08:30:00")
    private LocalDateTime fechaInicioTrabajo;

    @Schema(description = "Fecha de finalización del trabajo (requerida para finalización)",
            example = "2025-01-15T12:45:00")
    private LocalDateTime fechaFinTrabajo;
}
