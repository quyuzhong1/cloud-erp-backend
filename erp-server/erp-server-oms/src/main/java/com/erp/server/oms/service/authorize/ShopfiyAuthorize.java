package com.erp.server.oms.service.authorize;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.common.business.annotation.SaveData;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.base.AuthorizeDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.*;
import com.sdk.oms.shopify.constant.ShopifyConstant;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Component
@AuthSaveData(method = PlatformDictEnum.SHOPIFY)
public class ShopfiyAuthorize implements IShopAuthorizeService<T> {

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private ShopSdkServer shopSdkServer;

    @Resource
    private ShopAuthService shopAuthService;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 获取授权地址
     * @param dto
     * @return
     */
    @Override
    public String getShopAuthorizeUrl(ShopAuthorizeDTO dto) {
        ShopInfoEntity shopInfo = shopInfoService.getById(dto.getShopId());
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("店铺不存在");
        }
        AppClientEnum appClient = AppClientEnum.SHOP_AUTHORIZE;
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        findDTO.setBusinessType(appClient.getBusinessType());
        findDTO.setDictPlatform(appClient.getPlatform());
        findDTO.setPlatformType(appClient.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        // 全域名：SHOP_NAME.myshopify.com
        String fullDomain = shopInfo.getDomain().concat(ShopifyConstant.DOMAIN);
//        String grantOptions = "per-user";
        // 离线模式：token无过期
        String grantOptions = "offline-access";
        String path = String.format(cfgAppClient.getUrl(), fullDomain, cfgAppClient.getClientId(), grantOptions, cfgAppClient.getRedirectUrl(), ShopifyConstant.SHOP_SCOPE);
        return path;
    }

    /**
     * 授权
     * @param dto
     * @return
     */
    @Override
    public Boolean shopAuthorize(ShopAuthorizeDTO dto) {
        String bodyStr = "";
        try {
            // 二级域名
            String secondDomain = dto.getShop();
            if (dto.getShop().contains(ShopifyConstant.DOMAIN)) {
                secondDomain = dto.getShop().replace(ShopifyConstant.DOMAIN, "");
            }
            ShopInfoEntity shopInfo = shopInfoService.getByDomain(secondDomain);
            if (Objects.isNull(shopInfo)) {
                throw new ServiceException("店铺不存在");
            }
            AppClientEnum appClient = AppClientEnum.SHOP_ACCESS_TOKEN;
            CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
            findDTO.setBusinessType(appClient.getBusinessType());
            findDTO.setDictPlatform(appClient.getPlatform());
            findDTO.setPlatformType(appClient.getPlatformType());
            CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            if (Objects.isNull(cfgAppClient)) {
                throw new ServiceException("shopify应用授权配置不存在");
            }
            AuthorizeDTO.FindShopAuthorizeDTO findShopAuthorize = new AuthorizeDTO.FindShopAuthorizeDTO();
            findShopAuthorize.setAccessTokenUrl(cfgAppClient.getUrl());
            findShopAuthorize.setClientId(cfgAppClient.getClientId());
            findShopAuthorize.setClientSecret(cfgAppClient.getClientSecret());
            findShopAuthorize.setCode(dto.getCode());
            findShopAuthorize.setHmac(dto.getHmac());
            findShopAuthorize.setHost(dto.getHost());
            findShopAuthorize.setShop(dto.getShop());
            findShopAuthorize.setTimestamp(dto.getTimestamp());
            bodyStr = shopSdkServer.getShopAuthorizeInfo(findShopAuthorize);
            JSONObject jsonObject = JSONObject.parseObject(bodyStr);
            //token
            String accessToken = jsonObject.getOrDefault("access_token", "").toString();
            //过期时间
            Integer expiresIn = Integer.valueOf(jsonObject.getOrDefault("expires_in", 0).toString());
            if (StringUtils.isBlank(accessToken)) {
                return Boolean.FALSE;
            }
            String shopId = shopInfo.getId();
            //根据店铺id 获取到授权信息
            ShopAuthEntity shopAuth = shopAuthService.getByShopId(shopId);
            if (Objects.isNull(shopAuth)) {
                shopAuth = new ShopAuthEntity();
            }
            shopAuth.setShopId(shopId);
            shopAuth.setAccessToken(accessToken);
            shopAuth.setToken(accessToken);
            shopAuth.setExpiresIn(expiresIn);
            shopAuth.setAppClientId(cfgAppClient.getId());
            shopInfo.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
            shopInfo.setAuthTime(LocalDateTime.now());
            shopAuthService.saveOrUpdate(shopAuth);
            boolean result = shopInfoService.updateById(shopInfo);
            // 授权后添加任务
            dmpTaskFeign.createPlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(),shopInfo.getName(), shopInfo.getDictPlatform()));
            shopInfo.setIsGenTask(Boolean.TRUE);
            shopInfoService.updateShopInfoById(shopInfo);
            // 添加到缓存redis
            ShopifyShopInfoDTO shopInfoDTO = initShopInfoDTO(shopInfo, accessToken);

            // platform-token:平台名称:店铺ID
            String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.SHOPIFY.getCode(), shopId);
            redisUtil.set(tokenKey, shopInfoDTO);
            return result;
        } catch (Exception e) {
            log.error("店铺授权出错了===> bodyStr==>{} e==>{}", bodyStr, e);
        }

        return Boolean.FALSE;
    }

    /**
     * 取消授权
     * @param dto
     */
    @Override
    public Boolean cancelAuthorize(CancelAuthorizeDTO dto) {
        String shopId = dto.getShopId();
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("店铺不存在");
        }
        //授权状态
        String authStatus = shopInfo.getAuthStatus();
        if (!AuthStatusEnum.ALREADY.getCode().equals(authStatus)) {
            throw new ServiceException("该店铺未授权,无需取消授权");
        }
        shopInfo.setAuthStatus(AuthStatusEnum.CANCEL.getCode());
        Boolean result = shopInfoService.updateById(shopInfo);
        if (result) {
            shopAuthService.removeByShopId(shopId);
            // 删除授权
            dmpTaskFeign.removePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(),shopInfo.getName(), shopInfo.getDictPlatform()));
            shopInfo.setIsGenTask(Boolean.FALSE);
            shopInfoService.updateShopInfoById(shopInfo);
        }
        // 移除缓存
        // platform-token:平台名称:店铺ID
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.SHOPIFY.getCode(), shopInfo.getId());
        Object shopInfoObj = redisUtil.get(tokenKey);
        if (null != shopInfoObj) {
            redisUtil.del(tokenKey);
        }
        return result;
    }

    /**
     * shopInfo Entity 转换DTO
     */
    private ShopifyShopInfoDTO initShopInfoDTO(ShopInfoEntity shopInfo, String accessToken) {
        return new ShopifyShopInfoDTO()
                // 店铺ID
                .setId(shopInfo.getId())
                // 访问token
                .setAccessToken(accessToken)
                // 店铺名称
                .setName(shopInfo.getName())
                // 区域id
                .setDictAreaCode(shopInfo.getDictAreaCode())
                // 国家id
                .setDictCountryCode(shopInfo.getDictCountryCode())
                // 负责人id
                .setChargeId(shopInfo.getChargeId())
                // 店铺全域名: SHOP_NAME.myshopify.com
                .setShopDomain(shopInfo.getDomain().concat(ShopifyConstant.DOMAIN));
    }
}
