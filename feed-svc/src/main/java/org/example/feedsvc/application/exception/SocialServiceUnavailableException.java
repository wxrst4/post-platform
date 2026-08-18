package org.example.feedsvc.application.exception;

public class SocialServiceUnavailableException extends RuntimeException {
    public SocialServiceUnavailableException(Throwable cause) {
        super("Social service is unavailable", cause);
    }
}
