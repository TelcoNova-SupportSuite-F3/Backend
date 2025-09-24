package com.telconova.supportsuite.dominio.valueobjects;

import com.telconova.supportsuite.dominio.excepciones.DominioExcepcion;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode
public class Email {

    private static final String DOMINIO_TELCONOVA = "@telconova.com";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private final String valor;

    private Email(String valor) {
        this.valor = valor;
    }

    /**
     * Crea un nuevo Email validando el formato y dominio
     */
    public static Email de(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new DominioExcepcion("El email no puede estar vacío");
        }

        String emailLimpio = email.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(emailLimpio).matches()) {
            throw new DominioExcepcion("El formato del email no es válido");
        }

        if (!emailLimpio.endsWith(DOMINIO_TELCONOVA)) {
            throw new DominioExcepcion("Solo se permiten emails del dominio " + DOMINIO_TELCONOVA);
        }

        return new Email(emailLimpio);
    }

    /**
     * Verifica si es un email del dominio TelcoNova
     */
    public boolean esDominioTelconova() {
        return this.valor.endsWith(DOMINIO_TELCONOVA);
    }

    /**
     * Obtiene el nombre de usuario (parte antes del @)
     */
    public String getNombreUsuario() {
        return this.valor.substring(0, this.valor.indexOf('@'));
    }

    /**
     * Obtiene el dominio (parte después del @)
     */
    public String getDominio() {
        return this.valor.substring(this.valor.indexOf('@') + 1);
    }

    @Override
    public String toString() {
        return this.valor;
    }
}
