package com.example.config.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class InputSanitizationFilter extends OncePerRequestFilter {

    // Patrón estricto para URI/path (permitir letras, dígitos, guiones, barras y puntos)
    private static final Pattern STRICT_PATH_PATTERN =
            Pattern.compile("^[\\p{L}\\p{N}\\-_/\\.]*$");

    // Patrón estricto para valores por defecto (muy conservador)
    private static final Pattern STRICT_VALUE_PATTERN =
            Pattern.compile("^[\\p{L}\\p{N} _\\-\\.,@]*$");

    // Patrón relajado para endpoints que permiten espacios, acentos y algunos signos (pero NO <> {} \" ; etc)
    // Permite letras Unicode (\p{L}), números (\p{N}), espacios (\s) y signos comunes: - . , @ ( ) / % + & : ! ? ¿ ¡ :
    private static final Pattern RELAXED_VALUE_PATTERN =
            Pattern.compile("^[\\p{L}\\p{N}\\s\\-\\.,@()/%+&:!?¿¡]*$");

    // Endpoints que queremos permitir con validación relajada para sus parámetros
    private static final Set<String> RELAXED_ENDPOINT_PREFIXES = Set.of(
            "/v1/supermarkets/product",
            "/v1/farmacy/medicament/product",
            "/auth/web/token",
            "/auth/login"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();           // ruta: /v1/...
        String query = request.getQueryString();        // raw query string (opcional)

        // 1) Validar path con patrón estricto
        if (!isSafePath(uri)) {
            respondPreconditionFailed(response, "Invalid characters in path");
            return;
        }

        // 2) Decidir si usar patrón relajado para parámetros (si la URI empieza con alguno de los prefijos)
        boolean relaxed = isRelaxedEndpoint(uri);

        // 3) Validar parámetros (usamos request.getParameterMap() para valores ya decodificados)
        Map<String, String[]> params = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String name = entry.getKey();

            // validar nombre de parámetro con patrón estricto (no debe contener chars raros)
            if (!isSafeParamName(name)) {
                respondPreconditionFailed(response, "Invalid characters in parameter name: " + name);
                return;
            }

            String[] values = entry.getValue();
            if (values == null) continue;
            for (String val : values) {
                if (val == null) continue;
                boolean ok = relaxed ? isSafeRelaxedValue(val) : isSafeStrictValue(val);
                if (!ok) {
                    respondPreconditionFailed(response, "Invalid characters in parameter value: " + name);
                    return;
                }
            }
        }

        // 4) También validar raw query por seguridad (por si hubiera encoding malformado)
        if (query != null && !query.isEmpty()) {
            // permitimos los caracteres permitidos en query en general (verificamos que no contenga < > { } etc)
            if (containsForbiddenChars(query)) {
                respondPreconditionFailed(response, "Invalid characters in query string");
                return;
            }
        }

        // Si todo OK, continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    private boolean isRelaxedEndpoint(String uri) {
        // si el URI comienza con cualquiera de los prefijos (match simple)
        for (String p : RELAXED_ENDPOINT_PREFIXES) {
            if (uri.startsWith(p)) return true;
        }
        return false;
    }

    private boolean isSafePath(String path) {
        if (path == null) return false;
        return STRICT_PATH_PATTERN.matcher(path).matches();
    }

    private boolean isSafeParamName(String name) {
        if (name == null) return false;
        // nombres sencillos de params: letras, números, guion, underscore
        return name.matches("^[\\p{L}\\p{N}_\\-]+$");
    }

    private boolean isSafeStrictValue(String value) {
        return STRICT_VALUE_PATTERN.matcher(value).matches();
    }

    private boolean isSafeRelaxedValue(String value) {
        return RELAXED_VALUE_PATTERN.matcher(value).matches();
    }

    private boolean containsForbiddenChars(String text) {
        if (text == null) return false;
        // caracteres que consideramos peligrosos en raw query/url
        // (puedes agregar más si lo deseas)
        String forbidden = "<>{}\";`|\\^$*";
        for (char c : forbidden.toCharArray()) {
            if (text.indexOf(c) >= 0) return true;
        }
        return false;
    }

    private void respondPreconditionFailed(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED); // 412
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().write(msg);
    }
}