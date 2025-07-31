package com.lmj.platformserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmj.platformserver.dto.ChainRequestPageQueryDTO;
import com.lmj.platformserver.entity.ChainRequest;
import com.lmj.platformserver.vo.ChainRequestPageQueryVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ChainRequestMapper extends BaseMapper<ChainRequest> {
    IPage<ChainRequestPageQueryVo> getChainRequestPageQueryVoList(IPage<ChainRequestPageQueryVo> iPage, ChainRequestPageQueryDTO dto);

    List<ChainRequest> findChainRequestByInterfaceTestcaseIds(Collection<Long> ids);
}
