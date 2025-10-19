package com.telconova.supportsuite.aplicacion.servicios;

import com.telconova.supportsuite.aplicacion.dto.request.AgregarMaterialRequest;
import com.telconova.supportsuite.aplicacion.dto.response.MaterialResponse;
import com.telconova.supportsuite.aplicacion.dto.response.MaterialUtilizadoResponse;
import com.telconova.supportsuite.aplicacion.puertos.entrada.IMaterialService;
import com.telconova.supportsuite.aplicacion.puertos.salida.IMaterialRepository;
import com.telconova.supportsuite.aplicacion.puertos.salida.IOrdenTrabajoRepository;
import com.telconova.supportsuite.aplicacion.puertos.salida.IUsuarioRepository;
import com.telconova.supportsuite.dominio.entidades.Material;
import com.telconova.supportsuite.dominio.entidades.MaterialUtilizado;
import com.telconova.supportsuite.dominio.entidades.OrdenTrabajo;
import com.telconova.supportsuite.dominio.entidades.Usuario;
import com.telconova.supportsuite.dominio.enums.EstadoOrden;
import com.telconova.supportsuite.dominio.excepciones.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para operaciones con materiales
 *
 * @author TelcoNova Development Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialService implements IMaterialService {


    private final IMaterialRepository materialRepository;
    private final IOrdenTrabajoRepository ordenTrabajoRepository;
    private final IUsuarioRepository usuarioRepository;

    @Override
    public List<MaterialResponse> buscarMaterialesPorNombre(String nombreBusqueda, int limite) {
        log.info("Buscando materiales por nombre: '{}' con límite: {}", nombreBusqueda, limite);

        if (nombreBusqueda == null || nombreBusqueda.trim().length() < 2) {
            log.warn("Búsqueda demasiado corta, se requieren al menos 2 caracteres");
            return List.of();
        }

        List<Material> materiales = materialRepository.buscarPorNombre(nombreBusqueda.trim(), limite);

        log.info("Encontrados {} materiales para búsqueda: '{}'", materiales.size(), nombreBusqueda);

        return materiales.stream()
                .map(this::mapearMaterialAResponse)
                .toList();
    }

    @Override
    @Transactional
    public void agregarMaterialAOrden(Long ordenId, AgregarMaterialRequest request, String emailUsuario) {
        log.info("Agregando material {} (cantidad: {}) a orden {} por usuario: {}",
                request.getMaterialId(), request.getCantidad(), ordenId, emailUsuario);

        // Validar acceso a la orden
        validarAccesoOrden(ordenId, emailUsuario);

        // Obtener orden
        OrdenTrabajo orden = ordenTrabajoRepository.buscarPorId(ordenId)
                .orElseThrow(() -> OrdenNoEncontradaExcepcion.porId(ordenId));

        // Verificar que la orden no esté cancelada
        if (orden.getEstado() == EstadoOrden.CANCELADA) {
            throw new EstadoOrdenInvalidoExcepcion("No se pueden agregar materiales a una orden cancelada");
        }

        // Verificar que la orden permite agregar materiales
        if (!orden.puedeAgregarMateriales()) {
            throw EstadoOrdenInvalidoExcepcion.paraAgregarMateriales(orden.getEstado());
        }

        // Obtener material
        Material material = materialRepository.buscarPorId(request.getMaterialId())
                .orElseThrow(() -> MaterialNoValidoExcepcion.porId(request.getMaterialId()));

        // Verificar que el material está activo
        if (!material.estaActivo()) {
            throw MaterialNoValidoExcepcion.materialInactivo(material.getCodigo());
        }

        // Verificar stock disponible
        if (!material.tieneStockSuficiente(request.getCantidad())) {
            throw MaterialNoValidoExcepcion.stockInsuficiente(
                    material.getCodigo(), material.getStockDisponible(), request.getCantidad());
        }

        // Obtener usuario
        Usuario usuario = usuarioRepository.buscarPorEmail(emailUsuario)
                .orElseThrow(() -> new OrdenNoEncontradaExcepcion("Usuario no encontrado: " + emailUsuario));

        Optional<MaterialUtilizado> materialExistente = materialRepository
                .buscarMaterialUtilizado(ordenId, request.getMaterialId());

        if (materialExistente.isPresent()) {
            // Si existe, sumar la cantidad
            MaterialUtilizado materialActual = materialExistente.get();
            int cantidadAnterior = materialActual.getCantidadUtilizada();
            int nuevaCantidad = cantidadAnterior + request.getCantidad();

            // Actualizar cantidad del material utilizado
            materialActual.actualizarCantidad(nuevaCantidad);

            // Reducir stock del material
            material.reducirStock(request.getCantidad());
            materialRepository.guardar(material);

            // Guardar el material utilizado actualizado
            materialRepository.guardarMaterialUtilizado(materialActual);

            log.info("Material {} actualizado en orden {}. Cantidad anterior: {}, cantidad agregada: {}, nueva cantidad: {}",
                    material.getCodigo(), ordenId, cantidadAnterior, request.getCantidad(), nuevaCantidad);

        } else {
            // Si no existe, crear nuevo registro
            // Reducir stock del material
            material.reducirStock(request.getCantidad());
            materialRepository.guardar(material);

            // Crear nuevo registro de material utilizado
            MaterialUtilizado materialUtilizado = MaterialUtilizado.crear(
                    ordenId,
                    material.getId(),
                    request.getCantidad(),
                    material.getPrecioUnitario(),
                    usuario.getId(),
                    material.getCodigo(),
                    material.getNombre(),
                    material.getUnidadMedida()
            );

        // Guardar material utilizado
        materialRepository.guardarMaterialUtilizado(materialUtilizado);

        // Agregar material a la orden
        orden.agregarMaterialUtilizado(materialUtilizado);

        log.info("Nuevo material {} agregado a orden {} (cantidad: {})",
                material.getCodigo(), ordenId, request.getCantidad());
        }
        ordenTrabajoRepository.guardar(orden);
    }

    @Override
    public List<MaterialResponse> obtenerMaterialesActivos() {
        log.info("Obteniendo todos los materiales activos");

        List<Material> materiales = materialRepository.obtenerMaterialesActivos();

        log.info("Encontrados {} materiales activos", materiales.size());

        return materiales.stream()
                .map(this::mapearMaterialAResponse)
                .toList();
    }

    @Override
    public MaterialResponse obtenerMaterialPorId(Long materialId) {
        log.info("Obteniendo material por ID: {}", materialId);

        Material material = materialRepository.buscarPorId(materialId)
                .orElseThrow(() -> MaterialNoValidoExcepcion.porId(materialId));

        return mapearMaterialAResponse(material);
    }

    @Override
    public List<MaterialUtilizadoResponse> obtenerMaterialesUtilizadosPorOrden(Long ordenId, String emailUsuario) {
        log.info("Obteniendo materiales utilizados en orden {} para usuario: {}", ordenId, emailUsuario);

        // Validar acceso a la orden
        validarAccesoOrden(ordenId, emailUsuario);

        List<MaterialUtilizado> materialesUtilizados = materialRepository.obtenerMaterialesUtilizadosPorOrden(ordenId);

        return materialesUtilizados.stream()
                .map(this::mapearMaterialUtilizadoAResponse)
                .toList();
    }

    @Override
    public boolean verificarDisponibilidadStock(Long materialId, Integer cantidad) {
        log.debug("Verificando disponibilidad de stock para material {} cantidad: {}", materialId, cantidad);

        Material material = materialRepository.buscarPorId(materialId)
                .orElse(null);

        if (material == null || !material.estaActivo()) {
            return false;
        }

        return material.tieneStockSuficiente(cantidad);
    }

    private void validarAccesoOrden(Long ordenId, String emailUsuario) {
        OrdenTrabajo orden = ordenTrabajoRepository.buscarPorId(ordenId)
                .orElseThrow(() -> OrdenNoEncontradaExcepcion.porId(ordenId));

        Usuario usuario = usuarioRepository.buscarPorEmail(emailUsuario)
                .orElseThrow(() -> new OrdenNoEncontradaExcepcion("Usuario no encontrado: " + emailUsuario));

        // Verificar acceso según el rol
        if (!usuario.esAdministrador() && !orden.estaAsignadaATecnico(usuario.getId())) {
            throw AccesoNoAutorizadoExcepcion.paraOrden(ordenId, emailUsuario);
        }
    }

    private MaterialResponse mapearMaterialAResponse(Material material) {
        return MaterialResponse.builder()
                .id(material.getId())
                .codigo(material.getCodigo())
                .nombre(material.getNombre())
                .descripcion(material.getDescripcion())
                .unidadMedida(material.getUnidadMedida())
                .precioUnitario(material.getPrecioUnitario().doubleValue())
                .stockDisponible(material.getStockDisponible())
                .activo(material.estaActivo())
                .build();
    }

    private MaterialUtilizadoResponse mapearMaterialUtilizadoAResponse(MaterialUtilizado materialUtilizado) {
        String nombreCreador = usuarioRepository.buscarPorId(materialUtilizado.getRegistradoPor())
                .map(Usuario::getNombreCompleto)
                .orElse("Usuario desconocido");

        return MaterialUtilizadoResponse.builder()
                .id(materialUtilizado.getId())
                .codigoMaterial(materialUtilizado.getCodigoMaterial())
                .nombreMaterial(materialUtilizado.getNombreMaterial())
                .cantidadUtilizada(materialUtilizado.getCantidadUtilizada())
                .unidadMedida(materialUtilizado.getUnidadMedida())
                .precioUnitario(materialUtilizado.getPrecioUnitario().doubleValue())
                .costoTotal(materialUtilizado.getCostoTotal())
                .fechaRegistro(materialUtilizado.getFechaRegistro())
                .registradoPor(nombreCreador)
                .build();
    }

    @Override
    @Transactional
    public void devolverMaterialesDeOrden(Long ordenId) {
        log.info("Devolviendo materiales de la orden cancelada: {}", ordenId);

        // Obtener todos los materiales utilizados en la orden
        List<MaterialUtilizado> materialesUtilizados = materialRepository.obtenerMaterialesUtilizadosPorOrden(ordenId);

        if (materialesUtilizados.isEmpty()) {
            log.info("No hay materiales para devolver en la orden {}", ordenId);
            return;
        }

        // Devolver cada material al stock
        for (MaterialUtilizado materialUtilizado : materialesUtilizados) {
            Material material = materialRepository.buscarPorId(materialUtilizado.getMaterialId())
                    .orElse(null);

            if (material != null) {
                // Aumentar el stock con la cantidad que se había utilizado
                material.aumentarStock(materialUtilizado.getCantidadUtilizada());
                materialRepository.guardar(material);

                log.info("Material {} devuelto al stock. Cantidad: {}, Nuevo stock: {}",
                        material.getCodigo(),
                        materialUtilizado.getCantidadUtilizada(),
                        material.getStockDisponible());
            } else {
                log.warn("Material con ID {} no encontrado para devolución", materialUtilizado.getMaterialId());
            }
        }
        try {
            materialRepository.eliminarMaterialesUtilizadosPorOrden(ordenId);
            log.info("Registros de materiales utilizados eliminados de la orden {}", ordenId);
        } catch (Exception e) {
            log.error("Error al eliminar registros de materiales utilizados de la orden {}: {}",
                    ordenId, e.getMessage());
            throw new DominioExcepcion("Error al eliminar registros de materiales: " + e.getMessage());
        }


        log.info("Materiales devueltos exitosamente para orden {}", ordenId);
    }

    @Override
    @Transactional
    public void eliminarMaterialDeOrden(Long ordenId, Long materialUtilizadoId, String emailUsuario) {
        log.info("Eliminando material utilizado {} de orden {} por usuario: {}",
                materialUtilizadoId, ordenId, emailUsuario);

        // Validar acceso a la orden
        validarAccesoOrden(ordenId, emailUsuario);

        // Obtener orden
        OrdenTrabajo orden = ordenTrabajoRepository.buscarPorId(ordenId)
                .orElseThrow(() -> OrdenNoEncontradaExcepcion.porId(ordenId));

        // Verificar que la orden esté EN_PROCESO
        if (orden.getEstado() != EstadoOrden.EN_PROCESO) {
            throw new EstadoOrdenInvalidoExcepcion(
                    String.format("Solo se pueden eliminar materiales de órdenes EN_PROCESO. Estado actual: %s",
                            orden.getEstado()));
        }

        // Buscar el material utilizado
        MaterialUtilizado materialUtilizado = materialRepository.buscarMaterialUtilizadoPorId(materialUtilizadoId)
                .orElseThrow(() -> new MaterialNoValidoExcepcion(
                        "Material utilizado no encontrado con ID: " + materialUtilizadoId));

        // Verificar que el material pertenece a esta orden
        if (!materialUtilizado.getOrdenTrabajoId().equals(ordenId)) {
            throw new AccesoNoAutorizadoExcepcion(
                    String.format("El material utilizado %d no pertenece a la orden %d",
                            materialUtilizadoId, ordenId));
        }

        // Buscar el material en el inventario para devolverle el stock
        Material material = materialRepository.buscarPorId(materialUtilizado.getMaterialId())
                .orElse(null);

        if (material != null) {
            // Devolver la cantidad al stock
            material.aumentarStock(materialUtilizado.getCantidadUtilizada());
            materialRepository.guardar(material);

            log.info("Stock devuelto al material {}. Cantidad: {}, Nuevo stock: {}",
                    material.getCodigo(),
                    materialUtilizado.getCantidadUtilizada(),
                    material.getStockDisponible());
        } else {
            log.warn("Material con ID {} no encontrado en inventario para devolución de stock",
                    materialUtilizado.getMaterialId());
        }

        // Eliminar el registro de material utilizado
        materialRepository.eliminarMaterialUtilizado(materialUtilizadoId);

        // Remover de la colección de la orden
        orden.removerMaterialUtilizado(materialUtilizado);
        ordenTrabajoRepository.guardar(orden);

        log.info("Material utilizado {} eliminado exitosamente de orden {}. Stock devuelto.",
                materialUtilizadoId, ordenId);
    }
}