package com.lmj.platformserver.assertion;

import com.alibaba.fastjson2.JSON;
import lombok.SneakyThrows;
import org.graalvm.polyglot.HostAccess;

import java.util.*;

public class PreAssertionTool extends AbstractAssertionTool {

    private final Map<String, Object> requestContext;

    @HostAccess.Export
    public RequestProxy request;

    public PreAssertionTool(Map<String, Object> processedRequestContext, List<Map<String, Object>> variables) {
        super(variables);
        this.requestContext = processedRequestContext;
        // 在构造时，创建代理对象
        this.request = new RequestProxy();
    }

    public class RequestProxy {

        // 构造函数设为private，防止外部创建
        private RequestProxy() {}

        // 暴露 'headers' 属性，返回一个HeaderModifier代理
        @HostAccess.Export
        public HeaderModifier headers = new HeaderModifier();

        // 暴露 'queryParams' 属性，返回一个QueryModifier代理
        @HostAccess.Export
        public QueryParamModifier queryParams = new QueryParamModifier();

        @HostAccess.Export
        public BodyProxy body = new BodyProxy();
    }

    public class HeaderModifier {

        private HeaderModifier() {}

        @SuppressWarnings("unchecked")
        private List<Map<String, Object>> getHeadersList() {
            // 从外层类的requestContext中动态获取headers列表
            return (List<Map<String, Object>>) requestContext.get("headers");
        }

        public void add(String key, Object value) {
            if (key == null || key.equals("")) {
                return;
            }
            List<Map<String, Object>> headersList = getHeadersList();
            Map<String, Object> newHeader = new HashMap<>();
            newHeader.put("key", key);
            newHeader.put("value", objectValue2String(value));
            newHeader.put("enabled", true);
            if (headersList == null) {
                headersList = new ArrayList<>();
                headersList.add(newHeader);
                requestContext.put("headers", headersList);
            } else  {
                headersList.add(newHeader);
            }
        }

        @HostAccess.Export
        public void remove(String key) {
            List<Map<String, Object>> headersList = getHeadersList();
            if (headersList != null) {
                headersList.removeIf(header -> key.equals(header.get("key")));
            }
        }

        @HostAccess.Export
        public void upsert(String key, Object value) {
            if (key == null || key.equals("")) return;
            List<Map<String, Object>> headersList = getHeadersList();
            if (headersList == null) {
                add(key, value);
                return;
            }
            Optional<Map<String, Object>> existingHeader = headersList.stream()
                    .filter(header -> key.equalsIgnoreCase((String) header.get("key")))
                    .findFirst();

            if (existingHeader.isPresent()) {
                existingHeader.get().put("value", objectValue2String(value));
            } else {
                add(key, value);
            }
        }

        @HostAccess.Export
        public void clear() {
            List<Map<String, Object>> headersList = getHeadersList();
            if (headersList != null) {
                headersList.clear();
            }
        }
    }

    public class QueryParamModifier {

        private QueryParamModifier(){}

        @SuppressWarnings("unchecked")
        private List<Map<String, Object>> getQueryParamsList() {
            // 从外层类的requestContext中动态获取headers列表
            return (List<Map<String, Object>>) requestContext.get("queryParams");
        }

        public void add(String key, Object value) {
            if (key == null || key.equals("")) {
                return;
            }
            List<Map<String, Object>> queryParamsList = getQueryParamsList();
            Map<String, Object> newQueryParam = new HashMap<>();
            newQueryParam.put("key", key);
            newQueryParam.put("value", objectValue2String(value));
            newQueryParam.put("enabled", true);
            if (queryParamsList == null) {
                queryParamsList = new ArrayList<>();
                queryParamsList.add(newQueryParam);
                requestContext.put("queryParams", queryParamsList);
            } else {
                queryParamsList.add(newQueryParam);
            }
        }

        @HostAccess.Export
        public void remove(String key) {
            List<Map<String, Object>> queryParamsList = getQueryParamsList();
            if (queryParamsList != null) {
                queryParamsList.removeIf(param -> key.equals(param.get("key")));
            }
        }

        @HostAccess.Export
        public void upsert(String key, Object value) {
            if (key == null || key.equals("")) return;
            List<Map<String, Object>> paramsList = getQueryParamsList();
            if (paramsList == null) {
                add(key, value);
                return;
            }
            Optional<Map<String, Object>> existingParam = paramsList.stream()
                    .filter(param -> key.equals(param.get("key")))
                    .findFirst();

            if (existingParam.isPresent()) {
                existingParam.get().put("value", objectValue2String(value));
            } else {
                add(key, value);
            }

        }

        @HostAccess.Export
        public void clear() {
            List<Map<String, Object>> queryParamsList = getQueryParamsList();
            if (queryParamsList != null) {
                queryParamsList.clear();
            }
        }
    }

    public class BodyProxy {

        private BodyProxy() {}

        public String getType() {
            return (String) requestContext.get("requestBodyType");
        }

