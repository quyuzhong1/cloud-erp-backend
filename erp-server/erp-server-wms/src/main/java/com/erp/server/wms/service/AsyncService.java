package com.erp.server.wms.service;

import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;

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
     * (包含成功单据去重)
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
    /**
     * 自动出库
     * @author will
     * @date 2024/6/28 16:40
     * @param soB2cEntity
     * @param entity
     */
    void soB2cDeliveryAutoOut (SoB2cEntity soB2cEntity, SoB2cDeliveryEntity entity);

    /**
     * 生成销售出库单
     * @author will
     * @date 2024/7/21 21:28
     * @param b2cSoId
     */
    void asyncGenerateB2cSoOutstock (String b2cSoId);
    /**
     * 异步自动出库
     * @author will
     * @date 2024/8/8 20:26
     * @param soB2cEntity
     * @param entity
     */
    void syncSoB2cDeliveryAutoOut(SoB2cEntity soB2cEntity, SoB2cDeliveryEntity entity);

    /**
     * 异步扣减虚拟库存，自动出库
     * @author will
     * @date 2024/8/9 11:07
     * @param entity 
     */
    void syncAutoOut(SoB2cDeliveryEntity entity);

    void asyncCancelThirdWarehouseOrder(SoB2cEntity mainEntity);
}
