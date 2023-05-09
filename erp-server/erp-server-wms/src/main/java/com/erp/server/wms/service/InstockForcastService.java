package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.inventory.InstockForcastDTO;
import com.erp.model.wms.entity.InstockForcastEntity;

/**
 * <p>
 * 入库预报表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-09
 */
public interface InstockForcastService extends SuperService<InstockForcastEntity> {


    /**
     * 根据采购订单生成入库预报单
     * @param dto
     */
    void generateByPurchaseOrder(InstockForcastDTO.AddDTO dto);

}
