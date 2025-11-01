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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

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

    private static final String EMAIL_TECNICO = "tecnico@telconova.com";
    private static final String EMAIL_ADMIN = "admin@telconova.com";
    private static final Long ID_ORDEN = 1L;
    private static final Long ID_TECNICO = 1L;
    private static final String NUMERO_ORDEN = "ORD-2025-001";

    @BeforeEach
    void setUp() {
        tecnico = crearUsuario(ID_TECNICO, EMAIL_TECNICO, "Juan Técnico", RolUsuario.TECNICO);
        admin = crearUsuario(2L, EMAIL_ADMIN, "Admin Sistema", RolUsuario.ADMIN);
        orden = crearOrdenTrabajo();

        // Auto-inyección del servicio para evitar problemas transaccionales
        ordenTrabajoService.setSelf(ordenTrabajoService);
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    private Usuario crearUsuario(Long id, String email, String nombre, RolUsuario rol) {
        return Usuario.builder()
                .id(id)
                .email(Email.de(email))
                .nombreCompleto(nombre)
                .rol(rol)
                .activo(true)
                .build();
    }

    private OrdenTrabajo crearOrdenTrabajo() {
        return OrdenTrabajo.builder()
                .id(ID_ORDEN)
                .numeroOrden(NumeroOrden.de(NUMERO_ORDEN))
                .titulo("Instalación Internet")
                .descripcion("Instalación de servicio de internet fibra óptica")
                .estado(EstadoOrden.ASIGNADA)
                .prioridad(Prioridad.MEDIA)
                .tipoServicio(TipoServicio.INSTALACION)
                .clienteNombre("Cliente Test")
                .clienteTelefono(Telefono.de("+57 300 1234567"))
                .direccion("Calle 123 #45-67")
                .tecnicoAsignadoId(ID_TECNICO)
                .fechaCreacion(LocalDateTime.now())
                .fechaAsignacion(LocalDateTime.now())
                .build();
    }

    private ActualizarEstadoRequest crearRequestCambioEstado(EstadoOrden nuevoEstado) {
        ActualizarEstadoRequest request = new ActualizarEstadoRequest();
        request.setNuevoEstado(nuevoEstado);
        return request;
    }

    private void configurarMocksParaCambioEstado() {
        when(ordenTrabajoRepository.buscarPorId(ID_ORDEN)).thenReturn(Optional.of(orden));
        when(usuarioRepository.buscarPorEmail(EMAIL_TECNICO)).thenReturn(Optional.of(tecnico));
        when(ordenTrabajoRepository.guardar(any(OrdenTrabajo.class))).thenReturn(orden);
        doNothing().when(notificacionService).notificarCambioEstadoASupervisor(any());
        doNothing().when(notificacionService).notificarCambioEstadoACliente(any());
    }

    // ==================== TESTS: OBTENER ÓRDENES ====================

    @Test
    @DisplayName("Debe obtener órdenes por técnico exitosamente")
    void debeObtenerOrdenesPorTecnico() {
        // Arrange
        List<OrdenTrabajo> ordenes = List.of(orden);
        when(usuarioRepository.buscarPorEmail(EMAIL_TECNICO)).thenReturn(Optional.of(tecnico));
        when(ordenTrabajoRepository.obtenerOrdenesPorTecnico(ID_TECNICO)).thenReturn(ordenes);

        // Act
        List<OrdenTrabajoResponse> resultado = ordenTrabajoService.obtenerOrdenesPorTecnico(EMAIL_TECNICO);

        // Assert
        assertThat(resultado)
                .hasSize(1)
                .first()
                .extracting(OrdenTrabajoResponse::getNumeroOrden)
                .isEqualTo(NUMERO_ORDEN);

        verify(ordenTrabajoRepository).obtenerOrdenesPorTecnico(ID_TECNICO);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando usuario no es técnico")
    void debeLanzarExcepcionCuandoUsuarioNoEsTecnico() {
        // Arrange
        when(usuarioRepository.buscarPorEmail(EMAIL_ADMIN)).thenReturn(Optional.of(admin));

        // Act & Assert
        assertThatThrownBy(() -> ordenTrabajoService.obtenerOrdenesPorTecnico(EMAIL_ADMIN))
                .isInstanceOf(AccesoNoAutorizadoExcepcion.class);

        verify(ordenTrabajoRepository, never()).obtenerOrdenesPorTecnico(anyLong());
    }

    @Test
    @DisplayName("Debe obtener todas las órdenes del sistema")
    void debeObtenerTodasLasOrdenes() {
        // Arrange
        List<OrdenTrabajo> ordenes = List.of(orden);
        when(ordenTrabajoRepository.obtenerTodasLasOrdenes()).thenReturn(ordenes);

        // Act
        List<OrdenTrabajoResponse> resultado = ordenTrabajoService.obtenerTodasLasOrdenes();

        // Assert
        assertThat(resultado).hasSize(1);
        verify(ordenTrabajoRepository).obtenerTodasLasOrdenes();
    }

    @Test
    @DisplayName("Debe obtener orden por ID exitosamente")
    void debeObtenerOrdenPorId() {
        // Arrange
        when(ordenTrabajoRepository.buscarPorId(ID_ORDEN)).thenReturn(Optional.of(orden));
        when(usuarioRepository.buscarPorEmail(EMAIL_TECNICO)).thenReturn(Optional.of(tecnico));

        // Act
        OrdenTrabajoResponse resultado = ordenTrabajoService.obtenerOrdenPorId(ID_ORDEN, EMAIL_TECNICO);

        // Assert
        assertThat(resultado)
                .isNotNull()
                .satisfies(response -> {
                    assertThat(response.getId()).isEqualTo(ID_ORDEN);
                    assertThat(response.getNumeroOrden()).isEqualTo(NUMERO_ORDEN);
                });
    }

    @Test
    @DisplayName("Debe lanzar excepción al obtener orden inexistente")
    void debeLanzarExcepcionAlObtenerOrdenInexistente() {
        // Arrange
        Long idInexistente = 999L;
        when(ordenTrabajoRepository.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> ordenTrabajoService.obtenerOrdenPorId(idInexistente, EMAIL_TECNICO))
                .isInstanceOf(OrdenNoEncontradaExcepcion.class);
    }

    // ==================== TESTS PARAMETRIZADOS: TRANSICIONES DE ESTADO ====================

    @ParameterizedTest
    @MethodSource("proveerTransicionesExitosas")
    @DisplayName("Debe realizar transiciones de estado válidas correctamente")
    void debeRealizarTransicionesValidas(EstadoOrden estadoInicial, EstadoOrden estadoFinal) {
        // Arrange
        orden.setEstado(estadoInicial);
        ActualizarEstadoRequest request = crearRequestCambioEstado(estadoFinal);
        configurarMocksParaCambioEstado();

        if (estadoFinal == EstadoOrden.CANCELADA) {
            doNothing().when(materialService).devolverMaterialesDeOrden(ID_ORDEN);
        }

        if (estadoFinal == EstadoOrden.FINALIZADA) {
            when(evidenciaRepository.contarEvidenciasPorOrden(ID_ORDEN)).thenReturn(2L);
        }

        // Act
        OrdenTrabajoResponse resultado = ordenTrabajoService.actualizarEstadoOrden(
                ID_ORDEN, request, EMAIL_TECNICO);

        // Assert
        assertThat(resultado).isNotNull();
        verify(ordenTrabajoRepository).guardar(any(OrdenTrabajo.class));
        verify(notificacionService).notificarCambioEstadoASupervisor(any());
        verify(notificacionService).notificarCambioEstadoACliente(any());
    }

    static Stream<Arguments> proveerTransicionesExitosas() {
        return Stream.of(
                Arguments.of(EstadoOrden.ASIGNADA, EstadoOrden.EN_PROCESO),
                Arguments.of(EstadoOrden.EN_PROCESO, EstadoOrden.PAUSADA),
                Arguments.of(EstadoOrden.PAUSADA, EstadoOrden.EN_PROCESO),
                Arguments.of(EstadoOrden.EN_PROCESO, EstadoOrden.CANCELADA),
                Arguments.of(EstadoOrden.EN_PROCESO, EstadoOrden.FINALIZADA)
        );
    }

    @Test
    @DisplayName("Debe cancelar orden y devolver materiales")
    void debeCancelarOrdenYDevolverMateriales() {
        // Arrange
        orden.setEstado(EstadoOrden.EN_PROCESO);
        ActualizarEstadoRequest request = crearRequestCambioEstado(EstadoOrden.CANCELADA);
        configurarMocksParaCambioEstado();
        doNothing().when(materialService).devolverMaterialesDeOrden(ID_ORDEN);

        // Act
        OrdenTrabajoResponse resultado = ordenTrabajoService.actualizarEstadoOrden(
                ID_ORDEN, request, EMAIL_TECNICO);

        // Assert
        assertThat(resultado).isNotNull();
        verify(materialService).devolverMaterialesDeOrden(ID_ORDEN);
        verify(ordenTrabajoRepository).guardar(any(OrdenTrabajo.class));
    }

    @ParameterizedTest
    @CsvSource({
            "CANCELADA, EN_PROCESO, cancelada",
            "FINALIZADA, CANCELADA, finalizada"
    })
    @DisplayName("Debe lanzar excepción al intentar modificar orden cancelada o finalizada")
    void debeLanzarExcepcionAlModificarOrdenCanceladaOFinalizada(
            EstadoOrden estadoActual, EstadoOrden nuevoEstado, String mensajeEsperado) {
        // Arrange
        orden.setEstado(estadoActual);
        ActualizarEstadoRequest request = crearRequestCambioEstado(nuevoEstado);
        when(ordenTrabajoRepository.buscarPorId(ID_ORDEN)).thenReturn(Optional.of(orden));
        when(usuarioRepository.buscarPorEmail(EMAIL_TECNICO)).thenReturn(Optional.of(tecnico));

        // Act & Assert
        assertThatThrownBy(() -> ordenTrabajoService.actualizarEstadoOrden(
                ID_ORDEN, request, EMAIL_TECNICO))
                .isInstanceOf(EstadoOrdenInvalidoExcepcion.class)
                .hasMessageContaining(mensajeEsperado);

        verify(ordenTrabajoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe finalizar orden con evidencias correctamente")
    void debeFinalizarOrdenConEvidencias() {
        // Arrange
        orden.setEstado(EstadoOrden.EN_PROCESO);
        ActualizarEstadoRequest request = crearRequestCambioEstado(EstadoOrden.FINALIZADA);
        request.setFechaInicioTrabajo(LocalDateTime.now().minusHours(2));
        request.setFechaFinTrabajo(LocalDateTime.now());

        configurarMocksParaCambioEstado();
        when(evidenciaRepository.contarEvidenciasPorOrden(ID_ORDEN)).thenReturn(2L);

        // Act
        OrdenTrabajoResponse resultado = ordenTrabajoService.actualizarEstadoOrden(
                ID_ORDEN, request, EMAIL_TECNICO);

        // Assert
        assertThat(resultado).isNotNull();
        verify(evidenciaRepository).contarEvidenciasPorOrden(ID_ORDEN);
        verify(ordenTrabajoRepository).guardar(any(OrdenTrabajo.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al finalizar orden sin evidencias")
    void debeLanzarExcepcionAlFinalizarSinEvidencias() {
        // Arrange
        orden.setEstado(EstadoOrden.EN_PROCESO);
        ActualizarEstadoRequest request = crearRequestCambioEstado(EstadoOrden.FINALIZADA);
        when(ordenTrabajoRepository.buscarPorId(ID_ORDEN)).thenReturn(Optional.of(orden));
        when(usuarioRepository.buscarPorEmail(EMAIL_TECNICO)).thenReturn(Optional.of(tecnico));
        when(evidenciaRepository.contarEvidenciasPorOrden(ID_ORDEN)).thenReturn(0L);

        // Act & Assert
        assertThatThrownBy(() -> ordenTrabajoService.actualizarEstadoOrden(
                ID_ORDEN, request, EMAIL_TECNICO))
                .isInstanceOf(DominioExcepcion.class)
                .hasMessageContaining("Se requiere al menos un comentario o foto");

        verify(ordenTrabajoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción con transición inválida de estado")
    void debeLanzarExcepcionConTransicionInvalida() {
        // Arrange
        orden.setEstado(EstadoOrden.ASIGNADA);
        ActualizarEstadoRequest request = crearRequestCambioEstado(EstadoOrden.FINALIZADA);
        when(ordenTrabajoRepository.buscarPorId(ID_ORDEN)).thenReturn(Optional.of(orden));
        when(usuarioRepository.buscarPorEmail(EMAIL_TECNICO)).thenReturn(Optional.of(tecnico));

        // Act & Assert
        assertThatThrownBy(() -> ordenTrabajoService.actualizarEstadoOrden(
                ID_ORDEN, request, EMAIL_TECNICO))
                .isInstanceOf(EstadoOrdenInvalidoExcepcion.class);
    }

    // ==================== TESTS: OBTENER ÓRDENES POR ESTADO ====================

    @Test
    @DisplayName("Debe obtener órdenes por estado como administrador")
    void debeObtenerOrdenesPorEstadoComoAdmin() {
        // Arrange
        List<OrdenTrabajo> ordenes = List.of(orden);
        when(usuarioRepository.buscarPorEmail(EMAIL_ADMIN)).thenReturn(Optional.of(admin));
        when(ordenTrabajoRepository.obtenerOrdenesPorEstado(EstadoOrden.ASIGNADA)).thenReturn(ordenes);

        // Act
        List<OrdenTrabajoResponse> resultado = ordenTrabajoService.obtenerOrdenesPorEstado(
                EstadoOrden.ASIGNADA, EMAIL_ADMIN);

        // Assert
        assertThat(resultado).hasSize(1);
        verify(ordenTrabajoRepository).obtenerOrdenesPorEstado(EstadoOrden.ASIGNADA);
    }

    @Test
    @DisplayName("Debe obtener órdenes por estado como técnico")
    void debeObtenerOrdenesPorEstadoComoTecnico() {
        // Arrange
        List<OrdenTrabajo> ordenes = List.of(orden);
        when(usuarioRepository.buscarPorEmail(EMAIL_TECNICO)).thenReturn(Optional.of(tecnico));
        when(ordenTrabajoRepository.obtenerOrdenesPorTecnicoYEstado(ID_TECNICO, EstadoOrden.ASIGNADA))
                .thenReturn(ordenes);

        // Act
        List<OrdenTrabajoResponse> resultado = ordenTrabajoService.obtenerOrdenesPorEstado(
                EstadoOrden.ASIGNADA, EMAIL_TECNICO);

        // Assert
        assertThat(resultado).hasSize(1);
        verify(ordenTrabajoRepository).obtenerOrdenesPorTecnicoYEstado(ID_TECNICO, EstadoOrden.ASIGNADA);
    }

    // ==================== TESTS: CONTROL DE ACCESO ====================

    @Test
    @DisplayName("Administrador debe poder acceder a cualquier orden")
    void adminDebePodeAccederACualquierOrden() {
        // Arrange
        when(usuarioRepository.buscarPorEmail(EMAIL_ADMIN)).thenReturn(Optional.of(admin));

        // Act
        boolean resultado = ordenTrabajoService.puedeAccederOrden(ID_ORDEN, EMAIL_ADMIN);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Técnico debe poder acceder solo a su orden asignada")
    void tecnicoDebePodeAccederSoloASuOrden() {
        // Arrange
        when(usuarioRepository.buscarPorEmail(EMAIL_TECNICO)).thenReturn(Optional.of(tecnico));
        when(ordenTrabajoRepository.buscarPorId(ID_ORDEN)).thenReturn(Optional.of(orden));

        // Act
        boolean resultado = ordenTrabajoService.puedeAccederOrden(ID_ORDEN, EMAIL_TECNICO);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Técnico no debe poder acceder a orden de otro técnico")
    void tecnicoNoDebePodeAccederAOrdenDeOtro() {
        // Arrange
        String emailOtroTecnico = "otro@telconova.com";
        Usuario otroTecnico = crearUsuario(3L, emailOtroTecnico, "Otro Técnico", RolUsuario.TECNICO);
        when(usuarioRepository.buscarPorEmail(emailOtroTecnico)).thenReturn(Optional.of(otroTecnico));
        when(ordenTrabajoRepository.buscarPorId(ID_ORDEN)).thenReturn(Optional.of(orden));

        // Act
        boolean resultado = ordenTrabajoService.puedeAccederOrden(ID_ORDEN, emailOtroTecnico);

        // Assert
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Debe retornar falso cuando usuario no existe")
    void debeRetornarFalsoCuandoUsuarioNoExiste() {
        // Arrange
        String emailInexistente = "inexistente@telconova.com";
        when(usuarioRepository.buscarPorEmail(emailInexistente)).thenReturn(Optional.empty());

        // Act
        boolean resultado = ordenTrabajoService.puedeAccederOrden(ID_ORDEN, emailInexistente);

        // Assert
        assertThat(resultado).isFalse();
    }
}
