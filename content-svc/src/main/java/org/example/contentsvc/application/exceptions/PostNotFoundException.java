package org.example.contentsvc.application.exceptions;

import java.util.UUID;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(UUID id) {
        super("Post with id: " + id + " not found");
    }
}
