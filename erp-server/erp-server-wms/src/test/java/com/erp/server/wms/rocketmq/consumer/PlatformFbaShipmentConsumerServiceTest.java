package com.erp.server.wms.rocketmq.consumer;

import com.alibaba.fastjson.JSONObject;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.impl.FbaShipmentServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class})
public class PlatformFbaShipmentConsumerServiceTest {

    @Resource
    private PlatformFbaShipmentConsumerService service;

    @Test
    public void handle() {
        String json = "{\"name\":\"欧2-锐闻海运递延-18件-12.27\",\"shopId\":\"1734478618723094529\",\"shopName\":\"欧洲站波兰\",\"countryId\":\"CN\",\"fulfillmentCenter\":\"DTM1\",\"deliveryStatus\":\"unShipped\",\"platformShipmentStatus\":\"READY_TO_SHIP\",\"shipmentCreateTime\":1704878021929,\"labelType\":\"卖家标签\",\"packType\":\"\",\"deliveryFromAddress\":\"51800 CN Guangdong Shenzhen ZhongXing Road NO.140,BanTian Street Anita Zou\",\"fbaShipmentId\":\"FBA15HMQMDYN-手动测试-1\",\"platformUpdateTime\":1704169623856,\"receiveDTOList\":[{\"fbaShipmentId\":\"FBA15HMQMDYN-手动测试-1\",\"fnSku\":\"X001DT3ESZ\",\"sellerSku\":\"2487-UK2\",\"declareQty\":180,\"deliveryQty\":0,\"receiveQty\":0,\"receiveDate\":1704878021930},{\"fbaShipmentId\":\"FBA15HMQMDYN-手动测试-1\",\"fnSku\":\"X001NHI6YJ\",\"sellerSku\":\"3028-UK2\",\"declareQty\":72,\"deliveryQty\":0,\"receiveQty\":0,\"receiveDate\":1704878021930}],\"detailList\":[{\"fbaShipmentId\":\"FBA15HMQMDYN-手动测试-1\",\"fnSku\":\"X001NHI6YJ\",\"sellerSku\":\"3028-UK2\",\"declareQty\":72,\"deliveryQty\":0,\"receiveQty\":0,\"receiveDate\":1704878021930},{\"fbaShipmentId\":\"FBA15HMQMDYN-手动测试-1\",\"fnSku\":\"X001DT3ESZ\",\"sellerSku\":\"2487-UK2\",\"declareQty\":180,\"deliveryQty\":0,\"receiveQty\":0,\"receiveDate\":1704878021930}],\"uniqueId\":\"FBA15HMQMDYN-手动测试-1\"}";
        JSONObject jsonObject = JSONObject.parseObject(json);
        service.handle(json);
    }
}