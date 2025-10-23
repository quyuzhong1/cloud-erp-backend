package com.common.business.threadlocal;

import com.common.business.constant.UserStateConstants;
import com.common.business.vo.LoginUser;

import java.util.HashMap;
import java.util.Objects;

public final class UserContext {
    private UserContext() {
    }

    private static final ThreadLocal<LoginUser> userThreadLocal = new ThreadLocal<>();

    public static void setLoginUser(LoginUser loginUser) {
        userThreadLocal.set(loginUser);
    }

    public static LoginUser getLoginUser() {
        if(Objects.nonNull(UserContext.getIsUserSystem()) && UserContext.getIsUserSystem()){
            LoginUser loginUser = new LoginUser();
            loginUser.setUid(UserStateConstants.USER_SYSTEM_ID);
            loginUser.setUserName(UserStateConstants.USER_SYSTEM);
            loginUser.setUserAccount("");
            return loginUser;
        }
        return userThreadLocal.get();
    }

    /**
     * 是否使用system用户
     */
    private static final ThreadLocal<Boolean> isUserSystemThreadLocal = ThreadLocal.withInitial(()->false);

    public static void setIsUserSystem(Boolean isUserSystem) {
        isUserSystemThreadLocal.set(isUserSystem);
    }

    public static Boolean getIsUserSystem() {
        return isUserSystemThreadLocal.get();
    }

    /**
     * 获取登录用户，不存在则取系统用户
     * @return {@link LoginUser}
     */
    public static LoginUser getNonLoginUser() {
        if(Objects.nonNull(UserContext.getIsUserSystem()) && UserContext.getIsUserSystem()){
            LoginUser loginUser = new LoginUser();
            loginUser.setUid(UserStateConstants.USER_SYSTEM_ID);
            loginUser.setUserName(UserStateConstants.USER_SYSTEM);
            loginUser.setUserAccount("");
            return loginUser;
        }
        LoginUser loginUser = userThreadLocal.get();
        if (Objects.isNull(loginUser)) {
            loginUser = new LoginUser();
            loginUser.setUid(UserStateConstants.USER_SYSTEM_ID);
            loginUser.setUserName(UserStateConstants.USER_SYSTEM);
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

    public static void clearIsUserSystem() {
        userThreadLocal.remove();
    }


    public static void clear() {
        isUserSystemThreadLocal.remove();
    }
}
