package com.telconova.supportsuite.aplicacion.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests para clase MaterialResponseTest")
class MaterialResponseTest {

    @Test
    @DisplayName("Debe crear MaterialResponse")
    void debeCrearMaterialResponse() {
        // Arrange & Act
        MaterialResponse response = MaterialResponse.builder()
                .id(1L)
                .codigo("MAT-001")
                .nombre("Cable UTP Cat 6")
                .descripcion("Cable de red categoría 6")
                .unidadMedida("metros")
                .precioUnitario(2.50)
                .stockDisponible(1000)
                .activo(true)
                .build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCodigo()).isEqualTo("MAT-001");
        assertThat(response.getNombre()).isEqualTo("Cable UTP Cat 6");
        assertThat(response.getPrecioUnitario()).isEqualTo(2.50);
        assertThat(response.getStockDisponible()).isEqualTo(1000);
        assertThat(response.isActivo()).isTrue();
    }
}
