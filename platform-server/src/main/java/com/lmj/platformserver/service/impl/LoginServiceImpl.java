package com.lmj.platformserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmj.platformserver.constant.UserConstant;
import com.lmj.platformserver.dto.LoginDTO;
import com.lmj.platformserver.entity.User;
import com.lmj.platformserver.exception.LoginFailException;
import com.lmj.platformserver.mapper.UserMapper;
import com.lmj.platformserver.result.ResultCodeEnum;
import com.lmj.platformserver.service.LoginService;
import com.lmj.platformserver.utils.CaptchaUtil;
import com.lmj.platformserver.utils.JwtUtil;
import com.lmj.platformserver.vo.UserLoginVo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserLoginVo login(LoginDTO loginDTO, HttpSession httpSession) {

        String code = (String) httpSession.getAttribute("code");
        if (code == null) {
            throw new LoginFailException(ResultCodeEnum.CAPTCHA_EXPIRED);
        }
        // 检验验证码
        if (!CaptchaUtil.verifyCaptcha(code, loginDTO.getCaptcha())) {
            throw new LoginFailException(ResultCodeEnum.CAPTCHA_ERROR);
        }

        // 检验验证码成功后，需要移除
        httpSession.removeAttribute("code");

        String username = loginDTO.getUsername();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                , true);
        // 当找不到匹配的用户名时，抛出用户名不存在
        if (user == null) {
            throw new LoginFailException(ResultCodeEnum.USERNAME_NOT_FOUND);
        }
        // 用户状态为失效
        if (user.getActive().equals(UserConstant.INACTIVE)) {
            throw new LoginFailException(ResultCodeEnum.USER_INACTIVE);
        }
        // 密码错误
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new LoginFailException(ResultCodeEnum.PASSWORD_ERROR);
        }

        // 设置Jwt
        String token = JwtUtil.generateToken(user.getId());

        UserLoginVo userLoginVo = new UserLoginVo();
        userLoginVo.setId(user.getId());
        userLoginVo.setToken(token);
        return userLoginVo;
    }
}
