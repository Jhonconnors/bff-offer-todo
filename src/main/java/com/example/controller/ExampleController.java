package com.example.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@RestController
@RequestMapping("/v1")
@Slf4j
public class ExampleController {

    @PostMapping("/send")
    public ResponseEntity<?> sendRequestExample(@RequestBody Object request) {
        // 1. Log del evento recibido
        log.info("Evento recibido: {}", request);

        // 2. Crear headers personalizados
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-logs-received", "true");
        headers.add("x-logs-processed", "true");
        headers.add("x-logs-date", new Date().toString());

        // 3. Crear la entidad HTTP que será enviada
        HttpEntity<Object> entity = new HttpEntity<>(request, headers);

        // 4. Enviar al otro microservicio
        String url = "http://localhost:7780/v1/receive"; // <-- ajusta el endpoint destino si es necesario
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response;

        try {
            response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info("Respuesta del microservicio destino: {}", response.getBody());
        } catch (Exception e) {
            log.error("Error al enviar la solicitud al microservicio destino: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar al microservicio destino");
        }

        // 5. Devolver respuesta al cliente original
        return ResponseEntity.ok("Evento procesado y enviado correctamente");
    }
}