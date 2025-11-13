package org.example.microservicedevice.handlers.exceptions.model;

import org.springframework.http.HttpStatus;
import java.util.ArrayList;

public class BadRequestException extends CustomException {
    private static final String MESSAGE = "Bad request!";
    private static final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

    public BadRequestException(String resource) {
        super(MESSAGE, httpStatus, resource, new ArrayList<>());
    }
}
