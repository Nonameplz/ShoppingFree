package com.nonameplz.sfservice.enums;


import lombok.Getter;

@Getter
public enum userValidResult {
    USERNAME_INUSE("用户名已被占用!"),
    USERNAME_INVALID("用户名不符合规定!"),
    EMAIL_INUSE("邮箱已被占用!"),
    PHONE_NUMBER_INUSE("手机号已被占用!"),
    USER_UNAUTHORIZED("用户未经校验访问!"),
    USER_VALID_ERROR("用户名或密码错误!"),
    USER_NOT_FOUND("用户不存在!"),
    PASSWORDS_DO_NOT_MATCH("两次密码不相同!"),
    CANNOT_USE_OLD_PASSWORD("不能使用以前的密码!");
    ;

    private final String description;

    userValidResult(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
