package com.lmj.platformserver.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ChainRequestDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private List<Long> caseIds;
    private Map<Long, List<Long>> interfaceAndCaseIds;
    private List<Map<String, Object>> selectedCaseNameList;
}
