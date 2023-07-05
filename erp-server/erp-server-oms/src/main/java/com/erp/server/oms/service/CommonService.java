package com.erp.server.oms.service;

import com.common.business.vo.LoginUser;

import java.util.List;

/**
 * @author Lambda
 * @Classname CommonService

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

    /**
     * @description: 获取当前人需要审核的业务ids
     * @author Will
     * @date: 2023/7/5 11:10
     * @param businessKey
     * @return List<String>
     */
    List<String> listProcessCurBusinessIds (String businessKey);
}
