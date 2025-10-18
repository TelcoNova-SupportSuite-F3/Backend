package com.telconova.supportsuite.infraestructura.configuracion;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuración de Spring Security - SIN duplicación CORS
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    private String admin = "ADMIN";
    private String tecnico = "TECNICO";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF deshabilitado: Seguro en este contexto porque:
                // 1. Usamos JWT (autenticación stateless sin cookies de sesión)
                // 2. SessionCreationPolicy.STATELESS (no hay sesiones del lado del servidor)
                // 3. Las APIs REST sin estado no son vulnerables a ataques CSRF
                // 4. Los tokens JWT se envían en headers Authorization, no en cookies
                .csrf(AbstractHttpConfigurer::disable) // NOSONAR - JWT stateless API, CSRF no aplicable
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth

                        // Endpoints públicos (Swagger y Auth)
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/validate").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // Swagger UI (sin /api/v1)
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        // Endpoints de órdenes (con /api/v1)
                        .requestMatchers(HttpMethod.GET, "/api/v1/ordenes/mis-ordenes").hasRole(tecnico)
                        .requestMatchers(HttpMethod.GET, "/api/v1/ordenes/todas").hasRole(admin)
                        .requestMatchers(HttpMethod.GET, "/api/v1/ordenes/{id}").hasAnyRole(tecnico, admin)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/ordenes/{id}/estado").hasRole(tecnico)
                        .requestMatchers(HttpMethod.POST, "/api/v1/ordenes/{id}/finalizar").hasRole(tecnico)

                        // Endpoints de evidencias (con /api/v1)
                        .requestMatchers(HttpMethod.POST, "/api/v1/ordenes/{id}/evidencias/**").hasRole(tecnico)
                        .requestMatchers(HttpMethod.GET, "/api/v1/ordenes/{id}/evidencias").hasAnyRole(tecnico, admin)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/evidencias/{id}").hasAnyRole(tecnico, admin)

                        // Endpoints de materiales (con /api/v1)
                        .requestMatchers(HttpMethod.GET, "/api/v1/materiales/buscar").hasRole(tecnico)
                        .requestMatchers(HttpMethod.POST, "/api/v1/ordenes/{id}/materiales").hasRole(tecnico)
                        .requestMatchers(HttpMethod.GET, "/api/v1/materiales").hasAnyRole(tecnico, admin)
                        .requestMatchers(HttpMethod.POST, "/api/v1/materiales").hasRole(admin)

                        // Métricas (solo para admins)
                        .requestMatchers("/actuator/**").hasRole(admin)

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
