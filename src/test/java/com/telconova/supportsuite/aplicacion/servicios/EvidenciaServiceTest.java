package com.telconova.supportsuite.aplicacion.servicios;

import com.telconova.supportsuite.aplicacion.dto.response.EvidenciaResponse;
import com.telconova.supportsuite.aplicacion.puertos.salida.IAlmacenamientoArchivos;
import com.telconova.supportsuite.aplicacion.puertos.salida.IEvidenciaRepository;
import com.telconova.supportsuite.aplicacion.puertos.salida.IOrdenTrabajoRepository;
import com.telconova.supportsuite.aplicacion.puertos.salida.IUsuarioRepository;
import com.telconova.supportsuite.dominio.entidades.Evidencia;
import com.telconova.supportsuite.dominio.entidades.OrdenTrabajo;
import com.telconova.supportsuite.dominio.entidades.Usuario;
import com.telconova.supportsuite.dominio.enums.EstadoOrden;
import com.telconova.supportsuite.dominio.enums.RolUsuario;
import com.telconova.supportsuite.dominio.excepciones.AccesoNoAutorizadoExcepcion;
import com.telconova.supportsuite.dominio.excepciones.EvidenciaNoValidaExcepcion;
import com.telconova.supportsuite.dominio.excepciones.OrdenFinalizadaExcepcion;
import com.telconova.supportsuite.dominio.valueobjects.Email;
import com.telconova.supportsuite.dominio.valueobjects.NumeroOrden;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para EvidenciaService")
class EvidenciaServiceTest {

    @Mock
    private IEvidenciaRepository evidenciaRepository;

    @Mock
    private IOrdenTrabajoRepository ordenTrabajoRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private IAlmacenamientoArchivos almacenamientoArchivos;

    @InjectMocks
    private EvidenciaService evidenciaService;

    private OrdenTrabajo ordenEnProceso;
    private Usuario tecnico;
    private Evidencia evidenciaComentario;

    @BeforeEach
    void setUp() {
        // Arrange
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

        evidenciaComentario = Evidencia.crearComentario(
                1L,
                "Trabajo completado satisfactoriamente",
                1L
        );
    }

