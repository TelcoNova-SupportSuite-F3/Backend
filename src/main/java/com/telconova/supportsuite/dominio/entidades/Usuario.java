package com.telconova.supportsuite.dominio.entidades;

import com.telconova.supportsuite.dominio.enums.RolUsuario;
import com.telconova.supportsuite.dominio.valueobjects.Email;
import com.telconova.supportsuite.dominio.excepciones.DominioExcepcion;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {


    @EqualsAndHashCode.Include
    private Long id;

    private Email email;
    private String contrasenaEncriptada;
    private String nombreCompleto;
    private RolUsuario rol;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    /**
     * Constructor para crear un nuevo usuario
     */
    public static Usuario crear(Email email, String contrasenaEncriptada, String nombreCompleto, RolUsuario rol) {
        validarDatosCreacion(email, contrasenaEncriptada, nombreCompleto, rol);

        return Usuario.builder()
                .email(email)
                .contrasenaEncriptada(contrasenaEncriptada)
                .nombreCompleto(nombreCompleto.trim())
                .rol(rol)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    /**
     * Verifica si el usuario es un técnico
     */
    public boolean esTecnico() {
        return RolUsuario.TECNICO.equals(this.rol);
    }

    /**
     * Verifica si el usuario es un administrador
     */
    public boolean esAdministrador() {
        return RolUsuario.ADMIN.equals(this.rol);
    }

    /**
     * Verifica si el usuario está activo
     */
    public boolean estaActivo() {
        return this.activo;
    }

    /**
     * Desactiva el usuario
     */
    public void desactivar() {
        if (!this.activo) {
            throw new DominioExcepcion("El usuario ya está desactivado");
        }
        this.activo = false;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Activa el usuario
     */
    public void activar() {
        if (this.activo) {
            throw new DominioExcepcion("El usuario ya está activado");
        }
        this.activo = true;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Actualiza el nombre completo del usuario
     */
    public void actualizarNombreCompleto(String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            throw new DominioExcepcion("El nombre completo no puede estar vacío");
        }

        if (nuevoNombre.trim().length() > 255) {
            throw new DominioExcepcion("El nombre completo no puede exceder 255 caracteres");
        }

        this.nombreCompleto = nuevoNombre.trim();
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Actualiza la contraseña del usuario
     */
    public void actualizarContrasena(String nuevaContrasenaEncriptada) {
        if (nuevaContrasenaEncriptada == null || nuevaContrasenaEncriptada.trim().isEmpty()) {
            throw new DominioExcepcion("La contraseña encriptada no puede estar vacía");
        }

        this.contrasenaEncriptada = nuevaContrasenaEncriptada;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Verifica si puede acceder al sistema basándose en las reglas de negocio
     */
    public boolean puedeAccederSistema() {
        return this.activo && this.email != null && this.email.esDominioTelconova();
    }

    /**
     * Obtiene información básica del usuario como String
     */
    public String getInformacionBasica() {
        return String.format("%s (%s) - %s",
                this.nombreCompleto,
                this.email.getValor(),
                this.rol.name()
        );
    }

    private static void validarDatosCreacion(Email email, String contrasenaEncriptada,
                                             String nombreCompleto, RolUsuario rol) {
        if (email == null) {
            throw new DominioExcepcion("El email es obligatorio");
        }

        if (contrasenaEncriptada == null || contrasenaEncriptada.trim().isEmpty()) {
            throw new DominioExcepcion("La contraseña encriptada es obligatoria");
        }

        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            throw new DominioExcepcion("El nombre completo es obligatorio");
        }

        if (nombreCompleto.trim().length() > 255) {
            throw new DominioExcepcion("El nombre completo no puede exceder 255 caracteres");
        }

        if (rol == null) {
            throw new DominioExcepcion("El rol es obligatorio");
        }

        if (!email.esDominioTelconova()) {
            throw new DominioExcepcion("Solo se permiten emails del dominio @telconova.com");
        }
    }

    @Override
    public String toString() {
        return String.format("Usuario{id=%d, email='%s', nombre='%s', rol=%s, activo=%s}",
                id,
                email != null ? email.getValor() : "null",
                nombreCompleto,
                rol,
                activo
        );
    }
}
