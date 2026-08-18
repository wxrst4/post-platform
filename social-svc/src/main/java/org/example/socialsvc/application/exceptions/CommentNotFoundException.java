package org.example.socialsvc.application.exceptions;

import java.util.UUID;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(UUID commentId) {
        super("Comment with id: " + commentId + " not found");
    }
}
