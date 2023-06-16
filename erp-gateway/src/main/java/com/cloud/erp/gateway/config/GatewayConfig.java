package com.cloud.erp.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 网关配置
 * @CreateTime: 2023-06-16  14:52
 * @Author: zhangchunlin
 */
@Configuration
@Import(value = {GatewayExceptionConfig.class})
public class GatewayConfig {

    // 后续可以增加限流器、拦截黑名单、调用时长统计等等

}