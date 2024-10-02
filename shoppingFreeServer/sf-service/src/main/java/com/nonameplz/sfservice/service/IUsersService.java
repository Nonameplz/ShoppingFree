package com.nonameplz.sfservice.service;

import com.nonameplz.sfservice.domain.dto.LoginFormDTO;
import com.nonameplz.sfservice.domain.dto.RegisterFormDTO;
import com.nonameplz.sfservice.domain.po.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nonameplz.sfservice.domain.vo.UserInfoVO;
import com.nonameplz.sfservice.domain.vo.UserLoginVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Noname
 * @since 2024-08-05
 */
public interface IUsersService extends IService<User> {

    //用户注册接口
    void register(RegisterFormDTO registerFormDTO);

    //用户登录接口
    UserLoginVO login(LoginFormDTO loginFormDTO);

    //获取用户信息
    UserInfoVO getUserInfo(String userId);

    //修改用户信息接口
}
