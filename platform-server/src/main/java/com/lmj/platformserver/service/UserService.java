package com.lmj.platformserver.service;

import com.lmj.platformserver.dto.UserLoginDTO;
import com.lmj.platformserver.vo.UserLoginVo;

public interface UserService {
    UserLoginVo login(UserLoginDTO userLoginDTO);
}
