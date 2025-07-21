package com.lmj.platformserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.lmj.platformserver.vo.ScriptExecutionResultVo;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@TableName(value = "api_request_logs", autoResultMap = true)
public class ApiRequestLogs implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("interface_id")
    private Long interfaceId;

    @TableField("testcase_id")
    private Long testcaseId;

    @TableField("chain_id")
    private Long chainId;

    @TableField("request_type")
    private Byte requestType;

    @TableField(value = "request_data", typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> requestData;

    @TableField(value = "response_data", typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> responseData;

    @TableField(value = "pre_script_data", typeHandler = JacksonTypeHandler.class)
    private List<ScriptExecutionResultVo> preScriptData;

    @TableField(value = "post_script_data", typeHandler = JacksonTypeHandler.class)
    private List<ScriptExecutionResultVo> postScriptData;

    @TableField("execute_result")
    private Byte executeResult;

    @TableField("error_msg")
    private String errorMsg;

    @TableField("executor_id")
    private Long executorId;

    @TableField("executor_name")
    private String executorName;

    @TableField("execution_time")
    private LocalDateTime executionTime;
}
