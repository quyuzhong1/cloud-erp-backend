package com.erp.server.oms.service.authorize;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.AuthSaveData;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import com.sdk.oms.shopify.constant.ShopifyConstant;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.walmart.api.WalmartStaticKey;
import com.sdk.oms.walmart.dto.WalmartShopInfoDTO;
import com.sdk.oms.walmart.dto.walmart.WalmartTokenDTO;
import com.sdk.oms.walmart.service.WalmartSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    private AliExpressOrderService aliExpressOrderService;

    /**
     * 获取授权地址
     */
    @Override
    public String getShopAuthorizeUrl(ShopAuthorizeDTO dto) {
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
        String url = cfgAppClient.getUrl();
        //回调地址
        String redirectUrl = cfgAppClient.getRedirectUrl();
        String clientId = cfgAppClient.getClientId();
        String path = String.format(url, redirectUrl, clientId);
        return path;
    }

    /**
     * 授权
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean shopAuthorize(ShopAuthorizeDTO dto) {
        String shopId = dto.getShopId();
        if (StringUtils.isBlank(shopId)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_SHOP_USER_AUTH_PART);
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

        }catch (Exception e){

        }



            return Boolean.FALSE;
        }

        /**
         * 取消授权
         *
         * @param dto
         */
        @Override
        public Boolean cancelAuthorize (CancelAuthorizeDTO dto){
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
            // 移除缓存
            // platform-token:平台名称:店铺ID
            String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.WALMART.getCode(), shopInfo.getId());
            Object shopInfoObj = redisUtil.get(tokenKey);
            if (null != shopInfoObj) {
                redisUtil.del(tokenKey);
            }
            return result;
        }


    }
