package com.erp.server.wms.service;

import com.common.business.vo.LoginUser;

import java.util.List;

/**
 * @author yl
 * @Classname CommonService

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
    public LoginUser getUserInfo();

    /**
     * @description: 获取当前审核人
     * @author Will
     * @date: 2023/8/2 16:37
     * @param businessKey
     * @return List<String>
     */
    List<String> listProcessCurBusinessIds (String businessKey);
}
