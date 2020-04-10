package com.project.omega.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class IndustryNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public IndustryNotFoundException(String message) {
        super(message);
    }
}
