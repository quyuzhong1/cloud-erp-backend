package com.erp.server.oms.sdk.authorize;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.PlatformAnnotate;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.RefreshShopTokenDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import com.sdk.oms.walmart.api.WalmartStaticKey;
import com.sdk.oms.walmart.dto.WalmartShopInfoDTO;
import com.sdk.oms.walmart.dto.walmart.WalmartTokenDTO;
import com.sdk.oms.walmart.service.WalmartSdkClientService;
import com.sdk.oms.wildberries.dto.WildberriesResponse;
import com.sdk.oms.wildberries.dto.WildberriesShopInfoDTO;
import com.sdk.oms.wildberries.service.WildberriesSDKService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * WB授权
 * @Author Luo_WG
 * @Date 2023/10/23 18:01
 **/

@Slf4j
@Component
@PlatformAnnotate(method = PlatformDictEnum.WILDBERRIES)
public class WildberriesAuthorize implements IShopAuthorizeService<T> {
    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private ShopAuthService shopAuthService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 获取授权地址
     */
    @Override
    public String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto) {
        return null;
    }

    /**
     * 授权
     * @param dto
     * @return
     */
    @Override
    public Boolean shopAuthorize(ShopAuthorizeDTO dto, HttpServletResponse response) {
        String shopId = dto.getShopId();
        if (StringUtils.isBlank(shopId)) {
            throw new ServiceException(ApiError.ERROR_WALMART_SHOP_ID_NOT_NULL);
        }

        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        //根据店铺id 获取到授权信息
        ShopAuthEntity shopAuth = shopAuthService.getByShopId(shopId);
        if (Objects.isNull(shopAuth)) {
            throw new ServiceException("店铺未找到授权信息");
        }

        WildberriesSDKService sdkService = new WildberriesSDKService();
        WildberriesResponse response1 = sdkService.checkToken(shopAuth.getAccessToken());
        if (!response1.isSuccess()){
            throw new ServiceException("授权校验失败：{}", response1.getMsg());
        }
        shopAuth.setShopId(shopId);
        shopInfo.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        shopInfo.setAuthTime(LocalDateTime.now());
        shopAuthService.saveOrUpdate(shopAuth);
        boolean result = shopInfoService.updateById(shopInfo);
        // 添加到缓存redis
        WildberriesShopInfoDTO shopInfoDTO = new WildberriesShopInfoDTO()
                // 店铺ID
                .setId(shopInfo.getId())
                // 访问token
                .setAccessToken(shopAuth.getAccessToken())
                // 店铺名称
                .setName(shopInfo.getName())
                // 区域id
                .setDictAreaCode(shopInfo.getDictAreaCode())
                // 国家id
                .setDictCountryCode(shopInfo.getDictCountryCode())
                // 负责人id
                .setChargeId(shopInfo.getChargeId())
                //平台账户id
                .setClientId(dto.getClientId())
                //平台店铺秘钥
                .setClientSecret(dto.getClientSecret());
        // platform-token:平台名称:店铺ID
        String tokenKey =  CharSequenceUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.WILDBERRIES.getCode(), shopId);
        redisUtil.set(tokenKey, shopInfoDTO);
        return result;
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
        }
        // 移除缓存
        // platform-token:平台名称:店铺ID
        String tokenKey =  CharSequenceUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.WILDBERRIES.getCode(), shopInfo.getId());
        Object shopInfoObj = redisUtil.get(tokenKey);
        if (null != shopInfoObj) {
            redisUtil.del(tokenKey);
        }
        return result;
    }

    @Override
    public Boolean refreshToken(RefreshShopTokenDTO dto) {
        return Boolean.TRUE;
    }
}
