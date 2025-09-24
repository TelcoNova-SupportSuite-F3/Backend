package com.telconova.supportsuite.aplicacion.servicios;

import com.telconova.supportsuite.aplicacion.puertos.entrada.IMaterialService;
import com.telconova.supportsuite.aplicacion.puertos.salida.IMaterialRepository;
import com.telconova.supportsuite.aplicacion.puertos.salida.IOrdenTrabajoRepository;
import com.telconova.supportsuite.aplicacion.puertos.salida.IUsuarioRepository;
import com.telconova.supportsuite.aplicacion.dto.request.AgregarMaterialRequest;
import com.telconova.supportsuite.aplicacion.dto.response.MaterialResponse;
import com.telconova.supportsuite.aplicacion.dto.response.MaterialUtilizadoResponse;
import com.telconova.supportsuite.dominio.entidades.Material;
import com.telconova.supportsuite.dominio.entidades.MaterialUtilizado;
import com.telconova.supportsuite.dominio.entidades.OrdenTrabajo;
import com.telconova.supportsuite.dominio.entidades.Usuario;
import com.telconova.supportsuite.dominio.excepciones.MaterialNoValidoExcepcion;
import com.telconova.supportsuite.dominio.excepciones.OrdenNoEncontradaExcepcion;
import com.telconova.supportsuite.dominio.excepciones.AccesoNoAutorizadoExcepcion;
import com.telconova.supportsuite.dominio.excepciones.EstadoOrdenInvalidoExcepcion;
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
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
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
}