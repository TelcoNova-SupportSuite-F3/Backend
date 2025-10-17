package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia;

import com.telconova.supportsuite.dominio.entidades.OrdenTrabajo;
import com.telconova.supportsuite.dominio.enums.EstadoOrden;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.OrdenTrabajoEntity;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers.OrdenTrabajoMapper;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios.OrdenTrabajoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias para OrdenTrabajoRepositoryImpl")
class OrdenTrabajoRepositoryImplTest {


    private static final Long ORDEN_ID = 1L;
    private static final String NUMERO_ORDEN = "ORD-001";
    private static final Long TECNICO_ID = 99L;
    private static final EstadoOrden ESTADO = EstadoOrden.EN_PROCESO;
    private static final int DIAS_LIMITE = 7;

    @Mock
    private OrdenTrabajoJpaRepository jpaRepository;

    @Mock
    private OrdenTrabajoMapper mapper;

    @InjectMocks
    private OrdenTrabajoRepositoryImpl repository;

    private OrdenTrabajo ordenTrabajo;
    private OrdenTrabajoEntity entity;

    @BeforeEach
    void setUp() {
        ordenTrabajo = mock(OrdenTrabajo.class);
        entity = mock(OrdenTrabajoEntity.class);
    }

        @Test
        @DisplayName("Debe retornar la orden cuando se busca por ID existente")
        void buscarPorId_DebeRetornarOrden() {
            // Arrange
            when(jpaRepository.findById(ORDEN_ID)).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(ordenTrabajo);

            // Act
            Optional<OrdenTrabajo> resultado = repository.buscarPorId(ORDEN_ID);

            // Assert
            assertThat(resultado)
                    .isPresent()
                    .contains(ordenTrabajo);
            verify(jpaRepository).findById(ORDEN_ID);
            verify(mapper).toDomain(entity);
        }

    @Test
    @DisplayName("Debe retornar vacío cuando no existe la orden por ID")
    void buscarPorId_NoExisteOrden() {
        // Arrange
        when(jpaRepository.findById(ORDEN_ID)).thenReturn(Optional.empty());

        // Act
        Optional<OrdenTrabajo> resultado = repository.buscarPorId(ORDEN_ID);

        // Assert
        assertThat(resultado).isEmpty();
        verify(jpaRepository).findById(ORDEN_ID);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("Debe retornar las órdenes de un técnico")
    void obtenerOrdenesPorTecnico_DebeRetornarLista() {
        // Arrange
        when(jpaRepository.findOrdenesActivasPorTecnico(TECNICO_ID)).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(ordenTrabajo);

        // Act
        List<OrdenTrabajo> resultado = repository.obtenerOrdenesPorTecnico(TECNICO_ID);

        // Assert
        assertThat(resultado).containsExactly(ordenTrabajo);
        verify(jpaRepository).findOrdenesActivasPorTecnico(TECNICO_ID);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("Debe retornar las órdenes por estado")
    void obtenerOrdenesPorEstado_DebeRetornarLista() {
        // Arrange
        when(jpaRepository.findByEstado(ESTADO)).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(ordenTrabajo);

        // Act
        List<OrdenTrabajo> resultado = repository.obtenerOrdenesPorEstado(ESTADO);

        // Assert
        assertThat(resultado).containsExactly(ordenTrabajo);
        verify(jpaRepository).findByEstado(ESTADO);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("Debe verificar existencia por número de orden")
    void existePorNumeroOrden_DebeRetornarTrue() {
        // Arrange
        when(jpaRepository.existsByNumeroOrden(NUMERO_ORDEN)).thenReturn(true);

        // Act
        boolean existe = repository.existePorNumeroOrden(NUMERO_ORDEN);

        // Assert
        assertThat(existe).isTrue();
        verify(jpaRepository).existsByNumeroOrden(NUMERO_ORDEN);
    }

    @Test
    @DisplayName("Debe contar órdenes por estado")
    void contarOrdenesPorEstado_DebeRetornarCantidad() {
        // Arrange
        long cantidadEsperada = 5L;
        when(jpaRepository.countByEstado(ESTADO)).thenReturn(cantidadEsperada);

        // Act
        long cantidad = repository.contarOrdenesPorEstado(ESTADO);

        // Assert
        assertThat(cantidad).isEqualTo(cantidadEsperada);
        verify(jpaRepository).countByEstado(ESTADO);
    }

    @Test
    @DisplayName("Debe obtener órdenes vencidas según días límite")
    void obtenerOrdenesVencidas_DebeRetornarLista() {
        // Arrange
        when(jpaRepository.findOrdenesVencidas(any(LocalDateTime.class))).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(ordenTrabajo);

        // Act
        List<OrdenTrabajo> resultado = repository.obtenerOrdenesVencidas(DIAS_LIMITE);

        // Assert
        assertThat(resultado).containsExactly(ordenTrabajo);
        verify(jpaRepository).findOrdenesVencidas(any(LocalDateTime.class));
        verify(mapper).toDomain(entity);
    }
}
