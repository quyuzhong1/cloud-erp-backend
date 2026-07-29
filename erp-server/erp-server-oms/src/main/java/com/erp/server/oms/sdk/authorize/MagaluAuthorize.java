package com.erp.server.oms.sdk.authorize;

import cn.hutool.core.text.CharSequenceUtil;
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
import com.sdk.oms.magalu.dto.MagaluShopInfoDTO;
import com.sdk.oms.magalu.dto.MagaluTokenDTO;
import com.sdk.oms.magalu.service.MagaluService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Magalu授权
 */
@Slf4j
@Component
@PlatformAnnotate(method = PlatformDictEnum.MAGALU)
public class MagaluAuthorize implements IShopAuthorizeService<T> {

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
    private MagaluService magaluService;

    @Override
    public String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto) {
        ShopInfoEntity shopInfo = getShopInfo(dto);
        CfgAppClientEntity cfgAppClient = getCfgAppClient(AppClientEnum.MAGALU_AUTHORIZE);

        String state = PlatformDictEnum.MAGALU.getCode() + generateState();
        String key = CharSequenceUtil.format(RedisCacheConstants.AUTH_MAGALU_STATE, state);
        redisUtil.set(key, shopInfo.getId(), RedisCacheConstants.THIRD_PARTY_AUTH_EXPIRATION);

        return buildAuthorizeUrl(cfgAppClient, state);
    }

    @Override
    public Boolean shopAuthorize(ShopAuthorizeDTO dto, HttpServletResponse response) {
        String stateKey = CharSequenceUtil.format(RedisCacheConstants.AUTH_MAGALU_STATE, dto.getState());
        Object shopIdObj = redisUtil.get(stateKey);
        if (Objects.isNull(shopIdObj)) {
            throw new ServiceException("信息已失效, 请重新发起授权");
        }
        String shopId = shopIdObj.toString();
        if (StringUtils.isBlank(shopId)) {
            throw new ServiceException(ApiError.SHOP_AUTH_REQUIRED);
        }
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("店铺不存在");
        }
        if (StringUtils.isBlank(dto.getCode())) {
            throw new ServiceException(ApiError.SHOP_AUTHORIZE_CODE_REQUIRED);
        }

        CfgAppClientEntity cfgAppClient = getCfgAppClient(AppClientEnum.MAGALU_ACCESS_TOKEN);
        MagaluTokenDTO tokenDTO = magaluService.createToken(buildTokenParam(cfgAppClient, dto.getCode(), null));

        ShopAuthEntity shopAuth = shopAuthService.getByShopId(shopId);
        if (Objects.isNull(shopAuth)) {
            shopAuth = new ShopAuthEntity();
        }
        saveAuth(shopAuth, shopId, cfgAppClient, tokenDTO);

        shopInfo.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        shopInfo.setAuthTime(LocalDateTime.now());
        shopAuthService.saveOrUpdate(shopAuth);
        dmpTaskFeign.createAndEnablePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getName(), shopInfo.getDictPlatform()));
        shopInfo.setIsGenTask(Boolean.TRUE);
        shopInfoService.updateById(shopInfo);

        cacheShopInfo(shopInfo, cfgAppClient, tokenDTO);
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
        if (!AuthStatusEnum.ALREADY.getCode().equals(shopInfo.getAuthStatus())) {
            throw new ServiceException("该店铺未授权,无需取消授权");
        }

        shopInfo.setAuthStatus(AuthStatusEnum.CANCEL.getCode());
        Boolean result = shopInfoService.updateById(shopInfo);
        if (result) {
            shopAuthService.removeByShopId(shopId);
            dmpTaskFeign.removePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getName(), shopInfo.getDictPlatform()));
            shopInfo.setIsGenTask(Boolean.FALSE);
            shopInfoService.updateShopInfoById(shopInfo);
        }

        String tokenKey = CharSequenceUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.MAGALU.getCode(), shopInfo.getId());
        if (Objects.nonNull(redisUtil.get(tokenKey))) {
            redisUtil.del(tokenKey);
        }
        return result;
    }

    @Override
    public Boolean refreshToken(RefreshShopTokenDTO dto) {
        ShopInfoEntity shopInfo = shopInfoService.getById(dto.getShopId());
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("店铺不存在");
        }
        ShopAuthEntity shopAuth = shopAuthService.getByShopId(dto.getShopId());
        if (Objects.isNull(shopAuth)) {
            return Boolean.FALSE;
        }

        CfgAppClientEntity cfgAppClient = getCfgAppClient(AppClientEnum.MAGALU_ACCESS_TOKEN);
        MagaluTokenDTO tokenDTO;
        try {
            tokenDTO = magaluService.refreshToken(buildTokenParam(cfgAppClient, null, shopAuth.getRefreshToken()));
        } catch (Exception e) {
            log.info("::::: Magalu刷新token失败 ::::: 错误信息：{}", e.getMessage());
            refreshErrorWarn(shopAuth, e);
            return Boolean.FALSE;
        }

        String accessToken = tokenDTO.getAccessToken();
        String refreshToken = tokenDTO.getRefreshToken();
        Integer expiresIn = getExpiresIn(tokenDTO);
        LocalDateTime tokenExpireTime = getTokenExpireTime(expiresIn);
        shopAuthService.refreshToken(shopAuth.getId(), accessToken, refreshToken, expiresIn, tokenExpireTime);

        cacheShopInfo(shopInfo, cfgAppClient, tokenDTO);
        return Boolean.TRUE;
    }

    private ShopInfoEntity getShopInfo(ShopAuthorizeUrlDTO dto) {
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
        return shopInfo;
    }

    private CfgAppClientEntity getCfgAppClient(AppClientEnum appClient) {
        CfgAppClientDTO.FindDTO findDTO = CfgAppClientDTO.FindDTO.init(appClient);
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            throw new ServiceException("该类型店铺尚未配置开发者账号");
        }
        return cfgAppClient;
    }

    private String generateState() {
        byte[] randomBytes = new byte[256];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String buildAuthorizeUrl(CfgAppClientEntity cfgAppClient, String state) {
        String scope = getScope(cfgAppClient);
        String template = cfgAppClient.getUrl();
        String authorizeUrl;
        int placeholderCount = StringUtils.countMatches(template, "%s");
        if (placeholderCount >= 4) {
            authorizeUrl = String.format(template, cfgAppClient.getClientId(), urlEncode(cfgAppClient.getRedirectUrl()), urlEncode(scope), urlEncode(state));
        } else if (placeholderCount == 3) {
            authorizeUrl = String.format(template, cfgAppClient.getClientId(), urlEncode(cfgAppClient.getRedirectUrl()), urlEncode(state));
            if (StringUtils.isNotBlank(scope)) {
                authorizeUrl = authorizeUrl + (authorizeUrl.contains("?") ? "&" : "?") + "scope=" + urlEncode(scope);
            }
        } else {
            authorizeUrl = appendAuthorizeQuery(template, cfgAppClient.getClientId(), cfgAppClient.getRedirectUrl(), scope, state);
        }
        // Magalu 官方授权要求显式携带 response_type=code 与 choose_tenants=true
        return ensureAuthorizeRequiredParams(authorizeUrl);
    }

    private String appendAuthorizeQuery(String url, String clientId, String redirectUrl, String scope, String state) {
        String separator = url.contains("?") ? "&" : "?";
        StringBuilder builder = new StringBuilder(url)
                .append(separator)
                .append("client_id=").append(urlEncode(clientId))
                .append("&redirect_uri=").append(urlEncode(redirectUrl))
                .append("&state=").append(urlEncode(state));
        if (StringUtils.isNotBlank(scope)) {
            builder.append("&scope=").append(urlEncode(scope));
        }
        return builder.toString();
    }

    private String ensureAuthorizeRequiredParams(String authorizeUrl) {
        if (!StringUtils.containsIgnoreCase(authorizeUrl, "response_type=")) {
            authorizeUrl = authorizeUrl + (authorizeUrl.contains("?") ? "&" : "?") + "response_type=code";
        }
        if (!StringUtils.containsIgnoreCase(authorizeUrl, "choose_tenants=")) {
            authorizeUrl = authorizeUrl + (authorizeUrl.contains("?") ? "&" : "?") + "choose_tenants=true";
        }
        return authorizeUrl;
    }

    private String getScope(CfgAppClientEntity cfgAppClient) {
        Map<String, Object> extendData = cfgAppClient.getExtendData();
        if (extendData == null) {
            return "";
        }
        Object scope = extendData.get("scope");
        if (Objects.isNull(scope)) {
            scope = extendData.get("scopes");
        }
        if (scope instanceof Collection) {
            return StringUtils.join((Collection<?>) scope, " ");
        }
        return Objects.isNull(scope) ? "" : scope.toString();
    }

    private Map<String, String> buildTokenParam(CfgAppClientEntity cfgAppClient, String code, String refreshToken) {
        Map<String, String> paramMap = new HashMap<>(8);
        paramMap.put("clientId", cfgAppClient.getClientId());
        paramMap.put("clientSecret", cfgAppClient.getClientSecret());
        paramMap.put("redirectUri", cfgAppClient.getRedirectUrl());
        paramMap.put("baseUrl", cfgAppClient.getUrl());
        paramMap.put("code", code);
        paramMap.put("refreshToken", refreshToken);
        return paramMap;
    }

    private void saveAuth(ShopAuthEntity shopAuth, String shopId, CfgAppClientEntity cfgAppClient, MagaluTokenDTO tokenDTO) {
        Integer expiresIn = getExpiresIn(tokenDTO);
        shopAuth.setShopId(shopId);
        shopAuth.setToken(tokenDTO.getAccessToken());
        shopAuth.setAccessToken(tokenDTO.getAccessToken());
        shopAuth.setRefreshToken(tokenDTO.getRefreshToken());
        shopAuth.setAppClientId(cfgAppClient.getId());
        shopAuth.setExpiresIn(expiresIn);
        shopAuth.setTokenExpireTime(getTokenExpireTime(expiresIn));
    }

    private void cacheShopInfo(ShopInfoEntity shopInfo, CfgAppClientEntity cfgAppClient, MagaluTokenDTO tokenDTO) {
        MagaluShopInfoDTO shopInfoDTO = new MagaluShopInfoDTO()
                .setId(shopInfo.getId())
                .setName(shopInfo.getName())
                .setClientId(cfgAppClient.getClientId())
                .setClientSecret(cfgAppClient.getClientSecret())
                .setBaseUrl(cfgAppClient.getUrl())
                .setApiBaseUrl(getApiBaseUrl(cfgAppClient))
                .setChannelId(getChannelId(cfgAppClient))
                .setRedirectUrl(cfgAppClient.getRedirectUrl())
                .setAccessToken(tokenDTO.getAccessToken())
                .setRefreshToken(tokenDTO.getRefreshToken())
                .setTokenType(tokenDTO.getTokenType())
                .setScope(tokenDTO.getScope());

        String tokenKey = CharSequenceUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.MAGALU.getCode(), shopInfo.getId());
        redisUtil.set(tokenKey, shopInfoDTO, getExpiresIn(tokenDTO));
    }

    private Integer getExpiresIn(MagaluTokenDTO tokenDTO) {
        return Objects.isNull(tokenDTO.getExpiresIn()) ? 0 : tokenDTO.getExpiresIn();
    }

    private String getApiBaseUrl(CfgAppClientEntity cfgAppClient) {
        Map<String, Object> extendData = cfgAppClient.getExtendData();
        if (extendData == null) {
            return "https://api.magalu.com";
        }
        if (isProductionApiEnv(extendData)) {
            Object productionApiBaseUrl = extendData.get("productionApiBaseUrl");
            if (Objects.nonNull(productionApiBaseUrl) && StringUtils.isNotBlank(productionApiBaseUrl.toString())) {
                return productionApiBaseUrl.toString();
            }
            return "https://api.magalu.com";
        }
        if (Objects.isNull(extendData.get("apiBaseUrl"))) {
            return "https://api.magalu.com";
        }
        String apiBaseUrl = extendData.get("apiBaseUrl").toString();
        return StringUtils.isBlank(apiBaseUrl) ? "https://api.magalu.com" : apiBaseUrl;
    }

    private String getChannelId(CfgAppClientEntity cfgAppClient) {
        Map<String, Object> extendData = cfgAppClient.getExtendData();
        if (extendData == null) {
            return "";
        }
        if (isProductionApiEnv(extendData)) {
            Object productionChannelId = extendData.get("productionChannelId");
            return Objects.isNull(productionChannelId) ? "" : productionChannelId.toString();
        }
        if (Objects.isNull(extendData.get("channelId"))) {
            return "";
        }
        return extendData.get("channelId").toString();
    }

    private boolean isProductionApiEnv(Map<String, Object> extendData) {
        Object apiEnv = extendData.get("apiEnv");
        return apiEnv != null && "production".equalsIgnoreCase(apiEnv.toString());
    }

    private LocalDateTime getTokenExpireTime(Integer expiresIn) {
        if (Objects.isNull(expiresIn) || expiresIn <= 0) {
            return LocalDateTime.now();
        }
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(expiresIn);
        return expiresIn > 1800 ? expireTime.minusMinutes(30) : expireTime;
    }

    private String urlEncode(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException("URL编码失败");
        }
    }

    private void refreshErrorWarn(ShopAuthEntity shopAuthEntity, Exception e) {
        String refreshTokenKey = CharSequenceUtil.format(RedisCacheConstants.REDIS_REFRESH_PLATFORM_TOKEN, PlatformDictEnum.MAGALU.getCode(), shopAuthEntity.getShopId());
        redisUtil.incr(refreshTokenKey, 1);

        Object refreshTokenNumObj = redisUtil.get(refreshTokenKey);
        if (Objects.nonNull(refreshTokenNumObj)) {
            Integer refreshTokenNum = Integer.valueOf(String.valueOf(refreshTokenNumObj));
            if (refreshTokenNum >= 3) {
                shopAuthService.updateRefreshTokenError(shopAuthEntity.getId(), e.getMessage());

                WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
                warnMsgInfo.setBizName(PlatformDictEnum.MAGALU.getName());
                warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
                warnMsgInfo.setTitle(CharSequenceUtil.format("平台【{}】店铺id{}刷新token失败", PlatformDictEnum.MAGALU.getName(), shopAuthEntity.getShopId()));
                warnMsgInfo.setTableName("shop_auth");
                warnMsgInfo.setTableId(shopAuthEntity.getId());
                warnMsgInfo.setKeyInfo(e.getMessage());
                warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
                mqProducerService.sendWarnMsg(warnMsgInfo);
            }
        }
    }
}
