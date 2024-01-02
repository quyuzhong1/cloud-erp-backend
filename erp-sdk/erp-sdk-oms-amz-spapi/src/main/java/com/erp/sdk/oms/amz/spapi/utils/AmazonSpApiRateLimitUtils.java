package com.erp.sdk.oms.amz.spapi.utils;

import com.alibaba.fastjson.JSONObject;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.RateLimitConfiguration;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.RateLimitConfigurationOnRequests;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 亚马逊速率限制工具类
 *
 * @author Jim
 * @date 2023/10/16 12:13
 */
@Slf4j
@Component
public class AmazonSpApiRateLimitUtils {

    @Resource
    private RedisUtil redisUtil;

    public RateLimitConfiguration buildConfig(AmazonRequestTypeRateLimiterEnum rateLimiterEnum, String limitKey){
        if (StringUtils.isBlank(limitKey)){
            throw new ServiceException("找不到速率配置缓存key");
        }
        JSONObject extentJsonObj = rateLimiterEnum.getExtentJsonObj();
        // 默认请求速率配置
        String rateLimitStr = extentJsonObj.getString(AmazonRequestTypeRateLimiterEnum.rateLimitName);
//        String burstStr = extentJsonObj.getString(AmazonRequestTypeRateLimiterEnum.burstName);
        Long waitTimeOutInMilliSeconds = 10000L;
        // 获取动态速率
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj){
            rateLimitStr = limitObj.toString();
        }
        return RateLimitConfigurationOnRequests.builder()
                .rateLimitPermit(Double.parseDouble(rateLimitStr))
                .waitTimeOutInMilliSeconds(waitTimeOutInMilliSeconds)
                .build();
    }

}
