package com.lmj.platformserver.controller;

import com.lmj.platformserver.dto.LoginDTO;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.service.LoginService;
import com.lmj.platformserver.utils.CaptchaUtil;
import com.lmj.platformserver.vo.UserLoginVo;
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
    public Response<UserLoginVo> login(@RequestBody @Validated LoginDTO loginDTO) {
        log.info("用户登录：{}", loginDTO);
        UserLoginVo userLoginVo = loginService.login(loginDTO);
        return Response.success(userLoginVo);
    }

    @GetMapping("/captcha")
    public Response<String> getCaptcha(){
        log.info("获取验证码");
        return Response.success(CaptchaUtil.createCaptcha());
    }

}
