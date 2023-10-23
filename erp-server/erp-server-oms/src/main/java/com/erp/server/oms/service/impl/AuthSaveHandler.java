package com.erp.server.oms.service.impl;

import com.common.business.config.AbstractSparrowAnnotationBeanMap;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.exception.ServiceException;
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

    private static final int size = 1000;
    @Override
    public Class<AuthSaveData> getAnnotation() {
        return AuthSaveData.class;
    }

    @Override
    public void refresh(Map<AuthSaveData, IShopAuthorizeService> annotationBeanMap) {
        annotationBeanMap.forEach((pay, payment) -> PAY_MAP.put(pay.method(), payment));
    }

    public static void pullDataSave(ShopAuthorizeDTO dto) throws Exception {
        //通过枚举获取对应service
        IShopAuthorizeService service = PAY_MAP.get(dto.getPlatformCode());
        //拉取数据 存库
        service.shopAuthorize(dto);
    }

    public static void cleanDataSave(ShopAuthorizeDTO dto) {
        //通过枚举获取对应service
        IShopAuthorizeService service = PAY_MAP.get(dto.getPlatformCode());
        //清除数据
        service.cleanShopAuthorize(dto);
    }

}
