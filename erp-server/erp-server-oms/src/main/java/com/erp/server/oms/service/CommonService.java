package com.erp.server.oms.service;

import com.common.business.vo.LoginUser;

/**
 * @author Lambda
 * @Classname CommonService
 * @Description TODO
 * @Date 2023-05-11 19:21
 * @Created by yl
 */
public interface CommonService {

    /**
     * 获取用户信息
     * @author yl
     * @date 2023-03-15 11:58
     * @param
     * @return com.common.business.vo.LoginUser
     */
    public LoginUser getUserInfo();
}
