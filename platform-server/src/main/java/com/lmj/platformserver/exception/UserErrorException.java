package com.lmj.platformserver.exception;

import com.lmj.platformserver.result.ResultCodeEnum;

import java.io.Serial;

public class UserErrorException extends BaseException{
    @Serial
    private static final long serialVersionUID = 1L;

    public UserErrorException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum);
    }
}
