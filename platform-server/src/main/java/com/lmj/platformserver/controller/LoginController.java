package com.lmj.platformserver.controller;

import cn.hutool.captcha.ICaptcha;
import com.lmj.platformserver.dto.UserLoginDTO;
import com.lmj.platformserver.entity.BaseEntity;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.service.UserService;
import com.lmj.platformserver.utils.CaptchaUtil;
import com.lmj.platformserver.vo.UserLoginVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping("/login")
@Validated
@Slf4j
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping
    public Response<UserLoginVo> login(@RequestBody @Validated UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO);
        UserLoginVo userLoginVo = userService.login(userLoginDTO);
        return Response.success(userLoginVo);
    }

    @GetMapping("/captcha")
    public Response<String> getCaptcha(){
        log.info("获取验证码");
        return Response.success(CaptchaUtil.createCaptcha());
    }

}
