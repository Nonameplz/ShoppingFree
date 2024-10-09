package com.nonameplz.sfservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.nonameplz.sfcommon.exception.CommonException;
import com.nonameplz.sfcommon.exception.DbException;
import com.nonameplz.sfcommon.exception.UnauthorizedException;
import com.nonameplz.sfcommon.utils.BeanUtils;
import com.nonameplz.sfservice.domain.dto.LoginFormDTO;
import com.nonameplz.sfservice.domain.dto.RegisterFormDTO;
import com.nonameplz.sfservice.domain.dto.ResetPasswordDTO;
import com.nonameplz.sfservice.domain.dto.UserInfoDTO;
import com.nonameplz.sfservice.domain.po.User;
import com.nonameplz.sfservice.domain.po.UserResetpasswordRecord;
import com.nonameplz.sfservice.domain.vo.UserInfoVO;
import com.nonameplz.sfservice.domain.vo.UserLoginVO;
import com.nonameplz.sfservice.mapper.UserResetpasswordRecordMapper;
import com.nonameplz.sfservice.mapper.UsersMapper;
import com.nonameplz.sfservice.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nonameplz.sfservice.utils.AliOSSUtils;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

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
    private final AliOSSUtils aliOSSUtils;
    private final UserResetpasswordRecordMapper userResetPasswordRecordMapper;

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

        if (!registerFormDTO.getPassword().equals(registerFormDTO.getRePassword())) {
            throw new CommonException(PASSWORDS_DO_NOT_MATCH.getDescription(), 500);
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
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userUUID", user.getUserUUID());
        usersMapper.update(user, updateWrapper);

        return userLoginVO;
    }

    @Override
    public UserInfoVO getUserInfo(String userUUID) {

        User UserInfo = lambdaQuery()
                .select()
                .eq(User::getUserUUID, userUUID)
                .one();

        if (UserInfo == null) {
            throw new DbException(USER_NOT_FOUND.getDescription());
        }

        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(UserInfo, userInfoVO);

        return userInfoVO;
    }

    @Override
    public UserInfoVO updateUserInfo(UserInfoDTO userInfoDTO) throws IOException {
        //todo 判断用户账户是否异常
        //1.判断是否为可更新数据
        User updateUser = lambdaQuery()
                .select()
                .eq(User::getUserUUID, userInfoDTO.getUserUUID())
                .one();

        if (!updateUser.getUsername().equals(userInfoDTO.getUsername())) {
            List<String> verifyUsername = selectSingleColumn(userInfoDTO.getUsername(), User::getUsername);
            if (!verifyUsername.isEmpty()) {
                throw new DbException(USERNAME_INUSE.getDescription());
            }
            updateUser.setUsername(userInfoDTO.getUsername());
        }

        if (!updateUser.getEmail().equals(userInfoDTO.getEmail())) {
            List<String> verifyEmail = selectSingleColumn(userInfoDTO.getEmail(), User::getEmail);
            if (!verifyEmail.isEmpty()) {
                throw new DbException(EMAIL_INUSE.getDescription());
            }
            updateUser.setEmail(userInfoDTO.getEmail());
        }

        if (!updateUser.getPhoneNumber().equals(userInfoDTO.getPhoneNumber())) {
            List<String> verifyPhoneNumber = selectSingleColumn(userInfoDTO.getPhoneNumber(), User::getPhoneNumber);
            if (!verifyPhoneNumber.isEmpty()) {
                throw new DbException(PHONE_NUMBER_INUSE.getDescription());
            }
            updateUser.setPhoneNumber(userInfoDTO.getPhoneNumber());
        }


        //2.判断是否要更新头像
        if (userInfoDTO.getAvatarFile() != null) {
            try {
                MultipartFile avatarFile = (MultipartFile) userInfoDTO.getAvatarFile();
                String avatarUrl = aliOSSUtils.upload(avatarFile, "userAvatar/", userInfoDTO.getAvatarUrl());
                updateUser.setAvatarUrl(avatarUrl);
            } catch (Exception e) {
                log.info("不涉及用户头像更新");
            }
        }

        if (!(userInfoDTO.getAddress() == null)) {
            updateUser.setAddress(userInfoDTO.getAddress());
        }

        //3.更新数据库
        updateUser.setUpdatedAt(LocalDateTime.now());
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userUUID", updateUser.getUserUUID());
        usersMapper.update(updateUser, updateWrapper);

        return BeanUtils.copyBean(updateUser, UserInfoVO.class);
    }

    @Override
    public void deleteUser(String userUUID) {

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userUUID", userUUID);
        updateWrapper.set("status", "deleted");

        usersMapper.update(null, updateWrapper);
    }

    @Override
    public void resetUserPassword(ResetPasswordDTO resetPasswordDTO, String userUUID) {

        if (!resetPasswordDTO.getResetPassword().equals(resetPasswordDTO.getRePassword())) {
            throw new CommonException(PASSWORDS_DO_NOT_MATCH.getDescription(), 500);
        }

        User updateUser = lambdaQuery()
                .select()
                .eq(User::getUserUUID, userUUID)
                .one();

        if (updateUser == null) {
            throw new DbException(USER_NOT_FOUND.getDescription());
        }

        if (passwordEncoder.matches(resetPasswordDTO.getResetPassword(),updateUser.getPasswordHash())){
            throw new DbException(CANNOT_USE_OLD_PASSWORD.getDescription());
        }

        QueryWrapper<UserResetpasswordRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userUUID", updateUser.getUserUUID());
        List<UserResetpasswordRecord> userResetPasswordRecords = userResetPasswordRecordMapper.selectList(queryWrapper);

        for (UserResetpasswordRecord userResetPasswordRecord : userResetPasswordRecords) {
            if (passwordEncoder.matches(resetPasswordDTO.getResetPassword(),userResetPasswordRecord.getOldPassword())) {
                throw new DbException(CANNOT_USE_OLD_PASSWORD.getDescription());
            }
        }
        String newPasswordHash = passwordEncoder.encode(resetPasswordDTO.getResetPassword());

        UserResetpasswordRecord userResetPasswordRecord = new UserResetpasswordRecord();
        userResetPasswordRecord.setId(UUID.randomUUID().toString());
        userResetPasswordRecord.setUserUUID(updateUser.getUserUUID());
        userResetPasswordRecord.setOldPassword(updateUser.getPasswordHash());
        userResetPasswordRecord.setCreateTime(LocalDateTime.now());
        userResetPasswordRecordMapper.insert(userResetPasswordRecord);

        updateUser.setPasswordHash(newPasswordHash);
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userUUID", updateUser.getUserUUID());
        usersMapper.update(updateUser, updateWrapper);
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
