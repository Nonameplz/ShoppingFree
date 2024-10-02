package com.nonameplz.sfservice.domain.vo;

import lombok.Data;

import java.util.Map;

@Data
public class UserLoginVO {
    private Map<String,String> token;
    private String userUUID;
    private String username;
    private String role;
}
