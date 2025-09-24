package com.telconova.supportsuite.aplicacion.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para material utilizado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de material utilizado en una orden")
public class MaterialUtilizadoResponse {

    @Schema(description = "ID del registro", example = "1")
    private Long id;

    @Schema(description = "Código del material", example = "CAB-UTP-001")
    private String codigoMaterial;

    @Schema(description = "Nombre del material", example = "Cable UTP Categoría 6")
    private String nombreMaterial;

    @Schema(description = "Cantidad utilizada", example = "15")
    private int cantidadUtilizada;

    @Schema(description = "Unidad de medida", example = "metros")
    private String unidadMedida;

    @Schema(description = "Precio unitario al momento de uso", example = "2.50")
    private double precioUnitario;

    @Schema(description = "Costo total", example = "37.50")
    private double costoTotal;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Fecha de registro")
    private LocalDateTime fechaRegistro;

    @Schema(description = "Usuario que registró el material")
    private String registradoPor;
}
