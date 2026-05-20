package com.erp.server.wms.service.impl;

import com.common.business.annotation.PlatformRetryAnno;
import com.common.business.config.AbstractSparrowAnnotationBeanMap;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.server.wms.service.IPlatformRetryService;
import com.erp.server.wms.service.SoOutstockService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PlatformRetryHandler extends AbstractSparrowAnnotationBeanMap<PlatformRetryAnno, IPlatformRetryService> {
    private static final Map<PlatformDictEnum, IPlatformRetryService> PAY_MAP = Maps.newHashMap();
    private static SoOutstockService soOutstockService;


    @Override
    public Class<PlatformRetryAnno> getAnnotation() {
        return PlatformRetryAnno.class;
    }

    @Override
    public void refresh(Map<PlatformRetryAnno, IPlatformRetryService> annotationBeanMap) {
        annotationBeanMap.forEach((pay, payment) -> PAY_MAP.put(pay.method(), payment));
    }

    @Resource
    public void setSoOutstockService(SoOutstockService soOutstockService) {
        PlatformRetryHandler.soOutstockService = soOutstockService;
    }

    public static Boolean retrySoOutStock(SoB2cEntity currentEntity, List<SoB2cEntity> soB2cList) {
        IPlatformRetryService service = PAY_MAP.get(PlatformDictEnum.getByCode(currentEntity.getDictPlatform()));
        if (service == null) {
            return soOutstockService.defaultHandleRetry(currentEntity, soB2cList);
        }
        return service.retrySoOutStock(currentEntity, soB2cList);
    }


}
