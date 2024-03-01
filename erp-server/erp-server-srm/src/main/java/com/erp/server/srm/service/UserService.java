package com.erp.server.srm.service;

import com.erp.model.sys.dto.UserPagingSearchDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @author zdy
 * @ClassName UserService
 * @description: TODO
 * @date 2024年01月09日
 * @version: 1.0
 */
public interface UserService {
    String getSupplierId();
    /**
     * 导出供应商协作用户列表
     * @param dto
     * @param response
     */
    void exportSupplier(UserPagingSearchDTO dto, HttpServletResponse response);
    /**
     * 导入供应商协作用户
     *
     * @param excelFile
     * @param response
     * @param isSuper 是否是管理员
     * @return
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);

    void downloadTemplate(HttpServletResponse response);
}
