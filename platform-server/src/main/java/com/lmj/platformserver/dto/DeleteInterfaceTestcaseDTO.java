package com.lmj.platformserver.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class DeleteInterfaceTestcaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "ID不能为空")
    @NotNull(message = "ID不能为空")
    private List<Long> ids;
}
