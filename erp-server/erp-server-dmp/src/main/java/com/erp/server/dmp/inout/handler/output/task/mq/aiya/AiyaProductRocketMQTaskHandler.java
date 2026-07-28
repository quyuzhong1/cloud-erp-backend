package com.erp.server.dmp.inout.handler.output.task.mq.aiya;

import com.common.business.enums.PlatformDictEnum;
import com.erp.server.dmp.inout.handler.output.task.mq.AbstractWarehouseProductRocketMQTaskHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * 爱亚仓库商品 Output：推送 RocketMQ，由 OMS PlatformListingConsumer 消费落库。
 */
@Service
@Scope("prototype")
public class AiyaProductRocketMQTaskHandler extends AbstractWarehouseProductRocketMQTaskHandler {

    @Override
    protected String defaultSourcePlatform() {
        return PlatformDictEnum.AIYA.getCode();
    }
}
