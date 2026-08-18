package org.example.socialsvc.application.exceptions;

import java.util.UUID;

public class LikeAlreadyExistsException extends RuntimeException {
    public LikeAlreadyExistsException(UUID userId, UUID postId) {
        super("User " + userId + " already liked post " + postId);
    }
}
