package com.lmj.platformserver.dto;

import com.lmj.platformserver.groups.Add;
import com.lmj.platformserver.groups.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {

    @NotNull(groups = Update.class, message = "id不能为空")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "昵称不能为空")
    private String nickName;

    private String avatarUrl;

    @NotBlank(groups = Add.class, message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须为6~20位")
    private String password;

}
