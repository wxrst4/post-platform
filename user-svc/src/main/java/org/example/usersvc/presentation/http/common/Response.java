package org.example.usersvc.presentation.http.common;

import lombok.*;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Response<T> {
    private T data;
    private ErrorDetails error;

    private int httpStatusCode;

    public record ErrorDetails(
            String code,
            String message
    ) {
    }

    public static <T> Response<T> of(T data) {
        return new Response<>(data, null, HttpStatus.OK.value());
    }
}