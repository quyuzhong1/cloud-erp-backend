package com.erp.server.oms.service.impl;

import com.common.business.config.AbstractSparrowAnnotationBeanMap;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.erp.server.oms.service.AuthSaveData;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class AuthSaveHandler extends AbstractSparrowAnnotationBeanMap<AuthSaveData, IShopAuthorizeService> {
    private static final Map<PlatformDictEnum, IShopAuthorizeService> PAY_MAP = Maps.newHashMap();

    @Override
    public Class<AuthSaveData> getAnnotation() {
        return AuthSaveData.class;
    }

    @Override
    public void refresh(Map<AuthSaveData, IShopAuthorizeService> annotationBeanMap) {
        annotationBeanMap.forEach((pay, payment) -> PAY_MAP.put(pay.method(), payment));
    }

    public static String getShopAuthorizeUrl(ShopAuthorizeDTO dto) {
        IShopAuthorizeService service = PAY_MAP.get(dto.getPlatformCode());
        return service.getShopAuthorizeUrl(dto);
    }

    public static Boolean shopAuthorize(ShopAuthorizeDTO dto){
        IShopAuthorizeService service = PAY_MAP.get(dto.getPlatformCode());
        return service.shopAuthorize(dto);
    }

    public static Boolean cleanShopAuthorize(CancelAuthorizeDTO dto) {
        IShopAuthorizeService service = PAY_MAP.get(dto.getPlatformCode());
        return service.cancelAuthorize(dto);
    }

}
