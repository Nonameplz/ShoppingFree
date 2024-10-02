package com.nonameplz.sfservice.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


@Data
@Schema(description = "登录表单实体")
public class LoginFormDTO {
    @NotEmpty(message = "用户名不能为空")
    @Schema(description = "用户名/邮箱/手机号",requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
    @NotEmpty(message = "密码不能为空")
    @Schema(description = "用户密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
    @Schema(description = "是否记住我", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean rememberMe = false;
}
