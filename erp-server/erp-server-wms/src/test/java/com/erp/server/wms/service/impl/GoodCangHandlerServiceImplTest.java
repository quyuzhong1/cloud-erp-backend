package com.erp.server.wms.service.impl;

import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.service.ThirdWarehouseService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class})
public class GoodCangHandlerServiceImplTest {

    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;

    private ThirdWarehouseService thirdWarehouseService;

    @PostConstruct
    public void init(){
        thirdWarehouseService = thirdWarehouseRegistry.getHandler(OmsPlatformEnum.OMS_GOOD_CANG.getCode());
    }

    @Test
    public void testCreateInboundBill() {
        final ThirdWarehouseCreateInboundReq createInboundReq = ThirdWarehouseCreateInboundReq.builder()
                .referenceNo("wj132123412")
                .transitType("0")
                .receivingShippingType("2")
                .trackingNumber("123")
                .warehouseCode("USEA")
                .etaDate(LocalDateTime.now())
                .verify("0")
                .transitWarehouseCode("DG")
                .smCode("USEAAIRFREIGHT6000D")
                .customsType("1")
                .collectingService("1")
                .deliveryCode("deliveryCode")
                .shiperInfo(ThirdWarehouseCreateInboundReq.ShiperInfo.builder()
                        .contacterName("mark")
                        .phone("123")
                        .countryCode("CN")
                        .stateName("广东")
                        .cityName("深圳")
                        .region("龙岗")
                        .address1("星河")
                        .build())
                .collect(ThirdWarehouseCreateInboundReq.Collect.builder()
                        .contacterFirstName("1")
                        .contacterLastName("2")
                        .contactPhone("123")
                        .collectCountryCode("CN")
                        .collectStateName("广东")
                        .collectCityName("深圳")
                        .collectZipcode("13214564")
                        .collectStreet("21")
                        .build())
                .items(Arrays.asList(ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("BGQH2N-000900EU")
                        .boxNo(1)
                        .quantity(1)
                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("BGQH2N-000900EU")
                        .boxNo(1)
                        .quantity(3)
                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("USRAD01454-C")
                        .boxNo(3)
                        .quantity(10)
                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("BGQH2N-000900EU")
                        .boxNo(2)
                        .quantity(11)
                        .build()))
                .build();
        ApiResult<String> code = thirdWarehouseService.createInboundBill(createInboundReq,"1726456935660867586");
        System.out.println(code);
    }

    @Test
    public  void testEditInboundBill() {
        final ThirdWarehouseCreateInboundReq createInboundReq = ThirdWarehouseCreateInboundReq.builder()
                .receivingCode("RVG1149-231129-0004")
                .referenceNo("wj132123412")
                .transitType("0")
                .receivingShippingType("2")
                .trackingNumber("123")
                .warehouseCode("USEA")
                .etaDate(LocalDateTime.now())
                .verify("1")
                .transitWarehouseCode("DG")
                .smCode("USEAAIRFREIGHT6000D")
                .customsType("1")
                .collectingService("1")
                .deliveryCode("deliveryCode")
                .shiperInfo(ThirdWarehouseCreateInboundReq.ShiperInfo.builder()
                        .contacterName("mark")
                        .phone("123")
                        .countryCode("CN")
                        .stateName("广东")
                        .cityName("深圳")
                        .region("龙岗")
                        .address1("星河")
                        .build())
                .collect(ThirdWarehouseCreateInboundReq.Collect.builder()
                        .contacterFirstName("1")
                        .contacterLastName("2")
                        .contactPhone("123")
                        .collectCountryCode("CN")
                        .collectStateName("广东")
                        .collectCityName("深圳")
                        .collectZipcode("13214564")
                        .collectStreet("21")
                        .build())
                .items(Arrays.asList(ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("BGQH2N-000900EU")
                        .boxNo(1)
                        .quantity(1)
                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("BGQH2N-000900EU")
                        .boxNo(1)
                        .quantity(3)
                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("USRAD01454-C")
                        .boxNo(3)
                        .quantity(10)
                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("BGQH2N-000900EU")
                        .boxNo(2)
                        .quantity(11)
                        .build()))
                .build();
        ApiResult<String> code = thirdWarehouseService.editInboundBill(createInboundReq,"1726456935660867586");
        System.out.println(code);
    }

    @Test
    public  void testCancelInboundBill() {
        ThirdWarehouseCancelInboundReq cancelInboundReq = new ThirdWarehouseCancelInboundReq();
        cancelInboundReq.setReceivingCode("RVG1149-231129-0003");
        ApiResult<String> result = thirdWarehouseService.cancelInboundBill(cancelInboundReq,"1726456935660867586");
        System.out.println(result);
    }

    @Test
    public void testCreateOutboundBill() {
        ThirdWarehouseCreateOutboundReq createOutboundReq = ThirdWarehouseCreateOutboundReq.builder()
                .referenceNo("WJTEST1234")
                .shippingMethod("RETURN-OR")
                .warehouseCode("USEA")
                .verify(1)
                .receiverInfo(ThirdWarehouseCreateOutboundReq.ReceiverInfo.builder()
                        .name("mark")
                        .phone("123234567")
                        .countryCode("US")
                        .province("nowyork")
                        .city("hs")
                        .address1("13")
                        .zipcode("51200")
                        .build())
                .items(Arrays.asList(ThirdWarehouseCreateOutboundReq.Item.builder()
                        .productSku("USRAD01454-C")
                        .quantity(5)
                        .build()))
                .build();
        ApiResult<String> code = thirdWarehouseService.createOutboundBill(createOutboundReq,"1726456935660867586");
        System.out.println(code);
        System.out.println(code.getData());
    }

    @Test
    public void testCancelOutboundBill() {
    }

    @Test
    public void testHasWarehouse() {
    }

    @Test
    public void testHasWarehouse_GoodCangServiceReturnsNoItems() {
    }

}
