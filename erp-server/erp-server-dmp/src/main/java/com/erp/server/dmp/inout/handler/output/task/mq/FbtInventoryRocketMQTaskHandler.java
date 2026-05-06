package com.erp.server.dmp.inout.handler.output.task.mq;

import com.erp.server.dmp.inout.handler.output.task.mq.eccang.EccangInventoryRocketMQTaskHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * FBT库存MQ推送处理器
 */
@Service
@Scope("prototype")
public class FbtInventoryRocketMQTaskHandler extends EccangInventoryRocketMQTaskHandler {
}
