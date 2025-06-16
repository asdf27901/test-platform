package com.lmj.platformserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.InterfacePageQueryDTO;
import com.lmj.platformserver.entity.Interface;
import com.lmj.platformserver.vo.InterfaceVo;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface InterfaceMapper extends BaseMapper<Interface> {

    IPage<InterfaceVo> getInterfaceList(IPage<InterfaceVo> voIPage, InterfacePageQueryDTO interfacePageQueryDTO);
}
