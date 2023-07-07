package com.erp.server.sys.service;

import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import com.common.business.service.SuperService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 金蝶业务员 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-07
 */
public interface KingdeeBusinessOperatorService extends SuperService<KingdeeBusinessOperatorEntity> {

    
    /**
     * 导入数据
     * @author yl
     * @date 2023-07-07 16:49
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
}
