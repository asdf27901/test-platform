package com.lmj.platformserver.dto;

import com.lmj.platformserver.groups.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ChainRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(groups = Update.class, message = "ID不能为空")
    private Long id;

    @NotBlank(message = "链路名称不能为空")
    private String name;

    @NotNull(message = "已选用例ID不能为空")
    @NotEmpty(message = "已选用例ID不能为空")
    private List<Long> caseIds;

    @NotNull(message = "已选用例ID不能为空")
    @NotEmpty(message = "已选用例ID不能为空")
    private Map<Long, List<Long>> interfaceAndCaseIds;
}
