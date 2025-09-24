package com.telconova.supportsuite.aplicacion.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para evidencia
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de una evidencia")
public class EvidenciaResponse {

    @Schema(description = "ID único de la evidencia", example = "1")
    private Long id;

    @Schema(description = "Tipo de evidencia", example = "COMENTARIO")
    private String tipo;

    @Schema(description = "Contenido del comentario", example = "Trabajo iniciado correctamente")
    private String contenido;

    @Schema(description = "URL de la foto", example = "https://storage.telconova.com/evidence/foto123.jpg")
    private String urlFoto;

    @Schema(description = "Nombre original del archivo", example = "evidencia_orden_001.jpg")
    private String nombreArchivo;

    @Schema(description = "Tamaño del archivo", example = "2.5 MB")
    private String tamanoArchivo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Fecha de creación")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Usuario que creó la evidencia")
    private String creadoPor;
}
