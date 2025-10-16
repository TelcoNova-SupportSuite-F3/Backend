package com.telconova.supportsuite.aplicacion.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@DisplayName("Tests para clase LoginResponse")
class OrdenTrabajoResponseTest {


    @Test
    @DisplayName("Debe crear OrdenTrabajoResponse completo")
    void debeCrearOrdenTrabajoResponseCompleto() {
        // Arrange
        LocalDateTime fechaCreacion = LocalDateTime.now().minusDays(2);

        MaterialUtilizadoResponse material1 = MaterialUtilizadoResponse.builder()
                .id(1L)
                .codigoMaterial("MAT-001")
                .nombreMaterial("Cable UTP")
                .cantidadUtilizada(10)
                .unidadMedida("metros")
                .precioUnitario(2.50)
                .costoTotal(25.00)
                .fechaRegistro(fechaCreacion)
                .registradoPor("tecnico 1")
                .build();

        MaterialUtilizadoResponse material2 = MaterialUtilizadoResponse.builder()
                .id(2L)
                .codigoMaterial("MAT-002")
                .nombreMaterial("Conector RJ45")
                .cantidadUtilizada(5)
                .unidadMedida("unidades")
                .precioUnitario(1.00)
                .costoTotal(5.00)
                .fechaRegistro(fechaCreacion)
                .registradoPor("tecnico 1")
                .build();

        List<MaterialUtilizadoResponse> materiales = Arrays.asList(material1, material2);

        EvidenciaResponse evidencia = EvidenciaResponse.builder()
                .id(1L)
                .tipo("FOTO")
                .contenido(null)
                .urlFoto("https://storage.com/foto.jpg")
                .nombreArchivo("evidencia.jpg")
                .tamanoArchivo("2.5 MB")
                .fechaCreacion(fechaCreacion)
                .creadoPor("tecnico 1")
                .build();

        List<EvidenciaResponse> evidencias = List.of(evidencia);
        // Act
        OrdenTrabajoResponse response = OrdenTrabajoResponse.builder()
                .id(1L)
                .numeroOrden("ORD-2025-001")
                .titulo("Instalación Internet Residencial")
                .descripcion("Instalación de red nueva")
                .estado("EN_PROCESO")
                .prioridad("ALTA")
                .tipoServicio("INSTALACION")
                .clienteNombre("Cliente Test")
                .clienteTelefono("+57 300 1234567")
                .direccion("Calle 123 #45-67, Ciudad")
                .tecnicoAsignado(new TecnicoResponse(10L, " ", "Tecnico 1", true))
                .fechaAsignacion(fechaCreacion)
                .fechaInicioTrabajo(fechaCreacion.plusHours(1))
                .fechaFinTrabajo(fechaCreacion.plusHours(2))
                .fechaCreacion(fechaCreacion)
                .evidencias(evidencias)
                .materialesUtilizados(materiales)
                .costoTotalMateriales(30.00)
                .duracionTrabajoHoras(2.0)
                .estaVencida(false)
                .build();

        // Assert
        assert response.getId() == 1L;
        assert response.getNumeroOrden().equals("ORD-2025-001");
        assert response.getTitulo().equals("Instalación Internet Residencial");
        assert response.getDescripcion().equals("Instalación de red nueva");
        assert response.getEstado().equals("EN_PROCESO");
        assert response.getPrioridad().equals("ALTA");
        assert response.getTipoServicio().equals("INSTALACION");
        assert response.getClienteNombre().equals("Cliente Test");
        assert response.getClienteTelefono().equals("+57 300 1234567");
        assert response.getDireccion().equals("Calle 123 #45-67, Ciudad");
        assert response.getTecnicoAsignado() != null;
        assert response.getFechaAsignacion().equals(fechaCreacion);
        assert response.getFechaInicioTrabajo().equals(fechaCreacion.plusHours(1));
        assert response.getFechaFinTrabajo().equals(fechaCreacion.plusHours(2));
        assert response.getFechaCreacion().equals(fechaCreacion);
        assert response.getEvidencias() != null && response.getEvidencias().size() == 1;
        assert response.getMaterialesUtilizados() != null && response.getMaterialesUtilizados().size() == 2;
        assert response.getCostoTotalMateriales() == 30.00;
        assert response.getDuracionTrabajoHoras() == 2.0;

        assertThat(response.getMaterialesUtilizados()).hasSize(2);
        assertThat(response.getCostoTotalMateriales()).isEqualTo(30.00);
        assertThat(response.getDuracionTrabajoHoras()).isEqualTo(2.0);

    }
}