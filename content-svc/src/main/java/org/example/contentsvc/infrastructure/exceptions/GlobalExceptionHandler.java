package org.example.contentsvc.infrastructure.exceptions;

import org.example.contentsvc.application.exceptions.ChannelAlreadyExistsException;
import org.example.contentsvc.application.exceptions.ChannelNotFoundException;
import org.example.contentsvc.application.exceptions.PostNotFoundException;
import org.example.contentsvc.presentation.common.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ChannelNotFoundException.class)
    public ResponseEntity<Response<?>> handleChannelNotFound(ChannelNotFoundException exception) {
        var wrapped = errorResponse(
                "channel_not_found",
                exception.getMessage(),
                HttpStatus.NOT_FOUND
        );

        return ResponseEntity.status(wrapped.getHttpStatusCode()).body(wrapped);
    }

    @ExceptionHandler(ChannelAlreadyExistsException.class)
    public ResponseEntity<Response<?>> handleChannelAlreadyExists(ChannelAlreadyExistsException exception) {
        var wrapped = errorResponse(
                "channel_already_exists",
                exception.getMessage(),
                HttpStatus.CONFLICT
        );

        return ResponseEntity
                .status(wrapped.getHttpStatusCode())
                .body(wrapped);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<Response<?>> handlePostNotFound(PostNotFoundException exception) {
        var wrapped = errorResponse(
                "post_not_found",
                exception.getMessage(),
                HttpStatus.NOT_FOUND
        );

        return ResponseEntity.status(wrapped.getHttpStatusCode()).body(wrapped);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<?>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error ->
                        error.getField()
                                + ": "
                                + error.getDefaultMessage()
                )
                .orElse("Request validation failed");

        var wrapped = errorResponse(
                "validation_error",
                message,
                HttpStatus.BAD_REQUEST
        );

        return ResponseEntity.badRequest().body(wrapped);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Response<?>> handleSecurityException(SecurityException exception) {
        var wrapped = errorResponse(
                "access_denied",
                exception.getMessage(),
                HttpStatus.FORBIDDEN
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(wrapped);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Response<?>> handleIllegalState(IllegalStateException exception) {
        var wrapped = errorResponse(
                "invalid_operation",
                exception.getMessage(),
                HttpStatus.CONFLICT
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(wrapped);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<?>> handleInternalError(Exception exception) {
        var wrapped = errorResponse(
                "internal_error",
                exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(wrapped);
    }

    private Response<?> errorResponse(String code, String message, HttpStatus status) {
        return Response.builder()
                .error(new Response.ErrorDetails(
                                code,
                                message
                        )
                )
                .httpStatusCode(status.value())
                .build();
    }
}
