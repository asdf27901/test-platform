package com.lmj.platformserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.InterfaceTestcaseListQueryDTO;
import com.lmj.platformserver.entity.InterfaceTestcase;
import com.lmj.platformserver.vo.InterfaceTestcasePageVo;
import com.lmj.platformserver.vo.InterfaceTestcaseUlVo;
import com.lmj.platformserver.vo.InterfaceTestcaseVo;
import com.lmj.platformserver.vo.RequestResultVo;

import java.util.List;
import java.util.Map;

public interface InterfaceTestcaseService {
    void save(List<InterfaceTestcase> interfaceTestcases, Long envId);

    IPage<InterfaceTestcasePageVo> getInterfaceTestcaseList(InterfaceTestcaseListQueryDTO interfaceTestcaseListQueryDTO);

    void deleteInterfaceTestcaseBatch(List<Long> ids);

    InterfaceTestcaseVo getInterfaceTestcaseDetail(Long id);

    RequestResultVo sendInterfaceTestcaseRequest(Map<String, Object> requestData, Long envId);

    List<InterfaceTestcaseUlVo> getInterfaceTestcaseById(Long interfaceId);
}
