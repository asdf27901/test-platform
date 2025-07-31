package com.lmj.platformserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmj.platformserver.dto.InterfacePageQueryDTO;
import com.lmj.platformserver.dto.SaveInterfacesDTO;
import com.lmj.platformserver.entity.Interface;
import com.lmj.platformserver.exception.InterfaceErrorException;
import com.lmj.platformserver.mapper.InterfaceMapper;
import com.lmj.platformserver.mapper.InterfaceTestcaseMapper;
import com.lmj.platformserver.result.ResultCodeEnum;
import com.lmj.platformserver.service.InterfaceService;
import com.lmj.platformserver.vo.InterfaceVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InterfaceServiceImpl implements InterfaceService {

    @Autowired
    private InterfaceMapper interfaceMapper;

    @Autowired
    private InterfaceTestcaseMapper interfaceTestcaseMapper;

    @Override
    public void saveInterfaces(SaveInterfacesDTO saveInterfacesDTO) {
        List<Interface> interfaces = saveInterfacesDTO.getInterfaces();
        // 需要判断里面是否有重复名称的接口
        List<String> names = interfaces.stream().map(Interface::getName).toList();
        Set<String> nameSet = new HashSet<>(names);
        if (names.size() > nameSet.size()) {
            throw new InterfaceErrorException(ResultCodeEnum.DUPLICATE_INTERFACE_NAME);
        }
        List<Interface> duplicateInterfaceList = interfaceMapper.selectList(
                new LambdaQueryWrapper<Interface>()
                        .in(Interface::getName, nameSet)
        );
        if (duplicateInterfaceList != null && !duplicateInterfaceList.isEmpty()) {
            throw new InterfaceErrorException(ResultCodeEnum.DUPLICATE_INTERFACE_NAME, duplicateInterfaceList);
        }
        // 找到是否有相同请求方法、相同请求路径的接口
        Set<String> uniqueMethodPathKeys = interfaces.stream().map(i -> i.getMethod().toUpperCase() + ":" + i.getPath()).collect(Collectors.toSet());
        if (interfaces.size() > uniqueMethodPathKeys.size()) {
            throw new InterfaceErrorException(ResultCodeEnum.DUPLICATE_INTERFACE_PATH);
        }
        LambdaQueryWrapper<Interface> queryWrapper = new LambdaQueryWrapper<>();
        interfaces.forEach(i -> queryWrapper.or(
                nestedWrapper -> nestedWrapper
                        .eq(Interface::getMethod, i.getMethod())
                        .eq(Interface::getPath, i.getPath())
        ));
        duplicateInterfaceList = interfaceMapper.selectList(queryWrapper);
        if (duplicateInterfaceList != null && !duplicateInterfaceList.isEmpty()) {
            throw new InterfaceErrorException(ResultCodeEnum.DUPLICATE_INTERFACE_PATH, duplicateInterfaceList);
        }

        interfaceMapper.insert(interfaces);
    }

    @Override
    public IPage<InterfaceVo> getInterfaceList(InterfacePageQueryDTO interfacePageQueryDTO) {
        IPage<InterfaceVo> voIPage = new Page<>(interfacePageQueryDTO.getCurrent(), interfacePageQueryDTO.getSize());
        return interfaceMapper.getInterfaceList(voIPage, interfacePageQueryDTO);
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        // 删除前，需要查找接口ID下是否存在测试用例
        Set<Long> existTestcaseIds = interfaceTestcaseMapper.getInterfaceListByInterfaceIds(new HashSet<>(ids));
        if (existTestcaseIds != null && !existTestcaseIds.isEmpty()) {
            throw new InterfaceErrorException(existTestcaseIds + "存在未删除测试用例，删除接口失败");
        }
        interfaceMapper.deleteByIds(ids);
    }

    @Override
    public void updateInterface(Interface i) {
        Long id = i.getId();
        Interface ii = interfaceMapper.selectById(id);
        if (ii == null) {
            throw new InterfaceErrorException(ResultCodeEnum.INTERFACE_ID_NOT_FOUND);
        }
        if (!i.getName().equals(ii.getName())) {
            Interface one = interfaceMapper.selectOne(
                    new LambdaQueryWrapper<Interface>()
                            .eq(Interface::getName, i.getName())
            );
            if (one != null) {
                throw new InterfaceErrorException(ResultCodeEnum.DUPLICATE_INTERFACE_NAME);
            }
        }
        if (!i.getPath().equals(ii.getPath()) || !i.getMethod().equals(ii.getMethod())) {
            Interface one = interfaceMapper.selectOne(
                    new LambdaQueryWrapper<Interface>()
                            .eq(Interface::getPath, i.getPath())
                            .eq(Interface::getMethod, i.getMethod())
            );
            if (one != null) {
                throw new InterfaceErrorException(ResultCodeEnum.DUPLICATE_INTERFACE_PATH);
            }
        }
        interfaceMapper.updateById(i);
    }

    @Override
    public List<Interface> getActiveInterfaceList() {
        return interfaceMapper.selectList(
                new LambdaQueryWrapper<Interface>()
                        .orderByAsc(Interface::getId)
        );
    }

    @Override
    public Interface getInterfaceDetail(Long id) {
        Interface i = interfaceMapper.selectById(id);
        if (i == null) {
            throw new InterfaceErrorException(ResultCodeEnum.INTERFACE_ID_NOT_FOUND);
        }
        return i;
    }
}
