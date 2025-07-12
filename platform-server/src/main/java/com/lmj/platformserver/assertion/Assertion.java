package com.lmj.platformserver.assertion;

public interface Assertion {

    void test(String testcaseName, Runnable callback);

    Expectation expect(Object o);
}
