package com.telconova.supportsuite.dominio.valueobjects;

import com.telconova.supportsuite.dominio.excepciones.DominioExcepcion;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode
public class Telefono {

    // Patrones para validación de teléfonos colombianos
    private static final Pattern PATRON_MOVIL = Pattern.compile("^\\+573\\d{9}$");
    private static final Pattern PATRON_FIJO = Pattern.compile("^\\+57[1-9]\\d{7}$");
    private static final Pattern PATRON_FIJO_MEDELLIN = Pattern.compile("^\\+574\\d{7}$");

    private final String valor;

    private Telefono(String valor) {
        this.valor = valor;
    }

    /**
     * Crea un teléfono validando el formato para Colombia
     */
    public static Telefono de(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new DominioExcepcion("El teléfono no puede estar vacío");
        }

        String telefonoLimpio = telefono.trim();

        // Normalizar el formato removiendo espacios extra para validación
        String telefonoParaValidacion = telefonoLimpio.replaceAll("\\s+", "");

        if (!esValidoParaColombia(telefonoParaValidacion)) {
            throw new DominioExcepcion("El formato del teléfono no es válido para Colombia. " +
                    "Formatos válidos: +573XXXXXXXXX (móvil), +57XXXXXXXX (fijo)");
        }
        return new Telefono(telefonoLimpio);
    }

    /**
     * Crea un teléfono sin validación estricta (para datos legacy)
     */
    public static Telefono deSinValidacion(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return null;
        }
        return new Telefono(telefono.trim());
    }

    /**
     * Valida si el teléfono es válido para Colombia
     */
    private static boolean esValidoParaColombia(String telefono) {
        return PATRON_MOVIL.matcher(telefono).matches() ||
                PATRON_FIJO.matcher(telefono).matches() ||
                PATRON_FIJO_MEDELLIN.matcher(telefono).matches();
    }

    /**
     * Verifica si es un número móvil
     */
    public boolean esMovil() {
        return PATRON_MOVIL.matcher(this.valor.replaceAll("\\s+", "")).matches();
    }

    /**
     * Verifica si es un número fijo
     */
    public boolean esFijo() {
        String normalizado = this.valor.replaceAll("\\s+", "");
        return PATRON_FIJO.matcher(normalizado).matches() ||
                PATRON_FIJO_MEDELLIN.matcher(normalizado).matches();
    }

    /**
     * Obtiene el número sin formato
     */
    public String getSinFormato() {
        return this.valor.replaceAll("[\\s\\-\\(\\)]", "");
    }

    /**
     * Obtiene el número formateado para mostrar
     */
    public String getFormateado() {
        String sinFormato = getSinFormato();
        if (sinFormato.startsWith("+573") && sinFormato.length() == 13) {
            // Móvil: +57 3XX XXX XXXX
            return String.format("+57 %s %s %s",
                    sinFormato.substring(3, 6),
                    sinFormato.substring(6, 9),
                    sinFormato.substring(9));
        } else if (sinFormato.startsWith("+57") && sinFormato.length() == 11) {
            // Fijo: +57 X XXX XXXX
            return String.format("+57 %s %s %s",
                    sinFormato.substring(3, 4),
                    sinFormato.substring(4, 7),
                    sinFormato.substring(7));
        }
        return this.valor; // Retornar original si no se puede formatear
    }

    @Override
    public String toString() {
        return this.valor;
    }
}
