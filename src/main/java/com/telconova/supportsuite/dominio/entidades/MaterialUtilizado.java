package com.telconova.supportsuite.dominio.entidades;

import com.telconova.supportsuite.dominio.excepciones.MaterialNoValidoExcepcion;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa Material Utilizado en una orden
 *
 * @author TelcoNova Development Team
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MaterialUtilizado {

    @EqualsAndHashCode.Include
    private Long id;

    private Long ordenTrabajoId;
    private Long materialId;
    private Integer cantidadUtilizada;
    private BigDecimal precioUnitario;
    private LocalDateTime fechaRegistro;
    private Long registradoPor;

    // Información desnormalizada del material para auditoría
    private String codigoMaterial;
    private String nombreMaterial;
    private String unidadMedida;

    /**
     * Crea un registro de material utilizado
     */
    public static MaterialUtilizado crear(Long ordenTrabajoId, Long materialId, Integer cantidad,
                                          BigDecimal precioUnitario, Long usuarioId,
                                          String codigoMaterial, String nombreMaterial, String unidadMedida) {
        validarDatos(ordenTrabajoId, materialId, cantidad, usuarioId);

        return MaterialUtilizado.builder()
                .ordenTrabajoId(ordenTrabajoId)
                .materialId(materialId)
                .cantidadUtilizada(cantidad)
                .precioUnitario(precioUnitario != null ? precioUnitario : BigDecimal.ZERO)
                .fechaRegistro(LocalDateTime.now())
                .registradoPor(usuarioId)
                .codigoMaterial(codigoMaterial)
                .nombreMaterial(nombreMaterial)
                .unidadMedida(unidadMedida)
                .build();
    }

    /**
     * Incrementa la cantidad utilizada
     */
    public void incrementarCantidad(Integer cantidadAdicional) {
        if (cantidadAdicional <= 0) {
            throw MaterialNoValidoExcepcion.cantidadInvalida(cantidadAdicional);
        }

        this.cantidadUtilizada += cantidadAdicional;
    }

    /**
     * Actualiza la cantidad utilizada del material
     */
    public void actualizarCantidad(int nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        this.cantidadUtilizada = nuevaCantidad;
        this.fechaRegistro = LocalDateTime.now(); // Actualizar fecha para tracking
    }

    /**
     * Calcula el costo total del material utilizado
     */
    public double getCostoTotal() {
        if (this.precioUnitario == null || this.cantidadUtilizada == null) {
            return 0.0;
        }

        return this.precioUnitario.doubleValue() * this.cantidadUtilizada;
    }

    /**
     * Obtiene información resumida del material utilizado
     */
    public String getInformacionResumida() {
        return String.format("%s - %s: %d %s (Total: $%.2f)",
                this.codigoMaterial, this.nombreMaterial,
                this.cantidadUtilizada, this.unidadMedida,
                getCostoTotal());
    }

    private static void validarDatos(Long ordenTrabajoId, Long materialId, Integer cantidad, Long usuarioId) {
        if (ordenTrabajoId == null) {
            throw new MaterialNoValidoExcepcion("El ID de la orden de trabajo es obligatorio");
        }

        if (materialId == null) {
            throw new MaterialNoValidoExcepcion("El ID del material es obligatorio");
        }

        if (cantidad == null || cantidad <= 0) {
            throw MaterialNoValidoExcepcion.cantidadInvalida(cantidad != null ? cantidad : 0);
        }

        if (usuarioId == null) {
            throw new MaterialNoValidoExcepcion("El ID del usuario que registra es obligatorio");
        }
    }

    @Override
    public String toString() {
        return String.format("MaterialUtilizado{id=%d, material=%s, cantidad=%d, orden=%d}",
                id, codigoMaterial, cantidadUtilizada, ordenTrabajoId);
    }
}
