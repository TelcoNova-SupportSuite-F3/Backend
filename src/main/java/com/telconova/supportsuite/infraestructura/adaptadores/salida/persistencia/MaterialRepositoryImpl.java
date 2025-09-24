package com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia;

import com.telconova.supportsuite.aplicacion.puertos.salida.IMaterialRepository;
import com.telconova.supportsuite.dominio.entidades.Material;
import com.telconova.supportsuite.dominio.entidades.MaterialUtilizado;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.MaterialEntity;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.entidades.MaterialUtilizadoEntity;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers.MaterialMapper;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.mappers.MaterialUtilizadoMapper;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios.MaterialJpaRepository;
import com.telconova.supportsuite.infraestructura.adaptadores.salida.persistencia.repositorios.MaterialUtilizadoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio de materiales usando JPA
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MaterialRepositoryImpl implements IMaterialRepository {

    private final MaterialJpaRepository materialJpaRepository;
    private final MaterialUtilizadoJpaRepository materialUtilizadoJpaRepository;
    private final MaterialMapper materialMapper;
    private final MaterialUtilizadoMapper materialUtilizadoMapper;

    @Override
    public Optional<Material> buscarPorId(Long id) {
        log.debug("Buscando material por ID: {}", id);
        return materialJpaRepository.findById(id)
                .map(materialMapper::toDomain);
    }

    @Override
    public Optional<Material> buscarPorCodigo(String codigo) {
        log.debug("Buscando material por código: {}", codigo);
        return materialJpaRepository.findByCodigo(codigo)
                .map(materialMapper::toDomain);
    }

    @Override
    public List<Material> buscarPorNombre(String nombreBusqueda, int limite) {
        log.debug("Buscando materiales por nombre: '{}' con límite: {}", nombreBusqueda, limite);
        return materialJpaRepository.buscarPorNombreConLimite(nombreBusqueda, PageRequest.of(0, limite))
                .stream()
                .map(materialMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Material guardar(Material material) {
        log.debug("Guardando material: {}", material.getCodigo());
        MaterialEntity entity = materialMapper.toEntity(material);
        MaterialEntity savedEntity = materialJpaRepository.save(entity);
        return materialMapper.toDomain(savedEntity);
    }

    @Override
    public List<Material> obtenerMaterialesActivos() {
        log.debug("Obteniendo materiales activos");
        return materialJpaRepository.findByActivoTrue()
                .stream()
                .map(materialMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public MaterialUtilizado guardarMaterialUtilizado(MaterialUtilizado materialUtilizado) {
        log.debug("Guardando material utilizado para orden: {}", materialUtilizado.getOrdenTrabajoId());
        MaterialUtilizadoEntity entity = materialUtilizadoMapper.toEntity(materialUtilizado);
        MaterialUtilizadoEntity savedEntity = materialUtilizadoJpaRepository.save(entity);
        return materialUtilizadoMapper.toDomain(savedEntity);
    }

    @Override
    public List<MaterialUtilizado> obtenerMaterialesUtilizadosPorOrden(Long ordenTrabajoId) {
        log.debug("Obteniendo materiales utilizados para orden: {}", ordenTrabajoId);
        return materialUtilizadoJpaRepository.findMaterialesUtilizadosPorOrden(ordenTrabajoId)
                .stream()
                .map(materialUtilizadoMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MaterialUtilizado> buscarMaterialUtilizado(Long ordenTrabajoId, Long materialId) {
        log.debug("Buscando material utilizado para orden {} y material {}", ordenTrabajoId, materialId);
        return materialUtilizadoJpaRepository.findByOrdenTrabajoIdAndMaterialId(ordenTrabajoId, materialId)
                .map(materialUtilizadoMapper::toDomain);
    }

    @Override
    public boolean existePorCodigo(String codigo) {
        log.debug("Verificando existencia de material por código: {}", codigo);
        return materialJpaRepository.existsByCodigo(codigo);
    }

    @Override
    public List<Material> obtenerMaterialesConStockBajo(int stockMinimo) {
        log.debug("Obteniendo materiales con stock menor a: {}", stockMinimo);
        return materialJpaRepository.findMaterialesConStockBajo(stockMinimo)
                .stream()
                .map(materialMapper::toDomain)
                .collect(Collectors.toList());
    }
}
