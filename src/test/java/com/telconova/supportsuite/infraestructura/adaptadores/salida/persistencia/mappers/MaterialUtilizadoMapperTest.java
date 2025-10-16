package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers;

import com.telconova.supportsuite.dominio.entidades.MaterialUtilizado;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.MaterialUtilizadoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para clase MaterialUtilizadoMapper")
class MaterialUtilizadoMapperTest {

    private MaterialUtilizadoMapper materialUtilizadoMapper;


    @BeforeEach
    void setUp() {
        materialUtilizadoMapper = new MaterialUtilizadoMapper();
    }


    @Test
    @DisplayName("Debe convertir MaterialUtilizadoEntity a MaterialUtilizado correctamente")
    void debeConvertirMaterialUtilizadoEntityADominio() {
        // Arrange
        MaterialUtilizadoEntity entity = MaterialUtilizadoEntity.builder()
                .id(1L)
                .ordenTrabajoId(100L)
                .materialId(10L)
                .cantidadUtilizada(5)
                .precioUnitario(new BigDecimal("2.5"))
                .fechaRegistro(LocalDateTime.now())
                .registradoPor(1L)
                .nombreMaterial("Cable UTP")
                .unidadMedida("metros")
                .codigoMaterial("MAT-001")
                .build();

        // Act
        MaterialUtilizado dominio = materialUtilizadoMapper.toDomain(entity);

        // Assert
        assertThat(dominio).isNotNull();
        assertThat(dominio.getId()).isEqualTo(1L);
        assertThat(dominio.getOrdenTrabajoId()).isEqualTo(100L);
        assertThat(dominio.getMaterialId()).isEqualTo(10L);
        assertThat(dominio.getCantidadUtilizada()).isEqualTo(5);
        assertThat(dominio.getPrecioUnitario()).isEqualTo(new BigDecimal("2.5"));
        assertThat(dominio.getNombreMaterial()).isEqualTo("Cable UTP");
        assertThat(dominio.getCodigoMaterial()).isEqualTo("MAT-001");
    }

    @Test
    @DisplayName("Debe convertir MaterialUtilizado a MaterialUtilizadoEntity correctamente")
    void debeConvertirMaterialUtilizadoAEntity() {
        // Arrange
        MaterialUtilizado dominio = MaterialUtilizado.builder()
                .id(2L)
                .ordenTrabajoId(200L)
                .materialId(20L)
                .cantidadUtilizada(10)
                .precioUnitario(new BigDecimal("1.5"))
                .fechaRegistro(LocalDateTime.now())
                .registradoPor(1L)
                .nombreMaterial("Conector RJ45")
                .unidadMedida("unidad")
                .codigoMaterial("MAT-002")
                .build();

        // Act
        MaterialUtilizadoEntity entity = materialUtilizadoMapper.toEntity(dominio);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getOrdenTrabajoId()).isEqualTo(200L);
        assertThat(entity.getMaterialId()).isEqualTo(20L);
        assertThat(entity.getCantidadUtilizada()).isEqualTo(10);
        assertThat(entity.getRegistradoPor()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Debe manejar null en MaterialUtilizadoMapper")
    void debeManejarNullEnMaterialUtilizadoMapper() {
        // Act & Assert
        assertThat(materialUtilizadoMapper.toDomain(null)).isNull();
        assertThat(materialUtilizadoMapper.toEntity(null)).isNull();
    }
}
