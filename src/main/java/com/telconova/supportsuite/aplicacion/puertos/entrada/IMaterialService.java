package com.telconova.supportsuite.aplicacion.puertos.entrada;

import com.telconova.supportsuite.aplicacion.dto.request.AgregarMaterialRequest;
import com.telconova.supportsuite.aplicacion.dto.response.MaterialResponse;
import com.telconova.supportsuite.aplicacion.dto.response.MaterialUtilizadoResponse;

import java.util.List;


public interface IMaterialService {

    /**
     * Busca materiales por nombre (autocompletado)
     */
    List<MaterialResponse> buscarMaterialesPorNombre(String nombreBusqueda, int limite);

    /**
     * Agrega un material a una orden de trabajo
     */
    void agregarMaterialAOrden(Long ordenId, AgregarMaterialRequest request, String emailUsuario);

    /**
     * Obtiene todos los materiales activos
     */
    List<MaterialResponse> obtenerMaterialesActivos();

    /**
     * Obtiene un material por ID
     */
    MaterialResponse obtenerMaterialPorId(Long materialId);

    /**
     * Obtiene materiales utilizados en una orden
     */
    List<MaterialUtilizadoResponse> obtenerMaterialesUtilizadosPorOrden(Long ordenId, String emailUsuario);

    /**
     * Verifica disponibilidad de stock
     */
    boolean verificarDisponibilidadStock(Long materialId, Integer cantidad);
}
