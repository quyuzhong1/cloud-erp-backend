package com.sdk.oms.shopify.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.base.AuthorizeDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.sdk.oms.shopify.constant.ShopifyConstant;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.erp.rpc.oms.feign.ShopInfoFeign;
/**
 * @author Lambda
 * @Classname ShopServer1
 * @Description TODO
 * @Date 2023-08-28 19:54
 * @Created by yl
 */
@Slf4j
@Component
public class ShopSdkServer {

    private static RedisUtil redisUtil;

    @Resource
    private ShopInfoFeign shopInfoFeign;


    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    public void setRedisUtil(RedisUtil redisUtil){
        ShopSdkServer.redisUtil = redisUtil;
    }

    /**
     * 获取授权的url
     *
     * @param entity
     * @param shopDomain
     * @return
     */
    public String getShopAuthorizeUrl(CfgAppClientEntity entity, String shopDomain,String shopId) {
        if (Objects.isNull(entity)) {
            return "";
        }
        String grantOptions = "per-user";
        String shopAuthorizeUrl = String.format(entity.getUrl(), shopDomain, entity.getClientId(), grantOptions, entity.getRedirectUrl(), ShopifyConstant.SHOP_SCOPE,shopId);
        return shopAuthorizeUrl;
    }


    /**
     * 获取到店铺的授权信息
     *
     * @param
     * @return com.alibaba.fastjson2.JSONObject
     * @author yl
     * @date 2023-08-29 8:59
     */
    public String getShopAuthorizeInfo(AuthorizeDTO.FindShopAuthorizeDTO dto, HttpServletResponse response) {
        String code = dto.getCode();
        String host = dto.getHost();
        String shop = dto.getShop();
        String timestamp = dto.getTimestamp();
        String hmac = dto.getHmac();
        String clientSecret = dto.getClientSecret();
        String params = "code=" + code + "&host=" + host + "&shop=" + shop + "&timestamp=" + timestamp;
        boolean verify = verifyShop(params, hmac, shop, clientSecret);
        if (!verify) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new ServiceException(ApiError.ERROR_401);
        }
        String accessTokenUrl = dto.getAccessTokenUrl();
        String path = String.format(accessTokenUrl, shop);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("client_id", dto.getClientId());
        paramsMap.put("client_secret", dto.getClientSecret());
        paramsMap.put("code", dto.getCode());
//        Request request = new Request.Builder()

//                .post(OkHttpUtils.createFormBody(paramsMap))
//                .headers(OkHttpUtils.createHeaders(null))
//                .url(path)
//                .build();
        try {
//            Response execute = OkHttpUtils.retryClient.newCall(request).execute();
//            ResponseBody bodyStr = execute.body();
            String bodyStr = OkHttpUtils.doPost(path, paramsMap, null);
            if (null != bodyStr){
                return bodyStr;
            } else {
                throw new ServiceException("Request Shopify error body is null");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 验证店铺授权信息
     *
     * @param params
     * @param hmac
     * @param shop
     * @return
     */
    public boolean verifyShop(String params, String hmac, String shop, String clientSecret) {
        HMac HMAC = new HMac(HmacAlgorithm.HmacSHA256, clientSecret.getBytes(StandardCharsets.UTF_8));
        String digest = HMAC.digestHex(params.getBytes(StandardCharsets.UTF_8));
        Boolean verify = HMAC.verify(digest.getBytes(StandardCharsets.UTF_8), hmac.getBytes(StandardCharsets.UTF_8));
        if (!verify) {
            log.info("shop[{}], digest[{}], verify[{}]", shop, digest, verify);
        }
        return verify;
    }

    /**
     * 缓存获取Token
     */
    public ShopifyShopInfoDTO getTokenAndDomainByShopId(String shopId) {
        // platform-token:平台名称:店铺ID
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.SHOPIFY.getCode(), shopId);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj) {
            if (tokenObj instanceof ShopifyShopInfoDTO) {
                return (ShopifyShopInfoDTO) tokenObj;
            }
        }else {
            ShopAuthEntity shopAuthEntity = shopInfoFeign.getShopAuthByShopId(shopId);
            ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(shopId);
            CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
            AppClientEnum appClientEnum = AppClientEnum.TIKTOK_ACCESS_TOKEN;
            findDTO.setBusinessType(appClientEnum.getBusinessType());
            findDTO.setDictPlatform(appClientEnum.getPlatform());
            findDTO.setPlatformType(appClientEnum.getPlatformType());
            CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            if (Objects.isNull(cfgAppClient)) {
                return null;
            }
            ShopifyShopInfoDTO shopInfoDTO = new ShopifyShopInfoDTO()
                    // 店铺ID
                    .setId(shopInfoEntity.getId())
                    // 访问token
                    .setAccessToken(shopAuthEntity.getAccessToken())
                    // 店铺名称
                    .setName(shopInfoEntity.getName())
                    // 区域id
                    .setDictAreaCode(shopInfoEntity.getDictAreaCode())
                    // 国家id
                    .setDictCountryCode(shopInfoEntity.getDictCountryCode())
                    // 负责人id
                    .setChargeId(shopInfoEntity.getChargeId())
                    // 店铺全域名: SHOP_NAME.myshopify.com
                    .setShopDomain(shopInfoEntity.getDomain().concat(ShopifyConstant.DOMAIN));

            redisUtil.set(tokenKey, shopInfoDTO, shopAuthEntity.getExpiresIn());
        }
        return null;
    }

}
