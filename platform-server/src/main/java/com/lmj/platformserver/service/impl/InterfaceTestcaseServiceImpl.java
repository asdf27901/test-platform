package com.lmj.platformserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmj.platformserver.dto.InterfaceTestcaseListQueryDTO;
import com.lmj.platformserver.entity.BaseEntity;
import com.lmj.platformserver.entity.Interface;
import com.lmj.platformserver.entity.InterfaceTestcase;
import com.lmj.platformserver.exception.InterfaceErrorException;
import com.lmj.platformserver.mapper.InterfaceMapper;
import com.lmj.platformserver.mapper.InterfaceTestcaseMapper;
import com.lmj.platformserver.result.ResultCodeEnum;
import com.lmj.platformserver.service.InterfaceTestcaseService;
import com.lmj.platformserver.vo.InterfaceTestcaseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InterfaceTestcaseServiceImpl implements InterfaceTestcaseService {

    @Autowired
    private InterfaceTestcaseMapper interfaceTestcaseMapper;

    @Autowired
    private InterfaceMapper interfaceMapper;

    @Override
    public void save(List<InterfaceTestcase> interfaceTestcases) {
        Set<Long> interfaceIds = interfaceTestcases.stream().map(InterfaceTestcase::getInterfaceId).collect(Collectors.toSet());
        List<Long> ids = interfaceMapper.selectList(
                new LambdaQueryWrapper<Interface>()
                        .in(Interface::getId, interfaceIds)
        ).stream().map(BaseEntity::getId).toList();
        if (ids.size() != interfaceIds.size()) {
            throw new InterfaceErrorException(ResultCodeEnum.INTERFACE_ID_NOT_FOUND);
        }

        interfaceTestcaseMapper.insert(interfaceTestcases);
    }

    @Override
    public IPage<InterfaceTestcaseVo> getInterfaceTestcaseList(InterfaceTestcaseListQueryDTO interfaceTestcaseListQueryDTO) {
        Page<InterfaceTestcaseVo> page = new Page<>(interfaceTestcaseListQueryDTO.getCurrent(), interfaceTestcaseListQueryDTO.getSize());
        return interfaceTestcaseMapper.getInterfaceTestcaseList(page, interfaceTestcaseListQueryDTO);
    }
}