    @Test
    @DisplayName("Debe registrar comentario exitosamente")
    void debeRegistrarComentarioExitosamente() {
        // Arrange
        String comentario = "Instalación completada";
        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenEnProceso));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(evidenciaRepository.guardar(any(Evidencia.class))).thenReturn(evidenciaComentario);
        when(ordenTrabajoRepository.guardar(any(OrdenTrabajo.class))).thenReturn(ordenEnProceso);

        // Act
        EvidenciaResponse response = evidenciaService.registrarComentario(1L, comentario, "tecnico@telconova.com");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTipo()).isEqualTo("COMENTARIO");
        assertThat(response.getCreadoPor()).isEqualTo("Juan Técnico");

        verify(ordenTrabajoRepository, times(2)).buscarPorId(1L);
        verify(evidenciaRepository, times(1)).guardar(any(Evidencia.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al registrar comentario en orden finalizada")
    void debeLanzarExcepcionAlRegistrarComentarioEnOrdenFinalizada() {
        // Arrange
        OrdenTrabajo ordenFinalizada = OrdenTrabajo.builder()
                .id(1L)
                .numeroOrden(NumeroOrden.de("ORD-2025-001"))
                .estado(EstadoOrden.FINALIZADA)
                .tecnicoAsignadoId(1L)
                .build();

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenFinalizada));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));

        // Act & Assert
        assertThatThrownBy(() ->
                evidenciaService.registrarComentario(1L, "comentario", "tecnico@telconova.com")
        )
                .isInstanceOf(OrdenFinalizadaExcepcion.class);

        verify(evidenciaRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al registrar comentario en orden cancelada")
    void debeLanzarExcepcionAlRegistrarComentarioEnOrdenCancelada() {
        // Arrange
        OrdenTrabajo ordenCancelada = OrdenTrabajo.builder()
                .id(1L)
                .numeroOrden(NumeroOrden.de("ORD-2025-001"))
                .estado(EstadoOrden.CANCELADA)
                .tecnicoAsignadoId(1L)
                .build();

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenCancelada));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));

        // Act & Assert
        assertThatThrownBy(() ->
                evidenciaService.registrarComentario(1L, "comentario", "tecnico@telconova.com")
        )
                .isInstanceOf(OrdenFinalizadaExcepcion.class)
                .hasMessageContaining("cancelada");

        verify(evidenciaRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe registrar foto exitosamente")
    void debeRegistrarFotoExitosamente() {
        // Arrange
        MultipartFile archivo = mock(MultipartFile.class);
        when(archivo.getOriginalFilename()).thenReturn("foto.jpg");
        when(archivo.getContentType()).thenReturn("image/jpeg");
        when(archivo.getSize()).thenReturn(1024L);
        when(archivo.isEmpty()).thenReturn(false);

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenEnProceso));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(almacenamientoArchivos.esTipoMimePermitido("image/jpeg")).thenReturn(true);
        when(almacenamientoArchivos.guardarArchivo(any(), anyString())).thenReturn("ruta/foto.jpg");

        Evidencia evidenciaFoto = Evidencia.crearFoto(1L, "ruta/foto.jpg", "foto.jpg", "image/jpeg", 1024L, 1L);
        when(evidenciaRepository.guardar(any(Evidencia.class))).thenReturn(evidenciaFoto);

        // Act
        EvidenciaResponse response = evidenciaService.registrarFoto(1L, archivo, "tecnico@telconova.com");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTipo()).isEqualTo("FOTO");
        assertThat(response.getNombreArchivo()).isEqualTo("foto.jpg");

        verify(almacenamientoArchivos, times(1)).guardarArchivo(archivo, "evidencias");
        verify(evidenciaRepository, times(1)).guardar(any(Evidencia.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al registrar foto con archivo vacío")
    void debeLanzarExcepcionAlRegistrarFotoConArchivoVacio() {
        // Arrange
        MultipartFile archivoVacio = mock(MultipartFile.class);
        when(archivoVacio.isEmpty()).thenReturn(true);

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenEnProceso));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));

        // Act & Assert
        assertThatThrownBy(() ->
                evidenciaService.registrarFoto(1L, archivoVacio, "tecnico@telconova.com")
        )
                .isInstanceOf(EvidenciaNoValidaExcepcion.class);

        verify(almacenamientoArchivos, never()).guardarArchivo(any(), anyString());
    }

    @Test
    @DisplayName("Debe obtener evidencias por orden correctamente")
    void debeObtenerEvidenciasPorOrden() {
        // Arrange
        List<Evidencia> evidencias = Arrays.asList(evidenciaComentario);

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenEnProceso));
        when(usuarioRepository.buscarPorEmail("tecnico@telconova.com")).thenReturn(Optional.of(tecnico));
        when(evidenciaRepository.obtenerEvidenciasPorOrden(1L)).thenReturn(evidencias);
        when(usuarioRepository.buscarPorId(1L)).thenReturn(Optional.of(tecnico));

        // Act
        List<EvidenciaResponse> responses = evidenciaService.obtenerEvidenciasPorOrden(1L, "tecnico@telconova.com");

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCreadoPor()).isEqualTo("Juan Técnico");

        verify(evidenciaRepository, times(1)).obtenerEvidenciasPorOrden(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al obtener evidencias sin acceso")
    void debeLanzarExcepcionAlObtenerEvidenciasSinAcceso() {
        // Arrange
        Usuario otroTecnico = Usuario.builder()
                .id(2L)
                .email(Email.de("otro@telconova.com"))
                .rol(RolUsuario.TECNICO)
                .activo(true)
                .build();

        when(ordenTrabajoRepository.buscarPorId(1L)).thenReturn(Optional.of(ordenEnProceso));
        when(usuarioRepository.buscarPorEmail("otro@telconova.com")).thenReturn(Optional.of(otroTecnico));

        // Act & Assert
        assertThatThrownBy(() ->
                evidenciaService.obtenerEvidenciasPorOrden(1L, "otro@telconova.com")
        )
                .isInstanceOf(AccesoNoAutorizadoExcepcion.class);

        verify(evidenciaRepository, never()).obtenerEvidenciasPorOrden(anyLong());
    }
}
