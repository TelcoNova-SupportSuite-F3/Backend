package com.telconova.supportsuite.infraestructura.adaptadores.entrada.web;

import com.telconova.supportsuite.aplicacion.puertos.entrada.IMaterialService;
import com.telconova.supportsuite.aplicacion.dto.request.AgregarMaterialRequest;
import com.telconova.supportsuite.aplicacion.dto.response.MaterialResponse;
import com.telconova.supportsuite.aplicacion.dto.response.MaterialUtilizadoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones con materiales
 */
@Slf4j
@RestController
@RequestMapping("/materiales")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Materiales", description = "Gestión de materiales y su utilización")
public class MaterialController {

    private final IMaterialService materialService;

    @Operation(
            summary = "Buscar materiales",
            description = "Busca materiales por nombre con autocompletado (mínimo 2 caracteres)"
    )
    @GetMapping("/buscar")
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    public ResponseEntity<List<MaterialResponse>> buscarMateriales(
            @Parameter(description = "Texto de búsqueda") @RequestParam String q,
            @Parameter(description = "Límite de resultados") @RequestParam(defaultValue = "10") int limite) {
        log.info("Buscando materiales con texto: '{}' límite: {}", q, limite);

        List<MaterialResponse> materiales = materialService.buscarMaterialesPorNombre(q, limite);
        return ResponseEntity.ok(materiales);
    }

    @Operation(
            summary = "Obtener materiales activos",
            description = "Lista todos los materiales activos disponibles"
    )
    @GetMapping
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    public ResponseEntity<List<MaterialResponse>> obtenerMaterialesActivos() {
        log.info("Obteniendo materiales activos");

        List<MaterialResponse> materiales = materialService.obtenerMaterialesActivos();
        return ResponseEntity.ok(materiales);
    }

    @Operation(
            summary = "Obtener material por ID",
            description = "Obtiene los detalles de un material específico"
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    public ResponseEntity<MaterialResponse> obtenerMaterialPorId(
            @Parameter(description = "ID del material") @PathVariable Long id) {
        log.info("Obteniendo material por ID: {}", id);

        MaterialResponse material = materialService.obtenerMaterialPorId(id);
        return ResponseEntity.ok(material);
    }

    @Operation(
            summary = "Agregar material a orden",
            description = "Agrega un material a una orden de trabajo (solo si está EN_PROCESO)"
    )
    @PostMapping("/ordenes/{ordenId}/materiales")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<Void> agregarMaterialAOrden(
            @Parameter(description = "ID de la orden") @PathVariable Long ordenId,
            @Valid @RequestBody AgregarMaterialRequest request,
            Authentication authentication) {
        String emailUsuario = authentication.getName();
        log.info("Agregando material {} a orden {} por usuario: {}",
                request.getMaterialId(), ordenId, emailUsuario);

        materialService.agregarMaterialAOrden(ordenId, request, emailUsuario);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Obtener materiales utilizados en orden",
            description = "Lista los materiales utilizados en una orden específica"
    )
    @GetMapping("/ordenes/{ordenId}/materiales")
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    public ResponseEntity<List<MaterialUtilizadoResponse>> obtenerMaterialesUtilizados(
            @Parameter(description = "ID de la orden") @PathVariable Long ordenId,
            Authentication authentication) {
        String emailUsuario = authentication.getName();
        log.info("Obteniendo materiales utilizados en orden {} para usuario: {}", ordenId, emailUsuario);

        List<MaterialUtilizadoResponse> materiales = materialService.obtenerMaterialesUtilizadosPorOrden(ordenId, emailUsuario);
        return ResponseEntity.ok(materiales);
    }

    @Operation(
            summary = "Verificar disponibilidad de stock",
            description = "Verifica si hay stock suficiente de un material"
    )
    @GetMapping("/{id}/stock/{cantidad}")
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> verificarStock(
            @Parameter(description = "ID del material") @PathVariable Long id,
            @Parameter(description = "Cantidad requerida") @PathVariable Integer cantidad) {
        log.info("Verificando stock para material {} cantidad: {}", id, cantidad);

        boolean disponible = materialService.verificarDisponibilidadStock(id, cantidad);
        return ResponseEntity.ok(disponible);
    }
}
