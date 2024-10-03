package com.nonameplz.sfservice.interceptor;

import com.nonameplz.sfcommon.utils.UserContext;
import com.nonameplz.sfservice.utils.JwtTool;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.HandlerInterceptor;


@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtTool jwtTool;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NotNull HttpServletResponse response,
                             @NotNull Object handler) throws Exception {
        // 1.获取请求头中的 token
        String token = request.getHeader("Authorization");
        // 2.校验token
        String userId = jwtTool.validateToken(token);
        // 3.存入上下文
        UserContext.setUser(userId);
        // 4.放行
        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request,
                                @NotNull HttpServletResponse response,
                                @NotNull Object handler,
                                Exception ex) throws Exception {
        // 清理用户
        UserContext.removeUser();
    }
}
