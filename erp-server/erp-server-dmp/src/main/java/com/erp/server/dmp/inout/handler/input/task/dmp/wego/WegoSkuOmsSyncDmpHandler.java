package com.erp.server.dmp.inout.handler.input.task.dmp.wego;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @deprecated 已改走旧链路 {@link WegoSkuInfoDmpHandler}（Mongo → dmp 表 → Product MQ → PlatformListingConsumer）。
 * 环境配置若仍指向本类名，会继续生效（本类直接继承新 Handler）；请尽快把 Input Handler 改为 {@code WegoSkuInfoDmpHandler}。
 */
@Deprecated
@Service
@Scope("prototype")
public class WegoSkuOmsSyncDmpHandler extends WegoSkuInfoDmpHandler {
}
