package com.sdk.tms.kuaidi100.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 快递100订阅推送接口外层请求对象.
 *
 * @author jack
 * @date 2026-07-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kuaidi100SubscribeRequest implements Serializable {

    /**
     * 返回格式，固定json.
     */
    @Builder.Default
    private String schema = "json";

    /**
     * JSON字符串，内容见Kuaidi100SubscribeParam.
     *
     * @see Kuaidi100SubscribeParam
     */
    private String param;
}
