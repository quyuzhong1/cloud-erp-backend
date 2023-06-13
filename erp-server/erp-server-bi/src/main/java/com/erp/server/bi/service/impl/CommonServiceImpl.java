package com.erp.server.bi.service.impl;

import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.erp.server.bi.service.CommonService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Classname CommonServiceImpl
 * @Description TODO
 * @Date 2022-12-09 10:04
 * @Created by yl
 */
@Service
public class CommonServiceImpl  implements CommonService {
    @Override
    public LoginUser getUserInfo() {
        String userId = "1549948476757303297";
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
