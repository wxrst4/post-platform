package org.example.usersvc.application.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.usersvc.application.mapper.AuthMapper;
import org.example.usersvc.application.user.UserService;
import org.example.usersvc.domain.entity.UserEntity;
import org.example.usersvc.infrastructure.security.JwtService;
import org.example.usersvc.infrastructure.security.UserPrincipal;
import org.example.usersvc.presentation.http.auth.dto.LoginUserRequest;
import org.example.usersvc.presentation.http.auth.dto.RefreshTokenRequest;
import org.example.usersvc.presentation.http.auth.dto.RegisterUserRequest;
import org.example.usersvc.presentation.http.auth.dto.TokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final AuthenticationManager authenticationManager;

    public TokenResponse register(RegisterUserRequest request) {
        UserEntity user = authMapper.toEntity(request);
        UserEntity createdUser = userService.create(user);

        UserPrincipal userPrincipal = (UserPrincipal) userService.loadUserByUsername(createdUser.getUsername());

        return generateToken(userPrincipal);
    }

    public TokenResponse login(LoginUserRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        return generateToken(userPrincipal);
    }

    public TokenResponse refresh(RefreshTokenRequest request){
        String username = jwtService.extractUsername(request.refreshToken());
        UserPrincipal userPrincipal = (UserPrincipal) userService.loadUserByUsername(username);


        if (!jwtService.isValid(request.refreshToken(), userPrincipal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_refresh_token");
        }

        return generateToken(userPrincipal);
    }

    private TokenResponse generateToken(UserPrincipal userPrincipal) {
        String accessToken = jwtService.generateAccessToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal);

        return new TokenResponse(accessToken, refreshToken);
    }
}
