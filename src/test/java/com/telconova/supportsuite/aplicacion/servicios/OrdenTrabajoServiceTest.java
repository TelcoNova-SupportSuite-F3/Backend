package com.telconova.supportsuite.aplicacion.servicios;

import com.telconova.supportsuite.aplicacion.dto.request.ActualizarEstadoRequest;
import com.telconova.supportsuite.aplicacion.dto.response.OrdenTrabajoResponse;
import com.telconova.supportsuite.aplicacion.puertos.entrada.*;
import com.telconova.supportsuite.aplicacion.puertos.salida.*;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para OrdenTrabajoService")
class OrdenTrabajoServiceTest {

    @Mock
    private IOrdenTrabajoRepository ordenTrabajoRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private IEvidenciaRepository evidenciaRepository;


    @Mock
    private IMaterialService materialService;


    @Mock
    private INotificacionService notificacionService;

    @InjectMocks
    private OrdenTrabajoService ordenTrabajoService;

    private OrdenTrabajo orden;
    private Usuario tecnico;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        // Arrange - Configuración común
        tecnico = Usuario.builder()
                .id(1L)
                .email(Email.de("tecnico@telconova.com"))
                .nombreCompleto("Juan Técnico")
                .rol(RolUsuario.TECNICO)
                .activo(true)
                .build();

        admin = Usuario.builder()
                .id(2L)
                .email(Email.de("admin@telconova.com"))
                .nombreCompleto("Admin Sistema")
                .rol(RolUsuario.ADMIN)
                .activo(true)
                .build();

        orden = OrdenTrabajo.builder()
                .id(1L)
                .numeroOrden(NumeroOrden.de("ORD-2025-001"))
                .titulo("Instalación Internet")
                .descripcion("Instalación de servicio de internet fibra óptica")
                .estado(EstadoOrden.ASIGNADA)
                .prioridad(Prioridad.MEDIA)
                .tipoServicio(TipoServicio.INSTALACION)
                .clienteNombre("Cliente Test")
                .clienteTelefono(Telefono.de("+57 300 1234567"))
                .direccion("Calle 123 #45-67")
                .tecnicoAsignadoId(1L)
                .fechaCreacion(LocalDateTime.now())
                .fechaAsignacion(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Debe obtener órdenes por técnico exitosamente")
    void debeObtenerOrdenesPorTecnico() {
        // Arrange
        List<OrdenTrabajo> ordenes = Arrays.asList(orden);
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(ordenTrabajoRepository.obtenerOrdenesPorTecnico(1L)).thenReturn(ordenes);

        // Act
        List<OrdenTrabajoResponse> responses =
                ordenTrabajoService.obtenerOrdenesPorTecnico("tecnico@telconova.com");

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getNumeroOrden()).isEqualTo("ORD-2025-001");

        verify(ordenTrabajoRepository, times(1)).obtenerOrdenesPorTecnico(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando usuario no es técnico")
    void debeLanzarExcepcionCuandoUsuarioNoEsTecnico() {
        // Arrange
        when(usuarioRepository.buscarPorEmail("admin@telconova.com")).thenReturn(Optional.of(admin));

        // Act & Assert
        assertThatThrownBy(() ->
                ordenTrabajoService.obtenerOrdenesPorTecnico("admin@telconova.com")
        )
                .isInstanceOf(AccesoNoAutorizadoExcepcion.class);

        verify(ordenTrabajoRepository, never()).obtenerOrdenesPorTecnico(anyLong());
    }

    @Test
    @DisplayName("Debe obtener todas las órdenes como admin")
    void debeObtenerTodasLasOrdenesComoAdmin() {
        // Arrange
        List<OrdenTrabajo> ordenes = Arrays.asList(orden);
        when(ordenTrabajoRepository.obtenerTodasLasOrdenes()).thenReturn(ordenes);

        // Act
        List<OrdenTrabajoResponse> responses = ordenTrabajoService.obtenerTodasLasOrdenes();

        // Assert
        assertThat(responses).hasSize(1);
        verify(ordenTrabajoRepository, times(1)).obtenerTodasLasOrdenes();
    }

    @Test
    @DisplayName("Debe obtener orden por ID exitosamente")
    void debeObtenerOrdenPorId() {
        // Arrange
        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));

        // Act
        OrdenTrabajoResponse response =
                ordenTrabajoService.obtenerOrdenPorId(1L, "tecnico@telconova.com");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNumeroOrden()).isEqualTo("ORD-2025-001");

