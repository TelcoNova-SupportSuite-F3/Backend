package com.telconova.supportsuite.aplicacion.servicios;

import com.telconova.supportsuite.aplicacion.puertos.entrada.IAutenticacionService;
import com.telconova.supportsuite.aplicacion.puertos.salida.IUsuarioRepository;
import com.telconova.supportsuite.aplicacion.puertos.salida.ISeguridadService;
import com.telconova.supportsuite.aplicacion.dto.request.LoginRequest;
import com.telconova.supportsuite.aplicacion.dto.response.LoginResponse;
import com.telconova.supportsuite.dominio.entidades.Usuario;
import com.telconova.supportsuite.dominio.excepciones.UsuarioNoValidoExcepcion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Servicio de aplicación para operaciones de autenticación
 *
 * @author TelcoNova Development Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutenticacionService implements IAutenticacionService{

    private final IUsuarioRepository usuarioRepository;
    private final ISeguridadService seguridadService;

    @Override
    public LoginResponse iniciarSesion(LoginRequest request) {
        log.info("Iniciando proceso de autenticación para usuario: {}", request.getEmail());

        try {
            // Buscar usuario por email
            Usuario usuario = usuarioRepository.buscarPorEmail(request.getEmail())
                    .orElseThrow(() -> UsuarioNoValidoExcepcion.porCredencialesInvalidas());

            // Verificar que el usuario esté activo
            if (!usuario.estaActivo()) {
                log.warn("Intento de acceso con usuario inactivo: {}", request.getEmail());
                throw UsuarioNoValidoExcepcion.porUsuarioInactivo(request.getEmail());
            }

            // Verificar contraseña
            if (!seguridadService.verificarContrasena(request.getContrasena(), usuario.getContrasenaEncriptada())) {
                log.warn("Intento de acceso con credenciales inválidas para: {}", request.getEmail());
                throw UsuarioNoValidoExcepcion.porCredencialesInvalidas();
            }

            // Verificar que puede acceder al sistema
            if (!usuario.puedeAccederSistema()) {
                log.warn("Usuario sin permisos para acceder al sistema: {}", request.getEmail());
                throw UsuarioNoValidoExcepcion.porEmailInvalido(request.getEmail());
            }

            // Generar token JWT
            String token = seguridadService.generarTokenJwt(usuario.getEmail().getValor(), usuario.getRol().name());
            Date expiracion = seguridadService.obtenerExpiracionToken(token);

            log.info("Autenticación exitosa para usuario: {} con rol: {}",
                    request.getEmail(), usuario.getRol());

            try {
                LoginResponse response = LoginResponse.builder()
                        .token(token)
                        .tipoToken("Bearer")
                        .email(usuario.getEmail().getValor())
                        .nombreCompleto(usuario.getNombreCompleto())
                        .rol(usuario.getRol().name())
                        .expiracion(LocalDateTime.ofInstant(expiracion.toInstant(), ZoneId.systemDefault()))
                        .activo(usuario.estaActivo())
                        .build();
                log.debug("LoginResponse creado exitosamente: {}", response);
                return response;
        } catch (Exception e) {
                log.error("Error construyendo LoginResponse", e);

                // Respuesta de respaldo sin fecha de expiración para debug
                return LoginResponse.builder()
                        .token(token)
                        .tipoToken("Bearer")
                        .email(usuario.getEmail().getValor())
                        .nombreCompleto(usuario.getNombreCompleto())
                        .rol(usuario.getRol().name())
                        .expiracion(LocalDateTime.now().plusDays(1)) // Fecha fija temporal
                        .activo(usuario.estaActivo())
                        .build();
            }
        }catch (Exception e) {
            log.error("Error durante la autenticación para usuario: {}", request.getEmail(), e);
            throw e;
        }
    }

    @Override
    public boolean validarToken(String token) {
        try {
            return seguridadService.validarTokenJwt(token);
        } catch (Exception e) {
            log.warn("Token inválido o expirado: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String obtenerEmailDeToken(String token) {
        try {
            return seguridadService.extraerEmailDeToken(token);
        } catch (Exception e) {
            log.warn("No se pudo extraer email del token: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean tokenHaExpirado(String token) {
        try {
            return seguridadService.tokenHaExpirado(token);
        } catch (Exception e) {
            log.warn("Error verificando expiración del token: {}", e.getMessage());
            return true;
        }
    }

    @Override
    public String generarNuevoToken(String email) {
        log.info("Generando nuevo token para usuario: {}", email);

        Usuario usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> UsuarioNoValidoExcepcion.porEmailInvalido(email));

        if (!usuario.estaActivo()) {
            throw UsuarioNoValidoExcepcion.porUsuarioInactivo(email);
        }

        return seguridadService.generarTokenJwt(email, usuario.getRol().name());
    }
}
