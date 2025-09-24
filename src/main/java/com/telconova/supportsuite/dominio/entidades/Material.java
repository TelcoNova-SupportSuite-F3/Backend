package com.telconova.supportsuite.dominio.entidades;

import com.telconova.supportsuite.dominio.excepciones.MaterialNoValidoExcepcion;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un Material
 *
 * @author TelcoNova Development Team
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Material {

    @EqualsAndHashCode.Include
    private Long id;

    private String codigo;
    private String nombre;
    private String descripcion;
    private String unidadMedida;
    private BigDecimal precioUnitario;
    private Integer stockDisponible;
    private boolean activo;
    private LocalDateTime fechaCreacion;

    /**
     * Crea un nuevo material
     */
    public static Material crear(String codigo, String nombre, String descripcion,
                                 String unidadMedida, BigDecimal precioUnitario, Integer stockInicial) {
        validarDatosCreacion(codigo, nombre, unidadMedida);

        return Material.builder()
                .codigo(codigo.trim().toUpperCase())
                .nombre(nombre.trim())
                .descripcion(descripcion != null ? descripcion.trim() : null)
                .unidadMedida(unidadMedida.trim())
                .precioUnitario(precioUnitario != null ? precioUnitario.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .stockDisponible(stockInicial != null ? stockInicial : 0)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    /**
     * Verifica si hay stock suficiente
     */
    public boolean tieneStockSuficiente(int cantidadSolicitada) {
        return this.stockDisponible >= cantidadSolicitada;
    }

    /**
     * Reduce el stock disponible
     */
    public void reducirStock(int cantidad) {
        if (cantidad <= 0) {
            throw MaterialNoValidoExcepcion.cantidadInvalida(cantidad);
        }

        if (!tieneStockSuficiente(cantidad)) {
            throw MaterialNoValidoExcepcion.stockInsuficiente(
                    this.codigo, this.stockDisponible, cantidad);
        }

        this.stockDisponible -= cantidad;
    }

    /**
     * Aumenta el stock disponible
     */
    public void aumentarStock(int cantidad) {
        if (cantidad <= 0) {
            throw MaterialNoValidoExcepcion.cantidadInvalida(cantidad);
        }

        this.stockDisponible += cantidad;
    }

    /**
     * Actualiza el precio unitario
     */
    public void actualizarPrecio(BigDecimal nuevoPrecio) {
        if (nuevoPrecio == null || nuevoPrecio.compareTo(BigDecimal.ZERO) < 0) {
            throw new MaterialNoValidoExcepcion("El precio no puede ser negativo");
        }

        this.precioUnitario = nuevoPrecio.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Desactiva el material
     */
    public void desactivar() {
        this.activo = false;
    }

    /**
     * Activa el material
     */
    public void activar() {
        this.activo = true;
    }

    /**
     * Verifica si el material está activo
     */
    public boolean estaActivo() {
        return this.activo;
    }

    /**
     * Verifica si tiene stock disponible
     */
    public boolean tieneStock() {
        return this.stockDisponible > 0;
    }

    /**
     * Calcula el valor total del stock
     */
    public BigDecimal getValorTotalStock() {
        return this.precioUnitario.multiply(BigDecimal.valueOf(this.stockDisponible));
    }

    /**
     * Obtiene información resumida del material
     */
    public String getInformacionResumida() {
        return String.format("%s - %s (%s) - Stock: %d %s",
                this.codigo, this.nombre,
                this.precioUnitario.toString(),
                this.stockDisponible, this.unidadMedida);
    }

    private static void validarDatosCreacion(String codigo, String nombre, String unidadMedida) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new MaterialNoValidoExcepcion("El código del material es obligatorio");
        }

        if (codigo.trim().length() > 50) {
            throw new MaterialNoValidoExcepcion("El código no puede exceder 50 caracteres");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MaterialNoValidoExcepcion("El nombre del material es obligatorio");
        }

        if (nombre.trim().length() > 255) {
            throw new MaterialNoValidoExcepcion("El nombre no puede exceder 255 caracteres");
        }

        if (unidadMedida == null || unidadMedida.trim().isEmpty()) {
            throw new MaterialNoValidoExcepcion("La unidad de medida es obligatoria");
        }

        if (unidadMedida.trim().length() > 50) {
            throw new MaterialNoValidoExcepcion("La unidad de medida no puede exceder 50 caracteres");
        }
    }

    @Override
    public String toString() {
        return String.format("Material{id=%d, codigo='%s', nombre='%s', stock=%d, activo=%s}",
                id, codigo, nombre, stockDisponible, activo);
    }
}
