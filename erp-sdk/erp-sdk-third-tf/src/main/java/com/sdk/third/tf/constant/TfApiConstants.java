package com.sdk.third.tf.constant;

/**
 * TF Fiscal API常量类
 * 
 * @author system
 * @date 2025/01/XX
 */
public class TfApiConstants {

    /**
     * API基础URL
     */
    public static final String BASE_URL = "https://tffiscal.com.br/api";

    /**
     * Content-Type
     */
    public static final String CONTENT_TYPE_JSON = "application/json;charset=utf-8";

    /**
     * Header字段名常量
     */
    public static class Header {
        /**
         * 签名Header字段名
         */
        public static final String SIGN = "sign";

        /**
         * 时间戳Header字段名
         */
        public static final String TIMESTAMP = "timestamp";

        /**
         * Token Header字段名
         */
        public static final String TOKEN = "token";
    }

    /**
     * 字典类型常量
     */
    public static class DictType {
        /**
         * 平台访问令牌（经销商token/b2b_token）
         */
        public static final String TF_ACCESS_TOKEN = "TF-ACCESS_TOKEN";

        /**
         * AppKey（用于签名）
         */
        public static final String TF_APP_KEY = "TF-APP-KEY";
    }
}
