package com.lmj.platformserver.exception;

import com.lmj.platformserver.result.ResultCodeEnum;

import java.io.Serial;

public class LoginFailException extends BaseException {
    @Serial
    private static final long serialVersionUID = 1L;

    public LoginFailException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum);
    }
}
