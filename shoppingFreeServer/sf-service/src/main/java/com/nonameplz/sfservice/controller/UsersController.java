package com.nonameplz.sfservice.controller;


import com.nonameplz.sfcommon.domain.R;
import com.nonameplz.sfservice.domain.dto.LoginFormDTO;
import com.nonameplz.sfservice.domain.dto.RegisterFormDTO;
import com.nonameplz.sfservice.domain.vo.UserInfoVO;
import com.nonameplz.sfservice.domain.vo.UserLoginVO;
import com.nonameplz.sfservice.service.IUsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping()
    @Operation(summary = "user Info", description = "获取用户信息接口")
    public R<UserInfoVO> getUserInfo(@RequestParam("UserUUID") String UserUUID) {
        return R.ok(usersService.getUserInfo(UserUUID));
    }

}
