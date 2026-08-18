package org.example.feedsvc.infrastructure.exception;


import org.example.feedsvc.application.exception.ContentServiceUnavailableException;
import org.example.feedsvc.application.exception.SocialServiceUnavailableException;
import org.example.feedsvc.presentation.common.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ContentServiceUnavailableException.class)
    public ResponseEntity<Response<Void>>
    handleContentServiceUnavailable(ContentServiceUnavailableException exception) {
        return error(
                HttpStatus.SERVICE_UNAVAILABLE,
                "CONTENT_SERVICE_UNAVAILABLE",
                exception.getMessage()
        );
    }

    @ExceptionHandler(SocialServiceUnavailableException.class)
    public ResponseEntity<Response<Void>>
    handleSocialServiceUnavailable(SocialServiceUnavailableException exception) {
        return error(
                HttpStatus.SERVICE_UNAVAILABLE,
                "SOCIAL_SERVICE_UNAVAILABLE",
                exception.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Void>>
    handleIllegalArgument(IllegalArgumentException exception) {
        return error(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                exception.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>>
    handleException(Exception exception) {
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Internal server error"
        );
    }

    private ResponseEntity<Response<Void>> error(
            HttpStatus status,
            String code,
            String message
    ) {
        Response<Void> response = Response.<Void>builder()
                .data(null)
                .error(new Response.ErrorDetails(code, message))
                .httpStatusCode(status.value())
                .build();

        return ResponseEntity.status(status).body(response);
    }
}