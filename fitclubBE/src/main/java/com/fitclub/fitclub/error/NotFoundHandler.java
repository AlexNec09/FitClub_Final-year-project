package com.fitclub.fitclub.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundHandler extends RuntimeException {
    public NotFoundHandler(String message) {
        super(message);
    }
}
