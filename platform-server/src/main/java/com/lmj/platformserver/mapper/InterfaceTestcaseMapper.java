package com.lmj.platformserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmj.platformserver.dto.InterfaceTestcaseListQueryDTO;
import com.lmj.platformserver.entity.InterfaceTestcase;
import com.lmj.platformserver.vo.InterfaceTestcasePageVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Set;

@Mapper
public interface InterfaceTestcaseMapper extends BaseMapper<InterfaceTestcase> {
    IPage<InterfaceTestcasePageVo> getInterfaceTestcaseList(Page<InterfaceTestcasePageVo> page, InterfaceTestcaseListQueryDTO dto);

    Set<Long> getInterfaceListByInterfaceIds(Collection<Long> ids);
}
