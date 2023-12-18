package com.erp.server.wms.sdk;

import com.common.business.annotation.PlatformAnnotate;
import com.common.business.config.AbstractSparrowAnnotationBeanMap;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class DliverySaveHandler extends AbstractSparrowAnnotationBeanMap<PlatformAnnotate, IPlatformDeliveryService> {
    private static final Map<PlatformDictEnum, IPlatformDeliveryService> PAY_MAP = Maps.newHashMap();

    @Override
    public Class<PlatformAnnotate> getAnnotation() {
        return PlatformAnnotate.class;
    }

    @Override
    public void refresh(Map<PlatformAnnotate, IPlatformDeliveryService> annotationBeanMap) {
        annotationBeanMap.forEach((pay, payment) -> PAY_MAP.put(pay.method(), payment));
    }

    public static String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto) {
        IPlatformDeliveryService service = PAY_MAP.get(PlatformDictEnum.getByCode(dto.getPlatformCode()));
        return service.getShopAuthorizeUrl(dto);
    }

    public static Boolean shopAuthorize(ShopAuthorizeDTO dto){
        IPlatformDeliveryService service = PAY_MAP.get(PlatformDictEnum.getByCode(dto.getPlatformCode()));
        return service.shopAuthorize(dto);
    }

    public static Boolean cleanShopAuthorize(CancelAuthorizeDTO dto) {
        IPlatformDeliveryService service = PAY_MAP.get(PlatformDictEnum.getByCode(dto.getPlatformCode()));
        return service.cancelAuthorize(dto);
    }

}
