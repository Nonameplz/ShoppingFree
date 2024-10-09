package com.nonameplz.sfservice.controller;


import com.nonameplz.sfcommon.domain.R;
import com.nonameplz.sfcommon.utils.UserContext;
import com.nonameplz.sfservice.domain.dto.LoginFormDTO;
import com.nonameplz.sfservice.domain.dto.RegisterFormDTO;
import com.nonameplz.sfservice.domain.dto.ResetPasswordDTO;
import com.nonameplz.sfservice.domain.dto.UserInfoDTO;
import com.nonameplz.sfservice.domain.vo.UserInfoVO;
import com.nonameplz.sfservice.domain.vo.UserLoginVO;
import com.nonameplz.sfservice.service.IUsersService;
import com.nonameplz.sfservice.utils.AliOSSUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Noname
 * @since 2024-08-05
 */

@Tag(name = "用户接口")
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final IUsersService usersService;

    @PostMapping("/login")
    public R<UserLoginVO> login(@Validated @RequestBody LoginFormDTO loginFormDTO) {
        UserLoginVO loginUserInfo = usersService.login(loginFormDTO);
        //todo 将token存入Redis

        return R.ok(loginUserInfo);
    }

    @PostMapping("/register")
    @Operation(summary = "user register", description = "用户注册接口")
    public R<Void> register(@Validated @RequestBody RegisterFormDTO registerFormDTO) {
        usersService.register(registerFormDTO);
        return R.ok();
    }

    @GetMapping("/{userUUID}")
    @Operation(summary = "Get User Info", description = "获取用户信息接口")
    public R<UserInfoVO> getUserInfo(@PathVariable("userUUID") String userUUID) {
        if (userUUID == null || UserContext.getUser() == null){
            return R.error("请求异常!");
        }
        if (!userUUID.equals(UserContext.getUser())){
            return R.error("请求异常!");
        }
        return R.ok(usersService.getUserInfo(userUUID));
    }

    @PutMapping("/{userUUID}")
    @Operation(summary = "Update UserInfo", description = "更新用户信息接口")
    public R<UserInfoVO> updateUserInfo(@PathVariable("userUUID") String userUUID,
                                        @RequestBody UserInfoDTO userInfoDTO) throws IOException {

        if (!isValidUserContext(userUUID, userInfoDTO)) {
            return R.error("数据异常!");
        }

        return R.ok(usersService.updateUserInfo(userInfoDTO));
    }

    private boolean isValidUserContext(String userUUID, UserInfoDTO userInfoDTO) {
        if (userUUID == null || UserContext.getUser() == null || userInfoDTO.getUserUUID() == null) {
            return false;
        }
        return (userUUID.equals(userInfoDTO.getUserUUID())) ||
                (userUUID.equals(UserContext.getUser()) && userInfoDTO.getUserUUID().equals(UserContext.getUser()));
    }

    @DeleteMapping("/{userUUID}")
    public R<Void> deleteUserInfo(@PathVariable String userUUID) {
        if (userUUID == null || UserContext.getUser() == null){
            return R.error("请求异常!");
        }
        if (!userUUID.equals(UserContext.getUser())){
            return R.error("请求异常!");
        }
        usersService.deleteUser(userUUID);
        return R.ok();
    }

    @PostMapping("/resetPassword")
    public R<Void> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        //todo 邮箱或手机号验证

        usersService.resetUserPassword(resetPasswordDTO, UserContext.getUser());

        return R.ok();
    }

}
