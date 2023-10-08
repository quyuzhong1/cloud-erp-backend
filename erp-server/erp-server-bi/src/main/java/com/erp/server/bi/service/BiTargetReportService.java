package com.erp.server.bi.service;

import com.erp.model.bi.dto.TargetFinishDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;

/**
 * @author Will
 * @version 1.0
 * @description: 目标相关报表接口
 * @date 2023/9/14 12:22
 */
public interface BiTargetReportService {
    /**
     * @description: 业绩目标完成
     * @author Will
     * @date: 2023/9/14 16:20
     * @param dto
     * @return LinkedHashMap<Object>
     */
    LinkedHashMap<String, Object> targetFinish(TargetFinishDTO.ParamDTO dto);

    /**
     * 导出
     */
    void exportExcel(TargetFinishDTO.ParamDTO dto, HttpServletResponse response);
}
