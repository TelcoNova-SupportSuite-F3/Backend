package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia;

import com.telconova.supportsuite.dominio.entidades.Material;
import com.telconova.supportsuite.dominio.entidades.MaterialUtilizado;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.MaterialEntity;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.MaterialUtilizadoEntity;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers.MaterialMapper;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers.MaterialUtilizadoMapper;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios.MaterialJpaRepository;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios.MaterialUtilizadoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias para MaterialRepositoryImpl")
class MaterialRepositoryImplTest {

    @Mock
    private MaterialJpaRepository materialJpaRepository;

    @Mock
    private MaterialUtilizadoJpaRepository materialUtilizadoJpaRepository;

    @Mock
    private MaterialMapper materialMapper;

    @Mock
    private MaterialUtilizadoMapper materialUtilizadoMapper;

    @InjectMocks
    private MaterialRepositoryImpl materialRepository;

    private Material materialDominio;
    private MaterialEntity materialEntity;
    private MaterialUtilizado materialUtilizadoDominio;
    private MaterialUtilizadoEntity materialUtilizadoEntity;

    @BeforeEach
    void setUp() {
        // Configurar material de dominio
        materialDominio = Material.builder()
                .id(1L)
                .codigo("MAT-001")
                .nombre("Cable UTP Cat6")
                .descripcion("Cable de red categoría 6")
                .unidadMedida("Metro")
                .precioUnitario(new BigDecimal("2.50"))
                .stockDisponible(100)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        // Configurar material entity
        materialEntity = new MaterialEntity();
        materialEntity.setId(1L);
        materialEntity.setCodigo("MAT-001");
        materialEntity.setNombre("Cable UTP Cat6");
        materialEntity.setDescripcion("Cable de red categoría 6");
        materialEntity.setUnidadMedida("Metro");
        materialEntity.setPrecioUnitario(new BigDecimal("2.50"));
        materialEntity.setStockDisponible(100);
        materialEntity.setActivo(true);
        materialEntity.setFechaCreacion(LocalDateTime.now());

        // Configurar material utilizado de dominio
        materialUtilizadoDominio = MaterialUtilizado.builder()
                .id(1L)
                .ordenTrabajoId(100L)
                .materialId(1L)
                .cantidadUtilizada(5)
                .precioUnitario(new BigDecimal("2.50"))
                .fechaRegistro(LocalDateTime.now())
                .registradoPor(1L)
                .codigoMaterial("MAT-001")
                .nombreMaterial("Cable UTP Cat6")
                .unidadMedida("Metro")
                .build();

        // Configurar material utilizado entity
        materialUtilizadoEntity = new MaterialUtilizadoEntity();
        materialUtilizadoEntity.setId(1L);
        materialUtilizadoEntity.setOrdenTrabajoId(100L);
        materialUtilizadoEntity.setMaterialId(1L);
        materialUtilizadoEntity.setCantidadUtilizada(5);
        materialUtilizadoEntity.setPrecioUnitario(new BigDecimal("2.50"));
        materialUtilizadoEntity.setFechaRegistro(LocalDateTime.now());
        materialUtilizadoEntity.setRegistradoPor(1L);
        materialUtilizadoEntity.setCodigoMaterial("MAT-001");
        materialUtilizadoEntity.setNombreMaterial("Cable UTP Cat6");
        materialUtilizadoEntity.setUnidadMedida("Metro");
    }

    @Test
    void testBuscarPorId_Encontrado() {
        // Arrange
        Long materialId = 1L;
        when(materialJpaRepository.findById(materialId)).thenReturn(Optional.of(materialEntity));
        when(materialMapper.toDomain(materialEntity)).thenReturn(materialDominio);

        // Act
        Optional<Material> resultado = materialRepository.buscarPorId(materialId);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(materialDominio.getId(), resultado.get().getId());
        assertEquals(materialDominio.getCodigo(), resultado.get().getCodigo());
        assertEquals(materialDominio.getNombre(), resultado.get().getNombre());

        verify(materialJpaRepository).findById(materialId);
        verify(materialMapper).toDomain(materialEntity);
    }

