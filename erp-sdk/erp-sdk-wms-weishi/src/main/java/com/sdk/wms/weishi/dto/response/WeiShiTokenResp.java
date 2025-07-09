package com.sdk.wms.weishi.dto.response;

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
public class WeiShiTokenResp {

    /**
     * accessToken
     */
    private String accessToken;
    /**
     * accessToken过期时间
     */
    private Long expireIn;
}
