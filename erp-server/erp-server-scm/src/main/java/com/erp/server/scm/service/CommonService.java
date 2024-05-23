package com.erp.server.scm.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author yl
 * @Classname CommonService

 * @Date 2023-03-15 11:50
 * @Created by yl
 */
public interface CommonService {


    
    /**
     * 公共的下载模板
     * @author yl
     * @date 2023-03-21 9:21
     * @param request
     * @param response
     * @return void
     */
    void downloadTemplate(HttpServletRequest request, HttpServletResponse response,String type);

    /**
     * @description: 获取当前人需要审核的业务ids
     * @author Will
     * @date: 2023/7/5 11:10
     * @param businessKey
     * @return List<String>
     */
    List<String> listProcessCurBusinessIds (String businessKey);
}
