package com.lmj.platformserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.ChangeUserActiveDTO;
import com.lmj.platformserver.dto.UserDTO;
import com.lmj.platformserver.dto.UserPageQueryDTO;
import com.lmj.platformserver.entity.User;
import com.lmj.platformserver.vo.UserInfoVo;

import java.util.List;

public interface UserService {
    UserInfoVo getUserInfo();

    IPage<UserInfoVo> getUserList(UserPageQueryDTO userPageQueryDTO);

    void addUser(UserDTO userDTO);

    void updateUser(UserDTO userDTO);

    void changeUserActive(ChangeUserActiveDTO changeUserActiveDTO);

    List<User> getActiveUserList();
}
