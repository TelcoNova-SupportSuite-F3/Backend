package com.telconova.supportsuite.aplicacion.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para orden de trabajo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información completa de una orden de trabajo")
public class OrdenTrabajoResponse {

    @Schema(description = "ID único de la orden", example = "1")
    private Long id;

    @Schema(description = "Número de orden", example = "ORD-2025-001")
    private String numeroOrden;

    @Schema(description = "Título de la orden", example = "Instalación Internet Residencial")
    private String titulo;

    @Schema(description = "Descripción detallada", example = "Instalación de servicio de internet 100Mbps")
    private String descripcion;

    @Schema(description = "Estado actual", example = "EN_PROCESO")
    private String estado;

    @Schema(description = "Prioridad", example = "ALTA")
    private String prioridad;

    @Schema(description = "Tipo de servicio", example = "INSTALACION")
    private String tipoServicio;

    @Schema(description = "Nombre del cliente", example = "Ana García Ruiz")
    private String clienteNombre;

    @Schema(description = "Teléfono del cliente", example = "+57 300 123 4567")
    private String clienteTelefono;

    @Schema(description = "Dirección del cliente", example = "Carrera 70 #45-23, Medellín")
    private String direccion;

    @Schema(description = "Información del técnico asignado")
    private TecnicoResponse tecnicoAsignado;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Fecha de asignación")
    private LocalDateTime fechaAsignacion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Fecha de inicio del trabajo")
    private LocalDateTime fechaInicioTrabajo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Fecha de finalización del trabajo")
    private LocalDateTime fechaFinTrabajo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Fecha de creación de la orden")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Lista de evidencias de la orden")
    private List<EvidenciaResponse> evidencias;

    @Schema(description = "Lista de materiales utilizados")
    private List<MaterialUtilizadoResponse> materialesUtilizados;

    @Schema(description = "Costo total de materiales", example = "150.75")
    private double costoTotalMateriales;

    @Schema(description = "Duración del trabajo en horas", example = "4.25")
    private double duracionTrabajoHoras;

    @Schema(description = "Indica si la orden está vencida", example = "false")
    private boolean estaVencida;
}
