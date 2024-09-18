package com.erp.server.oms.sdk.authorize;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
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
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.dto.request.RefreshTokenRequest;
import com.erp.oms.aliexpress.service.AliExpressAuthService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 速卖通授权
 *
 * @author yl
 * @date 2023-11-21
 */

@Slf4j
@Component
@PlatformAnnotate(method = PlatformDictEnum.ALI_EXPRESS)
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

    @Resource
    private MQProducerService mqProducerService;

    /**
     * 获取授权地址
     */
    @Override
    public String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto) {
        List<ShopInfoEntity> shopInfoEntityList = dto.getShopInfoEntityList();
        ShopInfoEntity shopInfo;
        if (CollectionUtils.isEmpty(shopInfoEntityList)){
            shopInfo = shopInfoService.getById(dto.getShopId());
        } else {
            shopInfo = shopInfoEntityList.get(0);
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
    public AuthorizeResultDTO shopAuthorize(ShopAuthorizeDTO dto, HttpServletResponse response) {
        AuthorizeResultDTO resultDTO = new AuthorizeResultDTO();

        // 校验是否是本系统发起
        String stateKey = StrUtil.format(RedisCacheConstants.AUTH_ALIEXPRESS_STATE, dto.getState());
        log.error("stateKey:：{}",stateKey);
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

        resultDTO.setShopIdList(Arrays.asList(shopId));
        resultDTO.setIsAuthorize(Boolean.TRUE);
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
                LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(expiresIn);
                //提前半小时设置token失效，以免失效了以后才刷新容易出错
                LocalDateTime tokenExpireTime = localDateTime.minusMinutes(30);
                shopAuth.setTokenExpireTime(tokenExpireTime);

                String sellerId=jsonObject.getOrDefault("seller_id","").toString();
                Map<String, Object> extendJsonMap = new HashMap<>();
                extendJsonMap.put("sellerId", sellerId);
                shopInfo.setExtendData(extendJsonMap);
                shopInfo.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
                shopInfo.setAuthTime(LocalDateTime.now());
                shopAuthService.saveOrUpdate(shopAuth);
                dmpTaskFeign.createAndEnablePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(),shopInfo.getName(), shopInfo.getDictPlatform()));
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
            resultDTO.setIsAuthorize(Boolean.FALSE);
            return resultDTO;
        }


        return resultDTO;
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
//            // 删除授权
//            dmpTaskFeign.removePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getName(), shopInfo.getDictPlatform()));
            // 禁用启用任务和取消报告计划任务
            dmpTaskFeign.allAddOrUpdateTaskAndSchedule(new PlatformTaskDTO.DisabledDTO(shopInfo.getId(),
                    shopInfo.getName(),
                    shopInfo.getDictPlatform(),
                    true,
                    shopInfo.getDictCountryCode(),
                    shopInfo.getPlatformShopCode()));
            shopInfo.setIsGenTask(Boolean.FALSE);
            shopInfoService.updateShopInfoById(shopInfo);
        }
        return result;
    }


    /**
     * 刷新token
     */
    public Boolean refreshToken(RefreshShopTokenDTO dto){
        //先获取授权店铺 然后根据授权店铺进行
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            return Boolean.FALSE;
        }
        String appClientId= cfgAppClient.getId();
        String clientId = cfgAppClient.getClientId();
        String baseUrl= cfgAppClient.getUrl();
        String clientSecret=cfgAppClient.getClientSecret();

        ShopAuthEntity authEntity = shopAuthService.getByShopId(dto.getShopId());
        if (ObjectUtil.isEmpty(authEntity)) {
            return Boolean.FALSE;
        }
        RefreshTokenRequest request = RefreshTokenRequest.builder().
                baseUrl(baseUrl).
                refreshToken(authEntity.getRefreshToken()).
                clientId(clientId).
                clientSecret(clientSecret).
                build();
        try {
            JSONObject jsonObject = aliExpressAuthService.RefreshToken(request);
            String code = jsonObject.getOrDefault("code", "").toString();
            //表示成功
            if ("0".equals(code)) {
                String accessToken = jsonObject.getOrDefault("access_token", "").toString();
                String refreshToken = jsonObject.getOrDefault("refresh_token", "").toString();
                Integer expiresIn = jsonObject.getInteger("expires_in");

                LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(expiresIn);
                //提前半小时设置token失效，以免失效了以后才刷新容易出错
                LocalDateTime tokenExpireTime = localDateTime.minusMinutes(30);

                shopAuthService.refreshToken(authEntity.getId(), accessToken, refreshToken, expiresIn, tokenExpireTime);
                AliExpressShopInfoDTO shopInfoDTO = new AliExpressShopInfoDTO();
                shopInfoDTO.setId(authEntity.getShopId());
                shopInfoDTO.setClientId(clientId);
                shopInfoDTO.setClientSecret(clientSecret);
                shopInfoDTO.setBaseUrl(baseUrl);
                shopInfoDTO.setName("");
                shopInfoDTO.setToken(accessToken);
                String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.ALI_EXPRESS.getCode(), authEntity.getShopId());
                redisUtil.set(tokenKey, shopInfoDTO, expiresIn);
            } else {
                log.error("::::: 速卖通刷新token失败 ::::: 入参===》{}，错误信息：{}：" , request.toString(), jsonObject.toJSONString());
                //错误3次记录错误信息，不在重试，并且发送预警通知
                refreshErrorWarn(authEntity, jsonObject.toJSONString());
            }
        } catch (ApiException e) {
            log.error("刷新店铺id 为>>>{} token失败 {}", authEntity.getShopId(), e.getMessage());
            //错误3次记录错误信息，不在重试，并且发送预警通知
            refreshErrorWarn(authEntity, e.getMessage());
        }
        return Boolean.TRUE;
    }

    /**
     * 错误预警
     * @param authEntity
     * @param errorMsgStr
     */
    private void refreshErrorWarn(ShopAuthEntity authEntity, String errorMsgStr) {
        //记录错误次数
        String refreshTokenKey = StrUtil.format(RedisCacheConstants.REDIS_REFRESH_PLATFORM_TOKEN, PlatformDictEnum.ALI_EXPRESS.getCode(), authEntity.getShopId());
        redisUtil.incr(refreshTokenKey, 1);

        //获取错误次数
        Object refreshTokenNumObj = redisUtil.get(refreshTokenKey);
        if (ObjectUtil.isNotEmpty(refreshTokenNumObj)) {
            Integer refreshTokenNum = Integer.valueOf(String.valueOf(refreshTokenNumObj));
            if (refreshTokenNum >= 3) {
                //记录刷新失败信息
                shopAuthService.updateRefreshTokenError(authEntity.getId(), errorMsgStr);

                //预警通知
                WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
                warnMsgInfo.setBizName(PlatformDictEnum.MERCADOLIBRE.getName());
                warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
                warnMsgInfo.setTitle(StrUtil.format("平台【{}】店铺id{}刷新token失败", PlatformDictEnum.ALI_EXPRESS.getName(), authEntity.getShopId()));
                warnMsgInfo.setTableName("shop_auth");
                warnMsgInfo.setTableId(authEntity.getId());
                warnMsgInfo.setKeyInfo(errorMsgStr);
                warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
                mqProducerService.sendWarnMsg(warnMsgInfo);
            }
        }
    }


}
