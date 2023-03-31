package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.PurchaseChangeDetailDTO;
import com.erp.model.scm.entity.PurchaseChangeDetailEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
public interface PurchaseChangeDetailService extends SuperService<PurchaseChangeDetailEntity> {
    /**
     * @description: 新增变更单
     * @author Will
     * @date: 2023/3/30 19:41
     * @param details
     * @param purchaseChangeId
     */
    void add(List<PurchaseChangeDetailDTO.AddDTO> details, String purchaseChangeId);
}
