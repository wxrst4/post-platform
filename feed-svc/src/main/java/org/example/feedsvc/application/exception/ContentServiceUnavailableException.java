package org.example.feedsvc.application.exception;

public class ContentServiceUnavailableException extends RuntimeException {
    public ContentServiceUnavailableException(Throwable cause) {
        super("Content service is unavailable", cause);
    }
}
