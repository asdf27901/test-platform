package com.lmj.platformserver.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserInfoVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String username;

    private String avatarUrl;

    private String nickName;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
