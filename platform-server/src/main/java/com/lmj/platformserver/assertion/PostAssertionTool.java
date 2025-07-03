package com.lmj.platformserver.assertion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.graalvm.polyglot.HostAccess;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class PostAssertionTool extends AbstractAssertionTool {

    // 这里使用public是为了让js引擎能够获取到
    @HostAccess.Export
    public Map<String, Object> response;
}
