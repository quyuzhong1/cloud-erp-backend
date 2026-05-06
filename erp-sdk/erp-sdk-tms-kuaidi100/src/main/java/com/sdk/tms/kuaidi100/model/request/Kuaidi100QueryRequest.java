package com.sdk.tms.kuaidi100.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 功能描述：快递100实时查询外层请求对象
 *
 * @author jack
 * @date 2026-03-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kuaidi100QueryRequest implements Serializable {

    /**
     * 授权码，请向快递100申请企业版获取
     */
    private String customer;

    /**
     * 签名，用于验证身份，按param + key + customer的顺序进行MD5加密
     */
    private String sign;

    /**
     * 由其他字段拼接的JSON字符串参数内容
     * @see Kuaidi100QueryParam
     */
    private String param;
}
