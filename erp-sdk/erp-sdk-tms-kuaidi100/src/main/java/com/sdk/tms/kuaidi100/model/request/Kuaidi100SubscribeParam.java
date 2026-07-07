package com.sdk.tms.kuaidi100.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 快递100订阅推送接口param参数.
 *
 * @author jack
 * @date 2026-07-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kuaidi100SubscribeParam implements Serializable {

    /**
     * 快递公司编码.
     */
    private String company;

    /**
     * 快递单号.
     */
    private String number;

    /**
     * 快递100授权key.
     */
    private String key;

    /**
     * 订阅附加参数.
     */
    private Parameters parameters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameters implements Serializable {

        /**
         * 推送回调地址.
         */
        private String callbackurl;

        /**
         * 签名盐值，本系统按需求固定为空字符串.
         */
        @Builder.Default
        private String salt = "";

        /**
         * 收、寄件人电话，部分快递公司必填.
         */
        private String phone;

        /**
         * 轨迹增强版本.
         */
        @Builder.Default
        private String resultv2 = "4";
    }
}
