package com.erp.server.dmp.service.mq;

import com.common.message.handler.AbstractPlatformConsumerHandler;
import org.springframework.transaction.annotation.Transactional;

/**
 * 平台订单消费服务
 *
 * @Author Cloud
 * @Date 2023/8/31 17:10
 **/
public class PlatformOrderConsumerService<T> extends AbstractPlatformConsumerHandler<T> {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(T ext) {

    }
}
