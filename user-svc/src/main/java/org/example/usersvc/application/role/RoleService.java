package org.example.usersvc.application.role;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.usersvc.application.exceptions.RoleAlreadyExistsException;
import org.example.usersvc.application.exceptions.RoleNotFoundException;
import org.example.usersvc.application.exceptions.UserNotFoundException;
import org.example.usersvc.domain.entity.RoleEntity;
import org.example.usersvc.domain.entity.UserEntity;
import org.example.usersvc.infrastructure.repository.RoleRepository;
import org.example.usersvc.infrastructure.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private static final String DEFAULT_ROLE_NAME = "ROLE_USER";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Transactional
    public RoleEntity create(RoleEntity entity) {
        log.info("called RoleService.create({})", entity);
        String normalizedRoleName = normalizeRoleName(entity.getName());

        if (roleRepository.existsByName(normalizedRoleName)) {
            throw new RoleAlreadyExistsException(normalizedRoleName);
        }

        entity.setName(normalizedRoleName);
        return roleRepository.save(entity);
    }

    @Transactional
    public void deleteById(Long id) {
        log.info("called RoleService.deleteById({})", id);
        if (!roleRepository.existsById(id)) {
            throw new RoleNotFoundException(id);
        }

        roleRepository.deleteUserRolesByRoleId(id);
        roleRepository.deleteById(id);
    }

    public List<RoleEntity> resolveDefaultRoles(List<RoleEntity> roleEntities) {
        if (roleEntities != null && !roleEntities.isEmpty()) {
            return roleEntities;
        }

        return List.of(getOrCreateRole(DEFAULT_ROLE_NAME));
    }

    public List<GrantedAuthority> mapAuthorities(List<RoleEntity> roleEntities) {
        if (roleEntities == null || roleEntities.isEmpty()) {
            return List.of();
        }

        return roleEntities.stream()
                .map(this::toGrantedAuthority)
                .toList();
    }

    @Transactional
    public UserEntity assignRoleToUser(UUID userId, Long roleId) {
        log.info("called RoleService.assignRoleToUser({}, {})", userId, roleId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));

        List<RoleEntity> roles = user.getRoleEntities() == null
                ? new ArrayList<>()
                : new ArrayList<>(user.getRoleEntities());

        boolean alreadyAssigned = roles.stream()
                .anyMatch(existingRole -> existingRole.getId() != null
                        && existingRole.getId().equals(role.getId()));

        if (!alreadyAssigned) {
            roles.add(role);
            user.setRoleEntities(roles);
        }

        return userRepository.save(user);
    }

    private GrantedAuthority toGrantedAuthority(RoleEntity role) {
        return new SimpleGrantedAuthority(normalizeRoleName(role.getName()));
    }

    private RoleEntity getOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(
                        RoleEntity.builder()
                                .name(roleName)
                                .build()
                ));
    }

    private String normalizeRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return DEFAULT_ROLE_NAME;
        }

        String trimmedRoleName = roleName.trim();
        return trimmedRoleName.startsWith("ROLE_") ? trimmedRoleName : "ROLE_" + trimmedRoleName;
    }
}
