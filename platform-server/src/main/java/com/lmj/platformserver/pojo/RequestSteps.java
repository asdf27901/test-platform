package com.lmj.platformserver.pojo;

import com.lmj.platformserver.vo.ScriptExecutionResultVo;
import lombok.Data;

import java.util.Map;

@Data
public class RequestSteps {

    private Long interfaceId;
    private String interfaceName;
    private Long testcaseId;
    private String testcaseName;
    private Byte executeResult;
    private String errorMsg;
    private Map<String, Object> requestData;
    private Map<String, Object> responseData;
    private ScriptExecutionResultVo preScriptData;
    private ScriptExecutionResultVo postScriptData;

}