        @HostAccess.Export
        public String getJsonStr() {
            if (!"json".equals(getType())) return null;
            return JSON.toJSONString(requestContext.get("body"));
        }

        @HostAccess.Export
        @SneakyThrows
        @SuppressWarnings("unchecked")
        public void setJson(Map<String, Object> newJson) {
            if (!"json".equals(getType())) {
                throw new IllegalStateException("Cannot set JSON body when request body type is not 'json'");
            }
            Map<String, Object> body = JSON.to(Map.class, JSON.toJSONString(newJson));
            requestContext.put("body", body);
        }

        @HostAccess.Export
        @SuppressWarnings("unchecked")
        public List<Map<String, Object>> getForm() {
            String type = getType();
            if ("form-data".equals(type) || "x-www-form-urlencoded".equals(type)) {
                return (List<Map<String, Object>>) requestContext.get("body");
            }
            return null;
        }

        public void addForm(String key, Object value) {
            if (key == null || key.equals("")) {
                return;
            }
            String type = getType();
            if (!"form-data".equals(type) && !"x-www-form-urlencoded".equals(type)) {
                throw new IllegalStateException("Cannot add form when request body type is not 'form-data' or 'x-www-form-urlencoded'");
            }
            List<Map<String, Object>> form = getForm();
            Map<String, Object> newParam = new HashMap<>();
            newParam.put("key", key);
            newParam.put("value", objectValue2String(value));
            newParam.put("enabled", true);
            if ("form-data".equals(type)) {
                newParam.put("type", "text");
            }
            if (form == null) {
                List<Map<String, Object>> formData = new ArrayList<>();
                formData.add(newParam);
                requestContext.put("body", formData);
            } else {
                form.add(newParam);
            }
        }

        public void addFileForm(String key, String fileName, String filePath) {
            if (key == null || key.equals("")) {
                return;
            }
            String type = getType();
            if (!"form-data".equals(type)) {
                throw new IllegalStateException("Cannot add fileForm when request body type is not 'form-data'");
            }
            List<Map<String, Object>> form = getForm();
            Map<String, Object> newParam = new HashMap<>();
            newParam.put("key", key);
            newParam.put("value", fileName);
            newParam.put("type", "file");
            newParam.put("fileUrl", filePath);
            newParam.put("enabled", true);
            if (form == null) {
                List<Map<String, Object>> formData = new ArrayList<>();
                formData.add(newParam);
                requestContext.put("body", formData);
            } else {
                form.add(newParam);
            }
        }

        @HostAccess.Export
        public void upsertForm(String key, Object value) {
            if (key == null || key.equals("")) return;

            String type = getType();
            if (!"form-data".equals(type) && !"x-www-form-urlencoded".equals(type)) {
                throw new IllegalStateException("Cannot update/add form when request body type is not 'form-data' or 'x-www-form-urlencoded'");
            }

            List<Map<String, Object>> form = getForm();
            if (form == null) {
                addForm(key, objectValue2String(value));
                return;
            }

            Optional<Map<String, Object>> existingParam = form.stream()
                    .filter(p -> key.equals(p.get("key")))
                    .findFirst();

            if (existingParam.isPresent()) {
                Map<String, Object> param = existingParam.get();
                param.put("value", objectValue2String(value));
                param.remove("fileUrl");
                if ("form-data".equals(type)) {
                    param.put("type", "text");
                }
            } else {
                addForm(key, value);
            }
        }

        @HostAccess.Export
        public void upsertForm(String key, String fileName, String filePath) {
            if (key == null || key.equals("")) return;

            String type = getType();
            if (!"form-data".equals(type)) {
                throw new IllegalStateException("Cannot update/add fileForm when request body type is not 'form-data'");
            }
            List<Map<String, Object>> form = getForm();
            if (form == null) {
                addFileForm(key, fileName, filePath);
                return;
            }
            Optional<Map<String, Object>> existingParam = form.stream()
                    .filter(p -> key.equals(p.get("key")))
                    .findFirst();

            if (existingParam.isPresent()) {
                Map<String, Object> param = existingParam.get();
                param.put("value", fileName);
                param.put("type", "file");
                param.put("fileUrl", filePath);
            } else {
                addFileForm(key, fileName, filePath);
            }
        }

        @HostAccess.Export
        public void removeForm(String key) {
            List<Map<String, Object>> form = getForm();
            if (form == null || key == null) return;

            // 使用 removeIf 来移除所有匹配的项
            form.removeIf(param -> key.equals(param.get("key")));
        }

        @HostAccess.Export
        public void clearForm() {
            List<Map<String, Object>> form = getForm();
            if (form != null) {
                form.clear();
            }
        }
    }

    private static String objectValue2String(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof List<?> l) {
            StringJoiner sj = new StringJoiner(",");
            l.forEach(v -> sj.add(v.toString()));
            return sj.toString();
        } else if (value instanceof Map<?,?>){
            return JSON.toJSONString(value);
        } else {
            return String.valueOf(value);
        }
    }
}
