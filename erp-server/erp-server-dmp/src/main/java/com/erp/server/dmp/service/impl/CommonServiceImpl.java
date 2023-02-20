package com.erp.server.dmp.service.impl;

import com.erp.common.business.interceptor.CommonInterceptor;
import com.erp.common.business.vo.LoginUser;
import com.erp.server.dmp.service.CommonService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Classname CommonServiceImpl
 * @Description TODO
 * @Date 2022-12-09 10:04
 * @Created by yl
 */
@Service
public class CommonServiceImpl implements CommonService {

    @Override
    public LoginUser getUserInfo() {
        String userId = "";
        String userName = "";
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (Objects.isNull(loginUser)) {
            loginUser = new LoginUser();
            loginUser.setUid(userId);
            loginUser.setUserName(userName);
        }
        return loginUser;
    }
}
