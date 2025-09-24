package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia;

import com.telconova.supportsuite.aplicacion.puertos.salida.IOrdenTrabajoRepository;
import com.telconova.supportsuite.dominio.entidades.OrdenTrabajo;
import com.telconova.supportsuite.dominio.enums.EstadoOrden;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.OrdenTrabajoEntity;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers.OrdenTrabajoMapper;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios.OrdenTrabajoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio de órdenes de trabajo usando JPA
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class OrdenTrabajoRepositoryImpl implements IOrdenTrabajoRepository {

    private final OrdenTrabajoJpaRepository jpaRepository;
    private final OrdenTrabajoMapper mapper;

    @Override
    public Optional<OrdenTrabajo> buscarPorId(Long id) {
        log.debug("Buscando orden por ID: {}", id);
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<OrdenTrabajo> buscarPorNumeroOrden(String numeroOrden) {
        log.debug("Buscando orden por número: {}", numeroOrden);
        return jpaRepository.findByNumeroOrden(numeroOrden)
                .map(mapper::toDomain);
    }

    @Override
    public OrdenTrabajo guardar(OrdenTrabajo ordenTrabajo) {
        log.debug("Guardando orden: {}", ordenTrabajo.getNumeroOrden().getValor());
        OrdenTrabajoEntity entity = mapper.toEntity(ordenTrabajo);
        OrdenTrabajoEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<OrdenTrabajo> obtenerOrdenesPorTecnico(Long tecnicoId) {
        log.debug("Obteniendo órdenes para técnico: {}", tecnicoId);
        return jpaRepository.findOrdenesActivasPorTecnico(tecnicoId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrdenTrabajo> obtenerTodasLasOrdenes() {
        log.debug("Obteniendo todas las órdenes");
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrdenTrabajo> obtenerOrdenesPorEstado(EstadoOrden estado) {
        log.debug("Obteniendo órdenes por estado: {}", estado);
        return jpaRepository.findByEstado(estado)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrdenTrabajo> obtenerOrdenesPorTecnicoYEstado(Long tecnicoId, EstadoOrden estado) {
        log.debug("Obteniendo órdenes para técnico {} con estado: {}", tecnicoId, estado);
        return jpaRepository.findByTecnicoAsignadoIdAndEstado(tecnicoId, estado)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existePorNumeroOrden(String numeroOrden) {
        log.debug("Verificando existencia de orden por número: {}", numeroOrden);
        return jpaRepository.existsByNumeroOrden(numeroOrden);
    }

    @Override
    public long contarOrdenesPorEstado(EstadoOrden estado) {
        log.debug("Contando órdenes por estado: {}", estado);
        return jpaRepository.countByEstado(estado);
    }

    @Override
    public List<OrdenTrabajo> obtenerOrdenesVencidas(int diasLimite) {
        log.debug("Obteniendo órdenes vencidas (más de {} días)", diasLimite);
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasLimite);
        return jpaRepository.findOrdenesVencidas(fechaLimite)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}