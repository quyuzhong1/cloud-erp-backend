package com.cloud.erp.gateway.context;

import org.springframework.web.server.ServerWebExchange;

/**
 * @Author Luo_WG
 * @Date 2023/12/6 18:27
 **/
public interface ContextExtraDataGenerator<T> {

    /**
     * generate context extra data
     * @param serverWebExchange
     * @return
     */
    GatewayContextExtraData<T> generateContextExtraData(ServerWebExchange serverWebExchange);

}
