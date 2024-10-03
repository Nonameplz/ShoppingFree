package com.nonameplz.sfservice.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UserInfoDTO {

    private String userUUID;
    @NotEmpty(message = "用户名不能为空!")
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
    @NotEmpty(message = "邮箱不能为空!")
    @Schema(description = "用户邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
    @Schema(description = "用户手机号", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String phoneNumber;
    @Schema(description = "用户头像URL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String avatarUrl;
    @Schema(description = "用户地址", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String address;

}
