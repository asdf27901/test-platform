package com.lmj.platformserver.assertion;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.graalvm.polyglot.HostAccess;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class EnvironmentTool {

    // 需要区分一下情况
    // 如果variables为null，那么不涉及环境变量操作
    // 只要variables不为null，那么就可以进行增删改查
    private List<Map<String, Object>> variables;

    @HostAccess.Export
    public void set(String key, Object value) {
        if (variables != null && key != null) {

            // 需要先找key是否已经存在
            Map<String, Object> vo = variables.stream().filter(v -> key.equals(v.get("key"))).findFirst().orElse(null);
            if (vo == null) {
                if (value instanceof Map<?,?> || value instanceof List<?> || value == null) {
                    variables.add(Map.of("key", key, "value", JSON.parse(JSON.toJSONString(value)), "type", "object", "remark", ""));
                } else if (value instanceof Number) {
                    variables.add(Map.of("key", key, "value", value, "type", "number", "remark", ""));
                } else if (value instanceof Boolean) {
                    variables.add(Map.of("key", key, "value", value, "type", "boolean", "remark", ""));
                } else {
                    variables.add(Map.of("key", key, "value", value, "type", "string", "remark", ""));
                }
            } else {
                if (value instanceof Map<?,?> || value instanceof List<?> || value == null) {
                    vo.put("value", JSON.parse(JSON.toJSONString(value)));
                    vo.put("type", "object");
                } else {
                    vo.put("value", value);
                    if (value instanceof Number) {
                        vo.put("type", "number");
                    } else if (value instanceof Boolean) {
                        vo.put("type", "boolean");
                    } else {
                        vo.put("type", "string");
                    }
                }

            }

        }
    }

    @HostAccess.Export
    public void clear() {
        if (!CollectionUtils.isEmpty(variables)) {
            variables.clear();
        }
    }

    @HostAccess.Export
    public Object get(String key) {
        if (!CollectionUtils.isEmpty(variables) && key != null) {
            Map<String, Object> vo = variables.stream().filter(v -> key.equals(v.get("key"))).findFirst().orElse(null);
            if (vo != null) {
                return vo.get("value");
            }
        }
        return null;
    }

    @HostAccess.Export
    public void del(String key) {
        if (!CollectionUtils.isEmpty(variables) && key != null) {
            variables.removeIf(v -> key.equals(v.get("key")));
        }
    }
}
