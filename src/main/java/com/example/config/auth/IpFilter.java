package com.example.config.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

@Component
public class IpFilter extends OncePerRequestFilter {

    private final IpSecurityProperties ipProperties;
    private final Set<String> allowedNormalized = new HashSet<>();

    public IpFilter(IpSecurityProperties ipProperties) {
        this.ipProperties = ipProperties;
        // normalizar la lista configurada una sola vez
        if (ipProperties.getAllowedIps() != null) {
            for (String raw : ipProperties.getAllowedIps()) {
                String n = normalizeIpOrHostname(raw.trim());
                if (n != null && !n.isEmpty()) allowedNormalized.add(n);
            }
        }
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws IOException, ServletException {

        String ip = extractClientIp(request);

        // ya normalizado: comparar contra la lista normalizada
        if (!allowedNormalized.contains(ip)) {
            System.out.println("IP Invalida : " + ip);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("IP not allowed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae la IP cliente priorizando X-Forwarded-For y normaliza:
     * - ::1 o 0:0:... -> 127.0.0.1
     * - ::ffff:127.0.0.1 -> 127.0.0.1
     * - si viene lista en X-Forwarded-For, toma la primera (left-most)
     */
    private String extractClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        String ip = null;
        if (xff != null && !xff.isBlank()) {
            // X-Forwarded-For puede contener "client, proxy1, proxy2"
            ip = xff.split(",")[0].trim();
        } else {
            ip = req.getRemoteAddr();
        }
        return normalizeIpAddress(ip);
    }

    /**
     * Normaliza una IP cruda: maps IPv6 loopback y IPv4-mapped -> IPv4 literal.
     * Intenta devolver la dirección en forma canónica IPv4 o IPv6 (lo más simple).
     */
    private String normalizeIpAddress(String ip) {
        if (ip == null) return "";
        ip = ip.trim();

        // forma expandida de loopback que a veces aparece en logs
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        // IPv4-mapped IPv6 (::ffff:127.0.0.1)
        if (ip.startsWith("::ffff:")) {
            return ip.substring(7);
        }

        // quitar port si accidentalmente llegó "127.0.0.1:12345"
        int colonPos = ip.lastIndexOf(':');
        if (colonPos > 0 && ip.indexOf(']') == -1 && ip.chars().filter(ch -> ch == ':').count() == 1) {
            // simple heurística: si hay un único ':' y no es IPv6 con []
            String maybeIp = ip;
            String after = ip.substring(colonPos + 1);
            if (after.matches("\\d+")) {
                try {
                    maybeIp = ip.substring(0, colonPos);
                    InetAddress.getByName(maybeIp); // valida
                    return maybeIp;
                } catch (Exception ignored) { }
            }
        }

        // intentar resolución a forma canónica
        try {
            InetAddress addr = InetAddress.getByName(ip);
            String canonical = addr.getHostAddress();
            // quitar zona si IPv6 link-local como fe80::1%lo0
            int percent = canonical.indexOf('%');
            if (percent > 0) canonical = canonical.substring(0, percent);
            // mapear ::1 a 127.0.0.1 por consistencia si lo deseas
            if ("::1".equals(canonical)) return "127.0.0.1";
            if (canonical.startsWith("::ffff:")) return canonical.substring(7);
            return canonical;
        } catch (Exception e) {
            // si algo falla, devolvemos el string original (no normalizado)
            return ip;
        }
    }

    /**
     * Normaliza el valor configurado en properties (puede ser hostname o ip).
     * Si es hostname, intenta resolver y obtener hostAddress; si falla, devuelve raw.
     */
    private String normalizeIpOrHostname(String raw) {
        if (raw == null || raw.isBlank()) return raw;
        // admitir wildcard "localhost" -> resolver
        try {
            InetAddress addr = InetAddress.getByName(raw);
            String canonical = addr.getHostAddress();
            if ("::1".equals(canonical)) return "127.0.0.1";
            if (canonical.startsWith("::ffff:")) return canonical.substring(7);
            return canonical;
        } catch (Exception e) {
            // no se pudo resolver -> dejar el raw
            return raw;
        }
    }
}
