package com.lmj.platformserver.utils;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.lmj.platformserver.exception.BaseException;
import com.lmj.platformserver.exception.JsonParseException;
import com.lmj.platformserver.result.ResultCodeEnum;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InaccessibleObjectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class HttpUtil {

    @Autowired
    private AliOssUtil aliOssUtil;

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public Map<String, Object> processRequestDataForHttp(Map<String, Object> requestData) {
        Map<String, Object> processedData = new HashMap<>();

        processedData.put("host", requestData.get("host"));
        processedData.put("method", requestData.get("method"));
        processedData.put("pathParam", requestData.get("pathParam"));

        // 1. 处理 Query Params
        List<Map<String, Object>> queryParamsList = (List<Map<String, Object>>) requestData.get("queryParams");
        if (!CollectionUtils.isEmpty(queryParamsList)) {
            List<Map<String, Object>> enabledQueryParams = queryParamsList.stream()
                    .filter(param -> param.get("enabled") == Boolean.TRUE)
                    .collect(Collectors.toList());
            processedData.put("queryParams", enabledQueryParams);
        }

        // 2. 处理 Headers
        List<Map<String, Object>> headersList = (List<Map<String, Object>>) requestData.get("headers");
        if (!CollectionUtils.isEmpty(headersList)) {
            List<Map<String, Object>> enabledHeaders = headersList.stream()
                    .filter(header -> header.get("enabled") == Boolean.TRUE)
                    .collect(Collectors.toList());
            processedData.put("headers", enabledHeaders);
        }

        // 3. 处理 Request Body
        String requestBodyType = (String) requestData.get("requestBodyType");
        processedData.put("requestBodyType", requestBodyType);

        Map<String, Object> requestBodyMap = (Map<String, Object>) requestData.get("requestBody");
        if (requestBodyMap != null && !"none".equals(requestBodyType)) {
            switch (requestBodyType) {
                case "json" -> {
                    // 对于JSON，直接获取jsonBody内容
                    // 它可能是一个Map，也可能是序列化后的String，取决于前端
                    Object jsonBody = requestBodyMap.get("jsonBody");
                    if (jsonBody instanceof String) {
                        try {
                            processedData.put("body", JSON.to(Map.class, jsonBody));
                        } catch (JSONException e) {
                            throw new JsonParseException(ResultCodeEnum.JSON_PARSE_ERROR);
                        }

                    } else {
                        // 尝试将其转换为标准的JSON字符串
                        processedData.put("body", jsonBody);
                    }
                }
                case "form-data", "x-www-form-urlencoded" -> {
                    String paramListName = "form-data".equals(requestBodyType) ? "formDataParams" : "urlEncodedParams";
                    List<Map<String, Object>> bodyParamsList = (List<Map<String, Object>>) requestBodyMap.get(paramListName);

                    if (!CollectionUtils.isEmpty(bodyParamsList)) {
                        List<Map<String, Object>> enabledBodyParams = bodyParamsList.stream()
                                .filter(param -> param.get("enabled") == Boolean.TRUE)
                                .collect(Collectors.toList());
                        processedData.put("body", enabledBodyParams);
                    }
                }
            }
        }

        return processedData;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public HttpRequest createHttpRequest(Map<String, Object> requestData, String path) {
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

        switch (requestBodyType.toLowerCase()) {

            case "form-data" -> {
                httpRequest.removeHeader(Header.CONTENT_TYPE);

                List<Map<String, Object>> formDataParams = (List<Map<String, Object>>) requestData.get("body");
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
                List<Map<String, Object>> urlEncodedParams = (List<Map<String, Object>>) requestData.get("body");
                if (!CollectionUtils.isEmpty(urlEncodedParams)) {
                    urlEncodedParams.forEach(urlEncodedParam -> {
                        if ((Boolean) urlEncodedParam.get("enabled")) {
                            httpRequest.form((String) urlEncodedParam.get("key"), urlEncodedParam.get("value"));
                        }
                    });
                }
            }
            case "json" -> {
                String jsonBodyObject = (String) requestData.get("body");
                httpRequest.body(jsonBodyObject);
            }
            default -> {}
        }

        return httpRequest;
    }

    @SneakyThrows
    public HttpResponse sendCustomizeHttpRequest(HttpRequest httpRequest) {
        try {
            return httpRequest.execute();
        } catch (InaccessibleObjectException e) {
            throw new BaseException(ResultCodeEnum.REQUEST_INVALID);
        }
    }

    public Map<String, Object> getHttpResponseDataMap(HttpResponse httpResponse, long responseTime) {
        Map<String, List<String>> rawHeaders = httpResponse.headers();
        Map<String, List<String>> sanitizedHeaders = rawHeaders.entrySet().stream()
                .filter(entry -> entry.getKey() != null) // 只保留key不为null的值
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, Object> res = new HashMap<>();
        res.put("statusCode", httpResponse.getStatus());
        res.put("body", httpResponse.body());
        res.put("headers", sanitizedHeaders);
        res.put("cookies", httpResponse.getCookies());
        res.put("responseTime", responseTime);

        return res;
    }
}

