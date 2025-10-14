package com.telconova.supportsuite.aplicacion.puertos.salida;

import com.telconova.supportsuite.dominio.entidades.Material;
import com.telconova.supportsuite.dominio.entidades.MaterialUtilizado;
import java.util.Optional;
import java.util.List;

public interface IMaterialRepository {

    /**
     * Busca un material por ID
     */
    Optional<Material> buscarPorId(Long id);

    /**
     * Busca un material por código
     */
    Optional<Material> buscarPorCodigo(String codigo);

    /**
     * Busca materiales por nombre (autocompletado)
     */
    List<Material> buscarPorNombre(String nombreBusqueda, int limite);

    /**
     * Guarda un material
     */
    Material guardar(Material material);

    /**
     * Obtiene todos los materiales activos
     */
    List<Material> obtenerMaterialesActivos();

    /**
     * Guarda un registro de material utilizado
     */
    MaterialUtilizado guardarMaterialUtilizado(MaterialUtilizado materialUtilizado);

    /**
     * Obtiene materiales utilizados por orden
     */
    List<MaterialUtilizado> obtenerMaterialesUtilizadosPorOrden(Long ordenTrabajoId);

    /**
     * Busca material utilizado específico
     */
    Optional<MaterialUtilizado> buscarMaterialUtilizado(Long ordenTrabajoId, Long materialId);

    /**
     * Verifica si existe material por código
     */
    boolean existePorCodigo(String codigo);

    /**
     * Obtiene materiales con stock bajo
     */
    List<Material> obtenerMaterialesConStockBajo(int stockMinimo);

    /**
     * Elimina todos los materiales utilizados de una orden
     */
    void eliminarMaterialesUtilizadosPorOrden(Long ordenTrabajoId);

    /**
     * Elimina un registro específico de material utilizado
     */
    void eliminarMaterialUtilizado(Long materialUtilizadoId);

    /**
     * Busca un material utilizado por su ID
     */
    Optional<MaterialUtilizado> buscarMaterialUtilizadoPorId(Long materialUtilizadoId);
}
