package org.example.socialsvc.application.exceptions;

public class ContentServiceUnavailableException extends RuntimeException {
    public ContentServiceUnavailableException(Throwable cause) {
        super("Content Service is unavailable", cause);
    }
}
