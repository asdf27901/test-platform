package com.lmj.platformserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.constant.UserConstant;
import com.lmj.platformserver.context.UserContextHolder;
import com.lmj.platformserver.dto.UserDTO;
import com.lmj.platformserver.dto.UserPageQueryDTO;
import com.lmj.platformserver.entity.User;
import com.lmj.platformserver.exception.BaseException;
import com.lmj.platformserver.exception.UserErrorException;
import com.lmj.platformserver.mapper.UserMapper;
import com.lmj.platformserver.result.ResultCodeEnum;
import com.lmj.platformserver.service.UserService;
import com.lmj.platformserver.utils.AliOssUtil;
import com.lmj.platformserver.vo.UserInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AliOssUtil aliOssUtil;

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

    @Override
    public void addUser(UserDTO userDTO) {
        Long currentLoginUserId = UserContextHolder.getUserId();
        if (currentLoginUserId != 1) {
            throw new BaseException(ResultCodeEnum.NO_PERMISSION);
        }
        String username = userDTO.getUsername();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
        if (user != null) {
            throw new UserErrorException(ResultCodeEnum.USERNAME_HAS_EXIST);
        }
        user = new User();
        BeanUtils.copyProperties(userDTO, user);
        userMapper.insert(user);
    }

    @Override
    public void updateUser(UserDTO userDTO) {
        Long userId = userDTO.getId();
        Long currentLoginUserId = UserContextHolder.getUserId();
        if (!Objects.equals(userId, currentLoginUserId) && currentLoginUserId != 1) {
            throw new BaseException(ResultCodeEnum.NO_PERMISSION);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserErrorException(ResultCodeEnum.USER_NOT_FOUND);
        }
        if (user.getActive().equals(UserConstant.INACTIVE)) {
            throw new UserErrorException(ResultCodeEnum.USER_INACTIVE);
        }
        // 当取出来的图片地址和传入的地址不一样时，需要删除oss图片
        if (StringUtils.hasText(userDTO.getAvatarUrl()) && !userDTO.getAvatarUrl().equals(user.getAvatarUrl())){
            aliOssUtil.deleteFile(user.getAvatarUrl());
        }else {
            userDTO.setAvatarUrl(user.getAvatarUrl());
        }
        BeanUtils.copyProperties(userDTO, user);
        userMapper.updateById(user);
    }
}
