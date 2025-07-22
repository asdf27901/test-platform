package com.lmj.platformserver.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ApiRequestLogsPageQueryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long interfaceId;
    private Long testcaseId;
    private Long chainId;
    private String testcaseName;
    private Byte requestType;
    private String executorName;
    private Byte executeResult;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executionTime;
}
