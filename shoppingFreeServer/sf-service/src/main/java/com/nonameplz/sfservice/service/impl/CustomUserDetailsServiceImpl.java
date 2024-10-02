package com.nonameplz.sfservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.nonameplz.sfservice.domain.po.User;
import com.nonameplz.sfservice.mapper.UsersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static com.nonameplz.sfservice.enums.userValidResult.USER_VALID_ERROR;


public class CustomUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsersMapper usersMapper;

    //自定义用户信息服务 用于spring security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<User> eq = null;

        if (isEmail(username)) {
            eq = newEq(username, User::getEmail);
        }

        if (isPhone(username)) {
            eq = newEq(username, User::getPhoneNumber);
        }

        if (eq == null) {
            eq = newEq(username, User::getUsername);
        }

        User user = usersMapper.selectOne(eq);

        if (user == null) {
            throw new UsernameNotFoundException(USER_VALID_ERROR.getDescription());
        }
        return user;
    }

    private LambdaQueryWrapper<User> newEq(String username, SFunction<User, ?> getFun) {

        return new LambdaQueryWrapper<User>()
                .select(User::getUsername, User::getPasswordHash, User::getRole)
                .eq(getFun, username);
    }

    private boolean isEmail(String input) {
        return input.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    private boolean isPhone(String input) {
        return input.matches("^\\+?[0-9. ()-]{7,25}$");
    }
}
