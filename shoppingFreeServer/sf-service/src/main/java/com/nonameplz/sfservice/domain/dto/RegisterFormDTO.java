package com.nonameplz.sfservice.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Schema(description = "注册表单实体")
public class RegisterFormDTO {

    @NotEmpty(message = "用户名不能为空!")
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
    @NotEmpty(message = "密码不能为空!")
    @Schema(description = "用户密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
    @NotEmpty(message = "邮箱不能为空!")
    @Schema(description = "用户邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
    @Schema(description = "用户手机号", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String phoneNumber;
    @Schema(description = "用户安全问题", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String securityQuestion;
    @Schema(description = "用户安全问题答案", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String securityAnswer;
}
