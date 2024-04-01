package com.erp.sdk.oms.amz.spapi.dto;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 亚马逊获取token
 *
 * @Author Jim
 * @Date 2023/11/30
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class AmazonTokenDTO {
    /**
     * 短令牌
     */
    @SerializedName("access_token")
    private String accessToken;
    /**
     * 刷新令牌
     */
    @SerializedName("refresh_token")
    private String refreshToken;
    /**
     * token类型
     */
    @SerializedName("token_type")
    private String tokenType;
    /**
     * 失效时间
     */
    @SerializedName("expires_in")
    private Integer expiresIn;

}
