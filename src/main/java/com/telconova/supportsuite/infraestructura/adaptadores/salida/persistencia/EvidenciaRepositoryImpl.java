package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia;

import com.telconova.supportsuite.aplicacion.puertos.salida.IEvidenciaRepository;
import com.telconova.supportsuite.dominio.entidades.Evidencia;
import com.telconova.supportsuite.dominio.enums.TipoEvidencia;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.EvidenciaEntity;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers.EvidenciaMapper;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios.EvidenciaJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio de evidencias usando JPA
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class EvidenciaRepositoryImpl implements IEvidenciaRepository {

    private final EvidenciaJpaRepository jpaRepository;
    private final EvidenciaMapper mapper;

    @Override
    public Evidencia guardar(Evidencia evidencia) {
        log.debug("Guardando evidencia para orden: {}", evidencia.getOrdenTrabajoId());
        EvidenciaEntity entity = mapper.toEntity(evidencia);
        EvidenciaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Evidencia> buscarPorId(Long id) {
        log.debug("Buscando evidencia por ID: {}", id);
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Evidencia> obtenerEvidenciasPorOrden(Long ordenTrabajoId) {
        log.debug("Obteniendo evidencias para orden: {}", ordenTrabajoId);
        return jpaRepository.findEvidenciasPorOrdenOrdenadas(ordenTrabajoId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Evidencia> obtenerEvidenciasPorTipo(Long ordenTrabajoId, TipoEvidencia tipo) {
        log.debug("Obteniendo evidencias de tipo {} para orden: {}", tipo, ordenTrabajoId);
        return jpaRepository.findByOrdenTrabajoIdAndTipo(ordenTrabajoId, tipo)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long evidenciaId) {
        log.debug("Eliminando evidencia: {}", evidenciaId);
        jpaRepository.deleteById(evidenciaId);
    }

    @Override
    public long contarEvidenciasPorOrden(Long ordenTrabajoId) {
        log.debug("Contando evidencias para orden: {}", ordenTrabajoId);
        return jpaRepository.countByOrdenTrabajoId(ordenTrabajoId);
    }

    @Override
    public List<Evidencia> obtenerEvidenciasPorUsuario(Long usuarioId) {
        log.debug("Obteniendo evidencias creadas por usuario: {}", usuarioId);
        return jpaRepository.findByCreadoPor(usuarioId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
