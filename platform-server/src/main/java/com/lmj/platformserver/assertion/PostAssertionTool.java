package com.lmj.platformserver.assertion;

import org.graalvm.polyglot.HostAccess;

import java.util.List;
import java.util.Map;

public class PostAssertionTool extends AbstractAssertionTool {

    // 这里使用public是为了让js引擎能够获取到
    @HostAccess.Export
    public final Map<String, Object> response;

    public PostAssertionTool(Map<String, Object> response, List<Map<String, Object>> variables) {
        super(variables);
        this.response = response;
    }
}
