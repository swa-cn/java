package com.fenglin.springboottest.service;

import com.fenglin.springboottest.dto.RegisterRequest;
import com.fenglin.springboottest.entity.AppUser;

public interface AuthService {

    /** 注册（用户名 + 密码 + 图形验证码，不收集邮箱）。失败抛出 AuthException（携带 error 字段与 HTTP 状态）。 */
    void register(RegisterRequest req);

    /** 用户名是否已存在（大小写不敏感） */
    boolean isUsernameTaken(String username);

    /** 登录。支持用户名或历史邮箱登录（历史账号可能带邮箱，新注册用户 email 为 NULL，只能用户名登录）。失败抛出 AuthException('notfound'/'wrongpw', 401)。 */
    AppUser login(String account, String password);
}
