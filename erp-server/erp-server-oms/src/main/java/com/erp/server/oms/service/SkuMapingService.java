package com.erp.server.oms.service;

import com.erp.model.oms.entity.SkuMapingEntity;
import com.common.business.service.SuperService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * sku 对照表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
public interface SkuMapingService extends SuperService<SkuMapingEntity> {

    void downloadTemplate(HttpServletResponse response);

    /**
     * 导入sku对照信息
     * @author yl
     * @date 2023-06-29 11:01
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     */
    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response);
}
