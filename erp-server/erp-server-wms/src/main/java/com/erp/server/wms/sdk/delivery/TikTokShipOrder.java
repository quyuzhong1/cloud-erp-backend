package com.erp.server.wms.sdk.delivery;

import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.TIK_TOK)
public class TikTokShipOrder implements IPlatformService {

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Override
    public void shipOrder(PlatformShipOrderDTO dto) {
        SoB2cEntity entity = soB2cFeign.getById(dto.getSoB2cId());

        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(entity.getId());
        Map<String, Object> extendData = shopInfoEntity.getExtendData();
        Integer userType = Integer.valueOf(extendData.get("userType")+"");


    }
}
