package com.lmj.platformserver.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.constant.UserConstant;
import com.lmj.platformserver.context.UserContextHolder;
import com.lmj.platformserver.dto.UserPageQueryDTO;
import com.lmj.platformserver.entity.User;
import com.lmj.platformserver.exception.UserErrorException;
import com.lmj.platformserver.mapper.UserMapper;
import com.lmj.platformserver.result.ResultCodeEnum;
import com.lmj.platformserver.service.UserService;
import com.lmj.platformserver.vo.UserInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserInfoVo getUserInfo() {
        Long userId = UserContextHolder.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserErrorException(ResultCodeEnum.USER_NOT_FOUND);
        }
        if (UserConstant.INACTIVE.equals(user.getActive())){
            throw new UserErrorException(ResultCodeEnum.USER_INACTIVE);
        }
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(user, userInfoVo);
        return userInfoVo;
    }

    @Override
    public IPage<UserInfoVo> getUserList(UserPageQueryDTO userPageQueryDTO) {
        return userMapper.getUserList(userPageQueryDTO);
    }
}
