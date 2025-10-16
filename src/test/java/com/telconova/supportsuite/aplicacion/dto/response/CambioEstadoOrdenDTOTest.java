package com.telconova.supportsuite.aplicacion.dto.response;

import com.telconova.supportsuite.aplicacion.dto.request.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para clase CambioEstadoOrdenDTO")
class CambioEstadoOrdenDTOTest {

    @Test
    void debeCrearCambioEstadoOrdenDTO() {
        // Arrange
        LocalDateTime fecha = LocalDateTime.now();

        // Act
        CambioEstadoOrdenDTO dto = CambioEstadoOrdenDTO.builder()
                .numeroOrden("ORD-2025-001")
                .nombreTecnico("Juan Pérez")
                .estadoAnterior("PENDIENTE")
                .estadoNuevo("EN_PROCESO")
                .fechaHoraCambio(fecha)
                .clienteNombre("Cliente Test")
                .clienteTelefono("+57 300 123 4567")
                .emailCliente("cliente@email.com")
                .build();

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getNumeroOrden()).isEqualTo("ORD-2025-001");
        assertThat(dto.getNombreTecnico()).isEqualTo("Juan Pérez");
        assertThat(dto.getEstadoAnterior()).isEqualTo("PENDIENTE");
        assertThat(dto.getEstadoNuevo()).isEqualTo("EN_PROCESO");
        assertThat(dto.getFechaHoraCambio()).isEqualTo(fecha);
        assertThat(dto.getClienteNombre()).isEqualTo("Cliente Test");
        assertThat(dto.getClienteTelefono()).isEqualTo("+57 300 123 4567");
        assertThat(dto.getEmailCliente()).isEqualTo("cliente@email.com");
    }

    @Test
    void debeUsarSettersYGettersCorrectamente() {
        // Arrange
        LoginRequest request = new LoginRequest();
        MaterialResponse material = new MaterialResponse();

        // Act
        request.setEmail("nuevo@telconova.com");
        request.setContrasena("nueva123");

        material.setId(99L);
        material.setCodigo("NUEVO-001");
        material.setNombre("Nuevo Material");
        material.setDescripcion("Descripción nueva");
        material.setUnidadMedida("unidad");
        material.setPrecioUnitario(9.99);
        material.setStockDisponible(50);
        material.setActivo(false);

        // Assert
        assertThat(request.getEmail()).isEqualTo("nuevo@telconova.com");
        assertThat(request.getContrasena()).isEqualTo("nueva123");

        assertThat(material.getId()).isEqualTo(99L);
        assertThat(material.getCodigo()).isEqualTo("NUEVO-001");
        assertThat(material.getNombre()).isEqualTo("Nuevo Material");
        assertThat(material.getPrecioUnitario()).isEqualTo(9.99);
        assertThat(material.isActivo()).isFalse();
    }
}
