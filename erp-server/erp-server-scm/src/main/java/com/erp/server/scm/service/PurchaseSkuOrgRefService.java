package com.erp.server.scm.service;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.entity.PurchaseSkuOrgRefEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.PurchaseSkuOrgRefDTO;

import java.util.List;

/**
 * <p>
 * SKU与采购组织关系 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-05-28
 */
public interface PurchaseSkuOrgRefService extends SuperService<PurchaseSkuOrgRefEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-05-28
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PurchaseSkuOrgRefDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-05-28
    * @param dto
    * @return
    */
    Boolean update(PurchaseSkuOrgRefDTO.UpdateDTO dto);


    void addByPurchasePrice(PurchasePriceEntity entity);

    List<PurchaseSkuOrgRefEntity> getBySkuIdList(List<String> skuIdList);
}
