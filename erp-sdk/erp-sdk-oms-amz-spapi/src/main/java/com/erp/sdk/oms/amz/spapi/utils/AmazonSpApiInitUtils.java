package com.erp.sdk.oms.amz.spapi.utils;

import com.common.core.utils.UUID;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.*;
import com.erp.sdk.oms.amz.spapi.client.ApiClient;
import com.erp.sdk.oms.amz.spapi.enums.AmazonEndpointsEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;

/**
 * 亚马逊客户端公共初始化
 */

public class AmazonSpApiInitUtils {

    /**
     * 通用的 SP API 初始化方法
     *
     * @param tClass        要初始化的 API 类
     * @param shopInfoDTO   店铺信息
     * @param isSandbox     是否使用沙箱环境
     * @param <T>           API 类型
     * @return 已初始化的 API 客户端实例
     */
    public static <T> T create(Class<T> tClass, AmazonShopInfoDTO shopInfoDTO, boolean isSandbox) {
        // API 的区域信息
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        AmazonEndpointsEnum endpointsEnum = marketplaceEnum.getEndpointsEnum();

        AWSAuthenticationCredentials awsAuthenticationCredentials = new AWSAuthenticationCredentials(shopInfoDTO.getAccessKeyId(), shopInfoDTO.getSecretKey(), endpointsEnum.getRegion());

        LWAAuthorizationCredentials lwaAuthorizationCredentials = new LWAAuthorizationCredentials(shopInfoDTO.getClientId(), shopInfoDTO.getClientSecret(), shopInfoDTO.getRefreshToken(), shopInfoDTO.getAuthUrl(), null);

        AWSAuthenticationCredentialsProvider awsAuthenticationCredentialsProvider = new AWSAuthenticationCredentialsProvider(shopInfoDTO.getRoleStr(), UUID.randomUUID().toString());

        AWSSigV4Signer awsSigV4Signer = new AWSSigV4Signer(awsAuthenticationCredentials, awsAuthenticationCredentialsProvider);

        LWAAuthorizationSigner lwaAuthorizationSigner = new LWAAuthorizationSigner(lwaAuthorizationCredentials);

        // 创建客户端
        ApiClient apiClient = new ApiClient()
                .setAWSSigV4Signer(awsSigV4Signer)
                .setLWAAuthorizationSigner(lwaAuthorizationSigner)
                .setBasePath(isSandbox ? endpointsEnum.getSandboxEndpoints() : endpointsEnum.getEndpoints());
        try {
            // 利用反射来动态创建不同的 API 实例
            return tClass.getDeclaredConstructor(ApiClient.class).newInstance(apiClient);
        } catch (Exception e) {
            throw new RuntimeException("API 初始化失败", e);
        }
    }

}
