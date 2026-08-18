package org.example.socialsvc.infrastructure.exceptions;

import org.example.socialsvc.application.exceptions.*;
import org.example.socialsvc.presentation.common.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<Response<Void>> handleCommentNotFound(
            CommentNotFoundException exception
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "COMMENT_NOT_FOUND",
                exception.getMessage()
        );
    }

    @ExceptionHandler(LikeAlreadyExistsException.class)
    public ResponseEntity<Response<Void>> handleLikeAlreadyExists(
            LikeAlreadyExistsException exception
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "LIKE_ALREADY_EXISTS",
                exception.getMessage()
        );
    }

    @ExceptionHandler(SubscriptionAlreadyExistsException.class)
    public ResponseEntity<Response<Void>> handleSubscriptionAlreadyExists(
            SubscriptionAlreadyExistsException exception
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "SUBSCRIPTION_ALREADY_EXISTS",
                exception.getMessage()
        );
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Response<Void>> handleSecurityException(
            SecurityException exception
    ) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() +
                                ": " +
                                error.getDefaultMessage()
                )
                .findFirst()
                .orElse("Validation failed");

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                message
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Void>> handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                exception.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleException(
            Exception exception
    ) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Internal server error"
        );
    }

    private ResponseEntity<Response<Void>> buildErrorResponse(
            HttpStatus status,
            String code,
            String message
    ) {
        Response<Void> response = Response.<Void>builder()
                .data(null)
                .error(
                        new Response.ErrorDetails(
                                code,
                                message
                        )
                )
                .httpStatusCode(status.value())
                .build();

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<Response<Void>> handlePostNotFound(
            PostNotFoundException exception
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "POST_NOT_FOUND",
                exception.getMessage()
        );
    }

    @ExceptionHandler(ChannelNotFoundException.class)
    public ResponseEntity<Response<Void>> handleChannelNotFound(
            ChannelNotFoundException exception
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "CHANNEL_NOT_FOUND",
                exception.getMessage()
        );
    }

    @ExceptionHandler(ContentServiceUnavailableException.class)
    public ResponseEntity<Response<Void>>
    handleContentServiceUnavailable(
            ContentServiceUnavailableException exception
    ) {
        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "CONTENT_SERVICE_UNAVAILABLE",
                exception.getMessage()
        );
    }
}