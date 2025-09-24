package com.telconova.supportsuite.compartido.utilidades;

import com.telconova.supportsuite.compartido.constantes.ConfiguracionConstantes;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Utilidades para validaciones
 */

public class ValidacionUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern TELEFONO_PATTERN = Pattern.compile(
            "^(\\+57\\s?)?(3[0-9]{2}|[2-8][0-9])\\s?[0-9]{3}\\s?[0-9]{4}$"
    );

    private static final Pattern NUMERO_ORDEN_PATTERN = Pattern.compile(
            "^ORD-\\d{4}-\\d{3,6}$"
    );

    private ValidacionUtil() {
        // Utility class
    }

    /**
     * Valida formato de email
     */
    public static boolean esEmailValido(String email) {
        return StringUtils.isNotBlank(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Valida que el email sea del dominio TelcoNova
     */
    public static boolean esEmailTelconova(String email) {
        return esEmailValido(email) &&
                email.toLowerCase().endsWith(ConfiguracionConstantes.DOMINIO_EMAIL_PERMITIDO);
    }

    /**
     * Valida formato de teléfono colombiano
     */
    public static boolean esTelefonoValido(String telefono) {
        return StringUtils.isNotBlank(telefono) && TELEFONO_PATTERN.matcher(telefono).matches();
    }

    /**
     * Valida formato de número de orden
     */
    public static boolean esNumeroOrdenValido(String numeroOrden) {
        return StringUtils.isNotBlank(numeroOrden) &&
                NUMERO_ORDEN_PATTERN.matcher(numeroOrden).matches();
    }

    /**
     * Valida longitud mínima
     */
    public static boolean cumpleLongitudMinima(String texto, int longitudMinima) {
        return StringUtils.isNotBlank(texto) && texto.trim().length() >= longitudMinima;
    }

    /**
     * Valida longitud máxima
     */
    public static boolean cumpleLongitudMaxima(String texto, int longitudMaxima) {
        return texto == null || texto.length() <= longitudMaxima;
    }

    /**
     * Valida que un texto no esté vacío
     */
    public static boolean noEstaVacio(String texto) {
        return StringUtils.isNotBlank(texto);
    }

    /**
     * Valida que un número sea positivo
     */
    public static boolean esPositivo(Number numero) {
        return numero != null && numero.doubleValue() > 0;
    }

    /**
     * Valida que un número esté en un rango
     */
    public static boolean estaEnRango(Number numero, Number minimo, Number maximo) {
        if (numero == null || minimo == null || maximo == null) {
            return false;
        }
        double valor = numero.doubleValue();
        return valor >= minimo.doubleValue() && valor <= maximo.doubleValue();
    }

    /**
     * Valida longitud de comentario
     */
    public static boolean esComentarioValido(String comentario) {
        return noEstaVacio(comentario) &&
                cumpleLongitudMaxima(comentario, ConfiguracionConstantes.LONGITUD_MAXIMA_COMENTARIO);
    }

    /**
     * Valida código de material
     */
    public static boolean esCodigoMaterialValido(String codigo) {
        return noEstaVacio(codigo) &&
                cumpleLongitudMaxima(codigo, 50) &&
                codigo.matches("^[A-Z0-9\\-]+$"); // Solo letras mayúsculas, números y guiones
    }

    /**
     * Limpia y normaliza texto
     */
    public static String limpiarTexto(String texto) {
        if (StringUtils.isBlank(texto)) {
            return null;
        }
        return texto.trim().replaceAll("\\s+", " ");
    }

    /**
     * Normaliza email a minúsculas
     */
    public static String normalizarEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    /**
     * Valida que un ID sea válido
     */
    public static boolean esIdValido(Long id) {
        return id != null && id > 0;
    }

    /**
     * Valida cantidad de material
     */
    public static boolean esCantidadValida(Integer cantidad) {
        return cantidad != null && cantidad > 0;
    }
}
