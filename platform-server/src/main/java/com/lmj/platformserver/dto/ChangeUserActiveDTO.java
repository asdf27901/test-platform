package com.lmj.platformserver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class ChangeUserActiveDTO {

    @NotNull(message = "id不能为空")
    private Long id;

    @Range(max = 1, message = "active不合法")
    private Byte active;
}
