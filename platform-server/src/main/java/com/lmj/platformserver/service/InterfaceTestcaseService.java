package com.lmj.platformserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.InterfaceTestcaseListQueryDTO;
import com.lmj.platformserver.entity.InterfaceTestcase;
import com.lmj.platformserver.vo.InterfaceTestcaseVo;
import com.lmj.platformserver.vo.RequestResultVo;

import java.util.List;
import java.util.Map;

public interface InterfaceTestcaseService {
    void save(List<InterfaceTestcase> interfaceTestcases, Long interfaceId);

    IPage<InterfaceTestcaseVo> getInterfaceTestcaseList(InterfaceTestcaseListQueryDTO interfaceTestcaseListQueryDTO);

    void deleteInterfaceTestcaseBatch(List<Long> ids);

    InterfaceTestcase getInterfaceTestcaseDetail(Long id);

    RequestResultVo sendInterfaceTestcaseRequest(Map<String, Object> requestData);
}
