package com.erp.server.oms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.ShopCostEntity;
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
     * 单个费率设置
     * @author yl
     * @date 2023-08-23 15:31
     * @param dto
     * @param dto
     * @return com.common.business.dto.base.BatchResultDTO
     */
    Boolean setCost(ShopDTO.SetCostDTO dto);

    /**
     * 批量设置 费率
     * @param shop
     * @param dto
     * @return
     */
    BatchResultDTO batchSetCost(ShopInfoEntity shop, ShopDTO.BatchSetCostDTO dto);

    /**
     * 店铺的费率详情
     * @author yl
     * @date 2023-08-24 18:08
     * @param shopId
     * @return com.erp.model.oms.dto.ShopDTO.ViewCostDTO
     */
    ShopDTO.ViewCostDTO viewCost(String shopId);
    /**
     * @description: 根据店铺id查询
     * @author Will
     * @date: 2023/9/8 11:56
     * @param shopId
     * @return ShopCostEntity
     */
    ShopCostEntity getByShopId(String shopId);
}
