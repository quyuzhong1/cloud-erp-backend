package com.sdk.wms.jifeng.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Max;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JiFengAuthRequest {

    /**
     * 开发者应用Id
     */
    private String clientId;

    /**
     * 开发者应用Id
     */
    private String clientSecret;

    /**
     * wms域名
     */
    private String domain;
    /**
     * OMS账号邮箱
     */
    private String email;
    /**
     * 可选参数，授权成功后会原值返回
     */
    private String q;
    /**
     * oms授权用token
     */
    private String token;


    private String key;
    private String refreshToken;

    private Integer userId;

}
