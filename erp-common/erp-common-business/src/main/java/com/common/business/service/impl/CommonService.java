package com.common.business.service.impl;

import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;

import java.util.Objects;

public class CommonService {

    private CommonService() {
    }

    public static LoginUser getUserInfo() {
        String userId = "";
        String userName = "";
        LoginUser loginUser = UserContext.getLoginUser();
        if (Objects.isNull(loginUser)) {
            loginUser = new LoginUser();
            loginUser.setUid(userId);
            loginUser.setUserName(userName);
        }
        return loginUser;
    }

}