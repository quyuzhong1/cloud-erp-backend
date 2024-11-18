package com.common.business.handler;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.config.AbstractSparrowAnnotationBeanMap;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import com.common.core.exception.ServiceException;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
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

    public static List<String> shipOrder(PlatformShipOrderDTO dto) {
        IPlatformService service = PAY_MAP.get(PlatformDictEnum.getByCode(dto.getDictPlatform()));
        return service.shipOrder(dto);
    }

    public static Boolean deliveryIntercept(PlatformDeliveryInterceptDTO dto) {
        IPlatformService service = PAY_MAP.get(PlatformDictEnum.getByCode(dto.getDictPlatform()));
        return service.deliveryIntercept(dto);
    }

    public static Boolean queryAndUpdateOrderStatus(PlatformDeliveryInterceptDTO dto) {
        IPlatformService service = PAY_MAP.get(PlatformDictEnum.getByCode(dto.getDictPlatform()));
        return service.queryAndUpdateOrderStatus(dto);
    }


    public static Boolean batchQueryAndUpdateOrderStatus(String dictPlatform, List<PlatformOrderQueryDTO> dtoList) {
        IPlatformService service = PAY_MAP.get(PlatformDictEnum.getByCode(dictPlatform));
        return service.asyncBatchQueryAndUpdateOrderStatus(dtoList);
    }
}
