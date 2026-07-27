package com.erp.server.dmp.inout.handler.output.task.mq.wego;

import com.common.business.enums.PlatformDictEnum;
import com.erp.server.dmp.inout.handler.output.task.mq.AbstractWarehouseProductRocketMQTaskHandler;
import org.springframework.stereotype.Component;

/**
 * WEGO 仓库商品 Output：推送 RocketMQ，由 OMS PlatformListingConsumer 消费落库。
 */
@Component
public class WegoProductRocketMQTaskHandler extends AbstractWarehouseProductRocketMQTaskHandler {

    @Override
    protected String defaultSourcePlatform() {
        return PlatformDictEnum.WEGO.getCode();
    }
}
