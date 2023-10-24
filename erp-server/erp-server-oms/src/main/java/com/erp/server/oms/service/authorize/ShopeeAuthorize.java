package com.erp.server.oms.service.authorize;

import cn.hutool.core.util.StrUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.AuthSaveData;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import com.sdk.oms.shopify.constant.ShopifyConstant;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 沃尔玛授权
 * @Author Luo_WG
 * @Date 2023/10/23 18:01
 **/

@Slf4j
@Component
@AuthSaveData(method = PlatformDictEnum.SHOPEE)
public class ShopeeAuthorize implements IShopAuthorizeService<T> {
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
    public String getShopAuthorizeUrl(ShopAuthorizeDTO dto) {
        return shopAuthService.getShopeeCodeUrl(dto.getShopId());
    }

    /**
     * 授权
     * @param dto
     * @return
     */
    @Override
    public Boolean shopAuthorize(ShopAuthorizeDTO dto) {
        ShopAuthDTO.ReturnDTO returnDTO = new ShopAuthDTO.ReturnDTO();
        returnDTO.setCode(dto.getCode());
        returnDTO.setId(dto.getId());
        returnDTO.setShopId(Integer.valueOf(dto.getShopId()));
        returnDTO.setMainAccountId(dto.getMainAccountId());
        return shopInfoService.getShopeeReturn(returnDTO);
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

    /**
     * shopInfo Entity 转换DTO
     */
    private ShopifyShopInfoDTO initShopInfoDTO(ShopInfoEntity shopInfo, String accessToken) {
        return new ShopifyShopInfoDTO()
                // 店铺ID
                .setId(shopInfo.getId())
                // 访问token
                .setAccessToken(accessToken)
                // 店铺名称
                .setName(shopInfo.getName())
                // 区域id
                .setDictAreaCode(shopInfo.getDictAreaCode())
                // 国家id
                .setDictCountryCode(shopInfo.getDictCountryCode())
                // 负责人id
                .setChargeId(shopInfo.getChargeId())
                // 店铺全域名: SHOP_NAME.myshopify.com
                .setShopDomain(shopInfo.getDomain().concat(ShopifyConstant.DOMAIN));
    }
}
