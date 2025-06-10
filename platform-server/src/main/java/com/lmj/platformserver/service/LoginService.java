package com.lmj.platformserver.service;

import com.lmj.platformserver.dto.LoginDTO;
import jakarta.servlet.http.HttpSession;

public interface LoginService {
    String login(LoginDTO loginDTO, HttpSession httpSession);
}
