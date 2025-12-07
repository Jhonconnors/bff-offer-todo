package com.example.config;

import com.example.config.auth.IpFilter;
import com.example.config.auth.IpRateFilter;
import com.example.config.auth.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;


@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private IpFilter ipFilter;

    @Autowired
    private IpRateFilter ipRateFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())        // <<---- habilita CORS usando el bean corsConfigurationSource()
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // permitir preflight OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/web/token").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // tu orden de filtros (ipFilter primero)
        http.addFilterBefore(ipRateFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(ipFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Bean que define las reglas CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Origen permitido: tu Frontend local
        config.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));

        // Métodos permitidos (incluye OPTIONS)
        config.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));

        // Headers que tu frontend puede enviar
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));

        // Headers que el navegador podrá leer desde la respuesta (si hace falta)
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Si tu frontend envía cookies o Authorization con credenciales -> true
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // aplicar a todas las rutas
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

