package com.erp.server.scm.service;

import com.common.business.vo.LoginUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    public LoginUser getUserInfo();

    
    /**
     * 公共的下载模板
     * @author yl
     * @date 2023-03-21 9:21
     * @param request
     * @param response
     * @return void
     */
    void downloadTemplate(HttpServletRequest request, HttpServletResponse response,String type);
}
