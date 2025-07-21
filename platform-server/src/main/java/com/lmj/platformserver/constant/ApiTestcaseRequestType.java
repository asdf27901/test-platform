package com.lmj.platformserver.constant;

public enum ApiTestcaseRequestType {

    SINGLE((byte) 0),
    CHAIN((byte) 1);

    private final byte value;
    ApiTestcaseRequestType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
