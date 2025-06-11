package com.lmj.platformserver.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserInfoVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String username;

    private String avatarUrl;

    private String nickName;
}
