package com.erp.server.oms.service.impl;

import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.erp.server.oms.service.CommonService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author yl
 * @Classname CommonServiceImpl
 * @Description TODO
 * @Date 2023-03-15 11:50
 * @Created by yl
 */
@Service
public class CommonServiceImpl implements CommonService {
    @Override
    public LoginUser getUserInfo() {
        String userId = "1549948476757303297";
        String userName = "admin";
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (Objects.isNull(loginUser)) {
            loginUser = new LoginUser();
            loginUser.setUid(userId);
            loginUser.setUserName(userName);
            loginUser.setUserAccount("");
        }
        return loginUser;
    }

}
