package com.bai.app.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SexEnum {

    BOY(0),
    GIRL(1),
    OTHER(2);

    private final Integer code;

    public Integer getCode() {
        return code;
    }
}
