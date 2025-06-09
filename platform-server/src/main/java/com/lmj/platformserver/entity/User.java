package com.lmj.platformserver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;

    private String password;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("nickname")
    private String nickName;

    @TableField("is_active")
    private Byte active;
}
