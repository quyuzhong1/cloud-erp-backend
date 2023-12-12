package com.cloud.erp.gateway.context;

/**
 * @Author Luo_WG
 * @Date 2023/12/6 12:30
 **/
public interface GatewayContextExtraData<T> {

    /**
     * get context extra data
     * @return
     */
    T getData();
}