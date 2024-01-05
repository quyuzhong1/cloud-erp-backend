package com.erp.server.wms.shopify;

import com.common.business.dto.PlatformShipOrderDTO;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.sdk.delivery.ShopifyShipOrder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * Shopify单元测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class ErpServerWmsShopifyApplicationTests {


    @Resource
    private ShopifyShipOrder shopifyShipOrder;



    @Test
    public void shipOrder(){
//        PlatformShipOrderDTO platformShipOrderDTO = new PlatformShipOrderDTO();
//        platformShipOrderDTO.setDictPlatform("Shopify");
//        platformShipOrderDTO.setSoB2cId("1739995642983354369");
//        shopifyShipOrder.shipOrder(platformShipOrderDTO);
    }
}
