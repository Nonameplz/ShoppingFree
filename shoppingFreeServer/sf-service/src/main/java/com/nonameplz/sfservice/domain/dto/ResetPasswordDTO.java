package com.nonameplz.sfservice.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ResetPasswordDTO {
    @NotEmpty(message = "重置密码不能为空!")
    @Schema(description = "用户重置密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ResetPassword;
    @NotEmpty(message = "重置密码不能为空!")
    @Schema(description = "用户二次输入重置密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String RePassword;
}
