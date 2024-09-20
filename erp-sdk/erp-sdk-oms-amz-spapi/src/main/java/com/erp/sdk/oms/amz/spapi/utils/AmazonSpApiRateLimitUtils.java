package com.erp.sdk.oms.amz.spapi.utils;

import com.alibaba.fastjson.JSONObject;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.RateLimitConfiguration;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.RateLimitConfigurationOnRequests;
import com.erp.sdk.oms.amz.spapi.client.ApiClient;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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

    /**
     * 检查和缓存到redis
     */
    public void checkAndSetRedis(String limitKey, ApiResponse<?> reportWithHttpInfo) {
        String rateLimitStr;
        List<String> limitArray = reportWithHttpInfo.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
        rateLimitStr = limitArray.get(0);
        if (StringUtils.isNotBlank(rateLimitStr)){
            // 设置动态速率，失效时间=1/limit
            BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
            redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
        }
    }

    /**
     * 解析请求响应速率
     */
    public static String parseRateLimit(ApiResponse<?> apiResponse, String defaultRateLimit){
        if (null == apiResponse){
            return defaultRateLimit;
        }
        List<String> limitArray = apiResponse.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
        if (CollectionUtils.isEmpty(limitArray)){
            return defaultRateLimit;
        }
        return limitArray.get(0);
    }
}
