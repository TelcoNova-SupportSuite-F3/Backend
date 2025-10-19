package com.telconova.supportsuite.infraestructura.adaptadores.salida.seguridad;

import com.telconova.supportsuite.aplicacion.puertos.salida.ISeguridadService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Implementación del servicio de seguridad con JWT y BCrypt
 */
@Slf4j
@Service
public class SeguridadServiceImpl implements ISeguridadService {

    private final long jwtExpiration;
    private final PasswordEncoder passwordEncoder;
    private final SecretKey secretKey;

    public SeguridadServiceImpl(
            @Value("${security.jwt.secret-key}") String jwtSecret,
            @Value("${security.jwt.expiration}") long jwtExpiration) {
        this.jwtExpiration = jwtExpiration;
        this.passwordEncoder = new BCryptPasswordEncoder(12);
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String encriptarContrasena(String contrasenaPlana) {
        log.debug("Encriptando contraseña");
        return passwordEncoder.encode(contrasenaPlana);
    }

    @Override
    public boolean verificarContrasena(String contrasenaPlana, String contrasenaEncriptada) {
        log.debug("Verificando contraseña");
        return passwordEncoder.matches(contrasenaPlana, contrasenaEncriptada);
    }

    @Override
    public String generarTokenJwt(String email, String rol) {
        log.debug("Generando token JWT para usuario: {}", email);

        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", rol);
        claims.put("email", email);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean validarTokenJwt(String token) {
        try {
            log.debug("Validando token JWT");
            return !tokenHaExpirado(token);
        } catch (Exception e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String extraerEmailDeToken(String token) {
        log.debug("Extrayendo email del token");
        return extraerClaim(token, Claims::getSubject);
    }

    @Override
    public String extraerRolDeToken(String token) {
        log.debug("Extrayendo rol del token");
        return extraerClaim(token, claims -> claims.get("rol", String.class));
    }

    @Override
    public boolean tokenHaExpirado(String token) {
        Date expiration = extraerClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    @Override
    public Date obtenerExpiracionToken(String token) {
        return extraerClaim(token, Claims::getExpiration);
    }

    private <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodosLosClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
