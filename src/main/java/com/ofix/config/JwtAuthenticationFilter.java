package com.ofix.config;

import com.ofix.entity.Usuario;
import com.ofix.repository.UsuarioRepository;
import com.ofix.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioRepository usuarioRepository) {

        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("METHOD: " + request.getMethod());
        System.out.println("URL: " + request.getRequestURI());
        System.out.println("AUTH HEADER: " + request.getHeader("Authorization"));             

        String authHeader =
                request.getHeader("Authorization");
        
           

        // Si no existe Authorization o no comienza con Bearer,
        // dejamos que Spring Security continúe procesando la petición.
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            // Extraemos el email almacenado en el JWT.
            String email = jwtService.extraerEmail(token);

            // Solo autenticamos si todavía no existe una autenticación.
            if (email != null &&
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {

                Usuario usuario = usuarioRepository
                        .findByEmail(email)
                        .orElse(null);

                if (usuario != null &&
                        usuario.getActivo() &&
                        jwtService.esTokenValido(token, email)) {

                    // Convertimos el rol de Ofix en una autoridad
                    // que Spring Security pueda utilizar.
                    SimpleGrantedAuthority autoridad =
                            new SimpleGrantedAuthority(
                                    "ROLE_" + usuario.getRol().name()
                            );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    usuario,
                                    null,
                                    List.of(autoridad)
                            );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);

                    System.out.println("USUARIO AUTENTICADO: " + usuario.getEmail());
                    System.out.println("ROL AUTENTICADO: " + usuario.getRol());    
                    
                    System.out.println(
                        "AUTHORITIES: " +
                        SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getAuthorities()
                     );

                     System.out.println(
                                "AUTHENTICATED: " +
                                SecurityContextHolder.getContext()
                                        .getAuthentication()
                                        .isAuthenticated()
                     );
                }
            }

            //Token inválido
        } catch (Exception e) {
             System.out.println(
                     "ERROR JWT: " + e.getClass().getSimpleName()
                             + " - " + e.getMessage()           
             ); 

            
        }

        filterChain.doFilter(request, response);
    }
}