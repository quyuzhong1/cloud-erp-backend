package com.erp.server.oms.service.authorize;

import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.server.oms.service.ShopAuthorizeService;
import org.springframework.stereotype.Component;

/**
 * 沃尔玛授权
 * @Author Luo_WG
 * @Date 2023/10/23 18:01
 **/
@Component
public class WalmartAuthorize implements ShopAuthorizeService {

    @Override
    public Boolean shopAuthorize(ShopAuthorizeDTO dto) {
        return null;
    }
}
