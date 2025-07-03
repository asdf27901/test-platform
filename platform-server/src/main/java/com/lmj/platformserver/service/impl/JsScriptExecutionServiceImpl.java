package com.lmj.platformserver.service.impl;

import com.lmj.platformserver.assertion.AbstractAssertionTool;
import com.lmj.platformserver.service.JsScriptExecutionService;
import com.lmj.platformserver.vo.ScriptExecutionResultVo;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.graalvm.polyglot.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Service
public class JsScriptExecutionServiceImpl implements JsScriptExecutionService {

    @Autowired
    private Engine engine;

    @Override
    @SneakyThrows
    public ScriptExecutionResultVo executeJsScript(String scriptText, AbstractAssertionTool assertion) {

        ScriptExecutionResultVo resultVo = new ScriptExecutionResultVo();
        // 用于捕获console.log的输出
        @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 用于捕获console.err的输出
        @Cleanup ByteArrayOutputStream err = new ByteArrayOutputStream();

        try {
            @Cleanup Context context = Context.newBuilder("js")
                    .engine(engine)
                    .out(out)
                    .err(err)
                    .allowHostAccess(
                            HostAccess.newBuilder()
                                    .allowAccessAnnotatedBy(HostAccess.Export.class)
                                    .allowArrayAccess(true)
                                    .allowMapAccess(true)
                                    .allowListAccess(true)
                                    .allowAccessInheritance(true)
                                    // 明确允许JS函数实现指定的Java函数式接口
                                    .allowImplementations(Runnable.class)
                                    .build()
                    )
                    .allowHostClassLookup(className -> false)
                    .build();


            // 将 lt 对象注入到JS的全局作用域
            context.getBindings("js").putMember("lt", assertion);

            // 执行脚本
            context.eval("js", scriptText);
            resultVo.setResults(assertion.getResults());

        } catch (PolyglotException e) {
            resultVo.setError(e.getMessage());
        } finally {
            String logs = out.toString(StandardCharsets.UTF_8);
            String errors = err.toString(StandardCharsets.UTF_8);
            resultVo.setLogs(logs + (errors.isEmpty() ? "" : "\n--- Errors ---\n" + errors));
        }

        return resultVo;
    }
}
