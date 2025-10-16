package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers;

import com.telconova.supportsuite.dominio.entidades.Evidencia;
import com.telconova.supportsuite.dominio.enums.TipoEvidencia;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.EvidenciaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para clase EvidenciaMapper")
class EvidenciaMapperTest {

    private EvidenciaMapper evidenciaMapper;

    @BeforeEach
    void setUp() {
        evidenciaMapper = new EvidenciaMapper();
    }

    @Test
    @DisplayName("Debe convertir EvidenciaEntity a Evidencia correctamente")
    void debeConvertirEvidenciaEntityADominio() {
        // Arrange
        EvidenciaEntity entity = EvidenciaEntity.builder()
                .id(1L)
                .ordenTrabajoId(100L)
                .tipo(TipoEvidencia.FOTO)
                .contenido("Contenido de prueba")
                .rutaArchivo("/path/to/file.jpg")
                .nombreArchivoOriginal("foto.jpg")
                .tipoMime("image/jpeg")
                .tamanoArchivo(1024L)
                .fechaCreacion(LocalDateTime.now())
                .creadoPor(2L)
                .build();

        // Act
        Evidencia dominio = evidenciaMapper.toDomain(entity);

        // Assert
        assertThat(dominio).isNotNull();
        assertThat(dominio.getId()).isEqualTo(1L);
        assertThat(dominio.getOrdenTrabajoId()).isEqualTo(100L);
        assertThat(dominio.getTipo()).isEqualTo(TipoEvidencia.FOTO);
        assertThat(dominio.getContenido()).isEqualTo("Contenido de prueba");
        assertThat(dominio.getRutaArchivo()).isEqualTo("/path/to/file.jpg");
        assertThat(dominio.getNombreArchivoOriginal()).isEqualTo("foto.jpg");
        assertThat(dominio.getTipoMime()).isEqualTo("image/jpeg");
        assertThat(dominio.getTamanoArchivo()).isEqualTo(1024L);
        assertThat(dominio.getCreadoPor()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Debe convertir Evidencia a EvidenciaEntity correctamente")
    void debeConvertirEvidenciaAEntity() {
        // Arrange
        Evidencia dominio = Evidencia.builder()
                .id(1L)
                .ordenTrabajoId(100L)
                .tipo(TipoEvidencia.COMENTARIO)
                .contenido("Comentario de prueba")
                .rutaArchivo(null)
                .nombreArchivoOriginal(null)
                .tipoMime(null)
                .tamanoArchivo(null)
                .fechaCreacion(LocalDateTime.now())
                .creadoPor(2L)
                .build();

        // Act
        EvidenciaEntity entity = evidenciaMapper.toEntity(dominio);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getOrdenTrabajoId()).isEqualTo(100L);
        assertThat(entity.getTipo()).isEqualTo(TipoEvidencia.COMENTARIO);
        assertThat(entity.getContenido()).isEqualTo("Comentario de prueba");
        assertThat(entity.getRutaArchivo()).isNull();
        assertThat(entity.getCreadoPor()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Debe manejar null en EvidenciaMapper")
    void debeManejarNullEnEvidenciaMapper() {
        // Act & Assert
        assertThat(evidenciaMapper.toDomain(null)).isNull();
        assertThat(evidenciaMapper.toEntity(null)).isNull();
    }
}
