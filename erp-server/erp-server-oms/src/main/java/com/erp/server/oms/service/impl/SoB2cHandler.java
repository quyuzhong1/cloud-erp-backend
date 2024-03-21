package com.erp.server.oms.service.impl;

import com.common.business.annotation.PlatformAnnotate;
import com.common.business.annotation.PlatformSoB2cAnnotate;
import com.common.business.config.AbstractSparrowAnnotationBeanMap;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.erp.server.oms.service.ISoB2cHandleService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
@Slf4j
public class SoB2cHandler extends AbstractSparrowAnnotationBeanMap<PlatformSoB2cAnnotate, ISoB2cHandleService> {
    private static final Map<PlatformDictEnum, ISoB2cHandleService> PAY_MAP = Maps.newHashMap();

    @Override
    public Class<PlatformSoB2cAnnotate> getAnnotation() {
        return PlatformSoB2cAnnotate.class;
    }

    @Override
    public void refresh(Map<PlatformSoB2cAnnotate, ISoB2cHandleService> annotationBeanMap) {
        annotationBeanMap.forEach((pay, payment) -> PAY_MAP.put(pay.method(), payment));
    }

    /**
     * 规则处理
     */
    public static Boolean handleRule(SoB2cEntity mainEntity) {
        ISoB2cHandleService service = PAY_MAP.get(PlatformDictEnum.getByCode(mainEntity.getDictPlatform()));
        return service.handleRule(mainEntity);
    }

    /**
     * 处理生成销售出库单
     */
    public static Boolean handleSoOutStock(PlatformOrderDTO dto, SoB2cDTO.PullOrderResultDTO resultDTO, SoB2cEntity mainEntity){
        ISoB2cHandleService service = PAY_MAP.get(PlatformDictEnum.getByCode(mainEntity.getDictPlatform()));
        return service.handleSoOutStock(dto, resultDTO, mainEntity);
    }

}
