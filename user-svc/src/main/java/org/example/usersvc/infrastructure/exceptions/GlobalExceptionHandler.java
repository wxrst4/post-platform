package org.example.usersvc.infrastructure.exceptions;

import org.example.usersvc.application.exceptions.RoleAlreadyExistsException;
import org.example.usersvc.application.exceptions.RoleNotFoundException;
import org.example.usersvc.application.exceptions.UserAlreadyExistsException;
import org.example.usersvc.application.exceptions.UserNotFoundException;
import org.example.usersvc.presentation.http.common.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Response<?>> handleUserNotFound(UserNotFoundException exception) {
        var wrapped = errorResponse("user_not_found", exception.getMessage(), HttpStatus.NOT_FOUND);

        return ResponseEntity.status(wrapped.getHttpStatusCode()).body(wrapped);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Response<?>> handleUserAlreadyExists(UserAlreadyExistsException exception) {
        var wrapped = errorResponse("user_already_exists", exception.getMessage(), HttpStatus.CONFLICT);

        return ResponseEntity.status(wrapped.getHttpStatusCode()).body(wrapped);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<Response<?>> handleRoleNotFound(RoleNotFoundException exception) {
        var wrapped = errorResponse("role_not_found", exception.getMessage(), HttpStatus.NOT_FOUND);

        return ResponseEntity.status(wrapped.getHttpStatusCode()).body(wrapped);
    }

    @ExceptionHandler(RoleAlreadyExistsException.class)
    public ResponseEntity<Response<?>> handleRoleAlreadyExists(RoleAlreadyExistsException exception) {
        var wrapped = errorResponse("role_already_exists", exception.getMessage(), HttpStatus.CONFLICT);

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
