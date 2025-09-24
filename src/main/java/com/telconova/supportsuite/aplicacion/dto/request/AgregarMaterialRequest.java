package com.telconova.supportsuite.aplicacion.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para agregar material a una orden
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para agregar material a una orden de trabajo")
public class AgregarMaterialRequest {

    @NotNull(message = "El ID del material es obligatorio")
    @Schema(description = "ID único del material a agregar",
            example = "1",
            required = true)
    private Long materialId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    @Schema(description = "Cantidad de material a utilizar",
            example = "5",
            required = true)
    private Integer cantidad;
}
