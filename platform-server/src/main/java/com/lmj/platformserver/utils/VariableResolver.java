package com.lmj.platformserver.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson2.JSON;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class VariableResolver {

    private static final String REPLACE_REGEX = "\\{\\{([\\w-]+)\\}\\}";

    private static final int STACK_MAX_DEPTH = 100;

    private static boolean neededReplace(String template) {
        return ReUtil.contains(REPLACE_REGEX, template);
    }

    private static List<String> getVariableNameList(String template) {
        return ReUtil.findAllGroup1(REPLACE_REGEX, template);
    }

    private static String replaceString(String template, String regex, Object target) {
        if (target instanceof Map<?, ?> m) {
            return ReUtil.replaceAll(template, regex, JSON.toJSONString(m));
        } else if (target instanceof List<?> l) {
            StringJoiner sj = new StringJoiner(",");
            l.forEach(v -> sj.add(v.toString()));
            return ReUtil.replaceAll(template, regex, sj.toString());
        } else {
            return ReUtil.replaceAll(template, regex, String.valueOf(target));
        }
    }

    public static Object resolve(List<Map<String, Object>> v, Map<String, Object> data) {
        if (CollectionUtils.isEmpty(v) || MapUtil.isEmpty(data)) {
            // 当变量为空，或者数据为空时直接不处理
            return data;
        }

        // 将List转为Map效率更高
        Map<String, Object> variables = new HashMap<>();
        v.forEach(map -> variables.put((String) map.get("key"), map.get("value")));

        return resolve(data, variables, 1);
    }

    private static Object resolve(Object o, Map<String, Object> variables, int deep) {

        if (deep > STACK_MAX_DEPTH) {
            throw new IllegalStateException("对象嵌套太深，无法处理");
        }

        // 先判断对象o是不是Map集合
        if (o instanceof Map<?, ?> m) {
            // 如果是Map集合，那么遍历key和value
            HashMap<Object, Object> newMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : m.entrySet()) {
                Object key = resolve(entry.getKey(), variables, deep + 1);
                Object value = resolve(entry.getValue(), variables, deep + 1);
                newMap.put(key, value);
            }
            return newMap;
        } else if (o instanceof List<?> l) {
            List<Object> newList = new ArrayList<>();
            for (Object lo : l) {
                newList.add(resolve(lo, variables, deep + 1));
            }
            return newList;
        } else if (o instanceof String s) {
            if (!neededReplace(s)) {
                return s.trim();
            }
            // 不是List和Map，那就去variables集合中找是否存在同名的变量
            s = s.trim();

            List<String> variableNameList = getVariableNameList(s);
            boolean flag = s.equals("{{" + variableNameList.get(0) + "}}");
            // 需要判断是不是只有{{name}}的情况，如果是，就按取出来的变量类型直接返回即可
            if (flag) {
                Object ov = variables.get(variableNameList.get(0));
                if (ov != null) {
                    return ov;
                }
            } else {
                for (String name : variableNameList) {
                    Object ov = variables.get(name);
                    if (ov != null) {
                        s = replaceString(s, "\\{\\{" + name + "\\}\\}", ov);
                    }
                }
                return s;
            }
        }
        return o;
    }

    public static void main(String[] args) {
        // 构建内嵌在 "body" 字段中的JSON字符串
        // 这个Map代表了 body 内部的JSON结构
        Map<String, Object> bodyContentMap = new LinkedHashMap<>();

        List<Object> interfacesList = new ArrayList<>();
        Map<String, Object> interfaceDetails = new LinkedHashMap<>();

        List<String> arrList = List.of("{{arr}}", "{{arr}}");

        interfaceDetails.put("arr", arrList);
        interfaceDetails.put("name", "test");
        interfaceDetails.put("path", "/apihhh");
        interfaceDetails.put("method", "GET");
        interfaceDetails.put("test", "{{test}}");

        interfacesList.add(interfaceDetails);
        bodyContentMap.put("interfaces", interfacesList);

        // 构建最外层的完整Map结构
        Map<String, Object> rootMap = new LinkedHashMap<>();

        // 1. 构建 headers 列表
        List<Map<String, Object>> headers = List.of(
                Map.of("key", "Cache-Control", "value", new HashMap<>(Map.of("a", "123")), "description", "", "enabled", true),
                Map.of("key", "Accept", "value", "*/*", "description", "", "enabled", true),
                Map.of("key", "Accept-Encoding", "value", "gzip, deflate, br", "description", "", "enabled", true),
                Map.of("key", "Connection", "value", "keep-alive", "description", "", "enabled", true),
                Map.of("key", "token", "value", "{{token}}", "description", "", "enabled", true),
                Map.of("key", "Content-Type", "value", "application/json", "description", "", "enabled", true)
        );
        rootMap.put("headers", headers);

        // 2. 放入之前生成好的 body 字符串
        rootMap.put("body", bodyContentMap);

        // 3. 放入其他顶级字段
        rootMap.put("method", "POST");
        rootMap.put("queryParams", new ArrayList<>()); // 空列表
        rootMap.put("requestBodyType", "json");
        rootMap.put("host", "{{host}}");
        rootMap.put("{{pathParam}}", "a=1");

        System.out.println(rootMap);
        Object resolve = resolve(new ArrayList<>(
                List.of(
                        Map.of("key", "pathParam", "value", "host"),
                        Map.of("key", "arr", "value", new ArrayList<>(List.of(Map.of("a", "b", "b", "c")))),
                        Map.of("key", "token", "value", "我是token"),
                        Map.of("key", "host", "value", "http://localhost:8080"),
                        Map.of("key", "test", "value", "[1, 2, 3]")
                )
        ), rootMap);

        System.out.println(JSON.toJSONString(resolve));
    }
}
