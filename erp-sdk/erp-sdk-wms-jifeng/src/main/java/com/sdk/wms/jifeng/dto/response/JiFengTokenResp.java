package com.sdk.wms.jifeng.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class JiFengTokenResp {

    /**
     * accessToken
     */
    private String accessToken;
    /**
     * accessToken过期时间
     */
    private Long expireIn;
    /**
     * refreshToken过期时间
     */
    private Long refreshExpireIn;
    /**
     * refreshToken
     */
    private String refreshToken;
    /**
     * 授权用户ID
     */
    private Long userId;
}
