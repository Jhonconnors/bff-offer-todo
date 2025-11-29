package com.example.controller;

import com.example.exception.LoginFailedException;
import com.example.model.token.server.LoginRequest;
import com.example.model.token.server.LoginResponse;
import com.example.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@Slf4j
public class LogginController {

    private final LoginService loginService;

    public LogginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest body) {
        try {
            if (body.getUsername() == null || body.getPassword() == null) {
                throw new LoginFailedException("Username or password missing");
            }

            return ResponseEntity.ok(loginService.generateLogin(body));

        } catch (Exception ex) {
            log.error("Login error", ex);
            throw new LoginFailedException("Error processing login request");
        }
    }
}
