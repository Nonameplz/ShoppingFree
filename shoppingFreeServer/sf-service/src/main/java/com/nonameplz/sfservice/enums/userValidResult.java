package com.nonameplz.sfservice.enums;


import lombok.Getter;

@Getter
public enum userValidResult {
    USERNAME_INUSE("用户名已被占用!"),
    EMAIL_INUSE("邮箱已被占用!"),
    PHONE_NUMBER_INUSE("手机号已被占用!"),
    USER_UNAUTHORIZED("用户未经校验访问!"),
    USER_VALID_ERROR("用户名或密码错误！")
    ;

    private final String description;

    userValidResult(String description) {
        this.description = description;
    }


}
