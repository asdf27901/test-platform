package com.lmj.platformserver.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
public class RequestResultVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Map<String, Object> response;

    private ScriptExecutionResultVo postExecutionResult;
    private ScriptExecutionResultVo preExecutionResult;
}
