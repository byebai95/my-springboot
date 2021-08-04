package com.bai.app.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UserStatusEnum {

    NORMAL(0),
    LOCK(1),
    DELETE(2);

    private final Integer code;

    public Integer getCode() {
        return code;
    }
}
