package com.lmj.platformserver.dto;

import com.lmj.platformserver.entity.InterfaceTestcase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class InterfaceTestcaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Valid
    @NotNull(message = "用例为空")
    @NotEmpty(message = "用例为空")
    private List<InterfaceTestcase> interfaceTestcases;

}
