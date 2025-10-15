package com.telconova.supportsuite.aplicacion.servicios;

import com.telconova.supportsuite.aplicacion.dto.request.AgregarMaterialRequest;
import com.telconova.supportsuite.aplicacion.dto.response.MaterialResponse;
import com.telconova.supportsuite.aplicacion.dto.response.MaterialUtilizadoResponse;
import com.telconova.supportsuite.aplicacion.puertos.salida.IMaterialRepository;
import com.telconova.supportsuite.aplicacion.puertos.salida.IOrdenTrabajoRepository;
import com.telconova.supportsuite.aplicacion.puertos.salida.IUsuarioRepository;
import com.telconova.supportsuite.dominio.entidades.*;
import com.telconova.supportsuite.dominio.enums.*;
import com.telconova.supportsuite.dominio.excepciones.*;
import com.telconova.supportsuite.dominio.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para MaterialService")
class MaterialServiceTest {

    @Mock
    private IMaterialRepository materialRepository;

    @Mock
    private IOrdenTrabajoRepository ordenTrabajoRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @InjectMocks
    private MaterialService materialService;

    private Material material;
    private OrdenTrabajo ordenEnProceso;
    private Usuario tecnico;
    private MaterialUtilizado materialUtilizado;

    @BeforeEach
    void setUp() {
        // Arrange - Configuración común
        material = Material.builder()
                .id(1L)
                .codigo("CAB-UTP-001")
                .nombre("Cable UTP Cat 6")
                .descripcion("Cable UTP Categoría 6 para redes")
                .unidadMedida("metros")
                .precioUnitario(new BigDecimal("2.50"))
                .stockDisponible(100)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        tecnico = Usuario.builder()
                .id(1L)
                .email(Email.de("tecnico@telconova.com"))
                .nombreCompleto("Juan Técnico")
                .rol(RolUsuario.TECNICO)
                .activo(true)
                .build();

        ordenEnProceso = OrdenTrabajo.builder()
                .id(1L)
                .numeroOrden(NumeroOrden.de("ORD-2025-001"))
                .titulo("Instalación Internet")
                .estado(EstadoOrden.EN_PROCESO)
                .tecnicoAsignadoId(1L)
                .fechaCreacion(LocalDateTime.now())
                .build();

        materialUtilizado = MaterialUtilizado.crear(
                1L, 1L, 50, new BigDecimal("2.50"), 1L,
                "CAB-UTP-001", "Cable UTP Cat 6", "metros"
        );
    }

    @Test
    @DisplayName("Debe buscar materiales por nombre exitosamente")
    void debeBuscarMaterialesPorNombre() {
        // Arrange
        List<Material> materiales = Arrays.asList(material);
        when(materialRepository.buscarPorNombre("Cable", 10)).thenReturn(materiales);

        // Act
        List<MaterialResponse> responses = materialService.buscarMaterialesPorNombre("Cable", 10);

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getNombre()).isEqualTo("Cable UTP Cat 6");
        assertThat(responses.get(0).getCodigo()).isEqualTo("CAB-UTP-001");

