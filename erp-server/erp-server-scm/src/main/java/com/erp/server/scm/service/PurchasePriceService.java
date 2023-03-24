package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.entity.PurchasePriceEntity;

/**
 * <p>
 * 采购价目表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface PurchasePriceService extends SuperService<PurchasePriceEntity> {

    
    /**
     * 添加采购价目表
     * @author yl
     * @date 2023-03-24 12:22
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceEntity
     */
    PurchasePriceEntity add(PurchasePriceDTO.AddDTO dto);
}
