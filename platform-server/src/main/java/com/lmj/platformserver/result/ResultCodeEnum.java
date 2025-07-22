package com.lmj.platformserver.result;

public enum ResultCodeEnum {
    SUCCESS(200, "成功"),
    FAIL(400, "失败"),
    UNKNOWN_ERROR(401, "未知异常"),
    PARAMETER_ERROR(402, "参数异常"),
    REQUEST_METHOD_ERROR(403, "请求方式错误"),
    NO_PERMISSION(404, "没有权限"),

    USERNAME_NOT_FOUND(501, "用户名不存在"),
    PASSWORD_ERROR(502, "密码错误"),
    USER_INACTIVE(503, "用户状态无效"),
    CAPTCHA_ERROR(504, "验证码错误"),
    CAPTCHA_EXPIRED(505, "验证码失效"),
    USER_NOT_FOUND(506, "用户不存在"),
    USERNAME_HAS_EXIST(507, "用户名已存在"),
    DUPLICATE_INTERFACE_NAME(508, "接口名重复"),
    DUPLICATE_INTERFACE_PATH(509, "已有相同的请求方法和接口路径"),
    INTERFACE_ID_NOT_FOUND(510, "接口id不存在"),
    INTERFACE_TESTCASE_ID_NOT_FOUND(511, "接口用例id不存在"),
    ENVIRONMENT_VARIABLE_ID_NOT_FOUND(512, "环境变量ID不存在"),
    ENVIRONMENT_VARIABLE_DUPLICATE(513, "存在重复的环境变量名"),
    JSON_PARSE_ERROR(514, "Json解析出错，请检查Json格式"),
    HTTP_REQUEST_ERROR(515, "请检查配置是否正确，变量引用是否正确"),

    TOKEN_EXPIRED(601, "token已过期"),
    TOKEN_INVALID(602, "无效token"),

    FILE_UPLOAD_FAIL(701, "文件上传失败"),

    REQUEST_INVALID(801, "请检查请求方式和请求体是否匹配");

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
