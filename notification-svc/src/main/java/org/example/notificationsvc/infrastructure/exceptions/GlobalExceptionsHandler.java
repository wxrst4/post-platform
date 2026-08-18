package org.example.notificationsvc.infrastructure.exceptions;

import org.example.notificationsvc.application.exceptions.NotificationNotFoundException;
import org.example.notificationsvc.presentation.http.common.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionsHandler {

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<Response<?>> handleNotificationNotFound(NotificationNotFoundException exception) {
        var wrapped = errorResponse("notification_not_found", exception.getMessage(), HttpStatus.NOT_FOUND);

        return ResponseEntity.status(wrapped.getHttpStatusCode()).body(wrapped);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<?>> handleValidation(MethodArgumentNotValidException exception) {
        var message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Request validation failed");
        var wrapped = errorResponse("validation_error", message, HttpStatus.BAD_REQUEST);

        return ResponseEntity.badRequest().body(wrapped);
    }

    public Response<?> wrapInternalServerErrors(Exception exception) {
        return errorResponse("internal_error", exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Response<?>> handleResponseStatus(ResponseStatusException exception) {
        var status = HttpStatus.valueOf(exception.getStatusCode().value());
        var message = exception.getReason() != null ? exception.getReason() : status.getReasonPhrase();
        var wrapped = errorResponse("request_error", message, status);

        return ResponseEntity.status(wrapped.getHttpStatusCode()).body(wrapped);
    }

    @ExceptionHandler
    public ResponseEntity<Response<?>> handleInternalErrors(Exception exception) {
        var wrapped = wrapInternalServerErrors(exception);
        return ResponseEntity.status(wrapped.getHttpStatusCode()).body(wrapped);
    }

    private Response<?> errorResponse(String code, String message, HttpStatus status) {
        return Response.builder()
                .error(new Response.ErrorDetails(code, message))
                .httpStatusCode(status.value())
                .build();
    }
}
