package com.lmj.platformserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.ChainRequestDTO;
import com.lmj.platformserver.dto.ChainRequestPageQueryDTO;
import com.lmj.platformserver.vo.ChainRequestDetailVo;
import com.lmj.platformserver.vo.ChainRequestPageQueryVo;

import java.util.List;

public interface ChainRequestService {

    void saveChainRequest(ChainRequestDTO dto);

    IPage<ChainRequestPageQueryVo> getChainRequestPageQueryVoList(ChainRequestPageQueryDTO dto);

    void deleteChainRequest(List<Long> ids);

    ChainRequestDetailVo getChainRequestDetail(Long id);

    void updateChainRequest(ChainRequestDTO dto);
}
