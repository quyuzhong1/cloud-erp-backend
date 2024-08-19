package com.erp.server.oms.sdk.authorize;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.PlatformAnnotate;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.shop.ShopsBean;
import com.sdk.oms.tiktok.dto.tiktok.token.TokenDTO;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        long timestamp = 1715074867L;

        // 将时间戳转换为 LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
        System.out.println(localDateTime);
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
        String key = StrUtil.format(RedisCacheConstants.AUTH_TIKTOK_STATE, PlatformDictEnum.TIK_TOK.getCode() + state);

        redisUtil.set(key, shopInfo.getId(), RedisCacheConstants.THIRD_PARTY_AUTH_EXPIRATION);

        //拼接授权地址
        String shopAuthorizeUrl = "";
        if (ObjectUtil.isNotEmpty(cfgAppClient)) {
            //https://services.tiktokshop.com/open/authorize?service_id=7348713406587684614&state=%s
            shopAuthorizeUrl = String.format(cfgAppClient.getUrl(), PlatformDictEnum.TIK_TOK.getCode()+state);
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
        shopAuth.setAppClientId(cfgAppClient.getClientId());
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
        map.put("shopCipher", tokenDTO.getShopCipher());
        map.put("userType", tokenDTO.getUserType());

        ShopsBean shopsBean = tokenDTO.getShopsBean();
        if (ObjectUtil.isNotEmpty(shopsBean)) {
            map.put("region", shopsBean.getRegion());
            map.put("sellerType", shopsBean.getSellerType());
        }
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
        if (ObjectUtil.isNotEmpty(shopsBean)) {
            shopInfoDTO.setSite(shopsBean.getRegion());
            shopInfoDTO.setSellerType(shopsBean.getSellerType());
        }

        shopInfoDTO.setShopCipher(tokenDTO.getShopCipher());
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.TIK_TOK.getCode(), shopId);
        redisUtil.set(tokenKey, shopInfoDTO, 7L*3600L*24L);

        redisUtil.del(stateKey);

        return Boolean.TRUE;
    }

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
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.TIK_TOK.getCode(), shopInfo.getId());
        Object shopInfoObj = redisUtil.get(tokenKey);
        if (null != shopInfoObj) {
            redisUtil.del(tokenKey);
        }
        return result;
    }

    @Override
    public Boolean refreshToken(RefreshShopTokenDTO dto) {
        //先获取授权店铺 然后根据授权店铺进行
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.TIKTOK_ACCESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            return Boolean.FALSE;
        }
        String clientId = cfgAppClient.getClientId();
        String baseUrl = cfgAppClient.getUrl();
        String clientSecret = cfgAppClient.getClientSecret();
        ShopAuthEntity shopAuthEntity = shopAuthService.getByShopId(dto.getShopId());
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(dto.getShopId());
        if (ObjectUtil.isEmpty(shopAuthEntity)) {
            return Boolean.FALSE;
        }

        //组装请求实体
        ShopDTO.RefreshTokenDTO refreshTokenDTO = new ShopDTO.RefreshTokenDTO();
        refreshTokenDTO.setBaseUrl(baseUrl);
        refreshTokenDTO.setRefreshToken(shopAuthEntity.getRefreshToken());
        refreshTokenDTO.setClientId(clientId);
        refreshTokenDTO.setClientSecret(clientSecret);

        //请求SDK刷新token
        TokenDTO tokenDTO = null;

        try {
            tokenDTO = tikTokSdkClientService.refreshToken(refreshTokenDTO);
        } catch (Exception e) {
            log.info("::::: TikTok刷新token失败 ::::: 错误信息：" + e.getMessage());
            //错误3次记录错误信息，不在重试，并且发送预警通知
            refreshErrorWarn(shopAuthEntity, e);
        }

        //获取SDK返回的数据
        String accessToken = tokenDTO.getAccessToken();
        String refreshToken = tokenDTO.getRefreshToken();
        // token 过期时间为7天 ，提前一小时过期
        LocalDateTime localDateTime = LocalDateTime.now().plusDays(7L);
        //提前半小时设置token失效，以免失效了以后才刷新容易出错
        LocalDateTime tokenExpireTime = localDateTime.minusMinutes(30);

        //更新店铺token
        shopAuthService.refreshToken(shopAuthEntity.getId(), accessToken, refreshToken, 7*3600*24, tokenExpireTime);
        return Boolean.TRUE;
    }

    private void refreshErrorWarn(ShopAuthEntity shopAuthEntity, Exception e) {
        //记录错误次数
        String refreshTokenKey = StrUtil.format(RedisCacheConstants.REDIS_REFRESH_PLATFORM_TOKEN, PlatformDictEnum.TIK_TOK.getCode(), shopAuthEntity.getShopId());
        redisUtil.incr(refreshTokenKey, 1);

        //获取错误次数
        Object refreshTokenNumObj = redisUtil.get(refreshTokenKey);
        if (ObjectUtil.isNotEmpty(refreshTokenNumObj)) {
            Integer refreshTokenNum = Integer.valueOf(String.valueOf(refreshTokenNumObj));
            if (refreshTokenNum >= 3) {
                //记录刷新失败信息
                shopAuthService.updateRefreshTokenError(shopAuthEntity.getId(), e.getMessage());

                //预警通知
                WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
                warnMsgInfo.setBizName(PlatformDictEnum.TIK_TOK.getName());
                warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
                warnMsgInfo.setTitle(StrUtil.format("平台【{}】店铺id{}刷新token失败",PlatformDictEnum.TIK_TOK.getName(),shopAuthEntity.getShopId()));
                warnMsgInfo.setTableName("shop_auth");
                warnMsgInfo.setTableId(shopAuthEntity.getId());
                warnMsgInfo.setKeyInfo(e.getMessage());
                warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
                mqProducerService.sendWarnMsg(warnMsgInfo);
            }
        }
    }
}
