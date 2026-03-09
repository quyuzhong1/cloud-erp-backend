package com.erp.sdk.oms.amz.spapi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.business.constant.BusinessCommonConstants;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * 亚马逊SP-API端点
 *
 * @author Jim
 * @date 2023/10/17 10:13
 */
@Getter
@AllArgsConstructor
public enum AmazonEndpointsEnum {
    // 北美（加拿大、美国、墨西哥和巴西商城）
//    US_EAST_1("us-east-1", "北美", "https://sellingpartnerapi-na.amazon.com", "https://sandbox.sellingpartnerapi-na.amazon.com","https://sellercentral.amazon.com"),
    US_EAST_1("us-east-1", "北美", "https://sellingpartnerapi-na.lianhuatoys.cn", "https://sandbox.sellingpartnerapi-na.amazon.com","https://sellercentral.lianhuatoys.com"),
    // 欧洲（西班牙、英国、法国、荷兰、德国、意大利、瑞典、波兰、埃及、土耳其、阿联酋和印度商城）
//    EN_WAST_1("eu-west-1", "欧洲", "https://sellingpartnerapi-eu.amazon.com", "https://sandbox.sellingpartnerapi-eu.amazon.com","https://sellercentral-europe.amazon.com"),
    EN_WAST_1("eu-west-1", "欧洲", "https://sellingpartnerapi-eu.lianhuatoys.cn", "https://sandbox.sellingpartnerapi-eu.amazon.com","https://sellercentral-europe.lianhuatoys.com"),
    // 远东（新加坡、澳大利亚和日本商城）
//    US_WAST_2("us-west-2", "远东", "https://sellingpartnerapi-fe.amazon.com", "https://sandbox.sellingpartnerapi-fe.amazon.com",null),
    US_WAST_2("us-west-2", "远东", "https://sellingpartnerapi-fe.lianhuatoys.cn", "https://sandbox.sellingpartnerapi-fe.amazon.com",null),

    ;

    /**
     * 区域
     */
    @EnumValue
    @JsonValue
    private final String region;
    /**
     * 名称
     */
    private final String name;
    /**
     * 正式环境端点
     */
    private final String endpoints;
    /**
     * 沙箱环境端点
     */
    private final String sandboxEndpoints;

    /**
     * 批量授权的卖家平台URL
     */
    private final String batchSellerCentralUrl;


    /**
     * 通过Region查找枚举
     */
    public static AmazonEndpointsEnum getByRegion(String region) {
        return Stream.of(AmazonEndpointsEnum.values())
                .filter(e -> e.getRegion().equalsIgnoreCase(region))
                .findFirst()
                .orElse(null);
    }

    /**
     * 通过环境变量获取请求端点
     */
    public String getEndpointsByProfile() {
        if (BusinessCommonConstants.hasProfile("prod")){
            // 正式环境使用正式端点
            return this.endpoints;
        }
        // 其他环境使用沙箱端点
        return this.sandboxEndpoints;
    }

}
