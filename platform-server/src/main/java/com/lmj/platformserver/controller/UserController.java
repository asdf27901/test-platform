package com.lmj.platformserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.ChangeUserActiveDTO;
import com.lmj.platformserver.dto.UserDTO;
import com.lmj.platformserver.dto.UserPageQueryDTO;
import com.lmj.platformserver.groups.Add;
import com.lmj.platformserver.groups.Update;
import com.lmj.platformserver.result.Response;
import com.lmj.platformserver.service.UserService;
import com.lmj.platformserver.vo.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/add")
    public Response<?> addUser(@RequestBody @Validated(Add.class) UserDTO userDTO) {
        log.info("新增用户：{}", userDTO);
        userService.addUser(userDTO);
        return Response.success();
    }

    @PostMapping("/update")
    public Response<?> updateUser(@RequestBody @Validated(Update.class) UserDTO userDTO) {
        log.info("更新用户：{}", userDTO);
        userService.updateUser(userDTO);
        return Response.success();
    }

    @PostMapping("/changeUserActive")
    public Response<?> changeUserActive(@RequestBody @Validated ChangeUserActiveDTO changeUserActiveDTO) {
        log.info("修改用户状态: {}", changeUserActiveDTO);
        userService.changeUserActive(changeUserActiveDTO);
        return Response.success();
    }
}
