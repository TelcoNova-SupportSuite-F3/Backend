package com.telconova.supportsuite.aplicacion.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para clase MaterialUtilizadoResponse")
class MaterialUtilizadoResponseTest {


    @Test
    void debeCrearMaterialUtilizadoResponse() {
        // Arrange
        LocalDateTime fecha = LocalDateTime.now();

        // Act
        MaterialUtilizadoResponse response = MaterialUtilizadoResponse.builder()
                .id(1L)
                .codigoMaterial("MAT-001")
                .nombreMaterial("Cable UTP")
                .cantidadUtilizada(15)
                .unidadMedida("metros")
                .precioUnitario(2.50)
                .costoTotal(37.50)
                .fechaRegistro(fecha)
                .registradoPor("tecnico@telconova.com")
                .build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCantidadUtilizada()).isEqualTo(15);
        assertThat(response.getCostoTotal()).isEqualTo(37.50);
        assertThat(response.getRegistradoPor()).isEqualTo("tecnico@telconova.com");
    }
}
