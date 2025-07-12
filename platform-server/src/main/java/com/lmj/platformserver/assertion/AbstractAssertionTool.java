package com.lmj.platformserver.assertion;

import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractAssertionTool implements Assertion{

    private final List<AssertionResult> results = new ArrayList<>();

    @HostAccess.Export
    public final EnvironmentTool environment;

    public AbstractAssertionTool(List<Map<String, Object>> variables) {
        this.environment = new EnvironmentTool(variables);
    }

    @Override
    @HostAccess.Export
    public void test(String testcaseName, Runnable callback) {
        try {
            callback.run();
            // 没有报错就说明断言成功了
            this.addTestResult(new AssertionResult(testcaseName, Boolean.TRUE, null));
        } catch (PolyglotException e) {
            // js引擎抛出的异常
            if (e.isHostException() && e.asHostException() instanceof AssertionErrorException) {
                this.addTestResult(new AssertionResult(testcaseName, Boolean.FALSE, e.getMessage()));
            } else {
                // 非断言异常直接抛出给上层js引擎捕获，不再需要处理断言结果
                throw e;
            }
        } catch (Exception e) {
            // java 抛出的异常
            throw new RuntimeException();
        }
    }

    public void addTestResult(AssertionResult assertionResult) {
        this.results.add(assertionResult);
    }

    @Override
    @HostAccess.Export
    public Expectation expect(Object o) {
        return new Expectation(o);
    }

    public List<AssertionResult> getResults() {
        return results;
    }
}
