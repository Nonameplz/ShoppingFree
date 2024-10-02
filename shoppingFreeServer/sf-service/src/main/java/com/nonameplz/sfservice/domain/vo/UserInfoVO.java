package com.nonameplz.sfservice.domain.vo;

import lombok.Data;

@Data
public class UserInfoVO {

    private String userUUID;
    private String username;
    private String email;
    private String phoneNumber;
    private String status;
    private String role;
    private String avatarUrl;
    private String address;

}
