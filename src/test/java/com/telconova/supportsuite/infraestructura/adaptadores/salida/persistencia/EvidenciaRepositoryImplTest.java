package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia;

import com.telconova.supportsuite.dominio.entidades.Evidencia;
import com.telconova.supportsuite.dominio.enums.TipoEvidencia;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.EvidenciaEntity;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers.EvidenciaMapper;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios.EvidenciaJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias para EvidenciaRepositoryImpl")
class EvidenciaRepositoryImplTest {

    @Mock
    private EvidenciaJpaRepository jpaRepository;

    @Mock
    private EvidenciaMapper mapper;

    @InjectMocks
    private EvidenciaRepositoryImpl evidenciaRepository;

    private Evidencia evidenciaDominio;
    private EvidenciaEntity evidenciaEntity;

    @BeforeEach
    void setUp() {
        // Configurar evidencia de dominio
        evidenciaDominio = Evidencia.builder()
                .id(1L)
                .ordenTrabajoId(100L)
                .tipo(TipoEvidencia.FOTO)
                .contenido("Evidencia de prueba")
                .rutaArchivo("cloudinary/imagen.jpg")
                .nombreArchivoOriginal("imagen.jpg")
                .tipoMime("image/jpeg")
                .tamanoArchivo(1024L)
                .creadoPor(50L)
                .fechaCreacion(LocalDateTime.now())
                .build();

        // Configurar evidencia entity
        evidenciaEntity = new EvidenciaEntity();
        evidenciaEntity.setId(1L);
        evidenciaEntity.setOrdenTrabajoId(100L);
        evidenciaEntity.setTipo(TipoEvidencia.FOTO);
        evidenciaEntity.setContenido("Evidencia de prueba");
        evidenciaEntity.setNombreArchivoOriginal("imagen.jpg");
        evidenciaEntity.setRutaArchivo("cloudinary/imagen.jpg");
        evidenciaEntity.setTamanoArchivo(1024L);
        evidenciaEntity.setCreadoPor(50L);
        evidenciaEntity.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    void testGuardar_Exitoso() {
        // Arrange
        when(mapper.toEntity(evidenciaDominio)).thenReturn(evidenciaEntity);
        when(jpaRepository.save(evidenciaEntity)).thenReturn(evidenciaEntity);
        when(mapper.toDomain(evidenciaEntity)).thenReturn(evidenciaDominio);

        // Act
        Evidencia resultado = evidenciaRepository.guardar(evidenciaDominio);

        // Assert
        assertNotNull(resultado);
        assertEquals(evidenciaDominio.getId(), resultado.getId());
        assertEquals(evidenciaDominio.getOrdenTrabajoId(), resultado.getOrdenTrabajoId());
        assertEquals(evidenciaDominio.getTipo(), resultado.getTipo());

        verify(mapper).toEntity(evidenciaDominio);
        verify(jpaRepository).save(evidenciaEntity);
        verify(mapper).toDomain(evidenciaEntity);
    }

    @Test
    void testGuardar_NuevaEvidenciaSinId() {
        // Arrange
        Evidencia evidenciaNueva = Evidencia.builder()
                .ordenTrabajoId(100L)
                .tipo(TipoEvidencia.COMENTARIO)
                .contenido("Nueva evidencia")
                .build();

        EvidenciaEntity entityNueva = new EvidenciaEntity();
        entityNueva.setOrdenTrabajoId(100L);

        EvidenciaEntity entityGuardada = new EvidenciaEntity();
        entityGuardada.setId(2L);
        entityGuardada.setOrdenTrabajoId(100L);

        Evidencia evidenciaGuardada = Evidencia.builder()
                .id(2L)
                .ordenTrabajoId(100L)
                .tipo(TipoEvidencia.COMENTARIO)
                .build();

        when(mapper.toEntity(evidenciaNueva)).thenReturn(entityNueva);
        when(jpaRepository.save(entityNueva)).thenReturn(entityGuardada);
        when(mapper.toDomain(entityGuardada)).thenReturn(evidenciaGuardada);

        // Act
        Evidencia resultado = evidenciaRepository.guardar(evidenciaNueva);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertEquals(2L, resultado.getId());
        verify(jpaRepository).save(entityNueva);
    }

    @Test
    void testBuscarPorId_Encontrado() {
        // Arrange
        Long evidenciaId = 1L;
        when(jpaRepository.findById(evidenciaId)).thenReturn(Optional.of(evidenciaEntity));
        when(mapper.toDomain(evidenciaEntity)).thenReturn(evidenciaDominio);

        // Act
        Optional<Evidencia> resultado = evidenciaRepository.buscarPorId(evidenciaId);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(evidenciaDominio.getId(), resultado.get().getId());
        assertEquals(evidenciaDominio.getOrdenTrabajoId(), resultado.get().getOrdenTrabajoId());

        verify(jpaRepository).findById(evidenciaId);
        verify(mapper).toDomain(evidenciaEntity);
    }

    @Test
    void testBuscarPorId_NoEncontrado() {
        // Arrange
        Long evidenciaId = 999L;
        when(jpaRepository.findById(evidenciaId)).thenReturn(Optional.empty());

        // Act
        Optional<Evidencia> resultado = evidenciaRepository.buscarPorId(evidenciaId);

        // Assert
        assertFalse(resultado.isPresent());
        verify(jpaRepository).findById(evidenciaId);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void testObtenerEvidenciasPorOrden_ConResultados() {
        // Arrange
        Long ordenTrabajoId = 100L;

        EvidenciaEntity entity2 = new EvidenciaEntity();
        entity2.setId(2L);
        entity2.setOrdenTrabajoId(ordenTrabajoId);

        Evidencia evidencia2 = Evidencia.builder()
                .id(2L)
                .ordenTrabajoId(ordenTrabajoId)
                .build();

        List<EvidenciaEntity> entities = Arrays.asList(evidenciaEntity, entity2);

        when(jpaRepository.findEvidenciasPorOrdenOrdenadas(ordenTrabajoId)).thenReturn(entities);
        when(mapper.toDomain(evidenciaEntity)).thenReturn(evidenciaDominio);
        when(mapper.toDomain(entity2)).thenReturn(evidencia2);

        // Act
        List<Evidencia> resultado = evidenciaRepository.obtenerEvidenciasPorOrden(ordenTrabajoId);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(evidenciaDominio.getId(), resultado.get(0).getId());
        assertEquals(evidencia2.getId(), resultado.get(1).getId());

        verify(jpaRepository).findEvidenciasPorOrdenOrdenadas(ordenTrabajoId);
        verify(mapper, times(2)).toDomain(any(EvidenciaEntity.class));
    }

    @Test
    void testObtenerEvidenciasPorOrden_SinResultados() {
        // Arrange
        Long ordenTrabajoId = 999L;
        when(jpaRepository.findEvidenciasPorOrdenOrdenadas(ordenTrabajoId))
                .thenReturn(Collections.emptyList());

        // Act
        List<Evidencia> resultado = evidenciaRepository.obtenerEvidenciasPorOrden(ordenTrabajoId);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(jpaRepository).findEvidenciasPorOrdenOrdenadas(ordenTrabajoId);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void testObtenerEvidenciasPorTipo_ConResultados() {
        // Arrange
        Long ordenTrabajoId = 100L;
        TipoEvidencia tipo = TipoEvidencia.FOTO;

        List<EvidenciaEntity> entities = Collections.singletonList(evidenciaEntity);

        when(jpaRepository.findByOrdenTrabajoIdAndTipo(ordenTrabajoId, tipo)).thenReturn(entities);
        when(mapper.toDomain(evidenciaEntity)).thenReturn(evidenciaDominio);

        // Act
        List<Evidencia> resultado = evidenciaRepository.obtenerEvidenciasPorTipo(ordenTrabajoId, tipo);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(tipo, resultado.get(0).getTipo());

        verify(jpaRepository).findByOrdenTrabajoIdAndTipo(ordenTrabajoId, tipo);
        verify(mapper).toDomain(evidenciaEntity);
    }

    @Test
    void testObtenerEvidenciasPorTipo_TipoDespues() {
        // Arrange
        Long ordenTrabajoId = 100L;
        TipoEvidencia tipo = TipoEvidencia.FOTO;

        EvidenciaEntity entityDespues = new EvidenciaEntity();
        entityDespues.setId(3L);
        entityDespues.setTipo(TipoEvidencia.FOTO);

        Evidencia evidenciaDespues = Evidencia.builder()
                .id(3L)
                .tipo(TipoEvidencia.FOTO)
                .build();

        List<EvidenciaEntity> entities = Collections.singletonList(entityDespues);

        when(jpaRepository.findByOrdenTrabajoIdAndTipo(ordenTrabajoId, tipo)).thenReturn(entities);
        when(mapper.toDomain(entityDespues)).thenReturn(evidenciaDespues);

        // Act
        List<Evidencia> resultado = evidenciaRepository.obtenerEvidenciasPorTipo(ordenTrabajoId, tipo);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(TipoEvidencia.FOTO, resultado.get(0).getTipo());
    }

    @Test
    void testObtenerEvidenciasPorTipo_SinResultados() {
        // Arrange
        Long ordenTrabajoId = 100L;
        TipoEvidencia tipo = TipoEvidencia.COMENTARIO;

        when(jpaRepository.findByOrdenTrabajoIdAndTipo(ordenTrabajoId, tipo))
                .thenReturn(Collections.emptyList());

        // Act
        List<Evidencia> resultado = evidenciaRepository.obtenerEvidenciasPorTipo(ordenTrabajoId, tipo);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void testEliminar_Exitoso() {
        // Arrange
        Long evidenciaId = 1L;
        doNothing().when(jpaRepository).deleteById(evidenciaId);

        // Act
        assertDoesNotThrow(() -> evidenciaRepository.eliminar(evidenciaId));

        // Assert
        verify(jpaRepository).deleteById(evidenciaId);
    }

    @Test
    void testEliminar_IdNulo() {
        // Arrange
        Long evidenciaId = null;

        // Act & Assert
        assertDoesNotThrow(() -> evidenciaRepository.eliminar(evidenciaId));
        verify(jpaRepository).deleteById(evidenciaId);
    }

    @Test
    void testContarEvidenciasPorOrden_ConEvidencias() {
        // Arrange
        Long ordenTrabajoId = 100L;
        long cantidadEsperada = 5L;

        when(jpaRepository.countByOrdenTrabajoId(ordenTrabajoId)).thenReturn(cantidadEsperada);

        // Act
        long resultado = evidenciaRepository.contarEvidenciasPorOrden(ordenTrabajoId);

        // Assert
        assertEquals(cantidadEsperada, resultado);
        verify(jpaRepository).countByOrdenTrabajoId(ordenTrabajoId);
    }

    @Test
    void testContarEvidenciasPorOrden_SinEvidencias() {
        // Arrange
        Long ordenTrabajoId = 999L;
        when(jpaRepository.countByOrdenTrabajoId(ordenTrabajoId)).thenReturn(0L);

        // Act
        long resultado = evidenciaRepository.contarEvidenciasPorOrden(ordenTrabajoId);

        // Assert
        assertEquals(0L, resultado);
        verify(jpaRepository).countByOrdenTrabajoId(ordenTrabajoId);
    }

    @Test
    void testObtenerEvidenciasPorUsuario_ConResultados() {
        // Arrange
        Long usuarioId = 50L;

        EvidenciaEntity entity2 = new EvidenciaEntity();
        entity2.setId(2L);
        entity2.setCreadoPor(usuarioId);

        Evidencia evidencia2 = Evidencia.builder()
                .id(2L)
                .creadoPor(usuarioId)
                .build();

        List<EvidenciaEntity> entities = Arrays.asList(evidenciaEntity, entity2);

        when(jpaRepository.findByCreadoPor(usuarioId)).thenReturn(entities);
        when(mapper.toDomain(evidenciaEntity)).thenReturn(evidenciaDominio);
        when(mapper.toDomain(entity2)).thenReturn(evidencia2);

        // Act
        List<Evidencia> resultado = evidenciaRepository.obtenerEvidenciasPorUsuario(usuarioId);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(e -> e.getCreadoPor().equals(usuarioId)));

        verify(jpaRepository).findByCreadoPor(usuarioId);
        verify(mapper, times(2)).toDomain(any(EvidenciaEntity.class));
    }

    @Test
    void testObtenerEvidenciasPorUsuario_SinResultados() {
        // Arrange
        Long usuarioId = 999L;
        when(jpaRepository.findByCreadoPor(usuarioId)).thenReturn(Collections.emptyList());

        // Act
        List<Evidencia> resultado = evidenciaRepository.obtenerEvidenciasPorUsuario(usuarioId);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(jpaRepository).findByCreadoPor(usuarioId);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void testObtenerEvidenciasPorOrden_VerificaOrdenamiento() {
        // Arrange
        Long ordenTrabajoId = 100L;

        EvidenciaEntity entity1 = new EvidenciaEntity();
        entity1.setId(1L);
        entity1.setFechaCreacion(LocalDateTime.now().minusHours(2));

        EvidenciaEntity entity2 = new EvidenciaEntity();
        entity2.setId(2L);
        entity2.setFechaCreacion(LocalDateTime.now().minusHours(1));

        List<EvidenciaEntity> entitiesOrdenadas = Arrays.asList(entity1, entity2);

        Evidencia evid1 = Evidencia.builder().id(1L).build();
        Evidencia evid2 = Evidencia.builder().id(2L).build();

        when(jpaRepository.findEvidenciasPorOrdenOrdenadas(ordenTrabajoId))
                .thenReturn(entitiesOrdenadas);
        when(mapper.toDomain(entity1)).thenReturn(evid1);
        when(mapper.toDomain(entity2)).thenReturn(evid2);

        // Act
        List<Evidencia> resultado = evidenciaRepository.obtenerEvidenciasPorOrden(ordenTrabajoId);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(2L, resultado.get(1).getId());

        verify(jpaRepository).findEvidenciasPorOrdenOrdenadas(ordenTrabajoId);
    }

    @Test
    void testGuardar_ActualizacionEvidenciaExistente() {
        // Arrange
        Evidencia evidenciaActualizada = Evidencia.builder()
                .id(1L)
                .ordenTrabajoId(100L)
                .tipo(TipoEvidencia.COMENTARIO)
                .contenido("Descripción actualizada")
                .build();

        EvidenciaEntity entityActualizada = new EvidenciaEntity();
        entityActualizada.setId(1L);
        entityActualizada.setContenido("Descripción actualizada");

        when(mapper.toEntity(evidenciaActualizada)).thenReturn(entityActualizada);
        when(jpaRepository.save(entityActualizada)).thenReturn(entityActualizada);
        when(mapper.toDomain(entityActualizada)).thenReturn(evidenciaActualizada);

        // Act
        Evidencia resultado = evidenciaRepository.guardar(evidenciaActualizada);

        // Assert
        assertNotNull(resultado);
        assertEquals("Descripción actualizada", resultado.getContenido());
        verify(jpaRepository).save(entityActualizada);
    }

    @Test
    void testObtenerEvidenciasPorTipo_TodosLosTipos() {
        // Arrange
        Long ordenTrabajoId = 100L;

        for (TipoEvidencia tipo : TipoEvidencia.values()) {
            when(jpaRepository.findByOrdenTrabajoIdAndTipo(ordenTrabajoId, tipo))
                    .thenReturn(Collections.emptyList());

            // Act
            List<Evidencia> resultado = evidenciaRepository.obtenerEvidenciasPorTipo(ordenTrabajoId, tipo);

            // Assert
            assertNotNull(resultado);
        }

        // Verificar que se llamó para cada tipo
        verify(jpaRepository, times(TipoEvidencia.values().length))
                .findByOrdenTrabajoIdAndTipo(eq(ordenTrabajoId), any(TipoEvidencia.class));
    }
}
