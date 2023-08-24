package com.erp.server.oms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.ShopCostEntity;
import com.common.business.service.SuperService;
import com.erp.model.oms.entity.ShopInfoEntity;

/**
 * <p>
 * 店铺费用表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-22
 */
public interface ShopCostService extends SuperService<ShopCostEntity> {

    /**
     * 店铺设置费率
     * @author yl
     * @date 2023-08-23 15:31
     * @param shop
     * @param dto
     * @return com.common.business.dto.base.BatchResultDTO
     */
    BatchResultDTO setCost(ShopInfoEntity shop, ShopDTO.SetCostDTO dto);
}
