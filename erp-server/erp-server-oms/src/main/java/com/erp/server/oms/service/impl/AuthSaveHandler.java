package com.erp.server.oms.service.impl;

import com.common.business.annotation.PlatformAnnotate;
import com.common.business.config.AbstractSparrowAnnotationBeanMap;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.RefreshShopTokenDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class AuthSaveHandler extends AbstractSparrowAnnotationBeanMap<PlatformAnnotate, IShopAuthorizeService<T>> {
    private static final Map<PlatformDictEnum, IShopAuthorizeService<T>> PAY_MAP = Maps.newHashMap();

    @Override
    public Class<PlatformAnnotate> getAnnotation() {
        return PlatformAnnotate.class;
    }

    @Override
    public void refresh(Map<PlatformAnnotate, IShopAuthorizeService<T>> annotationBeanMap) {
        annotationBeanMap.forEach((pay, payment) -> PAY_MAP.put(pay.method(), payment));
    }

    /**
     * 获取授权url
     * @param dto
     */
    public static String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto) {
        IShopAuthorizeService<T> service = PAY_MAP.get(getAuthorizePlatform(dto.getPlatformCode()));
        if(Objects.isNull(service)){
            throw new ServiceException("未对接授权平台");
        }
        return service.getShopAuthorizeUrl(dto);
    }

    /**
     * 授权
     * @param dto
     * @param response
     */
    public static Boolean shopAuthorize(ShopAuthorizeDTO dto, HttpServletResponse response){
        IShopAuthorizeService<T> service = PAY_MAP.get(getAuthorizePlatform(dto.getPlatformCode()));
        if(Objects.isNull(service)){
            throw new ServiceException("未对接授权平台");
        }
        return service.shopAuthorize(dto, response);
    }

    /**
     * 取消授权
     * @param dto
     */
    public static Boolean cleanShopAuthorize(CancelAuthorizeDTO dto) {
        IShopAuthorizeService<T> service = PAY_MAP.get(getAuthorizePlatform(dto.getPlatformCode()));
        if(Objects.isNull(service)){
            throw new ServiceException("未对接授权平台");
        }
        return service.cancelAuthorize(dto);
    }

    /**
     * 刷新token
     * @param dto
     */
    public static Boolean refreshShopToken(RefreshShopTokenDTO dto) {
        IShopAuthorizeService<T> service = PAY_MAP.get(getAuthorizePlatform(dto.getPlatformCode()));
        if(Objects.isNull(service)){
            throw new ServiceException("未对接授权平台");
        }
        return service.refreshToken(dto);
    }

    private static PlatformDictEnum getAuthorizePlatform(String platformCode) {
        return PlatformDictEnum.getByCode(PlatformDictEnum.getApiPlatformCode(platformCode));
    }

}
