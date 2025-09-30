package com.telconova.supportsuite.infraestructura.adaptadores.entrada.web;

import com.telconova.supportsuite.aplicacion.puertos.entrada.IEvidenciaService;
import com.telconova.supportsuite.aplicacion.dto.request.RegistrarEvidenciaRequest;
import com.telconova.supportsuite.aplicacion.dto.response.EvidenciaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import java.util.List;

/**
 * Controlador REST para operaciones con evidencias
 */
@Slf4j
@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Evidencias", description = "Gestión de evidencias de órdenes")
public class EvidenciaController {

    private final IEvidenciaService evidenciaService;

    @Operation(
            summary = "Agregar comentario como evidencia",
            description = "Registra únicamente un comentario de texto como evidencia de la orden de trabajo",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Comentario agregado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Comentario inválido o muy largo"),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "403", description = "Sin permisos para esta orden"),
                    @ApiResponse(responseCode = "404", description = "Orden no encontrada")
            }
    )
    @PostMapping("/{id}/evidencias/comentario")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<EvidenciaResponse> agregarComentario(
            @Parameter(description = "ID de la orden de trabajo", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Comentario de la evidencia (máximo 500 caracteres)", required = true,
                    example = "Instalación completada satisfactoriamente. Cliente conforme con el servicio.")
            @RequestParam String comentario,
            Authentication authentication) {

        String emailUsuario = authentication.getName();
        log.info("Agregando comentario a orden {} por usuario: {}", id, emailUsuario);

        EvidenciaResponse evidencia = evidenciaService.registrarComentario(id, comentario, emailUsuario);
        return ResponseEntity.ok(evidencia);
    }

    @Operation(
            summary = "Subir foto como evidencia",
            description = "Sube únicamente una fotografía como evidencia de la orden de trabajo. " +
                    "Formatos permitidos: JPG, JPEG, PNG. Tamaño máximo: 10MB",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Foto subida exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Archivo inválido, formato no permitido o muy grande"),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "403", description = "Sin permisos para esta orden"),
                    @ApiResponse(responseCode = "404", description = "Orden no encontrada")
            }
    )
    @PostMapping(value = "/{id}/evidencias/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<EvidenciaResponse> subirFoto(
            @Parameter(description = "ID de la orden de trabajo", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Archivo de imagen (JPG, JPEG, PNG - máximo 10MB)", required = true)
            @RequestParam("foto") MultipartFile foto,
            Authentication authentication) {

        String emailUsuario = authentication.getName();
        log.info("Subiendo foto a orden {} por usuario: {} - Archivo: {} ({})",
                id, emailUsuario, foto.getOriginalFilename(), foto.getContentType());

        EvidenciaResponse evidencia = evidenciaService.registrarFoto(id, foto, emailUsuario);
        return ResponseEntity.ok(evidencia);
    }

    @Operation(
            summary = "Agregar evidencia mixta (comentario y/o foto)",
            description = "Registra evidencia que puede incluir comentario, foto, o ambos. " +
                    "Debe incluir al menos uno de los dos campos. " +
                    "Para archivos: JPG, JPEG, PNG, SVG - máximo 10MB",
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = RegistrarEvidenciaRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Evidencia agregada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Debe incluir al menos comentario o foto"),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "403", description = "Sin permisos para esta orden"),
                    @ApiResponse(responseCode = "404", description = "Orden no encontrada")
            }
    )
    @PostMapping(value = "/{id}/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<EvidenciaResponse> agregarEvidencia(
            @Parameter(description = "ID de la orden de trabajo", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestParam(required = false) String comentario,
            @Parameter(hidden = true) @RequestParam(value = "foto", required = false) MultipartFile foto,
            Authentication authentication) {

        String emailUsuario = authentication.getName();
        log.info("Agregando evidencia mixta a orden {} por usuario: {} - Comentario: {} - Archivo: {}",
                id, emailUsuario,
                comentario != null && !comentario.trim().isEmpty() ? "Sí" : "No",
                foto != null && !foto.isEmpty() ? foto.getOriginalFilename() : "No");

        RegistrarEvidenciaRequest request = RegistrarEvidenciaRequest.builder()
                .comentario(comentario)
                .foto(foto)
                .build();

        EvidenciaResponse evidencia = evidenciaService.registrarEvidenciaMixta(id, request, emailUsuario);
        return ResponseEntity.ok(evidencia);
    }

    @Operation(
            summary = "Obtener todas las evidencias de una orden",
            description = "Lista todas las evidencias (comentarios y fotos) de una orden de trabajo específica, " +
                    "ordenadas por fecha de creación descendente",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de evidencias obtenida exitosamente"),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "403", description = "Sin permisos para esta orden"),
                    @ApiResponse(responseCode = "404", description = "Orden no encontrada")
            }
    )
    @GetMapping("/{id}/evidencias")
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    public ResponseEntity<List<EvidenciaResponse>> obtenerEvidencias(
            @Parameter(description = "ID de la orden de trabajo", required = true, example = "1")
            @PathVariable Long id,
            Authentication authentication) {

        String emailUsuario = authentication.getName();
        log.info("Obteniendo evidencias de orden {} para usuario: {}", id, emailUsuario);

        List<EvidenciaResponse> evidencias = evidenciaService.obtenerEvidenciasPorOrden(id, emailUsuario);
        return ResponseEntity.ok(evidencias);
    }

    @Operation(
            summary = "Eliminar evidencia",
            description = "Elimina una evidencia específica. Solo puede ser eliminada por el usuario que la creó " +
                    "o por un administrador. Si es una foto, también se elimina de Cloudinary.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Evidencia eliminada exitosamente"),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "403", description = "Sin permisos para eliminar esta evidencia"),
                    @ApiResponse(responseCode = "404", description = "Evidencia no encontrada")
            }
    )
    @DeleteMapping("/evidencias/{evidenciaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarEvidencia(
            @Parameter(description = "ID de la evidencia a eliminar", required = true, example = "1")
            @PathVariable Long evidenciaId,
            Authentication authentication) {

        String emailUsuario = authentication.getName();
        log.info("Eliminando evidencia {} solicitado por: {}", evidenciaId, emailUsuario);

        evidenciaService.eliminarEvidencia(evidenciaId, emailUsuario);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Obtener evidencia específica",
            description = "Obtiene los detalles de una evidencia específica por su ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Evidencia obtenida exitosamente"),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "403", description = "Sin permisos para esta evidencia"),
                    @ApiResponse(responseCode = "404", description = "Evidencia no encontrada")
            }
    )
    @GetMapping("/evidencias/{evidenciaId}")
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    public ResponseEntity<EvidenciaResponse> obtenerEvidencia(
            @Parameter(description = "ID de la evidencia", required = true, example = "1")
            @PathVariable Long evidenciaId,
            Authentication authentication) {

        String emailUsuario = authentication.getName();
        log.info("Obteniendo evidencia {} solicitado por: {}", evidenciaId, emailUsuario);

        EvidenciaResponse evidencia = evidenciaService.obtenerEvidenciaPorId(evidenciaId, emailUsuario);
        return ResponseEntity.ok(evidencia);
    }
}
