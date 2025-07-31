package com.lmj.platformserver.exception;

import com.lmj.platformserver.result.ResultCodeEnum;

import java.io.Serial;

public class InterfaceTestcaseErrorException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InterfaceTestcaseErrorException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum);
    }

    public InterfaceTestcaseErrorException(String message) {
        super(message);
    }
}
