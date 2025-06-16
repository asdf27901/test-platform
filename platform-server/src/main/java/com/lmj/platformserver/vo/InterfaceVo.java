package com.lmj.platformserver.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class InterfaceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer current = 1;
    private Integer size = 10;
    private Long id;
    private String name;
    private String method;
    private String path;
    private String description;
    private String createUser;
    private String updateUser;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}
