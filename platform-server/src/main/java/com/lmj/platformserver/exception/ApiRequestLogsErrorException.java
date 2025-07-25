package com.lmj.platformserver.exception;

import com.lmj.platformserver.result.ResultCodeEnum;

import java.io.Serial;

public class ApiRequestLogsErrorException extends BaseException{
    @Serial
    private static final long serialVersionUID = 1L;

    public ApiRequestLogsErrorException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum);
    }
}
