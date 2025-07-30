package com.lmj.platformserver.result;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer code;
    private T data;
    private String message;

    private static <T> Response<T> build(T data, @NotNull ResultCodeEnum resultCodeEnum, String message) {
        Response<T> response = new Response<>();
        response.setData(data);
        response.setCode(resultCodeEnum.getCode());
        if (StringUtils.hasText(message)) {
            response.setMessage(message);
        }else {
            response.setMessage(resultCodeEnum.getMessage());
        }
        return response;
    }

    public static <T> Response<T> success() {
        return build(null, ResultCodeEnum.SUCCESS, null);
    }

    public static <T> Response<T> success(T data) {
        return build(data, ResultCodeEnum.SUCCESS, null);
    }

    public static <T> Response<T> success(T data, ResultCodeEnum resultCodeEnum) {
        return build(data, resultCodeEnum, null);
    }

    public static <T> Response<T> success(T data, ResultCodeEnum resultCodeEnum, String message) {
        return Response.build(data, resultCodeEnum, message);
    }

    public static <T> Response<T> fail() {
        return build(null, ResultCodeEnum.FAIL, null);
    }

    public static <T> Response<T> fail(ResultCodeEnum resultCodeEnum) {
        return build(null, resultCodeEnum, null);
    }

    public static <T> Response<T> fail(ResultCodeEnum resultCodeEnum, String message) {
        return build(null, resultCodeEnum, message);
    }

    public static <T> Response<T> fail(String message) {
        return build(null, ResultCodeEnum.FAIL, message);
    }
}
