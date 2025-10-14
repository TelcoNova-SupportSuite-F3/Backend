package com.telconova.supportsuite.aplicacion.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO con la información de cambio de estado de una orden
 */
@Getter
@Builder
public class CambioEstadoOrdenDTO {
    private String numeroOrden;
    private String nombreTecnico;
    private String estadoAnterior;
    private String estadoNuevo;
    private LocalDateTime fechaHoraCambio;
    private String clienteNombre;
    private String clienteTelefono;
    private String emailCliente;
}
