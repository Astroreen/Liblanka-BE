package me.astroreen.liblanka.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.auth.dto.AuthenticationRequest;
import me.astroreen.liblanka.domain.auth.dto.AuthenticationResponse;
import me.astroreen.liblanka.domain.auth.dto.RegisterRequest;
import me.astroreen.liblanka.domain.auth.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        authenticationService.register(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.login(request));
    }
}

