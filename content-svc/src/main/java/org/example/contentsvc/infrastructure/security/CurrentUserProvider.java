package org.example.contentsvc.infrastructure.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class CurrentUserProvider {

    public UUID getUserId() {
        UserPrincipal principal =
                (UserPrincipal) Objects.requireNonNull(
                        SecurityContextHolder.getContext().getAuthentication()
                ).getPrincipal();

        return Objects.requireNonNull(principal).getUserId();
    }
}
