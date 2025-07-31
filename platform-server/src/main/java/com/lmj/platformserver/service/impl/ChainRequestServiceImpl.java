package com.lmj.platformserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmj.platformserver.dto.ChainRequestDTO;
import com.lmj.platformserver.dto.ChainRequestPageQueryDTO;
import com.lmj.platformserver.entity.ChainRequest;
import com.lmj.platformserver.entity.InterfaceTestcase;
import com.lmj.platformserver.exception.ChainRequestErrorException;
import com.lmj.platformserver.mapper.ChainRequestMapper;
import com.lmj.platformserver.mapper.InterfaceTestcaseMapper;
import com.lmj.platformserver.result.ResultCodeEnum;
import com.lmj.platformserver.service.ChainRequestService;
import com.lmj.platformserver.vo.ChainRequestDetailVo;
import com.lmj.platformserver.vo.ChainRequestPageQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChainRequestServiceImpl implements ChainRequestService {
    
    @Autowired
    private ChainRequestMapper chainRequestMapper;

    @Autowired
    private InterfaceTestcaseMapper interfaceTestcaseMapper;
    
    @Override
    public void saveChainRequest(ChainRequestDTO dto) {
        List<Long> caseIds = dto.getCaseIds();
        Set<Long> caseIdsSet = new HashSet<>(caseIds);
        if (caseIdsSet.size() != caseIds.size()) {
            throw new ChainRequestErrorException(ResultCodeEnum.CHAIN_REQUEST_TESTCASE_ID_DUPLICATE);
        }

        ChainRequest chainRequest = new ChainRequest();
        chainRequest.setName(dto.getName());
        chainRequest.setCaseIds(dto.getCaseIds());
        chainRequest.setInterfaceAndCaseIds(dto.getInterfaceAndCaseIds());
        chainRequestMapper.insert(chainRequest);
    }

    @Override
    public IPage<ChainRequestPageQueryVo> getChainRequestPageQueryVoList(ChainRequestPageQueryDTO dto) {
        IPage<ChainRequestPageQueryVo> iPage = new Page<>(dto.getCurrent(), dto.getSize());
        return chainRequestMapper.getChainRequestPageQueryVoList(iPage, dto);
    }

    @Override
    public void deleteChainRequest(List<Long> ids) {
        chainRequestMapper.deleteByIds(ids);
    }

    @Override
    public ChainRequestDetailVo getChainRequestDetail(Long id) {
        ChainRequest chainRequest = chainRequestMapper.selectById(id);
        if (chainRequest == null) {
            throw new ChainRequestErrorException(ResultCodeEnum.CHAIN_REQUEST_ID_NOT_FOUND);
        }
        ChainRequestDetailVo chainRequestDetailVo = new ChainRequestDetailVo();
        chainRequestDetailVo.setId(chainRequest.getId());
        chainRequestDetailVo.setName(chainRequest.getName());
        chainRequestDetailVo.setCaseIds(chainRequest.getCaseIds());
        chainRequestDetailVo.setInterfaceAndCaseIds(chainRequest.getInterfaceAndCaseIds());

        if (chainRequest.getCaseIds() == null || chainRequest.getCaseIds().isEmpty()) {
            chainRequestDetailVo.setSelectedCaseNameList(Collections.emptyList());
            return chainRequestDetailVo;
        }

        String idOrderString = chainRequest.getCaseIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        List<InterfaceTestcase> interfaceTestcaseList = interfaceTestcaseMapper.selectList(
                new LambdaQueryWrapper<InterfaceTestcase>()
                        .in(InterfaceTestcase::getId, chainRequest.getCaseIds())
                        .last("ORDER BY FIELD(id, " + idOrderString + ")")
        );

        List<Map<String, Object>> list = interfaceTestcaseList.stream().map(i ->  Map.<String, Object>of("id", i.getId(), "name", i.getName())).toList();
        chainRequestDetailVo.setSelectedCaseNameList(list);
        return chainRequestDetailVo;
    }

    @Override
    public void updateChainRequest(ChainRequestDTO dto) {
        Long chainRequestId = dto.getId();
        ChainRequest chainRequest = chainRequestMapper.selectById(chainRequestId);
        if (chainRequest == null) {
            throw new ChainRequestErrorException(ResultCodeEnum.CHAIN_REQUEST_ID_NOT_FOUND);
        }
        chainRequest.setName(dto.getName());
        chainRequest.setCaseIds(dto.getCaseIds());
        chainRequest.setInterfaceAndCaseIds(dto.getInterfaceAndCaseIds());
        chainRequestMapper.updateById(chainRequest);
    }
}
