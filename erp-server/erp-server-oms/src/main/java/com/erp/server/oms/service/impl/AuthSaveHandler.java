package com.erp.server.oms.service.impl;

import com.common.business.config.AbstractSparrowAnnotationBeanMap;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;
import com.common.business.annotation.PlatformAnnotate;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class AuthSaveHandler extends AbstractSparrowAnnotationBeanMap<PlatformAnnotate, IShopAuthorizeService> {
    private static final Map<PlatformDictEnum, IShopAuthorizeService> PAY_MAP = Maps.newHashMap();

    @Override
    public Class<PlatformAnnotate> getAnnotation() {
        return PlatformAnnotate.class;
    }

    @Override
    public void refresh(Map<PlatformAnnotate, IShopAuthorizeService> annotationBeanMap) {
        annotationBeanMap.forEach((pay, payment) -> PAY_MAP.put(pay.method(), payment));
    }

    public static String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto) {
        IShopAuthorizeService service = PAY_MAP.get(PlatformDictEnum.getByCode(dto.getPlatformCode()));
        return service.getShopAuthorizeUrl(dto);
    }

    public static Boolean shopAuthorize(ShopAuthorizeDTO dto){
        IShopAuthorizeService service = PAY_MAP.get(PlatformDictEnum.getByCode(dto.getPlatformCode()));
        return service.shopAuthorize(dto);
    }

    public static Boolean cleanShopAuthorize(CancelAuthorizeDTO dto) {
        IShopAuthorizeService service = PAY_MAP.get(PlatformDictEnum.getByCode(dto.getPlatformCode()));
        return service.cancelAuthorize(dto);
    }

}
