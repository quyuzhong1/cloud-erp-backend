package com.erp.sdk.oms.yunting.cem.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 访问凭证响应
 *
 * @author ERP System
 */
@Data
public class TokenResponse {

    /**
     * 状态码，20000表示成功
     */
    @SerializedName("code")
    private Integer code;

    /**
     * 状态信息
     */
    @SerializedName("msg")
    private String msg;

    /**
     * 结果数据
     */
    @SerializedName("result")
    private TokenResult result;

    /**
     * Token结果数据
     */
    @Data
    public static class TokenResult {

        /**
         * 访问令牌
         */
        @SerializedName("access_token")
        private String accessToken;

        /**
         * 来源标识
         */
        @SerializedName("source")
        private String source;

        /**
         * 令牌类型
         */
        @SerializedName("token_type")
        private String tokenType;

        /**
         * 过期时间（秒），默认1800秒（30分钟）
         */
        @SerializedName("expires_in")
        private Integer expiresIn;

        /**
         * 创建时间戳（毫秒）
         */
        @SerializedName("create_timestamp")
        private Long createTimestamp;
    }
}

