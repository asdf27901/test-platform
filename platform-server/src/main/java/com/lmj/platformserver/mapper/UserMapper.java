package com.lmj.platformserver.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmj.platformserver.dto.UserPageQueryDTO;
import com.lmj.platformserver.entity.User;
import com.lmj.platformserver.vo.UserInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    default IPage<UserInfoVo> getUserList(UserPageQueryDTO userPageQueryDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(userPageQueryDTO.getUsername())) {
            wrapper.like(User::getUsername, userPageQueryDTO.getUsername());
        }
        if (StringUtils.hasText(userPageQueryDTO.getNickName())) {
            wrapper.like(User::getNickName, userPageQueryDTO.getNickName());
        }
        if (userPageQueryDTO.getEnd() != null && userPageQueryDTO.getBegin() != null) {
            wrapper.between(User::getCreatedTime, userPageQueryDTO.getBegin(), userPageQueryDTO.getEnd());
        } else if (userPageQueryDTO.getBegin() != null) {
            wrapper.gt(User::getCreatedTime, userPageQueryDTO.getBegin());
        } else if (userPageQueryDTO.getEnd() != null) {
            wrapper.le(User::getCreatedTime, userPageQueryDTO.getEnd());
        }
        wrapper.orderByAsc(User::getId);

        IPage<User> page = this.selectPage(new Page<>(userPageQueryDTO.getCurrent(), userPageQueryDTO.getSize()), wrapper);
        IPage<UserInfoVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<UserInfoVo> userInfoVoList = page.getRecords().stream().map(user -> {
            UserInfoVo userInfoVo = new UserInfoVo();
            BeanUtils.copyProperties(user, userInfoVo);
            return userInfoVo;
        }).toList();
        voPage.setRecords(userInfoVoList);
        return voPage;
    }
}
