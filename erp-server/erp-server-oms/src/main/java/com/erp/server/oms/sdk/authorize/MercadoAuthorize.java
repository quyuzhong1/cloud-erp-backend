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
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercado.dto.mercado.PlatformMercadoRefreshTokenDTO;
import com.sdk.oms.mercado.dto.mercado.PlatformMercadoTokenDTO;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 美客多授权
 */
@Slf4j
@Component
@PlatformAnnotate(method = PlatformDictEnum.MERCADOLIBRE)
public class MercadoAuthorize implements IShopAuthorizeService<T> {

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private ShopAuthService shopAuthService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private MercadoSdkClientService mercadoSdkClientService;

    @Resource
    private MQProducerService mqProducerService;

    /**
     * 获取授权地址
     * @param dto
     */
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
        AppClientEnum appClient = AppClientEnum.MERCADO_AUTHORIZE;
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
        String key = StrUtil.format(RedisCacheConstants.AUTH_MERCADO_STATE, PlatformDictEnum.MERCADOLIBRE.getCode()+state);

        redisUtil.set(key, shopInfo.getId(), RedisCacheConstants.THIRD_PARTY_AUTH_EXPIRATION);

        //拼接授权地址
        String shopAuthorizeUrl = "";
        if (ObjectUtil.isNotEmpty(cfgAppClient)) {
            //https://global-selling.mercadolibre.com/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s
            shopAuthorizeUrl = String.format(cfgAppClient.getUrl(), cfgAppClient.getClientId(), cfgAppClient.getRedirectUrl(), PlatformDictEnum.MERCADOLIBRE.getCode()+state);
        }
        return shopAuthorizeUrl;
    }

    /**
     * 授权
     * @param dto
     * @param response
     * @return
     */
    @Override
    public AuthorizeResultDTO shopAuthorize(ShopAuthorizeDTO dto, HttpServletResponse response) {
        AuthorizeResultDTO resultDTO = new AuthorizeResultDTO();

        // 校验是否是本系统发起
        String stateKey = StrUtil.format(RedisCacheConstants.AUTH_MERCADO_STATE, dto.getState());
        log.error("stateKey:：{}", stateKey);
        Object shopIdObj = redisUtil.get(stateKey);
        if (null == shopIdObj) {
            throw new ServiceException("信息已失效, 请重新发起授权");
        }
        String shopId = shopIdObj.toString();
        if (StringUtils.isBlank(shopId)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_SHOP_USER_AUTH_PART);
        }

        resultDTO.setShopIdList(Arrays.asList(shopId));
        resultDTO.setIsAuthorize(Boolean.TRUE);

        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("店铺不存在");
        }
        String code = dto.getCode();
        if (StringUtils.isBlank(code)) {
            throw new ServiceException(ApiError.ERROR_AUTHORIZE_CODE_NOT_NULL);
        }
        AppClientEnum appClient = AppClientEnum.MERCADO_ACCESS_TOKEN;
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

        PlatformMercadoTokenDTO platformMercadoTokenDTO = mercadoSdkClientService.sendMercadoPostToken(paramMap);
        if (ObjectUtil.isEmpty(platformMercadoTokenDTO)) {
            resultDTO.setIsAuthorize(Boolean.FALSE);
            return resultDTO;
        }
        //根据店铺id 获取到授权信息
        ShopAuthEntity shopAuth = shopAuthService.getByShopId(shopId);
        if (Objects.isNull(shopAuth)) {
            shopAuth = new ShopAuthEntity();
        }
        shopAuth.setShopId(shopId);
        shopAuth.setToken(platformMercadoTokenDTO.getAccessToken());
        shopAuth.setAccessToken(platformMercadoTokenDTO.getAccessToken());
        shopAuth.setRefreshToken(platformMercadoTokenDTO.getRefreshToken());
        shopAuth.setAppClientId(cfgAppClient.getId());
        shopAuth.setExpiresIn(platformMercadoTokenDTO.getExpiresIn());

        LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(platformMercadoTokenDTO.getExpiresIn());
        //提前半小时设置token失效，以免失效了以后才刷新容易出错
        LocalDateTime tokenExpireTime = localDateTime.minusMinutes(30);
        shopAuth.setTokenExpireTime(tokenExpireTime);

        shopInfo.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        shopInfo.setAuthTime(LocalDateTime.now());

        Map<String, Object> map = new HashMap<>();
        map.put("userId", platformMercadoTokenDTO.getUserId());
        shopInfo.setExtendData(map);
        shopAuthService.saveOrUpdate(shopAuth);
        dmpTaskFeign.createAndEnablePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getName(), shopInfo.getDictPlatform()));
        shopInfo.setIsGenTask(Boolean.TRUE);
        shopInfoService.updateById(shopInfo);
        MercadoShopInfoDTO shopInfoDTO = new MercadoShopInfoDTO();
        shopInfoDTO.setId(shopId);
        shopInfoDTO.setClientId(cfgAppClient.getClientId());
        shopInfoDTO.setClientSecret(cfgAppClient.getClientSecret());
        shopInfoDTO.setBaseUrl(cfgAppClient.getUrl());
        shopInfoDTO.setName(shopInfo.getName());
        shopInfoDTO.setAccessToken(platformMercadoTokenDTO.getAccessToken());
        shopInfoDTO.setUserId(platformMercadoTokenDTO.getUserId());
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.MERCADOLIBRE.getCode(), shopId);
        redisUtil.set(tokenKey, shopInfoDTO, platformMercadoTokenDTO.getExpiresIn());

        redisUtil.del(stateKey);

        return resultDTO;
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
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.MERCADOLIBRE.getCode(), shopInfo.getId());
        Object shopInfoObj = redisUtil.get(tokenKey);
        if (null != shopInfoObj) {
            redisUtil.del(tokenKey);
        }
        return result;
    }

    /**
     * 刷新token
     * @param dto
     * @return
     */
    @Override
    public Boolean refreshToken(RefreshShopTokenDTO dto) {
        //先获取授权店铺 然后根据授权店铺进行
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.MERCADO_ACCESS_TOKEN;
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
        PlatformMercadoRefreshTokenDTO platformMercadoRefreshTokenDTO = null;

        try {
            platformMercadoRefreshTokenDTO = mercadoSdkClientService.refreshToken(refreshTokenDTO);
        } catch (Exception e) {
            log.info("::::: 美客多刷新token失败 ::::: 错误信息：" + e.getMessage());
            //错误3次记录错误信息，不在重试，并且发送预警通知
            refreshErrorWarn(shopAuthEntity, e);
        }

        //获取SDK返回的数据
        String accessToken = platformMercadoRefreshTokenDTO.getAccessToken();
        String refreshToken = platformMercadoRefreshTokenDTO.getRefreshToken();
        Integer expiresIn = platformMercadoRefreshTokenDTO.getExpiresIn();
        LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(expiresIn);
        //提前半小时设置token失效，以免失效了以后才刷新容易出错
        LocalDateTime tokenExpireTime = localDateTime.minusMinutes(30);

        //更新店铺token
        shopAuthService.refreshToken(shopAuthEntity.getId(), accessToken, refreshToken, expiresIn, tokenExpireTime);

        MercadoShopInfoDTO shopInfoDTO = new MercadoShopInfoDTO();
        shopInfoDTO.setId(shopAuthEntity.getShopId());
        shopInfoDTO.setClientId(clientId);
        shopInfoDTO.setClientSecret(clientSecret);
        shopInfoDTO.setBaseUrl(baseUrl);
        shopInfoDTO.setName("");
        shopInfoDTO.setUserId(platformMercadoRefreshTokenDTO.getUserId());
        shopInfoDTO.setAccessToken(accessToken);

        //设置缓存
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.MERCADOLIBRE.getCode(), dto.getShopId());
        redisUtil.set(tokenKey, shopInfoDTO, expiresIn);

        return Boolean.TRUE;
    }

    private void refreshErrorWarn(ShopAuthEntity shopAuthEntity, Exception e) {
        //记录错误次数
        String refreshTokenKey = StrUtil.format(RedisCacheConstants.REDIS_REFRESH_PLATFORM_TOKEN, PlatformDictEnum.MERCADOLIBRE.getCode(), shopAuthEntity.getShopId());
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
                warnMsgInfo.setBizName(PlatformDictEnum.MERCADOLIBRE.getName());
                warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
                warnMsgInfo.setTitle(StrUtil.format("平台【{}】店铺id{}刷新token失败",PlatformDictEnum.MERCADOLIBRE.getName(),shopAuthEntity.getShopId()));
                warnMsgInfo.setTableName("shop_auth");
                warnMsgInfo.setTableId(shopAuthEntity.getId());
                warnMsgInfo.setKeyInfo(e.getMessage());
                warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
                mqProducerService.sendWarnMsg(warnMsgInfo);
            }
        }
    }

}
