package com.cloud.erp.context;

import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class UserService {

    public LoginUser getCurrentUser() {
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)){
            loginUser = new LoginUser();
            loginUser.setUid("0");
            loginUser.setUserName("system");
        }
        return loginUser;
    }
}
