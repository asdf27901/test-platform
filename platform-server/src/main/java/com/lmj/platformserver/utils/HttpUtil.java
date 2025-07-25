package com.lmj.platformserver.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.http.*;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.lmj.platformserver.exception.BaseException;
import com.lmj.platformserver.exception.JsonParseException;
import com.lmj.platformserver.result.ResultCodeEnum;
import lombok.SneakyThrows;
import org.brotli.dec.BrotliInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InaccessibleObjectException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

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
                    if (StringUtils.hasText((String) h.get("key")) && StringUtils.hasText((String) h.get("value"))) {
                        httpRequest.header((String) h.get("key"), (String) h.get("value"), true);
                    }
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
                            if ("text".equals(formDataParam.get("type"))) {
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
                Object jsonBodyObject = requestData.get("body");
                httpRequest.body(JSON.toJSONString(jsonBodyObject));
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
        } catch (HttpException e) {
            throw new BaseException(ResultCodeEnum.HTTP_REQUEST_ERROR);
        }
    }

    @SneakyThrows
    public Map<String, Object> getHttpRequestDataMap(HttpRequest httpRequest) {
        HashMap<String, Object> rawRequest = new HashMap<>();
        Map<String, List<String>> mutableHeaders = new HashMap<>(httpRequest.headers());
        String path = httpRequest.getUrl();
        URI uri = new URI(path);
        rawRequest.put("host", uri.getAuthority());
        rawRequest.put("path", path);
        rawRequest.put("method", httpRequest.getMethod().toString());
        rawRequest.put("body", httpRequest.bodyBytes() == null ? null : new String(httpRequest.bodyBytes()));

        Map<String, Object> form = httpRequest.form();
        if (mutableHeaders.containsKey(Header.CONTENT_TYPE.getValue())) {
            rawRequest.put("form", form);
        } else {
            if (MapUtil.isNotEmpty(form)) {
                mutableHeaders.put(Header.CONTENT_TYPE.getValue(), new ArrayList<>(List.of("multipart/form-data")));
                rawRequest.put("form", form);
            }
        }
        rawRequest.put("headers", mutableHeaders);
        return rawRequest;
    }

    public Map<String, Object> getHttpResponseDataMap(HttpResponse httpResponse, long responseTime) {
        Map<String, List<String>> rawHeaders = httpResponse.headers();
        Map<String, List<String>> sanitizedHeaders = rawHeaders.entrySet().stream()
                .filter(entry -> entry.getKey() != null) // 只保留key不为null的值
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, Object> res = new HashMap<>();
        res.put("statusCode", httpResponse.getStatus());
        res.put("headers", sanitizedHeaders);
        res.put("cookies", JSON.parse(JSON.toJSONString(httpResponse.getCookies())));
        res.put("responseTime", responseTime);

        byte[] responseBodyBytes = httpResponse.bodyBytes();
        String contentEncoding = httpResponse.header(Header.CONTENT_ENCODING);
        String bodyAsString;

        try {
            // 解压响应体
            byte[] decompressedBytes = decompressBody(responseBodyBytes, contentEncoding);

            // 将解压后的字节转换为字符串
            bodyAsString = new String(decompressedBytes, StandardCharsets.UTF_8);

            // 如果内容是JSON，尝试解析成JSON对象，否则直接使用字符串
            String contentType = httpResponse.header(Header.CONTENT_TYPE);
            if (contentType != null && contentType.toLowerCase().contains("application/json")) {
                res.put("body", JSON.parse(bodyAsString));
            } else {
                res.put("body", bodyAsString);
            }

        } catch (IOException e) {
            // 如果解压或转换失败，记录错误并存储原始的（可能是乱码的）字符串
            res.put("body", "Error processing response body: " + e.getMessage());
            // 或者存储原始字节的Base64编码，避免乱码
            // res.put("body_base64", Base64.getEncoder().encodeToString(responseBodyBytes));
        }

        return res;
    }

    private byte[] decompressBody(byte[] compressedBody, String contentEncoding) throws IOException {
        if (compressedBody == null || compressedBody.length == 0) {
            return new byte[0];
        }

        if (contentEncoding == null) {
            return compressedBody; // 没有压缩，直接返回
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(compressedBody);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        switch (contentEncoding.toLowerCase()) {
            case "br" -> {
                try (BrotliInputStream brotliInputStream = new BrotliInputStream(bis)) {
                    brotliInputStream.transferTo(bos);
                }
            }
            case "gzip" -> {
                try (GZIPInputStream gzipInputStream = new GZIPInputStream(bis)) {
                    gzipInputStream.transferTo(bos);
                }
            }
            // 可以添加对 "deflate" 的支持
            default -> {
                return compressedBody; // 不支持的压缩类型，直接返回
            }
        }

        return bos.toByteArray();
    }
}

