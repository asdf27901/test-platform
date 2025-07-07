package com.lmj.platformserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "environment_variables", autoResultMap = true)
public class EnvironmentVariable extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "环境名称不能为空")
    private String name;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> variables;

    @TableField(value = "create_user", fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(value = "update_user", fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
