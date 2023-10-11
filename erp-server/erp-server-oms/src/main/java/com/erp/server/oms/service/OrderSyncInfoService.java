package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.entity.SoInfoEntity;

/**
 * @author zdy
 * @ClassName OrderInfoServiceImpl
 * @description: 订单同步处理服务类
 * @date 2023年10月10日
 * @version: 1.0
 */
public interface OrderSyncInfoService extends SuperService<SoInfoEntity> {
    /**
     * 异步推送订单数据到mq
     * @param soInfoEntity
     */
    void asyncOrderToDmp(SoInfoEntity soInfoEntity);
}
