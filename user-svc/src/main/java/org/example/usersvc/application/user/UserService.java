package org.example.usersvc.application.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.usersvc.application.exceptions.UserAlreadyExistsException;
import org.example.usersvc.application.exceptions.UserNotFoundException;
import org.example.usersvc.application.role.RoleService;
import org.example.usersvc.domain.entity.UserEntity;
import org.example.usersvc.infrastructure.repository.UserRepository;
import org.example.usersvc.infrastructure.security.UserPrincipal;
import org.example.usersvc.presentation.http.user.dto.UpdateUserBioRequests;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder encoder;

    @Transactional
    public UserEntity create(UserEntity entity) {
        log.info("called UserService.createUser({})", entity);

        if (userRepository.existsByEmail(entity.getEmail())) {
            throw new UserAlreadyExistsException();
        }

        if (userRepository.existsByUsername(entity.getUsername())) {
            throw new UserAlreadyExistsException();
        }

        entity.setPassword(encoder.encode(entity.getPassword()));
        entity.setRoleEntities(
                roleService.resolveDefaultRoles(entity.getRoleEntities())
        );
        return userRepository.save(entity);
    }

    public UserEntity findById(UUID id) {
        log.info("called UserService.findById({})", id);
        return userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public void deleteById(UUID id) {
        log.info("called UserService.deleteById({})", id);

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
    }

    @Transactional
    public UserEntity updateBioById(UUID id, UpdateUserBioRequests requests) {
        log.info("called UserService.updateBioById({})", id);

        UserEntity user = userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setBio(requests.bio());
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("called UserService.loadUserByUsername({})", username);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return UserPrincipal.from(
                user,
                roleService.mapAuthorities(user.getRoleEntities())
        );
    }
}
