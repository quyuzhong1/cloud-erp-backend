package com.erp.server.wms.service.impl;

import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateInboundReq;
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
public class ImlHandlerServiceImplTest {

    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;

    private ThirdWarehouseService thirdWarehouseService;

    @PostConstruct
    public void init(){
        thirdWarehouseService = thirdWarehouseRegistry.getHandler(OmsPlatformEnum.OMS_IML.getCode());
    }

    @Test
    public void testCreateInboundBill() {
        final ThirdWarehouseCreateInboundReq createInboundReq = ThirdWarehouseCreateInboundReq.builder()
                .referenceNo("wjtest20231120")
                .transitType("0")
                .incomeType("1")
                .receivingShippingType("2")
                .trackingNumber("123")
                .warehouseCode("UAW1")
                .etaDate(LocalDateTime.now())
                .verify("0")
                .transitWarehouseCode("DG")
                .smCode("USEAAIRFREIGHT6000D1")
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
                        .contacterName("mark")
                        .contacterFirstName("1")
                        .contacterLastName("2")
                        .contactPhone("123")
                        .collectCountryCode("CN")
                        .collectStateId("6")
                        .collectCityId("77")
                        .collectAreaId("709")
                        .collectStateName("广东")
                        .collectCityName("深圳")
                        .collectZipcode("13214564")
                        .collectStreet("21")
                        .build())
                .items(Arrays.asList(ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("2823A")
                        .boxNo(1)
                        .quantity(1)
                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("2823A")
                        .boxNo(1)
                        .quantity(3)
                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("C003GBB1")
                        .boxNo(3)
                        .quantity(10)
                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("C003GBB1")
                        .boxNo(2)
                        .quantity(11)
                        .build()))
                .build();
        ApiResult<String> code = thirdWarehouseService.createInboundBill(createInboundReq,"1726457716430561281");
        System.out.println(code);
        System.out.println(code.getData());
    }

    @Test
    public  void testEditInboundBill() {
        final ThirdWarehouseCreateInboundReq createInboundReq = ThirdWarehouseCreateInboundReq.builder()
                .receivingCode("RV86526-231129-0003")
                .referenceNo("wj132123412")
                .transitType("0")
                .receivingShippingType("2")
                .trackingNumber("123")
                .warehouseCode("UAW1")
                .etaDate(LocalDateTime.now())
                .verify("0")
                .transitWarehouseCode("DG")
                .smCode("US")
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
                        .productSku("2823A")
                        .boxNo(1)
                        .quantity(1)
                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("2823A")
                        .boxNo(1)
                        .quantity(3)
                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("C003GBB1")
                        .boxNo(3)
                        .quantity(10)
                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("C003GBB1")
                        .boxNo(2)
                        .quantity(11)
                        .build()))
                .build();
        ApiResult<String> code = thirdWarehouseService.editInboundBill(createInboundReq,"1726457716430561281");
        System.out.println(code);
        System.out.println(code.getData());
    }

    @Test
    public  void testCancelInboundBill() {
        ThirdWarehouseCancelInboundReq cancelInboundReq = new ThirdWarehouseCancelInboundReq();
        cancelInboundReq.setReceivingCode("RV86526-231129-0014");
        ApiResult<String> result = thirdWarehouseService.cancelInboundBill(cancelInboundReq,"1726457716430561281");
        System.out.println(result);
        System.out.println(result.getData());
    }

    @Test
    public void testCreateOutboundBill() {
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
