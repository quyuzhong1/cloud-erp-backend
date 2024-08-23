package com.erp.server.dmp.service;

import com.common.business.service.SuperService;
import com.erp.model.dmp.dto.DmpPushWdtDetailDTO;
import com.erp.model.dmp.entity.DmpPushWdtDetailEntity;

import java.util.List;

/**
 * 推送旺店通中间表明细Service
 * @date 2024-07-25
 * @author tanmujin
 */
public interface DmpPushWdtDetailService extends SuperService<DmpPushWdtDetailEntity> {
    /**
     * 批量新增
     * @param dtoList
     * @return
     * @date: 2024-07-25
     * @author: tanmujin
     */
    Boolean addBatch(List<DmpPushWdtDetailDTO> dtoList);
}
