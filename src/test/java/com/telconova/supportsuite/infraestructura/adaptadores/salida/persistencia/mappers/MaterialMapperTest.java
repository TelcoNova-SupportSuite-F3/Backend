package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers;

import com.telconova.supportsuite.dominio.entidades.Material;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.MaterialEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para clase MaterialMapper")
class MaterialMapperTest {

    private MaterialMapper materialMapper;

    @BeforeEach
    void setUp() {
        materialMapper = new MaterialMapper();
    }


    @Test
    @DisplayName("Debe convertir MaterialEntity a Material correctamente")
    void debeConvertirMaterialEntityADominio() {
        // Arrange
        MaterialEntity entity = MaterialEntity.builder()
                .id(1L)
                .codigo("MAT-001")
                .nombre("Cable UTP")
                .descripcion("Cable de red")
                .unidadMedida("metros")
                .precioUnitario(new BigDecimal("2.5"))
                .stockDisponible(100)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        // Act
        Material dominio = materialMapper.toDomain(entity);

        // Assert
        assertThat(dominio).isNotNull();
        assertThat(dominio.getId()).isEqualTo(1L);
        assertThat(dominio.getCodigo()).isEqualTo("MAT-001");
        assertThat(dominio.getNombre()).isEqualTo("Cable UTP");
        assertThat(dominio.getDescripcion()).isEqualTo("Cable de red");
        assertThat(dominio.getUnidadMedida()).isEqualTo("metros");
        assertThat(dominio.getPrecioUnitario()).isEqualTo(new BigDecimal("2.5"));
        assertThat(dominio.getStockDisponible()).isEqualTo(100);
        assertThat(dominio.estaActivo()).isTrue();
    }

    @Test
    @DisplayName("Debe convertir Material a MaterialEntity correctamente")
    void debeConvertirMaterialAEntity() {
        // Arrange
        Material dominio = Material.builder()
                .id(2L)
                .codigo("MAT-002")
                .nombre("Conector RJ45")
                .descripcion("Conector de red")
                .unidadMedida("unidad")
                .precioUnitario(new BigDecimal("0.5"))
                .stockDisponible(500)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        // Act
        MaterialEntity entity = materialMapper.toEntity(dominio);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getCodigo()).isEqualTo("MAT-002");
        assertThat(entity.getNombre()).isEqualTo("Conector RJ45");
        assertThat(entity.getPrecioUnitario()).isEqualTo(new BigDecimal("0.5"));
        assertThat(entity.getActivo()).isTrue();
    }

    @Test
    @DisplayName("Debe manejar null en MaterialMapper")
    void debeManejarNullEnMaterialMapper() {
        // Act & Assert
        assertThat(materialMapper.toDomain(null)).isNull();
        assertThat(materialMapper.toEntity(null)).isNull();
    }


}
