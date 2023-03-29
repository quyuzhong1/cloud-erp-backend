package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.PurchaseApplicationRefPoDTO;
import com.erp.model.scm.entity.PurchaseApplicationRefPoEntity;

import java.util.List;

/**
 * <p>
 * 采购申请单和采购订单关联表 服务类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
public interface PurchaseApplicationRefPoService extends SuperService<PurchaseApplicationRefPoEntity> {
    /**
     * @description: 根据采购申请明细ids查询
     * @author Will
     * @date: 2023/3/28 20:12
     * @param dto
     * @return List<ListDTO>
     */
    List<PurchaseApplicationRefPoDTO.ListDTO> list(PurchaseApplicationRefPoDTO.SearchParamDTO dto);
}
