package org.example.usersvc.presentation.http.role;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.usersvc.application.mapper.RoleMapper;
import org.example.usersvc.application.role.RoleService;
import org.example.usersvc.domain.entity.RoleEntity;
import org.example.usersvc.presentation.http.role.dto.CreateRoleRequest;
import org.example.usersvc.presentation.http.role.dto.AssignRoleRequest;
import org.example.usersvc.presentation.http.role.dto.RoleResponse;
import org.example.usersvc.presentation.http.user.dto.UserResponse;
import org.example.usersvc.application.mapper.UserMapper;
import org.example.usersvc.domain.entity.UserEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;

    @PostMapping
    public RoleResponse create(
            @RequestBody @Valid CreateRoleRequest request
    ) {
        RoleEntity roleToCreate = roleMapper.toEntity(request);
        RoleEntity role = roleService.create(roleToCreate);
        return roleMapper.toResponse(role);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        roleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign")
    public UserResponse assignRoleToUser(
            @RequestBody @Valid AssignRoleRequest request
    ) {
        UserEntity user = roleService.assignRoleToUser(request.userId(), request.roleId());
        return userMapper.toResponse(user);
    }
}
