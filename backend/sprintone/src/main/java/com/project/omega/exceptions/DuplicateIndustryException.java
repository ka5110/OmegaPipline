package com.project.omega.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class DuplicateIndustryException extends Exception {
    private static final long serialVersionUID = 1L;

    public DuplicateIndustryException(String message) {
        super(message);
    }
}
