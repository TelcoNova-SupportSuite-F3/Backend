package com.telconova.supportsuite.aplicacion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de respuesta para material
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de un material")
public class MaterialResponse {

    @Schema(description = "ID único del material", example = "1")
    private Long id;

    @Schema(description = "Código del material", example = "CAB-UTP-001")
    private String codigo;

    @Schema(description = "Nombre del material", example = "Cable UTP Categoría 6")
    private String nombre;

    @Schema(description = "Descripción", example = "Cable de red UTP Cat 6 para instalaciones")
    private String descripcion;

    @Schema(description = "Unidad de medida", example = "metros")
    private String unidadMedida;

    @Schema(description = "Precio unitario", example = "2.50")
    private double precioUnitario;

    @Schema(description = "Stock disponible", example = "1000")
    private int stockDisponible;

    @Schema(description = "Indica si está activo", example = "true")
    private boolean activo;
}
