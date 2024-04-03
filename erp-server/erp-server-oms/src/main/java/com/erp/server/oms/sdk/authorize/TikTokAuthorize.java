package com.erp.server.oms.sdk.authorize;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.PlatformAnnotate;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
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
import com.sdk.oms.tictok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tictok.dto.tiktok.token.TokenDTO;
import com.sdk.oms.tictok.service.TikTokSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * TikTok授权
 * @Author Luo_WG
 * @Date 2024/4/3 10:50
 **/
@Slf4j
@Component
@PlatformAnnotate(method = PlatformDictEnum.TIK_TOK)
public class TikTokAuthorize implements IShopAuthorizeService<T> {

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private ShopAuthService shopAuthService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    public static void main(String[] args) {
        // 给定的时间戳
        long timestamp = 1660556783L;

        // 将时间戳转换为 LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);
        System.out.println(dateTime);
    }

    @Override
    public String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto) {
        List<ShopInfoEntity> shopInfoEntityList = dto.getShopInfoEntityList();
        ShopInfoEntity shopInfo;
        if (CollectionUtils.isEmpty(shopInfoEntityList)) {
            shopInfo = shopInfoService.getById(dto.getShopId());
        } else {
            shopInfo = shopInfoEntityList.get(0);
        }

        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("店铺不存在");
        }
        AppClientEnum appClient = AppClientEnum.TIKTOK_AUTHORIZE;
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        findDTO.setBusinessType(appClient.getBusinessType());
        findDTO.setDictPlatform(appClient.getPlatform());
        findDTO.setPlatformType(appClient.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);

        // 生成随机数据
        SecureRandom secureRandom = new SecureRandom();
        // 生成 256 字节的随机数据
        byte[] randomBytes = new byte[256];
        secureRandom.nextBytes(randomBytes);
        // 进行 Base64 编码
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        // 缓存state
        String key = StrUtil.format(RedisCacheConstants.AUTH_TIKTOK_STATE, state);

        redisUtil.set(key, shopInfo.getId(), RedisCacheConstants.THIRD_PARTY_AUTH_EXPIRATION);

        //拼接授权地址
        String shopAuthorizeUrl = "";
        if (ObjectUtil.isNotEmpty(cfgAppClient)) {
            //https://services.tiktokshop.com/open/authorize?service_id=7348713406587684614&state=%s
            shopAuthorizeUrl = String.format(cfgAppClient.getUrl(), state);
        }
        return shopAuthorizeUrl;
    }

    @Override
    public Boolean shopAuthorize(ShopAuthorizeDTO dto, HttpServletResponse response) {
// 校验是否是本系统发起
        String stateKey = StrUtil.format(RedisCacheConstants.AUTH_TIKTOK_STATE, dto.getState());
        log.error("stateKey:：{}", stateKey);
        Object shopIdObj = redisUtil.get(stateKey);
        if (null == shopIdObj) {
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
        AppClientEnum appClient = AppClientEnum.TIKTOK_ACCESS_TOKEN;
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        findDTO.setBusinessType(appClient.getBusinessType());
        findDTO.setDictPlatform(appClient.getPlatform());
        findDTO.setPlatformType(appClient.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            throw new ServiceException("该类型店铺尚未配置开发者账号");
        }
        Map<String, String> paramMap = new HashMap<>(4);
        paramMap.put("clientId", cfgAppClient.getClientId());
        paramMap.put("clientSecret", cfgAppClient.getClientSecret());
        paramMap.put("redirectUri", cfgAppClient.getRedirectUrl());
        paramMap.put("baseUrl", cfgAppClient.getUrl());
        paramMap.put("code", dto.getCode());

        TokenDTO tokenDTO = tikTokSdkClientService.sendTikTokPostToken(paramMap);
        if (ObjectUtil.isEmpty(tokenDTO)) {
            return Boolean.FALSE;
        }
        //根据店铺id 获取到授权信息
        ShopAuthEntity shopAuth = shopAuthService.getByShopId(shopId);
        if (Objects.isNull(shopAuth)) {
            shopAuth = new ShopAuthEntity();
        }
        shopAuth.setShopId(shopId);
        shopAuth.setToken(tokenDTO.getAccessToken());
        shopAuth.setAccessToken(tokenDTO.getAccessToken());
        shopAuth.setRefreshToken(tokenDTO.getRefreshToken());
        shopAuth.setAppClientId(cfgAppClient.getId());
        shopAuth.setExpiresIn(tokenDTO.getAccessTokenExpireIn());

        // token 过期时间为7天 ，提前一小时过期
        LocalDateTime localDateTime = LocalDateTime.now().plusDays(7L);
        //提前半小时设置token失效，以免失效了以后才刷新容易出错
        LocalDateTime tokenExpireTime = localDateTime.minusMinutes(30);
        shopAuth.setTokenExpireTime(tokenExpireTime);

        shopInfo.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        shopInfo.setAuthTime(LocalDateTime.now());

        Map<String, Object> map = new HashMap<>();
        map.put("openId", tokenDTO.getOpenId());
        map.put("sellerName", tokenDTO.getSellerName());
        map.put("sellerBaseRegion", tokenDTO.getSellerBaseRegion());
        shopInfo.setExtendData(map);
        shopAuthService.saveOrUpdate(shopAuth);
        dmpTaskFeign.createAndEnablePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getName(), shopInfo.getDictPlatform()));
        shopInfo.setIsGenTask(Boolean.TRUE);
        shopInfoService.updateById(shopInfo);
        TikTokShopInfoDTO shopInfoDTO = new TikTokShopInfoDTO();
        shopInfoDTO.setId(shopId);
        shopInfoDTO.setClientId(cfgAppClient.getClientId());
        shopInfoDTO.setClientSecret(cfgAppClient.getClientSecret());
        shopInfoDTO.setBaseUrl(cfgAppClient.getUrl());
        shopInfoDTO.setName(shopInfo.getName());
        shopInfoDTO.setAccessToken(tokenDTO.getAccessToken());
        shopInfoDTO.setSite(tokenDTO.getSellerBaseRegion());
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.MERCADOLIBRE.getCode(), shopId);
        redisUtil.set(tokenKey, shopInfoDTO, 7L*3600L*24L);

        redisUtil.del(stateKey);

        return Boolean.TRUE;
    }

    @Override
    public Boolean cancelAuthorize(CancelAuthorizeDTO dto) {
        return null;
    }

    @Override
    public Boolean refreshToken(RefreshShopTokenDTO dto) {
        return null;
    }
}
