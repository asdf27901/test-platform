package com.lmj.platformserver.dto;

import com.lmj.platformserver.entity.Interface;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class SaveInterfacesDTO {

    @Valid
    private List<Interface> interfaces;
}
