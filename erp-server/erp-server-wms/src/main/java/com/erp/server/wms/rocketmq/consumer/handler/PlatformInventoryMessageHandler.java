package com.erp.server.wms.rocketmq.consumer.handler;

import cn.hutool.json.JSONObject;
import com.common.core.controller.vo.ApiResult;

/**
 * 平台库存消息处理器（策略接口）
 */
public interface PlatformInventoryMessageHandler {

    /**
     * 顺序值，越小优先级越高
     */
    default int order() {
        return 1000;
    }

    /**
     * 是否支持当前消息
     */
    boolean supports(JSONObject message);

    /**
     * 处理消息
     */
    ApiResult<Object> handle(JSONObject message);
}
