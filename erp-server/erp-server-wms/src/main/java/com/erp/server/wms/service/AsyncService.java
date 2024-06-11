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


    /**
     * 异步标记发货
     *
     * @param soId              B2C订单ID
     * @param soCode            B2C订单单号
     * @param dictPlatform      平台
     * @param sourceDTOJson     来源DTO JSON
     * @param businessDesc      当前触发的业务描述
     * @param falseDeliveryFlag
     */
    void asyncShipOrder(String soId, String soCode, String dictPlatform, String sourceDTOJson, String businessDesc, boolean falseDeliveryFlag);
}
