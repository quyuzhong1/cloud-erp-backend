package com.erp.server.dmp.inout.handler.output.task.mq;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * TikTok 平台退货 MQ 输出（DMP 配置入口）。
 */
@Service
@Scope("prototype")
public class DmpOutputTikTokReturnRocketMQTaskHandler extends DmpOutputPlatformReturnRocketMQTaskHandler {
}
