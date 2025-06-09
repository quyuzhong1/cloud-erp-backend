package com.erp.server.oms.sdk.authorize;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.PlatformAnnotate;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.*;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 沃尔玛授权
 * @Author Luo_WG
 * @Date 2023/10/23 18:01
 **/

@Slf4j
@Component
@PlatformAnnotate(method = PlatformDictEnum.WALMART)
public class WalmartAuthorize implements IShopAuthorizeService<T> {
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
        if (StringUtils.isBlank(dto.getClientId())) {
            throw new ServiceException(ApiError.ERROR_WALMART_CLIENT_ID_NOT_NULL);
        }
        if (StringUtils.isBlank(dto.getClientSecret())) {
            throw new ServiceException(ApiError.ERROR_WALMART_CLIENT_SECRET_NOT_NULL);
        }
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        //根据店铺id 获取到授权信息
        ShopAuthEntity shopAuth = shopAuthService.getByShopId(shopId);
        if (Objects.isNull(shopAuth)) {
            shopAuth = new ShopAuthEntity();
        }
        //获取地址
        String url = WalmartStaticKey.baseUrl + "token";

        WalmartSdkClientService walmartSdkClientService = new WalmartSdkClientService();
        WalmartTokenDTO walmartTokenDTO = walmartSdkClientService.sendWalmartPostToken(url, dto.getClientId(), dto.getClientSecret());
        if (ObjectUtil.isEmpty(walmartTokenDTO)) {
            return Boolean.FALSE;
        }

        String appClientId = shopAuth.getAppClientId();
        //判断是否有授权过，如果没有就新增保存店铺秘钥信息，如果有就修改秘钥信息重新授权
        if (StringUtils.isBlank(shopAuth.getAppClientId())) {
            CfgAppClientDTO.AddDTO addDTO = new CfgAppClientDTO.AddDTO();
            addDTO.setBusinessType(AppClientEnum.WALMART_AUTHORIZE.getBusinessType());
            addDTO.setPlatformType(AppClientEnum.WALMART_AUTHORIZE.getPlatformType());
            addDTO.setDictPlatform(dto.getPlatformCode());
            addDTO.setClientId(dto.getClientId());
            addDTO.setClientSecret(dto.getClientSecret());
            appClientId = dmpTaskFeign.addCfgAppClient(addDTO);
            // 授权后添加任务
            dmpTaskFeign.createAndEnablePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getName(), shopInfo.getDictPlatform()));
        } else {
            CfgAppClientDTO.UpdateDTO updateDTO = new CfgAppClientDTO.UpdateDTO();
            updateDTO.setId(appClientId);
            updateDTO.setBusinessType(AppClientEnum.WALMART_AUTHORIZE.getBusinessType());
            updateDTO.setPlatformType(AppClientEnum.WALMART_AUTHORIZE.getPlatformType());
            updateDTO.setDictPlatform(dto.getPlatformCode());
            updateDTO.setClientId(dto.getClientId());
            updateDTO.setClientSecret(dto.getClientSecret());
            dmpTaskFeign.updateCfgAppClient(updateDTO);
        }
        shopAuth.setShopId(shopId);
        shopAuth.setExpiresIn(Integer.valueOf(walmartTokenDTO.getExpiresIn()));
        shopAuth.setAppClientId(appClientId);
        shopAuth.setAccessToken(walmartTokenDTO.getAccessToken());
        shopInfo.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        shopInfo.setAuthTime(LocalDateTime.now());
        shopAuthService.saveOrUpdate(shopAuth);
        boolean result = shopInfoService.updateById(shopInfo);
        shopInfo.setIsGenTask(Boolean.TRUE);
        shopInfoService.updateShopInfoById(shopInfo);
        // 添加到缓存redis
        WalmartShopInfoDTO shopInfoDTO = new WalmartShopInfoDTO()
                // 店铺ID
                .setId(shopInfo.getId())
                // 访问token
                .setAccessToken(walmartTokenDTO.getAccessToken())
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
        String tokenKey =  CharSequenceUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.WALMART.getCode(), shopId);
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
            // 禁用任务
            dmpTaskFeign.disabledPlatformTask(new PlatformTaskDTO.DisabledDTO(shopInfo.getId(),
                    shopInfo.getName(),
                    shopInfo.getDictPlatform(),
                    true,
                    shopInfo.getDictCountryCode(),
                    shopInfo.getPlatformShopCode()));
            shopInfo.setIsGenTask(Boolean.FALSE);
            shopInfoService.updateShopInfoById(shopInfo);
        }
        // 移除缓存
        // platform-token:平台名称:店铺ID
        String tokenKey =  CharSequenceUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.WALMART.getCode(), shopInfo.getId());
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
