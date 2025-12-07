package com.example.config.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class IpRateFilter extends OncePerRequestFilter {

    private final ConcurrentMap<String, Long> lastCall = new ConcurrentHashMap<>();
    private final long windowMillis = 1_200L;      // 1.2s
    private final long penaltySeconds = 15L;       // Retry-After sugerido

    private String key(HttpServletRequest req) {
        String ip = extractClientIp(req);
        String path = req.getRequestURI();
        return ip + "#" + path;
    }

    private String extractClientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            // si hay varios, el primero es el real
            return xf.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, jakarta.servlet.ServletException {

        String k = key(request);
        long now = System.currentTimeMillis();
        Long prev = lastCall.get(k);

        if (prev != null && (now - prev) < windowMillis) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(penaltySeconds));
            response.getWriter().write("Rate limit: intenta nuevamente en " + penaltySeconds + " segundos.");
            return; // corta la cadena y no pasa al resto de filtros ni controllers
        }

        lastCall.put(k, now);
        filterChain.doFilter(request, response);
    }
}