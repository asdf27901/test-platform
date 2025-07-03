package com.lmj.platformserver.assertion;

import com.alibaba.fastjson2.JSON;
import org.graalvm.polyglot.HostAccess;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAssertionTool implements Assertion{

    private final List<AssertionResult> results = new ArrayList<>();

    @Override
    @HostAccess.Export
    public Expectation expect(Object o) {
        return new Expectation(o);
    }

    @Override
    public void addTestResult(AssertionResult assertionResult) {
        this.results.add(assertionResult);
    }

    public List<AssertionResult> getResults() {
        return results;
    }

    @Override
    @HostAccess.Export
    public String toString() {
        return JSON.toJSONString(this);
    }
}
