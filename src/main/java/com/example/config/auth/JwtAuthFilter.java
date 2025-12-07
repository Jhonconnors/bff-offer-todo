package com.example.config.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private PublicKeyCacheService publicKeyCacheService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        String endpoint = request.getRequestURI();

        if (endpoint.contains("web/token")) {
            filterChain.doFilter(request, response);
            return;
        }
        // 1. Obtener el header Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. Validar que exista y empiece con "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token (quitar la palabra "Bearer ")
        String token = authHeader.substring(7);

        try {
            // 4. Obtener la llave pública del servicio de caché
            RSAPublicKey publicKey = publicKeyCacheService.getPublicKey();

            // 5. Parsear y validar el token usando la llave
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 6. Extraer los permisos (scope)
            String scope = claims.get("scope", String.class);

            // 7. Convertir permisos de String a lista de autoridades
            List<SimpleGrantedAuthority> authorities =
                    Arrays.stream(scope.split(" "))
                            .map(SimpleGrantedAuthority::new)
                            .toList();

            // 8. Crear la autenticación en el contexto de seguridad
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            claims.getSubject(), null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ex) {
            // Si algo falla (token expirado, firma falsa, etc.)
            publicKeyCacheService.publicKeyClean(); // Limpiamos caché por si la llave rotó
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid JWT");
            return;
        }

        // 9. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
