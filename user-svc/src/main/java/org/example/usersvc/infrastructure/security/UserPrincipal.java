package org.example.usersvc.infrastructure.security;

import org.example.usersvc.domain.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

public record UserPrincipal(
        UUID id,
        String username,
        String password,
        Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

    public static UserPrincipal from(
            UserEntity user,
            Collection<? extends GrantedAuthority> authorities
    ) {
        return new UserPrincipal
                (
                        user.getId(),
                        user.getUsername(),
                        user.getPassword(),
                        authorities
                );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
