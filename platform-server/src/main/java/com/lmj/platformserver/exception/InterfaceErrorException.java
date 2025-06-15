package com.lmj.platformserver.exception;

import com.lmj.platformserver.entity.Interface;
import com.lmj.platformserver.result.ResultCodeEnum;

import java.io.Serial;
import java.util.List;

public class InterfaceErrorException extends BaseException{

    @Serial
    private static final long serialVersionUID = 1L;

    private List<Interface> duplicateInterfaces;

    public InterfaceErrorException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum);
    }

    public InterfaceErrorException(ResultCodeEnum resultCodeEnum, List<Interface> duplicateInterfaces) {
        super(resultCodeEnum);
        this.duplicateInterfaces = duplicateInterfaces;
    }

    public List<Interface> getDuplicateInterfaces() {
        return duplicateInterfaces;
    }
}
