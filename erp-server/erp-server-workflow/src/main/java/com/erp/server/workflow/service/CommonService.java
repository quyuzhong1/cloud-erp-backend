package com.erp.server.workflow.service;

import com.common.business.vo.LoginUser;

/**
 * @author yl
 * @Classname CommonService
 * @Description TODO
 * @Date 2023-03-15 11:50
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
    LoginUser getUserInfo();

}
