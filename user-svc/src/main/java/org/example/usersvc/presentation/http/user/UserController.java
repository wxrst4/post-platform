package org.example.usersvc.presentation.http.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.usersvc.application.mapper.UserMapper;
import org.example.usersvc.application.user.UserService;
import org.example.usersvc.domain.entity.UserEntity;
import org.example.usersvc.presentation.http.user.dto.CreateUserRequest;
import org.example.usersvc.presentation.http.user.dto.UpdateUserBioRequests;
import org.example.usersvc.presentation.http.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable UUID id) {
        UserEntity user = userService.findById(id);
        return userMapper.toResponse(user);
    }

    @PutMapping("/{id}")
    public UserResponse updateBioById(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateUserBioRequests requests
    ) {
        UserEntity user = userService.updateBioById(id, requests);
        return userMapper.toResponse(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable UUID id) {
        userService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
