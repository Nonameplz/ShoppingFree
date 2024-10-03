package com.nonameplz.sfservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.nonameplz.sfcommon.exception.CommonException;
import com.nonameplz.sfcommon.exception.DbException;
import com.nonameplz.sfcommon.exception.UnauthorizedException;
import com.nonameplz.sfcommon.utils.BeanUtils;
import com.nonameplz.sfservice.domain.dto.LoginFormDTO;
import com.nonameplz.sfservice.domain.dto.RegisterFormDTO;
import com.nonameplz.sfservice.domain.po.User;
import com.nonameplz.sfservice.domain.vo.UserInfoVO;
import com.nonameplz.sfservice.domain.vo.UserLoginVO;
import com.nonameplz.sfservice.mapper.UsersMapper;
import com.nonameplz.sfservice.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nonameplz.sfservice.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static com.nonameplz.sfservice.enums.userInfoException.USER_NOT_FOUND;
import static com.nonameplz.sfservice.enums.userValidResult.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Noname
 * @since 2024-08-05
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsersServiceImpl extends ServiceImpl<UsersMapper, User> implements IUsersService {

    private final PasswordEncoder passwordEncoder;
    private final UsersMapper usersMapper;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTool jwtTool;

    @Override
    public void register(@NotNull RegisterFormDTO registerFormDTO) {

        //TODO: 优化查询语句
        List<String> username = selectSingleColumn(registerFormDTO.getUsername(), User::getUsername);
        List<String> email = selectSingleColumn(registerFormDTO.getEmail(), User::getEmail);
        List<String> phoneNumber = selectSingleColumn(registerFormDTO.getPhoneNumber(), User::getPhoneNumber);

        if (!username.isEmpty() || !email.isEmpty() || !phoneNumber.isEmpty()) {
            StringJoiner sj = new StringJoiner("|");

            if (!username.isEmpty()) {
                sj.add(USERNAME_INUSE.getDescription());
            }
            if (!email.isEmpty()) {
                sj.add(EMAIL_INUSE.getDescription());
            }
            if (!phoneNumber.isEmpty()) {
                sj.add(PHONE_NUMBER_INUSE.getDescription());
            }

            String msg = sj.toString();

            throw new DbException(msg);
        }

        User user = BeanUtils.copyBean(registerFormDTO, User.class);
        user.setUserUUID(UUID.randomUUID().toString());
        user.setPasswordHash(passwordEncoder.encode(registerFormDTO.getPassword()));
        if (
                !registerFormDTO.getSecurityQuestion().isEmpty() &&
                        !registerFormDTO.getSecurityAnswer().isEmpty()
        ) {
            user.setSecurityAnswerHash(passwordEncoder.encode(registerFormDTO.getSecurityAnswer()));
        }

        usersMapper.insert(user);

        //TODO: 添加邮箱验证
    }

    @Override
    public UserLoginVO login(@NotNull LoginFormDTO loginFormDTO) {
        try {
            // 验证用户身份
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginFormDTO.getUsername(), loginFormDTO.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new UnauthorizedException(USER_VALID_ERROR.getDescription(), e);
        }

        // 加载用户详情
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginFormDTO.getUsername());
        LambdaQueryWrapper<User> eq = new LambdaQueryWrapper<>();
        eq.eq(User::getUsername, userDetails.getUsername());

        User user = usersMapper.selectOne(eq);

        String loginVerifyToken = jwtTool.createToken(user.getUserUUID(), JwtTool.NORMAL_JWT_EXPIRE_TIME);
        String refreshToken = jwtTool.createToken(user.getUserUUID(), JwtTool.NORMAL_JWT_REFRESH_TIME);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("loginVerifyToken", loginVerifyToken);
        tokenMap.put("refreshToken", refreshToken);

        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        userLoginVO.setToken(tokenMap);

        user.setLastLogin(LocalDateTime.now());
        usersMapper.updateById(user);

        return userLoginVO;
    }

    @Override
    public UserInfoVO getUserInfo(String userId) {

        List<User> UserInfo = lambdaQuery()
                .select()
                .eq(User::getUserUUID, userId)
                .list();

        if (UserInfo.isEmpty()) {
            throw new DbException(USER_NOT_FOUND.getDescription());
        }

        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(UserInfo.get(0),userInfoVO);

        return userInfoVO;
    }



    private List<String> selectSingleColumn(String columnName, SFunction<User, String> getFun) {

        return lambdaQuery()
                .select(getFun)
                .eq(getFun, columnName)
                .list()
                .stream()
                .map(getFun)
                .toList();
    }
}
