package com.erp.sdk.oms.amz.spapi.dto;

import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 亚马逊限流信息
 *
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmazonLimitInfoDTO {

    /**
     * 请求类型
     */
    private AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum;

    /**
     * 限流key
     */
    private String limitKey;

    /**
     * 当前亚马逊接口rateLimit
     */
    private String rateLimitStr;

    /**
     * 当前是否限流
     */
    private boolean limitFlag;

}
