package com.lmj.platformserver.dto;

import com.lmj.platformserver.entity.Interface;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SaveInterfacesDTO {

    @Valid
    @NotEmpty(message = "接口为空")
    @NotNull(message = "接口为空")
    private List<Interface> interfaces;
}
