package com.nonameplz.sfservice.enums;

import lombok.Getter;

@Getter
public enum userInfoException {
    USER_NOT_FOUND("用户不存在!")
    ;
    private final String description;

    userInfoException(String description) {
        this.description = description;
    }
}
