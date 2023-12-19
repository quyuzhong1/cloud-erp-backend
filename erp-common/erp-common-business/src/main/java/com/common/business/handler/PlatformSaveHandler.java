package com.common.business.handler;

import com.common.business.annotation.PlatformAnnotate;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.config.AbstractSparrowAnnotationBeanMap;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class PlatformSaveHandler extends AbstractSparrowAnnotationBeanMap<PlatformShipOrderAnno, IPlatformService> {
    private static final Map<PlatformDictEnum, IPlatformService> PAY_MAP = Maps.newHashMap();

    @Override
    public Class<PlatformShipOrderAnno> getAnnotation() {
        return PlatformShipOrderAnno.class;
    }

    @Override
    public void refresh(Map<PlatformShipOrderAnno, IPlatformService> annotationBeanMap) {
        annotationBeanMap.forEach((pay, payment) -> PAY_MAP.put(pay.method(), payment));
    }

    public static String shipOrder(PlatformShipOrderDTO dto) {
        IPlatformService service = PAY_MAP.get(PlatformDictEnum.getByCode(dto.getPlatformCode()));
        return service.shipOrder(dto);
    }
}
