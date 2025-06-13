package com.lmj.platformserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.UserDTO;
import com.lmj.platformserver.dto.UserPageQueryDTO;
import com.lmj.platformserver.vo.UserInfoVo;

public interface UserService {
    UserInfoVo getUserInfo();

    IPage<UserInfoVo> getUserList(UserPageQueryDTO userPageQueryDTO);

    void addUser(UserDTO userDTO);

    void updateUser(UserDTO userDTO);
}