    @Test
    void testBuscarPorId_NoEncontrado() {
        // Arrange
        Long materialId = 999L;
        when(materialJpaRepository.findById(materialId)).thenReturn(Optional.empty());

        // Act
        Optional<Material> resultado = materialRepository.buscarPorId(materialId);

        // Assert
        assertFalse(resultado.isPresent());
        verify(materialJpaRepository).findById(materialId);
        verify(materialMapper, never()).toDomain(any());
    }

    @Test
    void testBuscarPorCodigo_Encontrado() {
        // Arrange
        String codigo = "MAT-001";
        when(materialJpaRepository.findByCodigo(codigo)).thenReturn(Optional.of(materialEntity));
        when(materialMapper.toDomain(materialEntity)).thenReturn(materialDominio);

        // Act
        Optional<Material> resultado = materialRepository.buscarPorCodigo(codigo);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(codigo, resultado.get().getCodigo());

        verify(materialJpaRepository).findByCodigo(codigo);
        verify(materialMapper).toDomain(materialEntity);
    }

    @Test
    void testBuscarPorCodigo_NoEncontrado() {
        // Arrange
        String codigo = "MAT-999";
        when(materialJpaRepository.findByCodigo(codigo)).thenReturn(Optional.empty());

        // Act
        Optional<Material> resultado = materialRepository.buscarPorCodigo(codigo);

        // Assert
        assertFalse(resultado.isPresent());
        verify(materialJpaRepository).findByCodigo(codigo);
        verify(materialMapper, never()).toDomain(any());
    }

    @Test
    void testBuscarPorNombre_ConResultados() {
        // Arrange
        String nombreBusqueda = "Cable";
        int limite = 10;

        MaterialEntity entity2 = new MaterialEntity();
        entity2.setId(2L);
        entity2.setNombre("Cable Fibra Óptica");

        Material material2 = Material.builder()
                .id(2L)
                .nombre("Cable Fibra Óptica")
                .activo(true)
                .build();

        List<MaterialEntity> entities = Arrays.asList(materialEntity, entity2);

        when(materialJpaRepository.buscarPorNombreConLimite(nombreBusqueda, PageRequest.of(0, limite)))
                .thenReturn(entities);
        when(materialMapper.toDomain(materialEntity)).thenReturn(materialDominio);
        when(materialMapper.toDomain(entity2)).thenReturn(material2);

        // Act
        List<Material> resultado = materialRepository.buscarPorNombre(nombreBusqueda, limite);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(m -> m.getNombre().contains("Cable")));

