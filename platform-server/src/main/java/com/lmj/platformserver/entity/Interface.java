package com.lmj.platformserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("interfaces")
public class Interface extends BaseEntity{
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "接口名称不能为空")
    private String name;

    @NotBlank(message = "请求方法不能为空")
    @Pattern(regexp = "(?i)GET|POST|PUT|DELETE|PATCH", message = "请求方法错误")
    private String method;

    @NotBlank(message = "请求路径不能为空")
    @Pattern(regexp = "/[a-zA-Z0-9/\\-_:]*", message = "请输入合法的路径, 以 / 开头")
    private String path;

    private String description;

    @TableField(value = "create_user", fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(value = "update_user", fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
