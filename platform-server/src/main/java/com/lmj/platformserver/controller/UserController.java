package com.lmj.platformserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.UserPageQueryDTO;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.service.UserService;
import com.lmj.platformserver.vo.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/getUserInfo")
    public Response<UserInfoVo> getUserInfo() {
        log.info("获取当前token的用户对象");
        UserInfoVo userInfoVo = userService.getUserInfo();
        return Response.success(userInfoVo);
    }

    @GetMapping("/userList")
    public Response<IPage<UserInfoVo>> getUserList(UserPageQueryDTO userPageQueryDTO) {
        log.info("获取用户列表");
        IPage<UserInfoVo> userInfoVoIPage = userService.getUserList(userPageQueryDTO);
        return Response.success(userInfoVoIPage);
    }
}
