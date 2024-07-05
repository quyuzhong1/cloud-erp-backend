package com.erp.server.wms.service;

import com.common.business.annotation.DataIdempotent;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.tms.dto.LogisticsBillDTO;

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
     * 向物流商更新重量
     */
    void updateLogisticWeight(LogisticsBillDTO.UpdateWeight updateWeight);
    /**
     * 异步标记发货
     *
     * @param soId              B2C订单ID
     * @param soCode            B2C订单单号
     * @param dictPlatform      平台
     * @param submitPlatformUniqueKey    提交平台唯一key
     * @param sourceDTOJson     来源DTO JSON
     * @param businessDesc      当前触发的业务描述
     * @param falseDeliveryFlag
     */
    void asyncShipOrder(String soId, String soCode, String dictPlatform, String submitPlatformUniqueKey, String sourceDTOJson, String businessDesc, boolean falseDeliveryFlag);


    /**
     * 提交平台标记发货
     */
    List<String> submitShipOrder(String soId, String dictPlatform, boolean falseDeliveryFlag, String submitPlatformUniqueKey);
}
