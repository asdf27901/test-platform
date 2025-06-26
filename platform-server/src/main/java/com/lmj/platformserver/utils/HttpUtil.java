package com.lmj.platformserver.utils;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class HttpUtil {

    @Autowired
    private AliOssUtil aliOssUtil;

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public Map<String, Object> sendCustomizeHttpRequest(Map<String, Object> requestData, String path) {

        System.out.println(requestData);

        // 先组装url参数
        String host = (String) requestData.get("host");

        UrlBuilder urlbuilder = UrlBuilder.of(host + path);

        // 获取路径参数
        String pathParam = (String) requestData.get("pathParam");
        if (pathParam != null && !pathParam.equals("")) {
            urlbuilder.addPath(pathParam);
        }

        // 获取查询参数
        List<Map<String, Object>> queryParams = (List<Map<String, Object>>) requestData.get("queryParams");
        if (!CollectionUtils.isEmpty(queryParams)) {
            queryParams.forEach(param -> {
                if ((Boolean) param.get("enabled")) {
                    urlbuilder.addQuery((String) param.get("key"), param.get("value"));
                }
            });
        }

        HttpRequest httpRequest = HttpRequest.of(urlbuilder);

        // 定义httpRequest的请求方法
        String method = (String) requestData.get("method");
        switch (method.toLowerCase()) {
            case "post" -> httpRequest.method(Method.POST);
            case "put" -> httpRequest.method(Method.PUT);
            case "delete" -> httpRequest.method(Method.DELETE);
            default -> httpRequest.method(Method.GET);
        }

        // 获取请求头参数
        List<Map<String, Object>> headers = (List<Map<String, Object>>) requestData.get("headers");
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach(h -> {
                if ((Boolean) h.get("enabled")) {
                    httpRequest.header((String) h.get("key"), (String) h.get("value"), true);
                }
            });
        }

        // 获取请求体类型
        String requestBodyType = (String) requestData.get("requestBodyType");
        Map<String, Object> requestBody = (Map<String, Object>) requestData.get("requestBody");

        switch (requestBodyType.toLowerCase()) {

            case "form-data" -> {
                httpRequest.removeHeader("Content-Type");
                List<Map<String, Object>> formDataParams = (List<Map<String, Object>>) requestBody.get("formDataParams");
                if (!CollectionUtils.isEmpty(formDataParams)) {
                    formDataParams.forEach(formDataParam -> {
                        if ((Boolean) formDataParam.get("enabled")) {
                            // 文本类型
                            if (formDataParam.get("type") == "text") {
                                httpRequest.form((String) formDataParam.get("key"), formDataParam.get("value"));
                            }
                            else {
                                // 需要从阿里云上拉取文件下来
                                String fileUrl = (String) formDataParam.get("fileUrl");
                                byte[] fileBytes = null;
                                if (fileUrl != null && !fileUrl.equals("")) {
                                    fileBytes = aliOssUtil.getFileBytes(fileUrl);
                                }
                                String fileName = (String) formDataParam.get("value");

                                httpRequest.form((String) formDataParam.get("key"), fileBytes, fileName);
                            }
                        }
                    });
                }
            }
            case "x-www-form-urlencoded" -> {
                List<Map<String, Object>> urlEncodedParams = (List<Map<String, Object>>) requestBody.get("urlEncodedParams");
                if (!CollectionUtils.isEmpty(urlEncodedParams)) {
                    urlEncodedParams.forEach(urlEncodedParam -> {
                        if ((Boolean) urlEncodedParam.get("enabled")) {
                            httpRequest.form((String) urlEncodedParam.get("key"), urlEncodedParam.get("value"));
                        }
                    });
                }
            }
            case "json" -> {
                ObjectMapper objectMapper = new ObjectMapper();
                Object jsonBodyObject = requestBody.get("jsonBody");
                String jsonBodyAsString = objectMapper.writeValueAsString(jsonBodyObject);
                httpRequest.body(jsonBodyAsString);
            }
            default -> {}
        }

        @Cleanup HttpResponse response = httpRequest.execute();

        Map<String, List<String>> rawHeaders = response.headers();
        Map<String, List<String>> sanitizedHeaders = rawHeaders.entrySet().stream()
                .filter(entry -> entry.getKey() != null) // 只保留key不为null的值
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, Object> res = new HashMap<>();
        res.put("body", response.body());
        res.put("headers", sanitizedHeaders);
        res.put("cookies", response.getCookies());

        return res;
    }
}