        verify(materialJpaRepository).buscarPorNombreConLimite(nombreBusqueda, PageRequest.of(0, limite));
        verify(materialMapper, times(2)).toDomain(any(MaterialEntity.class));
    }

    @Test
    void testBuscarPorNombre_SinResultados() {
        // Arrange
        String nombreBusqueda = "NoExiste";
        int limite = 10;

        when(materialJpaRepository.buscarPorNombreConLimite(nombreBusqueda, PageRequest.of(0, limite)))
                .thenReturn(Collections.emptyList());

        // Act
        List<Material> resultado = materialRepository.buscarPorNombre(nombreBusqueda, limite);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(materialMapper, never()).toDomain(any());
    }

    @Test
    void testBuscarPorNombre_ConLimitePersonalizado() {
        // Arrange
        String nombreBusqueda = "Material";
        int limite = 5;

        when(materialJpaRepository.buscarPorNombreConLimite(nombreBusqueda, PageRequest.of(0, limite)))
                .thenReturn(Collections.emptyList());

        // Act
        materialRepository.buscarPorNombre(nombreBusqueda, limite);

        // Assert
        verify(materialJpaRepository).buscarPorNombreConLimite(nombreBusqueda,PageRequest.of(0, limite));
    }

    @Test
    void testGuardar_MaterialNuevo() {
        // Arrange
        Material materialNuevo = Material.builder()
                .codigo("MAT-002")
                .nombre("Router WiFi")
                .activo(true)
                .build();

        MaterialEntity entityNueva = new MaterialEntity();
        entityNueva.setCodigo("MAT-002");

        MaterialEntity entityGuardada = new MaterialEntity();
        entityGuardada.setId(2L);
        entityGuardada.setCodigo("MAT-002");

        Material materialGuardado = Material.builder()
                .id(2L)
                .codigo("MAT-002")
                .activo(true)
                .build();

        when(materialMapper.toEntity(materialNuevo)).thenReturn(entityNueva);
        when(materialJpaRepository.save(entityNueva)).thenReturn(entityGuardada);
        when(materialMapper.toDomain(entityGuardada)).thenReturn(materialGuardado);

        // Act
        Material resultado = materialRepository.guardar(materialNuevo);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertEquals("MAT-002", resultado.getCodigo());

        verify(materialMapper).toEntity(materialNuevo);
        verify(materialJpaRepository).save(entityNueva);
        verify(materialMapper).toDomain(entityGuardada);
    }

    @Test
    void testGuardar_ActualizarMaterialExistente() {
        // Arrange
        when(materialMapper.toEntity(materialDominio)).thenReturn(materialEntity);
        when(materialJpaRepository.save(materialEntity)).thenReturn(materialEntity);
        when(materialMapper.toDomain(materialEntity)).thenReturn(materialDominio);

        // Act
        Material resultado = materialRepository.guardar(materialDominio);

        // Assert
        assertNotNull(resultado);
        assertEquals(materialDominio.getId(), resultado.getId());
        verify(materialJpaRepository).save(materialEntity);
    }

    @Test
    void testObtenerMaterialesActivos_ConResultados() {
        // Arrange
        MaterialEntity entity2 = new MaterialEntity();
        entity2.setId(2L);
        entity2.setActivo(true);

        Material material2 = Material.builder()
                .id(2L)
                .activo(true)
                .build();

        List<MaterialEntity> entities = Arrays.asList(materialEntity, entity2);

        when(materialJpaRepository.findByActivoTrue()).thenReturn(entities);
        when(materialMapper.toDomain(materialEntity)).thenReturn(materialDominio);
        when(materialMapper.toDomain(entity2)).thenReturn(material2);

        // Act
        List<Material> resultado = materialRepository.obtenerMaterialesActivos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Material::isActivo));

        verify(materialJpaRepository).findByActivoTrue();
        verify(materialMapper, times(2)).toDomain(any(MaterialEntity.class));
    }

    @Test
    void testObtenerMaterialesActivos_SinResultados() {
        // Arrange
        when(materialJpaRepository.findByActivoTrue()).thenReturn(Collections.emptyList());

        // Act
        List<Material> resultado = materialRepository.obtenerMaterialesActivos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(materialMapper, never()).toDomain(any());
    }

    @Test
    void testGuardarMaterialUtilizado_Exitoso() {
        // Arrange
        when(materialUtilizadoMapper.toEntity(materialUtilizadoDominio)).thenReturn(materialUtilizadoEntity);
        when(materialUtilizadoJpaRepository.save(materialUtilizadoEntity)).thenReturn(materialUtilizadoEntity);
        when(materialUtilizadoMapper.toDomain(materialUtilizadoEntity)).thenReturn(materialUtilizadoDominio);

        // Act
        MaterialUtilizado resultado = materialRepository.guardarMaterialUtilizado(materialUtilizadoDominio);

        // Assert
        assertNotNull(resultado);
        assertEquals(materialUtilizadoDominio.getId(), resultado.getId());
        assertEquals(materialUtilizadoDominio.getOrdenTrabajoId(), resultado.getOrdenTrabajoId());
        assertEquals(materialUtilizadoDominio.getMaterialId(), resultado.getMaterialId());

        verify(materialUtilizadoMapper).toEntity(materialUtilizadoDominio);
        verify(materialUtilizadoJpaRepository).save(materialUtilizadoEntity);
        verify(materialUtilizadoMapper).toDomain(materialUtilizadoEntity);
    }

    @Test
    void testObtenerMaterialesUtilizadosPorOrden_ConResultados() {
        // Arrange
        Long ordenTrabajoId = 100L;

        MaterialUtilizadoEntity entity2 = new MaterialUtilizadoEntity();
        entity2.setId(2L);
        entity2.setOrdenTrabajoId(ordenTrabajoId);

        MaterialUtilizado materialUtilizado2 = MaterialUtilizado.builder()
                .id(2L)
                .ordenTrabajoId(ordenTrabajoId)
                .build();

        List<MaterialUtilizadoEntity> entities = Arrays.asList(materialUtilizadoEntity, entity2);

        when(materialUtilizadoJpaRepository.findMaterialesUtilizadosPorOrden(ordenTrabajoId))
                .thenReturn(entities);
        when(materialUtilizadoMapper.toDomain(materialUtilizadoEntity)).thenReturn(materialUtilizadoDominio);
        when(materialUtilizadoMapper.toDomain(entity2)).thenReturn(materialUtilizado2);

        // Act
        List<MaterialUtilizado> resultado = materialRepository.obtenerMaterialesUtilizadosPorOrden(ordenTrabajoId);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(m -> m.getOrdenTrabajoId().equals(ordenTrabajoId)));

        verify(materialUtilizadoJpaRepository).findMaterialesUtilizadosPorOrden(ordenTrabajoId);
        verify(materialUtilizadoMapper, times(2)).toDomain(any(MaterialUtilizadoEntity.class));
    }

    @Test
    void testObtenerMaterialesUtilizadosPorOrden_SinResultados() {
        // Arrange
        Long ordenTrabajoId = 999L;
        when(materialUtilizadoJpaRepository.findMaterialesUtilizadosPorOrden(ordenTrabajoId))
                .thenReturn(Collections.emptyList());

        // Act
        List<MaterialUtilizado> resultado = materialRepository.obtenerMaterialesUtilizadosPorOrden(ordenTrabajoId);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(materialUtilizadoMapper, never()).toDomain(any());
    }

    @Test
    void testBuscarMaterialUtilizado_Encontrado() {
        // Arrange
        Long ordenTrabajoId = 100L;
        Long materialId = 1L;

        when(materialUtilizadoJpaRepository.findByOrdenTrabajoIdAndMaterialId(ordenTrabajoId, materialId))
                .thenReturn(Optional.of(materialUtilizadoEntity));
        when(materialUtilizadoMapper.toDomain(materialUtilizadoEntity)).thenReturn(materialUtilizadoDominio);

        // Act
        Optional<MaterialUtilizado> resultado = materialRepository.buscarMaterialUtilizado(ordenTrabajoId, materialId);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(ordenTrabajoId, resultado.get().getOrdenTrabajoId());
        assertEquals(materialId, resultado.get().getMaterialId());

        verify(materialUtilizadoJpaRepository).findByOrdenTrabajoIdAndMaterialId(ordenTrabajoId, materialId);
        verify(materialUtilizadoMapper).toDomain(materialUtilizadoEntity);
    }

    @Test
    void testBuscarMaterialUtilizado_NoEncontrado() {
        // Arrange
        Long ordenTrabajoId = 999L;
        Long materialId = 999L;

        when(materialUtilizadoJpaRepository.findByOrdenTrabajoIdAndMaterialId(ordenTrabajoId, materialId))
                .thenReturn(Optional.empty());

        // Act
        Optional<MaterialUtilizado> resultado = materialRepository.buscarMaterialUtilizado(ordenTrabajoId, materialId);

        // Assert
        assertFalse(resultado.isPresent());
        verify(materialUtilizadoMapper, never()).toDomain(any());
    }

    @Test
    void testExistePorCodigo_Existe() {
        // Arrange
        String codigo = "MAT-001";
        when(materialJpaRepository.existsByCodigo(codigo)).thenReturn(true);

        // Act
        boolean resultado = materialRepository.existePorCodigo(codigo);

        // Assert
        assertTrue(resultado);
        verify(materialJpaRepository).existsByCodigo(codigo);
    }

    @Test
    void testExistePorCodigo_NoExiste() {
        // Arrange
        String codigo = "MAT-999";
        when(materialJpaRepository.existsByCodigo(codigo)).thenReturn(false);

        // Act
        boolean resultado = materialRepository.existePorCodigo(codigo);

        // Assert
        assertFalse(resultado);
        verify(materialJpaRepository).existsByCodigo(codigo);
    }


    @Test
    void testObtenerMaterialesConStockBajo_SinResultados() {
        // Arrange
        int stockMinimo = 5;
        when(materialJpaRepository.findMaterialesConStockBajo(stockMinimo))
                .thenReturn(Collections.emptyList());

        // Act
        List<Material> resultado = materialRepository.obtenerMaterialesConStockBajo(stockMinimo);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(materialMapper, never()).toDomain(any());
    }

    @Test
    void testEliminarMaterialesUtilizadosPorOrden_ConMateriales() {
        // Arrange
        Long ordenTrabajoId = 100L;

        MaterialUtilizadoEntity entity2 = new MaterialUtilizadoEntity();
        entity2.setId(2L);

        List<MaterialUtilizadoEntity> entities = Arrays.asList(materialUtilizadoEntity, entity2);

        when(materialUtilizadoJpaRepository.findMaterialesUtilizadosPorOrden(ordenTrabajoId))
                .thenReturn(entities);
        doNothing().when(materialUtilizadoJpaRepository).deleteAll(entities);

        // Act
        assertDoesNotThrow(() -> materialRepository.eliminarMaterialesUtilizadosPorOrden(ordenTrabajoId));

        // Assert
        verify(materialUtilizadoJpaRepository).findMaterialesUtilizadosPorOrden(ordenTrabajoId);
        verify(materialUtilizadoJpaRepository).deleteAll(entities);
    }

    @Test
    void testEliminarMaterialesUtilizadosPorOrden_SinMateriales() {
        // Arrange
        Long ordenTrabajoId = 999L;

        when(materialUtilizadoJpaRepository.findMaterialesUtilizadosPorOrden(ordenTrabajoId))
                .thenReturn(Collections.emptyList());

        // Act
        assertDoesNotThrow(() -> materialRepository.eliminarMaterialesUtilizadosPorOrden(ordenTrabajoId));

        // Assert
        verify(materialUtilizadoJpaRepository).findMaterialesUtilizadosPorOrden(ordenTrabajoId);
        verify(materialUtilizadoJpaRepository, never()).deleteAll(anyList());
    }

    @Test
    void testBuscarMaterialUtilizadoPorId_Encontrado() {
        // Arrange
        Long materialUtilizadoId = 1L;

        when(materialUtilizadoJpaRepository.findById(materialUtilizadoId))
                .thenReturn(Optional.of(materialUtilizadoEntity));
        when(materialUtilizadoMapper.toDomain(materialUtilizadoEntity)).thenReturn(materialUtilizadoDominio);

        // Act
        Optional<MaterialUtilizado> resultado = materialRepository.buscarMaterialUtilizadoPorId(materialUtilizadoId);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(materialUtilizadoId, resultado.get().getId());

        verify(materialUtilizadoJpaRepository).findById(materialUtilizadoId);
        verify(materialUtilizadoMapper).toDomain(materialUtilizadoEntity);
    }

    @Test
    void testBuscarMaterialUtilizadoPorId_NoEncontrado() {
        // Arrange
        Long materialUtilizadoId = 999L;

        when(materialUtilizadoJpaRepository.findById(materialUtilizadoId))
                .thenReturn(Optional.empty());

        // Act
        Optional<MaterialUtilizado> resultado = materialRepository.buscarMaterialUtilizadoPorId(materialUtilizadoId);

        // Assert
        assertFalse(resultado.isPresent());
        verify(materialUtilizadoMapper, never()).toDomain(any());
    }

    @Test
    void testEliminarMaterialUtilizado_Exitoso() {
        // Arrange
        Long materialUtilizadoId = 1L;
        doNothing().when(materialUtilizadoJpaRepository).deleteById(materialUtilizadoId);

        // Act
        assertDoesNotThrow(() -> materialRepository.eliminarMaterialUtilizado(materialUtilizadoId));

        // Assert
        verify(materialUtilizadoJpaRepository).deleteById(materialUtilizadoId);
    }

    @Test
    void testGuardarMaterialUtilizado_ActualizarExistente() {
        // Arrange
        MaterialUtilizado materialActualizado = MaterialUtilizado.builder()
                .id(1L)
                .ordenTrabajoId(100L)
                .materialId(1L)
                .cantidadUtilizada(10)
                .build();

        MaterialUtilizadoEntity entityActualizada = new MaterialUtilizadoEntity();
        entityActualizada.setId(1L);
        entityActualizada.setCantidadUtilizada(10);

        when(materialUtilizadoMapper.toEntity(materialActualizado)).thenReturn(entityActualizada);
        when(materialUtilizadoJpaRepository.save(entityActualizada)).thenReturn(entityActualizada);
        when(materialUtilizadoMapper.toDomain(entityActualizada)).thenReturn(materialActualizado);

        // Act
        MaterialUtilizado resultado = materialRepository.guardarMaterialUtilizado(materialActualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals(10, resultado.getCantidadUtilizada());
        verify(materialUtilizadoJpaRepository).save(entityActualizada);
    }
}
