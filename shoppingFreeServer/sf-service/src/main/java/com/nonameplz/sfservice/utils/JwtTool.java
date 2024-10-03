package com.nonameplz.sfservice.utils;

import com.nonameplz.sfcommon.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

@Component
public class JwtTool {
    public static final Duration NORMAL_JWT_EXPIRE_TIME = Duration.ofMinutes(50);
    public static final Duration NORMAL_JWT_REFRESH_TIME = Duration.ofDays(14);

    //    private final JWTSigner jwtSigner;
    private final KeyPair JWTKeyPair;

    public JwtTool(KeyPair keyPair) {
//        this.jwtSigner = JWTSignerUtil.createSigner("RS256", keyPair);
        this.JWTKeyPair = keyPair;
    }

    /**
     * 创建 access-token
     *
     * @param userUUID 用户信息
     * @return access-token
     */
    public String createToken(String userUUID, Duration ttl) {
        // 1.生成jws
//        return JWT.create()
//                .setPayload("userUUID", userUUID)
//                .setExpiresAt(new Date(System.currentTimeMillis() + ttl.toMillis()))
//                .setSigner(jwtSigner)
//                .sign();
        HashMap<String, Object> jwtMap = new HashMap<>();
        jwtMap.put("UserUUID", userUUID);
        return Jwts.builder()
                .setSubject(userUUID)
                .setClaims(jwtMap)
                .setIssuedAt(new Date())
                .setExpiration(Date.from((Instant.now()).plus(ttl)))
                .signWith(SignatureAlgorithm.RS256, JWTKeyPair.getPrivate())
                .compact();
    }

    /**
     * 解析token
     *
     * @param token token
     * @return 解析刷新token得到的用户信息
     */
//    public String parseToken(String token) {
//        // 1.校验token是否为空
//        if (token == null) {
//            throw new UnauthorizedException("未登录");
//        }
//        // 2.校验并解析jwt
//        JWT jwt;
//        try {
//            jwt = JWT.of(token).setSigner(jwtSigner);
//        } catch (Exception e) {
//            throw new UnauthorizedException("无效的token", e);
//        }
//        // 2.校验jwt是否有效
//        if (!jwt.verify()) {
//            // 验证失败
//            throw new UnauthorizedException("无效的token");
//        }
//        // 3.校验是否过期
//        try {
//            JWTValidator.of(jwt).validateDate();
//        } catch (ValidateException e) {
//            throw new UnauthorizedException("token已经过期");
//        }
//        // 4.数据格式校验
//        Object userPayload = jwt.getPayload("user");
//        if (userPayload == null) {
//            // 数据为空
//            throw new UnauthorizedException("无效的token");
//        }
//
//        // 5.数据解析
//        try {
//            return Long.valueOf(userPayload.toString());
//        } catch (RuntimeException e) {
//            // 数据格式有误
//            throw new UnauthorizedException("无效的token");
//        }
//        return null;
//    }

    // 校验 JWT 令牌
    public String validateToken(String token) {
        // 1.校验token是否为空
        if (token == null) {
            throw new UnauthorizedException("未登录");
        }

        String UserUUID = null;
        Date expirationDate;

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(JWTKeyPair.getPublic()) // 使用公钥进行验证
                    .parseClaimsJws(token)
                    .getBody();
            UserUUID = claims.get("UserUUID", String.class);
            expirationDate = claims.getExpiration();
        } catch (Exception e) {
            // JWT 无效
            throw new UnauthorizedException("无效的token");
        }

        //校验令牌是否过期
        if (expirationDate.before(new Date())){
            throw new UnauthorizedException("token已经过期");
        }

        if (UserUUID == null) {
            throw new UnauthorizedException("无效的token");
        }

        return UserUUID;

    }
}