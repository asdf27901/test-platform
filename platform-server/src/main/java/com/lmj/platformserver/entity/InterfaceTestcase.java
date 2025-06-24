package com.lmj.platformserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.lmj.platformserver.annotation.validation.MultiFieldAssociationCheck;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

import java.io.Serial;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "interface_testcases", autoResultMap = true)
@MultiFieldAssociationCheck(
        when = "pathParam != null && !pathParam.equals('')",
        must = "queryParams == null || queryParams.?[#this['enabled'] == true].size() == 0",
        message = "路径参数和查询参数只能存在其中一个",
        errorField = "queryParams"
)
@MultiFieldAssociationCheck(
        when = "queryParams == null || queryParams.?[#this['enabled'] == true].size() > 0",
        must = "pathParam == null || pathParam.equals('')",
        message = "路径参数和查询参数只能存在其中一个",
        errorField = "pathParam"
)
public class InterfaceTestcase extends BaseEntity{

    @Serial
    private static final long serialVersionUID = 1L;

    @TableField("interface_id")
    private Long interfaceId;

    @NotBlank(message = "接口用例名称不能为空")
    @Pattern(
            regexp = "^(?!\\s*未命名用例\\s*$).*$", // 在Java字符串中, \s需要转义成\\s
            message = "用例名称不能是'未命名用例'"
    )
    private String name;

    @Range(max = 2L, message = "优先级错误")
    @NotNull(message = "优先级不能为空")
    private Byte priority;

    @NotBlank(message = "域名不能为空")
    @Pattern(
            regexp = "^https?://.*$",
            message = "域名必须以 http:// 或 https:// 开头"
    )
    private String host;

    @NotBlank(message = "请求方法不能为空")
    @Pattern(regexp = "(?i)GET|POST|PUT|DELETE|PATCH", message = "请求方法错误")
    @TableField("request_method")
    private String method;

    @NotBlank(message = "请求体类型不能为空")
    @Pattern(regexp = "(?i)none|form-data|x-www-form-urlencoded|json", message = "请求体类型错误")
    @TableField("request_body_type")
    private String requestBodyType;

    @TableField("request_path_variables")
    private String pathParam;

    @TableField(value = "request_query_params", typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> queryParams;

    @TableField(value = "request_headers", typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> headers;

    @TableField(value = "request_body", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> requestBody;

    @TableField("expected_status_code")
    private Integer expectedStatusCode;  // 预留字段

    @TableField(value = "expected_response_headers", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> expectedResponseHeaders;  // 预留字段

    @TableField(value = "expected_response_body_assertions", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> expectedResponseBodyAssertions;  // 预留字段

    @TableField("pre_request_script")
    private String preRequestScript;

    @TableField("post_request_script")
    private String postRequestScript;

    @TableField(value = "create_user", fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(value = "update_user", fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
