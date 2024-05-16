package com.erp.server.oms.schedule;

import com.erp.model.oms.dto.RefreshShopTokenDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.impl.AuthSaveHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class RefreshTokenJob {

    @Resource
    private ShopAuthService shopAuthService;

    /**
     * 刷新店铺token
     */
    @XxlJob("refreshShopToken")
    public void refreshShopToken() {
        List<ShopAuthEntity> shopAuthEntities = shopAuthService.listTokenExpiresShop();
        for (ShopAuthEntity shopAuthEntity : shopAuthEntities) {
            RefreshShopTokenDTO dto = new RefreshShopTokenDTO();
            dto.setShopId(shopAuthEntity.getShopId());
            dto.setPlatformCode(shopAuthEntity.getDictPlatform());
            AuthSaveHandler.refreshShopToken(dto);
        }
    }

}
