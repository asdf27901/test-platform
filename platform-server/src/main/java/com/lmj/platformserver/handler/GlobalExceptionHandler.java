package com.lmj.platformserver.handler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.lmj.platformserver.exception.BaseException;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.result.ResultCodeEnum;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public Response<?> exceptionHandler(BaseException ex) {
        log.error("业务异常: ", ex);
        return Response.fail(ex.getResultCodeEnum());
    }

    // RequestBody参数异常
    // 触发场景：当使用@Valid注解对@RequestBody参数进行验证时，如果请求体不符合约束条件（如字段为空、长度不符合要求等），会触发此异常。
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("参数异常：", ex);
        ResultCodeEnum resultCodeEnum = ResultCodeEnum.PARAMETER_ERROR;
        return Response.fail(resultCodeEnum, ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    // RequestParams或者PathVariable参数异常
    // 在Controller中使用@Validated检验RequestParams或者PathVariable参数，如果不符合约束条件，会触发此异常
    @ExceptionHandler(ConstraintViolationException.class)
    public Response<String> handleValidationExceptions(ConstraintViolationException e) {
        log.error("参数异常", e);
        ResultCodeEnum resultCodeEnum = ResultCodeEnum.PARAMETER_ERROR;
        return Response.fail(resultCodeEnum, e.getMessage().split(":")[1]);
    }

    // 当使用@ModelAttribute绑定查询参数或表单数据到对象时，如果绑定失败（如类型转换错误或字段不符合约束条件），会触发此异常
    // 当不使用@RequestParams、@PathVariable、@RequestBody时，默认使用@ModelAttribute
    @ExceptionHandler(BindException.class)
    public Response<String> handleValidationExceptions(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        Objects.requireNonNull(fieldError);
        // 获取字段名称
        String fieldName = fieldError.getField();
        // 获取错误代码（用于区分类型转换失败和校验失败）
        String errorCode = fieldError.getCode();
        ResultCodeEnum resultCodeEnum = ResultCodeEnum.PARAMETER_ERROR;
        // 根据错误代码返回不同的错误信息
        if ("typeMismatch".equals(errorCode)) {
            // 类型转换失败
            String requireTypeName = Objects.requireNonNull(e.getFieldType(fieldName)).getSimpleName();
            String errorMessage = String.format("字段 '%s' 期望的类型为 %s", fieldName, requireTypeName);
            log.error("参数类型转换异常：", e);
            return Response.fail(resultCodeEnum, errorMessage);
        } else {
            // 校验失败
            String validationMessage = fieldError.getDefaultMessage();
            log.error("参数校验异常", e);
            return Response.fail(resultCodeEnum, validationMessage);
        }
    }

    // 当使用@RequestBody接收参数时，当出现HttpMessageNotReadableException就是类型转换失败
    // 需要区分出是类型转换的异常还是json本身格式有问题
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Response<String> exceptionHandler(HttpMessageNotReadableException exception) {
        Throwable throwable = exception.getCause(); // 获取异常根本原因
        ResultCodeEnum resultCodeEnum = ResultCodeEnum.PARAMETER_ERROR;
        if (throwable instanceof InvalidFormatException e) {
            log.error("类型转换异常", e);
            String simpleName = e.getTargetType().getSimpleName();
            return Response.fail(resultCodeEnum, "类型异常，期望类型为：" + simpleName + "，输入值：[" + e.getValue() + "]");
        } else if (throwable instanceof JsonParseException) {
            log.error("无效的json格式", exception);
            return Response.fail(resultCodeEnum, "无效的json格式，请检查");
        } else {
            log.error("请求体解析错误", exception);
            return Response.fail(resultCodeEnum, "请求体解析错误");
        }
    }

    // RequestParams传递空值时异常
    // 当请求中缺少必需的查询参数（@RequestParam）或路径参数（@PathVariable）时触发
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Response<String> exceptionHandler(MissingServletRequestParameterException exception) {
        String name = exception.getParameterName();
        ResultCodeEnum resultCodeEnum = ResultCodeEnum.PARAMETER_ERROR;
        log.error("参数异常", exception);
        return Response.fail(resultCodeEnum, name + "参数传递为空");
    }

    // RequestParams或者PathVariable参数不匹配时异常
    // 当查询参数或路径参数的类型与Controller方法中声明的参数类型不匹配时触发
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Response<String> exceptionHandler(MethodArgumentTypeMismatchException exception) {
        String name = exception.getName();
        String requiredTypeName = Objects.requireNonNull(exception.getRequiredType()).getSimpleName();
        ResultCodeEnum resultCodeEnum = ResultCodeEnum.PARAMETER_ERROR;
        log.error("参数异常: ", exception);
        return Response.fail(resultCodeEnum, name + "类型不匹配, 期望类型为：" + requiredTypeName);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Response<String> exceptionHandler(HttpRequestMethodNotSupportedException exception) {
        log.error("请求异常：", exception);
        return Response.fail(ResultCodeEnum.REQUEST_METHOD_ERROR);
    }

    @ExceptionHandler({Exception.class})
    public Response<?> exceptionHandler(Exception e) {
        log.error("异常信息：", e);
        return Response.fail(ResultCodeEnum.UNKNOWN_ERROR);
    }
}
