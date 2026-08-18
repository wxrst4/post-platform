package org.example.usersvc.presentation.http.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.usersvc.application.auth.AuthService;
import org.example.usersvc.presentation.http.auth.dto.LoginUserRequest;
import org.example.usersvc.presentation.http.auth.dto.RefreshTokenRequest;
import org.example.usersvc.presentation.http.auth.dto.RegisterUserRequest;
import org.example.usersvc.presentation.http.auth.dto.TokenResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public TokenResponse register(
            @RequestBody @Valid RegisterUserRequest request
    ) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(
            @RequestBody @Valid LoginUserRequest request
    ) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(
            @RequestBody @Valid RefreshTokenRequest request
    ) {
        return authService.refresh(request);
    }
}
