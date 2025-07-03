package com.lmj.platformserver.assertion;

import java.io.Serial;

public class AssertionErrorException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 1L;

    public AssertionErrorException(String message) {
        super(message);
    }
}
