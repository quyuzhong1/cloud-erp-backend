package com.erp.server.wms.service;

import com.erp.model.oms.entity.SoB2cEntity;

import java.util.List;

/**
 * 重新生成销售出库单
 * @Author Jim
 * @Date 2024/03/12
 **/
public interface IPlatformRetryService<T> {

    /**
     * 重新生成销售出库单
     */
    Boolean retrySoOutStock(SoB2cEntity currentEntity, List<SoB2cEntity> list);
}
