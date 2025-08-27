package com.erp.server.wms.service;

import com.common.business.enums.ApproveTypeEnum;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;

/**
 * 样品台账流水构建器接口
 * @author wuhaotian
 * @date: 2025-08-21
 */
public interface SampleLedgerFlowBuilder {
    
    /**
     * 构建台账流水数据
     * @param sourceId 单据ID
     * @param sourceCode 单据编号
     * @param approveType 审核类型
     * @return 台账流水数据
     */
    SampleLedgerFlowDTO.AddFlowDTO buildFlow(String sourceId, String sourceCode, ApproveTypeEnum approveType);
    
    /**
     * 获取支持的单据类型
     * @return 单据类型
     */
    String getSupportedSourceType();

    Integer calculateQty(Integer originalQty, ApproveTypeEnum approveType);
}
