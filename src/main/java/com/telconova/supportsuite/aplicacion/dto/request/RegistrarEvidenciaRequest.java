package com.telconova.supportsuite.aplicacion.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para registrar evidencia
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para registrar evidencia en una orden de trabajo")
public class RegistrarEvidenciaRequest {
/*
    @Size(max = 500, message = "El comentario no puede exceder 500 caracteres")
    @Schema(description = "Comentario de la evidencia (máximo 500 caracteres)",
            example = "Instalación completada satisfactoriamente. Cliente conforme.")
    private String comentario;

    @Schema(description = "Archivo de foto como evidencia (JPG, JPEG, PNG)")
    private MultipartFile foto;*/


    @Size(max = 500, message = "El comentario no puede exceder 500 caracteres")
    @Schema(
            description = "Comentario de la evidencia (opcional, máximo 500 caracteres)",
            example = "Instalación completada satisfactoriamente. Cliente conforme con el servicio prestado.",
            maxLength = 500,
            nullable = true
    )
    private String comentario;

    @Schema(
            description = "Archivo de foto como evidencia (opcional). Formatos soportados: JPG, JPEG, PNG. Tamaño máximo: 10MB",
            type = "string",
            format = "binary",
            nullable = true
    )
    private MultipartFile foto;

    // Validación personalizada
    public boolean tieneContenido() {
        boolean tieneComentario = comentario != null && !comentario.trim().isEmpty();
        boolean tieneFoto = foto != null && !foto.isEmpty();
        return tieneComentario || tieneFoto;
    }
}
