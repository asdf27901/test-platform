package com.lmj.platformserver.vo;

import com.lmj.platformserver.assertion.AssertionResult;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ScriptExecutionResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<AssertionResult> results;
    private String logs;
    private String error;
}
