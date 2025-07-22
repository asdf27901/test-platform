package com.lmj.platformserver.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ApiRequestLogsPageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer current = 1;
    private Integer size = 10;
    private Long id;
    private Long interfaceId;
    private Long testcaseId;
    private Long chainId;
    private Byte requestType;
    private Long executorId;
    private String testcaseName;
    private Byte executeResult;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime begin;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;
}