        verify(materialRepository, times(1)).buscarPorNombre("Cable", 10);
    }

    @Test
    @DisplayName("Debe retornar lista vacía con búsqueda menor a 2 caracteres")
    void debeRetornarListaVaciaConBusquedaCorta() {
        // Arrange
        String busqueda = "C";

        // Act
        List<MaterialResponse> responses = materialService.buscarMaterialesPorNombre(busqueda, 10);

        // Assert
        assertThat(responses).isEmpty();
        verify(materialRepository, never()).buscarPorNombre(anyString(), anyInt());
    }

    @Test
    @DisplayName("Debe agregar material a orden exitosamente")
    void debeAgregarMaterialAOrdenExitosamente() {
        // Arrange
        AgregarMaterialRequest request = AgregarMaterialRequest.builder()
                .materialId(1L)
                .cantidad(50)
                .build();

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenEnProceso));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(materialRepository.buscarPorId(1L)).thenReturn(Optional.of(material));
        when(materialRepository.buscarMaterialUtilizado(1L, 1L)).thenReturn(Optional.empty());
        when(materialRepository.guardar(any(Material.class))).thenReturn(material);
        when(materialRepository.guardarMaterialUtilizado(any(MaterialUtilizado.class))).thenReturn(materialUtilizado);
        when(ordenTrabajoRepository.guardar(any(OrdenTrabajo.class))).thenReturn(ordenEnProceso);

        // Act
        materialService.agregarMaterialAOrden(1L, request, "tecnico@telconova.com");

        // Assert
        verify(materialRepository, times(1)).guardar(any(Material.class));
        verify(materialRepository, times(1)).guardarMaterialUtilizado(any(MaterialUtilizado.class));
        verify(ordenTrabajoRepository, times(1)).guardar(ordenEnProceso);
    }

    @Test
    @DisplayName("Debe lanzar excepción al agregar material a orden cancelada")
    void debeLanzarExcepcionAlAgregarMaterialAOrdenCancelada() {
        // Arrange
        OrdenTrabajo ordenCancelada = OrdenTrabajo.builder()
                .id(1L)
                .numeroOrden(NumeroOrden.de("ORD-2025-001"))
                .estado(EstadoOrden.CANCELADA)
                .tecnicoAsignadoId(1L)
                .build();

        AgregarMaterialRequest request = AgregarMaterialRequest.builder()
                .materialId(1L)
                .cantidad(50)
                .build();

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenCancelada));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));

        // Act & Assert
        assertThatThrownBy(() ->
                materialService.agregarMaterialAOrden(1L, request, "tecnico@telconova.com")
        )
                .isInstanceOf(EstadoOrdenInvalidoExcepcion.class)
                .hasMessageContaining("cancelada");

        verify(materialRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al agregar material con stock insuficiente")
    void debeLanzarExcepcionConStockInsuficiente() {
        // Arrange
        AgregarMaterialRequest request = AgregarMaterialRequest.builder()
                .materialId(1L)
                .cantidad(150) // Más del stock disponible (100)
                .build();

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenEnProceso));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(materialRepository.buscarPorId(1L)).thenReturn(Optional.of(material));

        // Act & Assert
        assertThatThrownBy(() ->
                materialService.agregarMaterialAOrden(1L, request, "tecnico@telconova.com")
        )
                .isInstanceOf(MaterialNoValidoExcepcion.class)
                .hasMessageContaining("insuficiente");

        verify(materialRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al agregar material inactivo")
    void debeLanzarExcepcionAlAgregarMaterialInactivo() {
        // Arrange
        Material materialInactivo = Material.builder()
                .id(1L)
                .codigo("CAB-UTP-001")
                .nombre("Cable UTP Cat 6")
                .activo(false)
                .stockDisponible(100)
                .build();

        AgregarMaterialRequest request = AgregarMaterialRequest.builder()
                .materialId(1L)
                .cantidad(50)
                .build();

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenEnProceso));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(materialRepository.buscarPorId(1L)).thenReturn(Optional.of(materialInactivo));

        // Act & Assert
        assertThatThrownBy(() ->
                materialService.agregarMaterialAOrden(1L, request, "tecnico@telconova.com")
        )
                .isInstanceOf(MaterialNoValidoExcepcion.class)
                .hasMessageContaining("inactivo");
    }

    @Test
    @DisplayName("Debe actualizar cantidad cuando el material ya existe en la orden")
    void debeActualizarCantidadCuandoMaterialYaExiste() {
        // Arrange
        AgregarMaterialRequest request = AgregarMaterialRequest.builder()
                .materialId(1L)
                .cantidad(20)
                .build();

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenEnProceso));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(materialRepository.buscarPorId(1L)).thenReturn(Optional.of(material));
        when(materialRepository.buscarMaterialUtilizado(1L, 1L)).thenReturn(Optional.of(materialUtilizado));
        when(materialRepository.guardar(any(Material.class))).thenReturn(material);
        when(materialRepository.guardarMaterialUtilizado(any(MaterialUtilizado.class))).thenReturn(materialUtilizado);
        when(ordenTrabajoRepository.guardar(any(OrdenTrabajo.class))).thenReturn(ordenEnProceso);

        // Act
        materialService.agregarMaterialAOrden(1L, request, "tecnico@telconova.com");

        // Assert
        verify(materialRepository, times(1)).guardarMaterialUtilizado(any(MaterialUtilizado.class));
        verify(materialRepository, times(1)).guardar(material);
    }

    @Test
    @DisplayName("Debe obtener materiales activos correctamente")
    void debeObtenerMaterialesActivos() {
        // Arrange
        List<Material> materiales = Arrays.asList(material);
        when(materialRepository.obtenerMaterialesActivos()).thenReturn(materiales);

        // Act
        List<MaterialResponse> responses = materialService.obtenerMaterialesActivos();

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).isActivo()).isTrue();

        verify(materialRepository, times(1)).obtenerMaterialesActivos();
    }

    @Test
    @DisplayName("Debe obtener material por ID correctamente")
    void debeObtenerMaterialPorId() {
        // Arrange
        when(materialRepository.buscarPorId(1L)).thenReturn(Optional.of(material));

        // Act
        MaterialResponse response = materialService.obtenerMaterialPorId(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCodigo()).isEqualTo("CAB-UTP-001");

        verify(materialRepository, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al buscar material inexistente")
    void debeLanzarExcepcionAlBuscarMaterialInexistente() {
        // Arrange
        when(materialRepository.buscarPorId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> materialService.obtenerMaterialPorId(999L))
                .isInstanceOf(MaterialNoValidoExcepcion.class);
    }

    @Test
    @DisplayName("Debe verificar disponibilidad de stock correctamente")
    void debeVerificarDisponibilidadStock() {
        // Arrange
        when(materialRepository.buscarPorId(1L)).thenReturn(Optional.of(material));

        // Act
        boolean disponible = materialService.verificarDisponibilidadStock(1L, 50);

        // Assert
        assertThat(disponible).isTrue();
        verify(materialRepository, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("Debe retornar false cuando stock es insuficiente")
    void debeRetornarFalseCuandoStockInsuficiente() {
        // Arrange
        when(materialRepository.buscarPorId(1L)).thenReturn(Optional.of(material));

        // Act
        boolean disponible = materialService.verificarDisponibilidadStock(1L, 150);

        // Assert
        assertThat(disponible).isFalse();
    }

    @Test
    @DisplayName("Debe devolver materiales de orden cancelada exitosamente")
    void debeDevolverMaterialesDeOrdenCancelada() {
        // Arrange
        List<MaterialUtilizado> materiales = Arrays.asList(materialUtilizado);
        when(materialRepository.obtenerMaterialesUtilizadosPorOrden(1L)).thenReturn(materiales);
        when(materialRepository.buscarPorId(1L)).thenReturn(Optional.of(material));
        when(materialRepository.guardar(any(Material.class))).thenReturn(material);

        // Act
        materialService.devolverMaterialesDeOrden(1L);

        // Assert
        verify(materialRepository, times(1)).obtenerMaterialesUtilizadosPorOrden(1L);
        verify(materialRepository, times(1)).guardar(material);
        verify(materialRepository, times(1)).eliminarMaterialesUtilizadosPorOrden(1L);
    }

    @Test
    @DisplayName("Debe eliminar material de orden en proceso exitosamente")
    void debeEliminarMaterialDeOrdenEnProceso() {
        // Arrange
        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenEnProceso));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(materialRepository.buscarMaterialUtilizadoPorId(1L)).thenReturn(Optional.of(materialUtilizado));
        when(materialRepository.buscarPorId(1L)).thenReturn(Optional.of(material));
        when(materialRepository.guardar(any(Material.class))).thenReturn(material);
        when(ordenTrabajoRepository.guardar(any(OrdenTrabajo.class))).thenReturn(ordenEnProceso);

        // Act
        materialService.eliminarMaterialDeOrden(1L, 1L, "tecnico@telconova.com");

        // Assert
        verify(materialRepository, times(1)).eliminarMaterialUtilizado(1L);
        verify(materialRepository, times(1)).guardar(material);
        verify(ordenTrabajoRepository, times(1)).guardar(ordenEnProceso);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar material de orden no en proceso")
    void debeLanzarExcepcionAlEliminarMaterialDeOrdenNoEnProceso() {
        // Arrange
        OrdenTrabajo ordenAsignada = OrdenTrabajo.builder()
                .id(1L)
                .numeroOrden(NumeroOrden.de("ORD-2025-001"))
                .estado(EstadoOrden.ASIGNADA)
                .tecnicoAsignadoId(1L)
                .build();

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenAsignada));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));

        // Act & Assert
        assertThatThrownBy(() ->
                materialService.eliminarMaterialDeOrden(1L, 1L, "tecnico@telconova.com")
        )
                .isInstanceOf(EstadoOrdenInvalidoExcepcion.class)
                .hasMessageContaining("EN_PROCESO");

        verify(materialRepository, never()).eliminarMaterialUtilizado(anyLong());
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar material que no pertenece a la orden")
    void debeLanzarExcepcionAlEliminarMaterialQueNoPertenece() {
        // Arrange
        MaterialUtilizado materialOtraOrden = MaterialUtilizado.crear(
                2L, 1L, 50, new BigDecimal("2.50"), 1L,
                "CAB-UTP-001", "Cable UTP Cat 6", "metros"
        );

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenEnProceso));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(materialRepository.buscarMaterialUtilizadoPorId(1L)).thenReturn(Optional.of(materialOtraOrden));

        // Act & Assert
        assertThatThrownBy(() ->
                materialService.eliminarMaterialDeOrden(1L, 1L, "tecnico@telconova.com")
        )
                .isInstanceOf(AccesoNoAutorizadoExcepcion.class)
                .hasMessageContaining("no pertenece");

        verify(materialRepository, never()).eliminarMaterialUtilizado(anyLong());
    }

    @Test
    @DisplayName("Debe obtener materiales utilizados por orden correctamente")
    void debeObtenerMaterialesUtilizadosPorOrden() {
        // Arrange
        List<MaterialUtilizado> materiales = Arrays.asList(materialUtilizado);
        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenEnProceso));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(materialRepository.obtenerMaterialesUtilizadosPorOrden(1L)).thenReturn(materiales);
        when(usuarioRepository.buscarPorId(1L)).thenReturn(Optional.of(tecnico));

        // Act
        List<MaterialUtilizadoResponse> responses =
                materialService.obtenerMaterialesUtilizadosPorOrden(1L, "tecnico@telconova.com");

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCodigoMaterial()).isEqualTo("CAB-UTP-001");
        assertThat(responses.get(0).getCantidadUtilizada()).isEqualTo(50);

        verify(materialRepository, times(1)).obtenerMaterialesUtilizadosPorOrden(1L);
    }
}
