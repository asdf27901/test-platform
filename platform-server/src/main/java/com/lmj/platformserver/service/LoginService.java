package com.lmj.platformserver.service;

import com.lmj.platformserver.dto.LoginDTO;
import com.lmj.platformserver.vo.UserLoginVo;
import jakarta.servlet.http.HttpSession;

public interface LoginService {
    UserLoginVo login(LoginDTO loginDTO, HttpSession httpSession);
}
