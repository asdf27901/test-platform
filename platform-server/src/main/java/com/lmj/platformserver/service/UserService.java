package com.lmj.platformserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.UserPageQueryDTO;
import com.lmj.platformserver.vo.UserInfoVo;

public interface UserService {
    UserInfoVo getUserInfo();

    IPage<UserInfoVo> getUserList(UserPageQueryDTO userPageQueryDTO);
}
