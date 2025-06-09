package com.lmj.platformserver.controller;

import cn.hutool.captcha.ShearCaptcha;
import com.lmj.platformserver.dto.LoginDTO;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.service.LoginService;
import com.lmj.platformserver.utils.CaptchaUtil;
import com.lmj.platformserver.vo.UserLoginVo;
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
    public Response<UserLoginVo> login(@RequestBody @Validated LoginDTO loginDTO, HttpServletRequest httpServletRequest) {
        log.info("用户登录：{}", loginDTO);
        HttpSession httpSession = httpServletRequest.getSession();
        UserLoginVo userLoginVo = loginService.login(loginDTO, httpSession);
        return Response.success(userLoginVo);
    }

    @GetMapping("/captcha")
    public Response<String> getCaptcha(HttpServletRequest httpServletRequest){
        log.info("获取验证码");
        HttpSession httpSession = httpServletRequest.getSession();
        System.out.println(httpSession);
        ShearCaptcha captcha = (ShearCaptcha) CaptchaUtil.createCaptcha();
        String code = captcha.getCode();
        log.info("验证码为：{}", code);
        httpSession.setAttribute("code", code);
        return Response.success(captcha.getImageBase64());
    }
}
