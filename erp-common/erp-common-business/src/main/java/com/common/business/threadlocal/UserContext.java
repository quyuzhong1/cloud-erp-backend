package com.common.business.threadlocal;

import com.common.business.vo.LoginUser;

import java.util.Objects;

public final class UserContext {
    private UserContext() {
    }

    private static final ThreadLocal<LoginUser> userThreadLocal = new ThreadLocal<>();

    public static void setLoginUser(LoginUser loginUser) {
        userThreadLocal.set(loginUser);
    }

    public static LoginUser getLoginUser() {
        return userThreadLocal.get();
    }

    /**
     * 获取登录用户，不存在则取系统用户
     * @return {@link LoginUser}
     */
    public static LoginUser getNonLoginUser() {
        LoginUser loginUser = userThreadLocal.get();
        if (Objects.isNull(loginUser)) {
            loginUser = new LoginUser();
            loginUser.setUid("0");
            loginUser.setUserName("system");
            loginUser.setUserAccount("");
        }
        return loginUser;
    }

    /**
     * 获取登录用户，不存在则设置为空
     * @return {@link LoginUser}
     */
    public static LoginUser getDefaultLoginUser() {
         return getNonLoginUser();
    }

    public static void clear() {
        userThreadLocal.remove();
    }
}
