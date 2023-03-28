package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;

/**
 * <p>
 * 采购价变更表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface PurchasePriceChangeService extends SuperService<PurchasePriceChangeEntity> {

    
    /**
     * 添加采购价目变更
     * @author yl
     * @date 2023-03-28 11:49
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceChangeEntity
     */
    PurchasePriceChangeEntity add(PurchasePriceChangeDTO.AddDTO dto);

    /**
     * 提交并审核
     * @author yl
     * @date 2023-03-28 14:08
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(PurchasePriceChangeDTO.AddDTO dto);

    /**
     * 采购价目变更详情
     * @author yl
     * @date 2023-03-28 14:24
     * @param id
     * @return com.erp.model.scm.dto.PurchasePriceChangeDTO.UpdateDTO
     */
    PurchasePriceChangeDTO.UpdateDTO view(String id);
}
