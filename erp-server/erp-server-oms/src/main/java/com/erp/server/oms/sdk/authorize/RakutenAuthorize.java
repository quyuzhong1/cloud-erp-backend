package com.erp.server.oms.sdk.authorize;

import com.common.business.annotation.PlatformAnnotate;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.RefreshShopTokenDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;
import com.erp.server.oms.service.IShopAuthorizeService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;

/**
 * ebay授权和校验
 *
 **/
@Slf4j
@Component
@PlatformAnnotate(method = PlatformDictEnum.RAKUTEN)
public class RakutenAuthorize implements IShopAuthorizeService<T> {

    /**
     * 获取授权地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto) {
        throw new ServiceException("授权功能未开通");
    }

    /**
     * 授权校验
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean shopAuthorize(ShopAuthorizeDTO dto, HttpServletResponse response) {

        throw new ServiceException("授权功能未开通");
    }

    /**
     * 取消授权
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean cancelAuthorize(CancelAuthorizeDTO dto) {
        throw new ServiceException("授权功能未开通");

    }
    @Override
    public Boolean refreshToken(RefreshShopTokenDTO dto) {
        return Boolean.TRUE;
    }
}
