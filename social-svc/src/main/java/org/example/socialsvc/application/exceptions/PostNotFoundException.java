package org.example.socialsvc.application.exceptions;

import java.util.UUID;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(UUID postId) {
        super("Could not find post with id: " + postId);
    }
}
