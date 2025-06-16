package com.lmj.platformserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.InterfacePageQueryDTO;
import com.lmj.platformserver.dto.SaveInterfacesDTO;
import com.lmj.platformserver.vo.InterfaceVo;

import java.util.List;

public interface InterfaceService {
    void saveInterfaces(SaveInterfacesDTO saveInterfacesDTO);

    IPage<InterfaceVo> getInterfaceList(InterfacePageQueryDTO interfacePageQueryDTO);

    void deleteBatch(List<Long> ids);
}
