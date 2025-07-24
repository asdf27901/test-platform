package com.lmj.platformserver.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class InterfacePageQueryDTO {
    private Integer current = 1;
    private Integer size = 10;
    private Long interfaceId;
    private Long userId;
    private String name;
    private String method;
    private String path;
    private String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime begin;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;
}
