package com.ofix.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                // CORS para Flutter Web
                .cors(cors -> {})

                // Desactivar CSRF para la API REST
                .csrf(csrf -> csrf.disable())

                // No utilizaremos sesiones
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Permitir peticiones OPTIONS de CORS
                        .requestMatchers(
                                CorsUtils::isPreFlightRequest
                        ).permitAll()

                        // Manejo de errores
                        .requestMatchers("/error").permitAll()

                        // Login público
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // Registro de usuario público
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios"
                        ).permitAll()

                        // Consultar categorías es público: el registro de
                        // profesionales las necesita antes de iniciar sesión
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categorias"
                        ).permitAll()

                        // Solo ADMIN puede eliminar usuarios
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/usuarios/**"
                        ).hasRole("ADMIN")

                        // ---- NUEVO: panel administrativo ----

                        // Todos los endpoints del panel son solo para ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Listar todos los usuarios es solo para ADMIN
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/usuarios"
                        ).hasRole("ADMIN")

                        // Categorías: consultar es para cualquier usuario con sesión,
                        // pero crear, editar y eliminar es solo para ADMIN
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/categorias/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/categorias/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/categorias/**"
                        ).hasRole("ADMIN")

                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )

                // Manejo de errores 401 y 403
                .exceptionHandling(exception ->
                        exception

                                // Usuario no autenticado
                                .authenticationEntryPoint(
                                        (request, response, authException) -> {

                                            System.out.println(
                                                    ">>> AUTHENTICATION ENTRY POINT - 401"
                                            );

                                            System.out.println(
                                                    "URL: " + request.getRequestURI()
                                            );

                                            System.out.println(
                                                    "METHOD: " + request.getMethod()
                                            );

                                            response.setStatus(
                                                    HttpStatus.UNAUTHORIZED.value()
                                            );
                                        }
                                )

                                // Usuario autenticado pero sin permisos
                                .accessDeniedHandler(
                                        (request, response, accessDeniedException) -> {

                                            System.out.println(
                                                    ">>> ACCESS DENIED - 403"
                                            );

                                            System.out.println(
                                                    "URL: " + request.getRequestURI()
                                            );

                                            System.out.println(
                                                    "METHOD: " + request.getMethod()
                                            );

                                            System.out.println(
                                                    "ERROR: "
                                                    + accessDeniedException.getMessage()
                                            );

                                            response.setStatus(
                                                    HttpStatus.FORBIDDEN.value()
                                            );
                                        }
                                )
                )

                // Filtro JWT antes del filtro de autenticación
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configuración CORS para Flutter Web
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        // Permitir Flutter en localhost y, para pruebas desde el celular.
        
        configuration.setAllowedOriginPatterns(
                List.of(
                        "http://localhost:*",
                        "http://127.0.0.1:*",
                        "http://192.168.*:*",
                        "http://10.*:*",
                        "http://172.*:*"
                )
        );        


        // Métodos permitidos
        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        // Headers permitidos
        configuration.setAllowedHeaders(
                List.of("*")
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}