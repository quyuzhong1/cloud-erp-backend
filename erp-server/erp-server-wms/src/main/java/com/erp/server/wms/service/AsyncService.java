package com.erp.server.wms.service;

import com.erp.model.oms.entity.SoB2cEntity;

import java.util.List;

/**
 * 异步服务类
 *
 * @author Jim
 * @date 2024/5/9 17:23
 */
public interface AsyncService {


    /**
     * 批量异常查询并更新订单状态
     *
     * @param soB2cEntityList 销售订单
     */
    void asyncBatchQueryAndUpdateOrderStatus(List<SoB2cEntity> soB2cEntityList);
}
