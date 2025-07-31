package com.lmj.platformserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "chain_requests", autoResultMap = true)
public class ChainRequest extends BaseEntity{

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;

    @TableField(value = "case_ids", typeHandler = JacksonTypeHandler.class)
    private List<Long> caseIds;

    @TableField(value = "interface_and_case_ids", typeHandler = JacksonTypeHandler.class)
    private Map<Long, List<Long>> interfaceAndCaseIds;

    @TableField(value = "create_user", fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(value = "update_user", fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
