package com.lmj.platformserver.service;

import com.lmj.platformserver.assertion.AbstractAssertionTool;
import com.lmj.platformserver.vo.ScriptExecutionResultVo;

public interface JsScriptExecutionService {

    ScriptExecutionResultVo executeJsScript(String scriptText, AbstractAssertionTool assertion);
}
