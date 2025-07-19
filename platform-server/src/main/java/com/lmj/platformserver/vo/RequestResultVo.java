package com.lmj.platformserver.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
public class RequestResultVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Long interfaceId;
    @JsonIgnore
    private Long testcaseId;
    private Map<String, Object> response;
    @JsonIgnore
    private Map<String, Object> request;
    private ScriptExecutionResultVo postExecutionResult;
    private ScriptExecutionResultVo preExecutionResult;
}
