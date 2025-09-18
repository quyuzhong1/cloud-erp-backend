package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;

import java.util.List;

/**
 * <p>
 * 三方仓发货单明细 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
 */
public interface ThirdWarehouseDeliveryDetailService extends SuperService<ThirdWarehouseDeliveryDetailEntity> {


    List<ThirdWarehouseDeliveryDetailEntity> listByMainId(String mainId);
    List<ThirdWarehouseDeliveryDetailEntity> listByMainIds(List<String> mainIds);

    void removeByMainIds(List<String> mainIds);
}