        verify(ordenTrabajoRepository, times(4)).buscarPorId(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al obtener orden inexistente")
    void debeLanzarExcepcionAlObtenerOrdenInexistente() {
        // Arrange
        when(ordenTrabajoRepository.buscarPorId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                ordenTrabajoService.obtenerOrdenPorId(999L, "tecnico@telconova.com")
        )
                .isInstanceOf(OrdenNoEncontradaExcepcion.class);
    }

    @Test
    @DisplayName("Debe actualizar estado de ASIGNADA a EN_PROCESO")
    void debeActualizarEstadoDeAsignadaAEnProceso() {
        // Arrange
        ActualizarEstadoRequest request = new ActualizarEstadoRequest();
        request.setNuevoEstado(EstadoOrden.EN_PROCESO);

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(ordenTrabajoRepository.guardar(any(OrdenTrabajo.class))).thenReturn(orden);
        doNothing().when(notificacionService).notificarCambioEstadoASupervisor(any());
        doNothing().when(notificacionService).notificarCambioEstadoACliente(any());

        // Act
        OrdenTrabajoResponse response =
                ordenTrabajoService.actualizarEstadoOrden(1L, request, "tecnico@telconova.com");

        // Assert
        assertThat(response).isNotNull();
        verify(ordenTrabajoRepository, times(1)).guardar(any(OrdenTrabajo.class));
        verify(notificacionService, times(1)).notificarCambioEstadoASupervisor(any());
        verify(notificacionService, times(1)).notificarCambioEstadoACliente(any());
    }

    @Test
    @DisplayName("Debe pausar orden en proceso")
    void debePausarOrdenEnProceso() {
        // Arrange
        orden.setEstado(EstadoOrden.EN_PROCESO);
        ActualizarEstadoRequest request = new ActualizarEstadoRequest();
        request.setNuevoEstado(EstadoOrden.PAUSADA);

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(ordenTrabajoRepository.guardar(any(OrdenTrabajo.class))).thenReturn(orden);
        doNothing().when(notificacionService).notificarCambioEstadoASupervisor(any());
        doNothing().when(notificacionService).notificarCambioEstadoACliente(any());

        // Act
        OrdenTrabajoResponse response =
                ordenTrabajoService.actualizarEstadoOrden(1L, request, "tecnico@telconova.com");

        // Assert
        assertThat(response).isNotNull();
        verify(ordenTrabajoRepository, times(1)).guardar(any(OrdenTrabajo.class));
    }

    @Test
    @DisplayName("Debe reanudar orden pausada a en proceso")
    void debeReanudarOrdenPausada() {
        // Arrange
        orden.setEstado(EstadoOrden.PAUSADA);
        ActualizarEstadoRequest request = new ActualizarEstadoRequest();
        request.setNuevoEstado(EstadoOrden.EN_PROCESO);

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(ordenTrabajoRepository.guardar(any(OrdenTrabajo.class))).thenReturn(orden);
        doNothing().when(notificacionService).notificarCambioEstadoASupervisor(any());
        doNothing().when(notificacionService).notificarCambioEstadoACliente(any());

        // Act
        OrdenTrabajoResponse response =
                ordenTrabajoService.actualizarEstadoOrden(1L, request, "tecnico@telconova.com");

        // Assert
        assertThat(response).isNotNull();
        verify(ordenTrabajoRepository, times(1)).guardar(any(OrdenTrabajo.class));
    }

    @Test
    @DisplayName("Debe cancelar orden y devolver materiales")
    void debeCancelarOrdenYDevolverMateriales() {
        // Arrange
        orden.setEstado(EstadoOrden.EN_PROCESO);
        ActualizarEstadoRequest request = new ActualizarEstadoRequest();
        request.setNuevoEstado(EstadoOrden.CANCELADA);

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(ordenTrabajoRepository.guardar(any(OrdenTrabajo.class))).thenReturn(orden);
        doNothing().when(materialService).devolverMaterialesDeOrden(1L);
        doNothing().when(notificacionService).notificarCambioEstadoASupervisor(any());
        doNothing().when(notificacionService).notificarCambioEstadoACliente(any());

        // Act
        OrdenTrabajoResponse response =
                ordenTrabajoService.actualizarEstadoOrden(1L, request, "tecnico@telconova.com");

        // Assert
        assertThat(response).isNotNull();
        verify(materialService, times(1)).devolverMaterialesDeOrden(1L);
        verify(ordenTrabajoRepository, times(1)).guardar(any(OrdenTrabajo.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar modificar orden cancelada")
    void debeLanzarExcepcionAlModificarOrdenCancelada() {
        // Arrange
        orden.setEstado(EstadoOrden.CANCELADA);
        ActualizarEstadoRequest request = new ActualizarEstadoRequest();
        request.setNuevoEstado(EstadoOrden.EN_PROCESO);

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));

        // Act & Assert
        assertThatThrownBy(() ->
                ordenTrabajoService.actualizarEstadoOrden(1L, request, "tecnico@telconova.com")
        )
                .isInstanceOf(EstadoOrdenInvalidoExcepcion.class)
                .hasMessageContaining("cancelada");

        verify(ordenTrabajoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar cancelar orden finalizada")
    void debeLanzarExcepcionAlCancelarOrdenFinalizada() {
        // Arrange
        orden.setEstado(EstadoOrden.FINALIZADA);
        ActualizarEstadoRequest request = new ActualizarEstadoRequest();
        request.setNuevoEstado(EstadoOrden.CANCELADA);

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));

        // Act & Assert
        assertThatThrownBy(() ->
                ordenTrabajoService.actualizarEstadoOrden(1L, request, "tecnico@telconova.com")
        )
                .isInstanceOf(EstadoOrdenInvalidoExcepcion.class)
                .hasMessageContaining("finalizada");
    }

    @Test
    @DisplayName("Debe finalizar orden con evidencias y fechas")
    void debeFinalizarOrdenConEvidenciasYFechas() {
        // Arrange
        orden.setEstado(EstadoOrden.EN_PROCESO);
        ActualizarEstadoRequest request = new ActualizarEstadoRequest();
        request.setNuevoEstado(EstadoOrden.FINALIZADA);
        request.setFechaInicioTrabajo(LocalDateTime.now().minusHours(2));
        request.setFechaFinTrabajo(LocalDateTime.now());

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(evidenciaRepository.contarEvidenciasPorOrden(1L)).thenReturn(2L);
        when(ordenTrabajoRepository.guardar(any(OrdenTrabajo.class))).thenReturn(orden);
        doNothing().when(notificacionService).notificarCambioEstadoASupervisor(any());
        doNothing().when(notificacionService).notificarCambioEstadoACliente(any());

        // Act
        OrdenTrabajoResponse response =
                ordenTrabajoService.actualizarEstadoOrden(1L, request, "tecnico@telconova.com");

        // Assert
        assertThat(response).isNotNull();
        verify(evidenciaRepository, times(1)).contarEvidenciasPorOrden(1L);
        verify(ordenTrabajoRepository, times(1)).guardar(any(OrdenTrabajo.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al finalizar sin evidencias")
    void debeLanzarExcepcionAlFinalizarSinEvidencias() {
        // Arrange
        orden.setEstado(EstadoOrden.EN_PROCESO);
        ActualizarEstadoRequest request = new ActualizarEstadoRequest();
        request.setNuevoEstado(EstadoOrden.FINALIZADA);
        request.setFechaInicioTrabajo(LocalDateTime.now().minusHours(2));
        request.setFechaFinTrabajo(LocalDateTime.now());

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(evidenciaRepository.contarEvidenciasPorOrden(1L)).thenReturn(0L);

        // Act & Assert
        assertThatThrownBy(() ->
                ordenTrabajoService.actualizarEstadoOrden(1L, request, "tecnico@telconova.com")
        )
                .isInstanceOf(DominioExcepcion.class)
                .hasMessageContaining("Se requiere al menos un comentario o foto para finalizar la orden");

        verify(ordenTrabajoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe obtener órdenes por estado como admin")
    void debeObtenerOrdenesPorEstadoComoAdmin() {
        // Arrange
        List<OrdenTrabajo> ordenes = Arrays.asList(orden);
        when(usuarioRepository.buscarPorEmail("admin@telconova.com")).thenReturn(Optional.of(admin));
        when(ordenTrabajoRepository.obtenerOrdenesPorEstado(EstadoOrden.ASIGNADA)).thenReturn(ordenes);

        // Act
        List<OrdenTrabajoResponse> responses =
                ordenTrabajoService.obtenerOrdenesPorEstado(EstadoOrden.ASIGNADA, "admin@telconova.com");

        // Assert
        assertThat(responses).hasSize(1);
        verify(ordenTrabajoRepository, times(1)).obtenerOrdenesPorEstado(EstadoOrden.ASIGNADA);
    }

    @Test
    @DisplayName("Debe obtener órdenes por estado como técnico")
    void debeObtenerOrdenesPorEstadoComoTecnico() {
        // Arrange
        List<OrdenTrabajo> ordenes = Arrays.asList(orden);
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(ordenTrabajoRepository.obtenerOrdenesPorTecnicoYEstado(1L, EstadoOrden.ASIGNADA))
                .thenReturn(ordenes);

        // Act
        List<OrdenTrabajoResponse> responses =
                ordenTrabajoService.obtenerOrdenesPorEstado(EstadoOrden.ASIGNADA, "tecnico@telconova.com");

        // Assert
        assertThat(responses).hasSize(1);
        verify(ordenTrabajoRepository, times(1))
                .obtenerOrdenesPorTecnicoYEstado(1L, EstadoOrden.ASIGNADA);
    }

    @Test
    @DisplayName("Admin debe poder acceder a cualquier orden")
    void adminDebePodeAccederACualquierOrden() {
        // Arrange
        when(usuarioRepository.buscarPorEmail("admin@telconova.com")).thenReturn(Optional.of(admin));

        // Act
        boolean puedeAcceder = ordenTrabajoService.puedeAccederOrden(1L, "admin@telconova.com");

        // Assert
        assertThat(puedeAcceder).isTrue();
    }

    @Test
    @DisplayName("Técnico debe poder acceder solo a su orden asignada")
    void tecnicoDebePodeAccederSoloASuOrden() {
        // Arrange
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(orden));

        // Act
        boolean puedeAcceder = ordenTrabajoService.puedeAccederOrden(1L, "tecnico@telconova.com");

        // Assert
        assertThat(puedeAcceder).isTrue();
    }

    @Test
    @DisplayName("Técnico no debe poder acceder a orden de otro técnico")
    void tecnicoNoDebePodeAccederAOrdenDeOtro() {
        // Arrange
        Usuario otroTecnico = Usuario.builder()
                .id(3L)
                .email(Email.de("otro@telconova.com"))
                .rol(RolUsuario.TECNICO)
                .activo(true)
                .build();

        when(usuarioRepository.buscarPorEmail("otro@telconova.com")).thenReturn(Optional.of(otroTecnico));
        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(orden));

        // Act
        boolean puedeAcceder = ordenTrabajoService.puedeAccederOrden(1L, "otro@telconova.com");

        // Assert
        assertThat(puedeAcceder).isFalse();
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar transición inválida")
    void debeLanzarExcepcionConTransicionInvalida() {
        // Arrange
        orden.setEstado(EstadoOrden.ASIGNADA);
        ActualizarEstadoRequest request = new ActualizarEstadoRequest();
        request.setNuevoEstado(EstadoOrden.FINALIZADA);

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));

        // Act & Assert
        assertThatThrownBy(() ->
                ordenTrabajoService.actualizarEstadoOrden(1L, request, "tecnico@telconova.com")
        )
                .isInstanceOf(EstadoOrdenInvalidoExcepcion.class);
    }
}
