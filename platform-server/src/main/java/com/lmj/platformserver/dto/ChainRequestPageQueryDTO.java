package com.lmj.platformserver.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChainRequestPageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer current = 1;
    private Integer size = 10;
    private Long id;
    private String name;
    private List<Long> caseIds;
    private Long userId;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime begin;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;
}
