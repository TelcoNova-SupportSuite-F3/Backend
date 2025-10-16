package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers;

import com.telconova.supportsuite.dominio.entidades.OrdenTrabajo;
import com.telconova.supportsuite.dominio.enums.EstadoOrden;
import com.telconova.supportsuite.dominio.enums.Prioridad;
import com.telconova.supportsuite.dominio.enums.TipoServicio;
import com.telconova.supportsuite.dominio.valueobjects.NumeroOrden;
import com.telconova.supportsuite.dominio.valueobjects.Telefono;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.OrdenTrabajoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para clase OrdenTrabajoMapper")
class OrdenTrabajoMapperTest {

    private OrdenTrabajoMapper ordenTrabajoMapper;


    @BeforeEach
    void setUp() {
        ordenTrabajoMapper = new OrdenTrabajoMapper();
    }

    @Test
    @DisplayName("Debe convertir OrdenTrabajoEntity a OrdenTrabajo correctamente")
    void debeConvertirOrdenTrabajoEntityADominio() {
        // Arrange
        OrdenTrabajoEntity entity = OrdenTrabajoEntity.builder()
                .id(1L)
                .numeroOrden("ORD-2025-001")
                .titulo("Instalación Internet")
                .descripcion("Instalación de servicio de internet")
                .estado(EstadoOrden.EN_PROCESO)
                .prioridad(Prioridad.ALTA)
                .tipoServicio(TipoServicio.INSTALACION)
                .clienteNombre("Cliente Test")
                .clienteTelefono("+57 300 1234567")
                .direccion("Calle 123")
                .tecnicoAsignadoId(10L)
                .fechaAsignacion(LocalDateTime.now())
                .fechaInicioTrabajo(LocalDateTime.now())
                .fechaFinTrabajo(null)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        // Act
        OrdenTrabajo dominio = ordenTrabajoMapper.toDomain(entity);

        // Assert
        assertThat(dominio).isNotNull();
        assertThat(dominio.getId()).isEqualTo(1L);
        assertThat(dominio.getNumeroOrden().getValor()).isEqualTo("ORD-2025-001");
        assertThat(dominio.getTitulo()).isEqualTo("Instalación Internet");
        assertThat(dominio.getEstado()).isEqualTo(EstadoOrden.EN_PROCESO);
        assertThat(dominio.getPrioridad()).isEqualTo(Prioridad.ALTA);
        assertThat(dominio.getTipoServicio()).isEqualTo(TipoServicio.INSTALACION);
        assertThat(dominio.getClienteTelefono().getValor()).isEqualTo("+57 300 1234567");
    }

    @Test
    @DisplayName("Debe convertir OrdenTrabajo a OrdenTrabajoEntity correctamente")
    void debeConvertirOrdenTrabajoAEntity() {
        // Arrange
        OrdenTrabajo dominio = OrdenTrabajo.builder()
                .id(2L)
                .numeroOrden(NumeroOrden.de("ORD-2025-002"))
                .titulo("Reparación")
                .descripcion("Reparación de servicio")
                .estado(EstadoOrden.EN_PROCESO)
                .prioridad(Prioridad.MEDIA)
                .tipoServicio(TipoServicio.REPARACION)
                .clienteNombre("Otro Cliente")
                .clienteTelefono(Telefono.de("+57 310 9876543"))
                .direccion("Avenida 456")
                .tecnicoAsignadoId(20L)
                .fechaAsignacion(LocalDateTime.now())
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        // Act
        OrdenTrabajoEntity entity = ordenTrabajoMapper.toEntity(dominio);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getNumeroOrden()).isEqualTo("ORD-2025-002");
        assertThat(entity.getTitulo()).isEqualTo("Reparación");
        assertThat(entity.getEstado()).isEqualTo(EstadoOrden.EN_PROCESO);
        assertThat(entity.getClienteTelefono()).isEqualTo("+57 310 9876543");
    }

    @Test
    @DisplayName("Debe manejar telefono null en OrdenTrabajoMapper")
    void debeManejarTelefonoNullEnOrdenTrabajoMapper() {
        // Arrange
        OrdenTrabajoEntity entity = OrdenTrabajoEntity.builder()
                .id(1L)
                .numeroOrden("ORD-2025-001")
                .titulo("Test")
                .clienteTelefono(null) // Teléfono null
                .build();

        OrdenTrabajo dominio = OrdenTrabajo.builder()
                .id(1L)
                .numeroOrden(NumeroOrden.de("ORD-2025-001"))
                .titulo("Test")
                .clienteTelefono(null) // Teléfono null
                .build();

        // Act
        OrdenTrabajo dominioResult = ordenTrabajoMapper.toDomain(entity);
        OrdenTrabajoEntity entityResult = ordenTrabajoMapper.toEntity(dominio);

        // Assert
        assertThat(dominioResult.getClienteTelefono()).isNull();
        assertThat(entityResult.getClienteTelefono()).isNull();
    }
}
