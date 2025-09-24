package com.telconova.supportsuite.infraestructura.adaptadores.entrada.web;

import com.telconova.supportsuite.aplicacion.puertos.entrada.IOrdenTrabajoService;
import com.telconova.supportsuite.aplicacion.dto.request.ActualizarEstadoRequest;
import com.telconova.supportsuite.aplicacion.dto.response.OrdenTrabajoResponse;
import com.telconova.supportsuite.dominio.enums.EstadoOrden;
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
 * Controlador REST para operaciones con órdenes de trabajo
 */
@Slf4j
@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Órdenes de Trabajo", description = "Gestión de órdenes de trabajo")
public class OrdenTrabajoController {

    private final IOrdenTrabajoService ordenTrabajoService;

    @Operation(
            summary = "Obtener mis órdenes",
            description = "Obtiene las órdenes asignadas al técnico autenticado"
    )
    @GetMapping("/mis-ordenes")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<List<OrdenTrabajoResponse>> obtenerMisOrdenes(Authentication authentication) {
        String emailUsuario = authentication.getName();
        log.info("Obteniendo órdenes para técnico: {}", emailUsuario);

        List<OrdenTrabajoResponse> ordenes = ordenTrabajoService.obtenerOrdenesPorTecnico(emailUsuario);
        return ResponseEntity.ok(ordenes);
    }

    @Operation(
            summary = "Obtener todas las órdenes",
            description = "Obtiene todas las órdenes del sistema (solo administradores)"
    )
    @GetMapping("/todas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrdenTrabajoResponse>> obtenerTodasLasOrdenes() {
        log.info("Obteniendo todas las órdenes del sistema");

        List<OrdenTrabajoResponse> ordenes = ordenTrabajoService.obtenerTodasLasOrdenes();
        return ResponseEntity.ok(ordenes);
    }

    @Operation(
            summary = "Obtener orden por ID",
            description = "Obtiene los detalles de una orden específica"
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    public ResponseEntity<OrdenTrabajoResponse> obtenerOrdenPorId(
            @Parameter(description = "ID de la orden") @PathVariable Long id,
            Authentication authentication) {
        String emailUsuario = authentication.getName();
        log.info("Obteniendo orden {} para usuario: {}", id, emailUsuario);

        OrdenTrabajoResponse orden = ordenTrabajoService.obtenerOrdenPorId(id, emailUsuario);
        return ResponseEntity.ok(orden);
    }

    @Operation(
            summary = "Actualizar estado de orden",
            description = "Actualiza el estado de una orden de trabajo"
    )
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<OrdenTrabajoResponse> actualizarEstadoOrden(
            @Parameter(description = "ID de la orden") @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoRequest request,
            Authentication authentication) {
        String emailUsuario = authentication.getName();
        log.info("Actualizando estado de orden {} a {} por usuario: {}",
                id, request.getNuevoEstado(), emailUsuario);

        OrdenTrabajoResponse orden = ordenTrabajoService.actualizarEstadoOrden(id, request, emailUsuario);
        return ResponseEntity.ok(orden);
    }

    @Operation(
            summary = "Finalizar orden",
            description = "Finaliza una orden de trabajo con fechas de inicio y fin obligatorias"
    )
    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<OrdenTrabajoResponse> finalizarOrden(
            @Parameter(description = "ID de la orden") @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoRequest request,
            Authentication authentication) {
        String emailUsuario = authentication.getName();
        log.info("Finalizando orden {} por usuario: {}", id, emailUsuario);

        OrdenTrabajoResponse orden = ordenTrabajoService.finalizarOrden(id, request, emailUsuario);
        return ResponseEntity.ok(orden);
    }

    @Operation(
            summary = "Obtener órdenes por estado",
            description = "Filtra órdenes por estado específico"
    )
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    public ResponseEntity<List<OrdenTrabajoResponse>> obtenerOrdenesPorEstado(
            @Parameter(description = "Estado de la orden") @PathVariable EstadoOrden estado,
            Authentication authentication) {
        String emailUsuario = authentication.getName();
        log.info("Obteniendo órdenes en estado {} para usuario: {}", estado, emailUsuario);

        List<OrdenTrabajoResponse> ordenes = ordenTrabajoService.obtenerOrdenesPorEstado(estado, emailUsuario);
        return ResponseEntity.ok(ordenes);
    }
}
