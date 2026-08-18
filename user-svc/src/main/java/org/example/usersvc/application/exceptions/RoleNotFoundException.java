package org.example.usersvc.application.exceptions;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(Long id) {
        super("Role not found with id: " + id);
    }
}
