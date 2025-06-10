package com.lmj.platformserver.controller;

import cn.hutool.captcha.ShearCaptcha;
import com.lmj.platformserver.dto.LoginDTO;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.service.LoginService;
import com.lmj.platformserver.utils.CaptchaUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
@Validated
@Slf4j
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping
    public Response<String> login(@RequestBody @Validated LoginDTO loginDTO, HttpServletRequest httpServletRequest) {
        log.info("用户登录：{}", loginDTO);
        HttpSession httpSession = httpServletRequest.getSession();
        String token = loginService.login(loginDTO, httpSession);
        return Response.success(token);
    }

    @GetMapping("/captcha")
    public Response<String> getCaptcha(HttpServletRequest httpServletRequest){
        log.info("获取验证码");
        HttpSession httpSession = httpServletRequest.getSession();
        ShearCaptcha captcha = (ShearCaptcha) CaptchaUtil.createCaptcha();
        String code = captcha.getCode();
        log.info("验证码为：{}", code);
        httpSession.setAttribute("code", code);
        return Response.success(captcha.getImageBase64());
    }
}
