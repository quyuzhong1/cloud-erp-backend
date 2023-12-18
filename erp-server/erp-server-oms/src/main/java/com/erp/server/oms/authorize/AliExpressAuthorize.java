package com.erp.server.oms.authorize;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.service.AliExpressAuthService;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.AuthSaveData;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 速卖通授权
 *
 * @author yl
 * @date 2023-11-21
 */

@Slf4j
@Component
@AuthSaveData(method = PlatformDictEnum.ALI_EXPRESS)
public class AliExpressAuthorize implements IShopAuthorizeService<T> {
    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private ShopAuthService shopAuthService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private AliExpressAuthService aliExpressAuthService;

    /**
     * 获取授权地址
     */
    @Override
    public String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto) {
        ShopInfoEntity shopInfo = shopInfoService.getById(dto.getShopId());
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("店铺不存在");
        }
        AppClientEnum appClient = AppClientEnum.ALI_EXPRESS_AUTHORIZE;
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        findDTO.setBusinessType(appClient.getBusinessType());
        findDTO.setDictPlatform(appClient.getPlatform());
        findDTO.setPlatformType(appClient.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            throw new ServiceException("该类型店铺尚未配置开发者账号");
        }
        // 生成随机数据
        SecureRandom secureRandom = new SecureRandom();
        // 生成 256 字节的随机数据
        byte[] randomBytes = new byte[256];
        secureRandom.nextBytes(randomBytes);
        // 进行 Base64 编码
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        // 添加到缓存
        // 缓存state
        String key = StrUtil.format(RedisCacheConstants.AUTH_ALIEXPRESS_STATE, state);
        redisUtil.set(key, shopInfo.getId(), RedisCacheConstants.THIRD_PARTY_AUTH_EXPIRATION);

        String url = cfgAppClient.getUrl();
        //回调地址
        String redirectUrl = cfgAppClient.getRedirectUrl();
        String clientId = cfgAppClient.getClientId();
        return String.format(url, redirectUrl, clientId, state);
    }

    /**
     * 授权
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean shopAuthorize(ShopAuthorizeDTO dto) {
        // 校验是否是本系统发起
        String stateKey = StrUtil.format(RedisCacheConstants.AUTH_ALIEXPRESS_STATE, dto.getState());
        Object shopIdObj = redisUtil.get(stateKey);
        if (null == shopIdObj){
            throw new ServiceException("信息已失效, 请重新发起授权");
        }
        String shopId = shopIdObj.toString();
        if (StringUtils.isBlank(shopId)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_SHOP_USER_AUTH_PART);
        }
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("店铺不存在");
        }
        String code = dto.getCode();
        if (StringUtils.isBlank(code)) {
            throw new ServiceException(ApiError.ERROR_AUTHORIZE_CODE_NOT_NULL);
        }
        AppClientEnum appClient = AppClientEnum.ALI_EXPRESS_TOKEN;
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        findDTO.setBusinessType(appClient.getBusinessType());
        findDTO.setDictPlatform(appClient.getPlatform());
        findDTO.setPlatformType(appClient.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            throw new ServiceException("该类型店铺尚未配置开发者账号");
        }
        try {
            Map<String, String> paramMap = new HashMap<>(4);
            paramMap.put("clientId", cfgAppClient.getClientId());
            paramMap.put("clientSecret", cfgAppClient.getClientSecret());
            paramMap.put("baseUrl", cfgAppClient.getUrl());
            paramMap.put("code", dto.getCode());
            JSONObject jsonObject = aliExpressAuthService.generateToken(paramMap);
            String resultCode = jsonObject.getOrDefault("code", "").toString();
            //成功
            if (AliexpressConstants.SUCCESS_CODE.equals(resultCode)) {
                //根据店铺id 获取到授权信息
                ShopAuthEntity shopAuth = shopAuthService.getByShopId(shopId);
                if (Objects.isNull(shopAuth)) {
                    shopAuth = new ShopAuthEntity();
                }
                String token=jsonObject.getOrDefault("access_token","").toString();
                String refreshToken=jsonObject.getOrDefault("refresh_token","").toString();
                Integer expiresIn=Integer.valueOf(jsonObject.getOrDefault("expires_in",0).toString());
                shopAuth.setShopId(shopId);
                shopAuth.setToken(token);
                shopAuth.setAccessToken(token);
                shopAuth.setRefreshToken(refreshToken);
                shopAuth.setAppClientId(cfgAppClient.getId());
                shopAuth.setExpiresIn(expiresIn);
                shopInfo.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
                shopInfo.setAuthTime(LocalDateTime.now());
                shopAuthService.saveOrUpdate(shopAuth);
                dmpTaskFeign.createPlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(),shopInfo.getName(), shopInfo.getDictPlatform()));
                shopInfo.setIsGenTask(Boolean.TRUE);
                shopInfoService.updateById(shopInfo);
                AliExpressShopInfoDTO shopInfoDTO=new AliExpressShopInfoDTO();
                shopInfoDTO.setId(shopId);
                shopInfoDTO.setClientId(cfgAppClient.getClientId());
                shopInfoDTO.setClientSecret(cfgAppClient.getClientSecret());
                shopInfoDTO.setBaseUrl(cfgAppClient.getUrl());
                shopInfoDTO.setName(shopInfo.getName());
                shopInfoDTO.setToken(token);
                String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.ALI_EXPRESS.getCode(), shopId);
                redisUtil.set(tokenKey, shopInfoDTO,expiresIn);

                redisUtil.del(stateKey);
            } else {
                String msg = jsonObject.getOrDefault("message", "").toString();
                throw new ServiceException(ApiError.ERROR_AUTHORIZE_FAIL, msg);
            }

        } catch (Exception e) {
            log.error("速卖通授权出错了>>>>>>{}", e);
            return Boolean.FALSE;
        }


        return Boolean.TRUE;
    }

    /**
     * 取消授权
     *
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
            dmpTaskFeign.removePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getName(), shopInfo.getDictPlatform()));
            shopInfo.setIsGenTask(Boolean.FALSE);
            shopInfoService.updateShopInfoById(shopInfo);
        }
        return result;
    }


    /**
     * 刷新token
     */
    public void refreshToken(ShopDTO.RefreshTokenDTO dto){


    }


}
