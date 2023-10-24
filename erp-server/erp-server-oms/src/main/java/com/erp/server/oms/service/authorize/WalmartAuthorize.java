package com.erp.server.oms.service.authorize;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.oms.service.AuthSaveData;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import com.sdk.oms.walmart.api.WalmartStaticKey;
import com.sdk.oms.walmart.dto.walmart.WalmartTokenDTO;
import com.sdk.oms.walmart.service.WalmartSdkClientService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 沃尔玛授权
 * @Author Luo_WG
 * @Date 2023/10/23 18:01
 **/
@Component
@AuthSaveData(method = PlatformDictEnum.WALMART)
public class WalmartAuthorize implements IShopAuthorizeService<T> {
    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private ShopAuthService shopAuthService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    /**
     * 授权
     * @param dto
     * @return
     */
    @Override
    public Boolean shopAuthorize(ShopAuthorizeDTO dto) {
        String shopId = dto.getShopId();
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);

        //根据店铺id 获取到授权信息
        ShopAuthEntity shopAuth = shopAuthService.getByShopId(shopId);
        if (Objects.isNull(shopAuth)) {
            shopAuth = new ShopAuthEntity();
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

        //获取地址
        String url = WalmartStaticKey.baseUrl + "token";

        WalmartSdkClientService walmartSdkClientService = new WalmartSdkClientService();
        WalmartTokenDTO walmartTokenDTO = walmartSdkClientService.sendWalmartPostToken(url, dto.getClientId(), dto.getClientSecret());
        if (ObjectUtil.isEmpty(walmartTokenDTO)) {
            return Boolean.FALSE;
        }
        shopAuth.setShopId(shopId);
        shopAuth.setExpiresIn(Integer.valueOf(walmartTokenDTO.getExpiresIn()));
        shopAuth.setAppClientId(appClientId);
        shopAuth.setAccessToken(walmartTokenDTO.getAccessToken());
        shopInfo.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        shopInfo.setAuthTime(LocalDateTime.now());
        shopAuthService.saveOrUpdate(shopAuth);
        return Boolean.TRUE;
    }

    /**
     * 取消授权
     * @param dto
     */
    @Override
    public void cleanShopAuthorize(ShopAuthorizeDTO dto) {

    }
}
