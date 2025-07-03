package com.lmj.platformserver.assertion;

import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;

public interface Assertion {

    @HostAccess.Export
    default void test(String testcaseName, Runnable callback) {
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

    @HostAccess.Export
    Expectation expect(Object o);

    void addTestResult(AssertionResult assertionResult);
}
