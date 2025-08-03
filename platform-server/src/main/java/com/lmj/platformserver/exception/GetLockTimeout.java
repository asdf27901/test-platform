package com.lmj.platformserver.exception;

import com.lmj.platformserver.result.ResultCodeEnum;

import java.io.Serial;

public class GetLockTimeout extends BaseException{
    @Serial
    private static final long serialVersionUID = 1L;

    public GetLockTimeout(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum);
    }

    public GetLockTimeout(String message) {
        super(message);
    }
}
