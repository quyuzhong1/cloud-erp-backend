package com.erp.server.wms.sdk;

import com.common.business.annotation.PlatformAnnotate;
import com.common.business.config.AbstractSparrowAnnotationBeanMap;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;
import com.erp.server.wms.service.IPlatformService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class PlatformSaveHandler extends AbstractSparrowAnnotationBeanMap<PlatformAnnotate, IPlatformService> {
    private static final Map<PlatformDictEnum, IPlatformService> PAY_MAP = Maps.newHashMap();

    @Override
    public Class<PlatformAnnotate> getAnnotation() {
        return PlatformAnnotate.class;
    }

    @Override
    public void refresh(Map<PlatformAnnotate, IPlatformService> annotationBeanMap) {
        annotationBeanMap.forEach((pay, payment) -> PAY_MAP.put(pay.method(), payment));
    }

    public static String shipOrder(ShopAuthorizeUrlDTO dto) {
        IPlatformService service = PAY_MAP.get(PlatformDictEnum.getByCode(dto.getPlatformCode()));
        return service.shipOrder(dto);
    }
}
