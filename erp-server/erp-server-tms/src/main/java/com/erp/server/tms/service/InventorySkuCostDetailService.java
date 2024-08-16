package com.erp.server.tms.service;
import com.erp.model.tms.entity.InventorySkuCostDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.InventorySkuCostDetailDTO;

/**
 * <p>
 * SKU存货成本明细 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-08-16
 */
public interface InventorySkuCostDetailService extends SuperService<InventorySkuCostDetailEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-08-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(InventorySkuCostDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-08-16
    * @param dto
    * @return
    */
    Boolean update(InventorySkuCostDetailDTO.UpdateDTO dto);


}
