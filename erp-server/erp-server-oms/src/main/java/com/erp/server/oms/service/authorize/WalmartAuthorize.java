package com.erp.server.oms.service.authorize;

import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.server.oms.service.AuthSaveData;
import com.erp.server.oms.service.IShopAuthorizeService;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

/**
 * 沃尔玛授权
 * @Author Luo_WG
 * @Date 2023/10/23 18:01
 **/
@Component
@AuthSaveData(method = PlatformDictEnum.WALMART)
public class WalmartAuthorize implements IShopAuthorizeService<T> {

    /**
     * 授权
     * @param dto
     * @return
     */
    @Override
    public Boolean shopAuthorize(ShopAuthorizeDTO dto) {
        return null;
    }

    /**
     * 取消授权
     * @param dto
     */
    @Override
    public void cleanShopAuthorize(ShopAuthorizeDTO dto) {

    }
}
