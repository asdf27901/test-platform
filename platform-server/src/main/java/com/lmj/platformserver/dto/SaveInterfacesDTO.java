package com.lmj.platformserver.dto;

import com.lmj.platformserver.entity.Interface;
import lombok.Data;

import java.util.List;

@Data
public class SaveInterfacesDTO {

    private List<Interface> interfaces;
}
