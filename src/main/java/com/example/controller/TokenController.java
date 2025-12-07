package com.example.controller;

import com.example.config.auth.model.TokenResponse;
import com.example.exception.InvalidJwtException;
import com.example.service.TokenRelayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class TokenController {
    private final TokenRelayService tokenRelayService;

    public TokenController(TokenRelayService tokenRelayService) {
        this.tokenRelayService = tokenRelayService;
    }

    @PostMapping("/web/token")
    public ResponseEntity<TokenResponse> exchangeClientJwt(
            @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidJwtException("Missing Authorization Bearer header");
        }

        String clientJwt = authorizationHeader.substring("Bearer ".length());

        TokenResponse signedJwt = tokenRelayService.requestTokenToTokenServer(clientJwt);

        return ResponseEntity.ok(signedJwt);
    }
}
