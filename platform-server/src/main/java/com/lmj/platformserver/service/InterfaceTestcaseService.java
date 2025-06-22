package com.lmj.platformserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.InterfaceTestcaseListQueryDTO;
import com.lmj.platformserver.entity.InterfaceTestcase;
import com.lmj.platformserver.vo.InterfaceTestcaseVo;

import java.util.List;

public interface InterfaceTestcaseService {
    void save(List<InterfaceTestcase> interfaceTestcases);

    IPage<InterfaceTestcaseVo> getInterfaceTestcaseList(InterfaceTestcaseListQueryDTO interfaceTestcaseListQueryDTO);

    void deleteInterfaceTestcaseBatch(List<Long> ids);
}
