package com.sdk.tms.kuaidi100.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 快递100订阅推送接口响应对象.
 *
 * @author jack
 * @date 2026-07-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kuaidi100SubscribeResponse implements Serializable {

    /**
     * 请求结果.
     */
    private Boolean result;

    /**
     * 返回码.
     */
    private String returnCode;

    /**
     * 返回消息.
     */
    private String message;
}
