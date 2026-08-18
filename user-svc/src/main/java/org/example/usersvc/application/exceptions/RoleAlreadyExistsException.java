package org.example.usersvc.application.exceptions;

public class RoleAlreadyExistsException extends RuntimeException {
    public RoleAlreadyExistsException(String name) {
        super("Role already exists: " + name);
    }
}
