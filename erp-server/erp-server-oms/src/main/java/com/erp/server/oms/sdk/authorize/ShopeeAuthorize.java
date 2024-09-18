package com.erp.server.oms.sdk.authorize;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.PlatformAnnotate;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.AuthTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import com.sdk.oms.shopee.dto.base.ShopeeTokenAuth;
import com.sdk.oms.shopee.dto.base.request.AuthRequest;
import com.sdk.oms.shopee.service.ShopeeAuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 沃尔玛授权
 * @Author Luo_WG
 * @Date 2023/10/23 18:01
 **/

@Slf4j
@Component
@PlatformAnnotate(method = PlatformDictEnum.SHOPEE)
public class ShopeeAuthorize implements IShopAuthorizeService<T> {
    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private ShopAuthService shopAuthService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ShopeeAuthService shopeeAuthService;

    @Resource
    private MQProducerService mqProducerService;

    /**
     * 获取授权地址
     */
    @Override
    public String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto) {
        String url = shopAuthService.getShopeeCodeUrl(dto);
        // 设置缓存
        String key = StrUtil.format(RedisCacheConstants.AUTH_SHOPEE_ID, dto.getShop());
        Object obj = redisUtil.get(key);
        if (null != obj){
            throw new ServiceException("正在申请授权中");
        }
        redisUtil.set(key, dto.getShopId(), RedisCacheConstants.THIRD_PARTY_AUTH_EXPIRATION);
        return url;
    }

    /**
     * 授权
     * @param dto
     * @return
     */
    @Override
    public AuthorizeResultDTO shopAuthorize(ShopAuthorizeDTO dto, HttpServletResponse response) {
        AuthorizeResultDTO resultDTO = new AuthorizeResultDTO();
        ShopAuthDTO.ReturnDTO returnDTO = new ShopAuthDTO.ReturnDTO();
        returnDTO.setCode(dto.getCode());
        returnDTO.setId(dto.getId());
        if(Objects.nonNull(dto.getShopId())){
            returnDTO.setShopId(Integer.valueOf(dto.getShopId()));
        }
        returnDTO.setMainAccountId(dto.getMain_account_id());
        if (StringUtils.isEmpty(dto.getShopId()) && Objects.isNull(dto.getMain_account_id())){
            throw new ServiceException("虾皮授权时,店铺和主账号不能同时为空");
        }
        Boolean result = shopInfoService.getShopeeReturn(returnDTO);
        // 删除授权缓存
        String key = StrUtil.format(RedisCacheConstants.AUTH_SHOPEE_ID, dto.getShopId());
        Object obj = redisUtil.get(key);
        if (null != obj){
            redisUtil.del(key);
        }
        resultDTO.setIsAuthorize(Boolean.TRUE);
        resultDTO.setShopIdList(Arrays.asList(dto.getShopId()));
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
//            dmpTaskFeign.removePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getDictPlatform()));
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
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.SHOPEE.getCode(), shopInfo.getId());
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
        AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            return Boolean.FALSE;
        }

        ShopAuthEntity authEntity = shopAuthService.getByShopId(dto.getShopId());
        if (ObjectUtil.isEmpty(authEntity)) {
            return Boolean.FALSE;
        }

        if (AuthTypeEnum.SHOP.getCode().equals(authEntity.getType())) {
            {
                //判断店铺是否授权
                if (Objects.isNull(authEntity.getShopId())) {
                    return Boolean.FALSE;
                }
                ShopInfoEntity shopInfo = shopInfoService.getById(authEntity.getShopId());
                if (Objects.isNull(shopInfo) || !AuthStatusEnum.ALREADY.getCode().equalsIgnoreCase(shopInfo.getAuthStatus())) {
                    return Boolean.FALSE;
                }
                AuthRequest authRequest = AuthRequest.builder()
                        .host(cfgAppClient.getUrl())
                        .refreshToken(authEntity.getRefreshToken())
                        .partnerId(Long.parseLong(cfgAppClient.getClientId()))
                        .tmpPartnerKey(cfgAppClient.getClientSecret())
                        .shopId(Long.parseLong(authEntity.getShopeeId()))
                        .build();
                ShopeeTokenAuth shopeeResponse = shopeeAuthService.refreshShopToken(authRequest);
                if (Objects.isNull(shopeeResponse) || StringUtils.isNotEmpty(shopeeResponse.getError())) {
                    shopInfo.setAuthStatus(AuthStatusEnum.NOT.getCode());
                    shopInfoService.saveOrUpdate(shopInfo);
                    log.error("授权异常：{}", shopeeResponse);

                    //错误3次记录错误信息，不在重试，并且发送预警通知
                    refreshErrorWarn(authEntity, shopeeResponse);

                    return Boolean.FALSE;
                }
                shopInfoService.saveOrUpdateShopee(shopeeResponse, AuthTypeEnum.SHOP.getCode(), authEntity.getShopeeId(), shopInfo, cfgAppClient.getId());
            }
        }

        if (AuthTypeEnum.MERCHANT.getCode().equals(authEntity.getType())) {
            {
                //判断店铺是否授权
                if (Objects.isNull(authEntity.getShopId())) {
                    return Boolean.FALSE;
                }
                ShopInfoEntity shopInfo = shopInfoService.getById(authEntity.getShopId());
                if (Objects.isNull(shopInfo) || !AuthStatusEnum.ALREADY.getCode().equalsIgnoreCase(shopInfo.getAuthStatus())) {
                    return Boolean.FALSE;
                }
                AuthRequest authRequest = AuthRequest.builder()
                        .host(cfgAppClient.getUrl())
                        .refreshToken(authEntity.getRefreshToken())
                        .partnerId(Long.parseLong(cfgAppClient.getClientId()))
                        .tmpPartnerKey(cfgAppClient.getClientSecret())
                        .merchantId(Long.parseLong(authEntity.getShopeeId()))
                        .build();
                ShopeeTokenAuth shopeeResponse = shopeeAuthService.refreshMerchantToken(authRequest);
                if (Objects.isNull(shopeeResponse) || StringUtils.isNotEmpty(shopeeResponse.getError())) {
                    shopInfo.setAuthStatus(AuthStatusEnum.NOT.getCode());
                    shopInfoService.saveOrUpdate(shopInfo);
                    log.error("授权异常：{}", shopeeResponse);

                    //错误3次记录错误信息，不在重试，并且发送预警通知
                    refreshErrorWarn(authEntity, shopeeResponse);

                    return Boolean.FALSE;
                }
                shopInfoService.saveOrUpdateShopee(shopeeResponse, AuthTypeEnum.MERCHANT.getCode(), authEntity.getShopeeId(), shopInfo, cfgAppClient.getId());
            }
        }
        return Boolean.TRUE;
    }

    private void refreshErrorWarn(ShopAuthEntity authEntity, ShopeeTokenAuth shopeeResponse) {
        //记录错误次数
        String refreshTokenKey = StrUtil.format(RedisCacheConstants.REDIS_REFRESH_PLATFORM_TOKEN, PlatformDictEnum.SHOPEE.getCode(), authEntity.getShopId());
        redisUtil.incr(refreshTokenKey, 1);

        //获取错误次数
        Object refreshTokenNumObj = redisUtil.get(refreshTokenKey);
        if (ObjectUtil.isNotEmpty(refreshTokenNumObj)) {
            Integer refreshTokenNum = Integer.valueOf(String.valueOf(refreshTokenNumObj));
            if (refreshTokenNum >= 3) {
                //记录刷新失败信息
                shopAuthService.updateRefreshTokenError(authEntity.getId(), shopeeResponse.toString());

                //预警通知
                WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
                warnMsgInfo.setBizName(PlatformDictEnum.MERCADOLIBRE.getName());
                warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
                warnMsgInfo.setTitle(StrUtil.format("平台【{}】店铺id{}刷新token失败",PlatformDictEnum.SHOPEE.getName(),authEntity.getShopId()));
                warnMsgInfo.setTableName("shop_auth");
                warnMsgInfo.setTableId(authEntity.getId());
                warnMsgInfo.setKeyInfo(shopeeResponse.toString());
                warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
                mqProducerService.sendWarnMsg(warnMsgInfo);
            }
        }
    }
}
