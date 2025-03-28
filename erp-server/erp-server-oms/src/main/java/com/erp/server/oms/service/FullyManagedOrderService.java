package com.erp.server.oms.service;

import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.CfgSettingDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author zdy
 * @ClassName FullyManagedOrderService
 * @description: 全托管订单服务
 * @date 2025年03月25日
 * @version: 1.0
 */
public interface FullyManagedOrderService extends SuperService<SoB2cEntity> {
    /**
     * 全托管订单tab列表
     * @param dto
     * @return
     */
    List<SoB2cDTO.TabListDTO> fullyManagedTabList(PermissionsDTO dto);

    /**
     * 设置订单超时预警时间
     * @param timeOutSettingDTO
     */
    void timeOutConfig(CfgSettingDTO.TimeOutSettingDTO timeOutSettingDTO);
    /**
     * 导出模板
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);
    /**
     * 导入Excel
     * @param excelFile
     * @param response
     * @return
     */
    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response);
}
