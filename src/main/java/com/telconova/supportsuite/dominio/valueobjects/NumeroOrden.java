package com.telconova.supportsuite.dominio.valueobjects;

import com.telconova.supportsuite.dominio.excepciones.DominioExcepcion;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode
public class NumeroOrden {


    private static final Pattern PATRON_NUMERO_ORDEN = Pattern.compile("^ORD-\\d{4}-\\d{3,6}$");
    private static final DateTimeFormatter FORMATO_ANIO = DateTimeFormatter.ofPattern("yyyy");

    private final String valor;

    private NumeroOrden(String valor) {
        this.valor = valor;
    }

    /**
     * Crea un número de orden validando el formato
     */
    public static NumeroOrden de(String numeroOrden) {
        if (numeroOrden == null || numeroOrden.trim().isEmpty()) {
            throw new DominioExcepcion("El número de orden no puede estar vacío");
        }

        String numeroLimpio = numeroOrden.trim().toUpperCase();

        if (!PATRON_NUMERO_ORDEN.matcher(numeroLimpio).matches()) {
            throw new DominioExcepcion("El formato del número de orden no es válido. Debe ser: ORD-YYYY-XXX");
        }

        return new NumeroOrden(numeroLimpio);
    }

    /**
     * Genera un nuevo número de orden automáticamente
     */
    public static NumeroOrden generar(int secuencial) {
        if (secuencial <= 0) {
            throw new DominioExcepcion("El secuencial debe ser mayor a cero");
        }

        String anioActual = LocalDateTime.now().format(FORMATO_ANIO);
        String numeroFormateado = String.format("ORD-%s-%03d", anioActual, secuencial);

        return new NumeroOrden(numeroFormateado);
    }

    /**
     * Obtiene el año del número de orden
     */
    public String getAnio() {
        return this.valor.substring(4, 8);
    }

    /**
     * Obtiene el secuencial del número de orden
     */
    public int getSecuencial() {
        String secuencialStr = this.valor.substring(9);
        return Integer.parseInt(secuencialStr);
    }

    /**
     * Verifica si es del año actual
     */
    public boolean esDelAnioActual() {
        String anioActual = LocalDateTime.now().format(FORMATO_ANIO);
        return getAnio().equals(anioActual);
    }

    @Override
    public String toString() {
        return this.valor;
    }
}
