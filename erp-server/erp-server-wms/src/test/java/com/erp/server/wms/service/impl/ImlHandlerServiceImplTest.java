package com.erp.server.wms.service.impl;

import com.common.business.enums.OmsPlatformEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.third.ThirdWarehouseCancelInboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCancelOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateOutboundReq;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.model.wms.enums.OverseasCustomsTypeNewEnum;
import com.erp.model.wms.enums.OverseasDeliveryModeEnum;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
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

    @Resource
    private RedisUtil redisUtil;

    @PostConstruct
    public void init(){
        thirdWarehouseService = thirdWarehouseRegistry.getHandler(OmsPlatformEnum.OMS_IML.getCode());
    }

    @Test
    public void testCreateInboundBill() {
        final ThirdWarehouseCreateInboundReq createInboundReq = ThirdWarehouseCreateInboundReq.builder()
                .referenceNo("wjtest202312070003")
                .incomeType(OverseasDeliveryModeEnum.COLLECT_AT_HOME.getCode())
                .transitType(OverseasInstockTypeEnum.TRANSFER_AGENT.getCode())
                .receivingType(OverseasInstockTypeEnum.TRANSFER_AGENT.getCode())
                .receivingShippingType(LogisticsMethodEnum.AIRFREIGHT.getCode())
                .customsType(OverseasCustomsTypeNewEnum.AGENCY_CUSTOMS_DECLARATION.getCode())
                .collectingService(OverseasDeliveryModeEnum.COLLECT_AT_HOME.getCode())
                .trackingNumber("123")
                .warehouseCode("RUS2")
                .etaDate(LocalDateTime.now())
                .verify("0")
                .transitWarehouseCode("SZW")
                .smCode("")
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
        ThirdWarehouseCreateOutboundReq createOutboundReq = ThirdWarehouseCreateOutboundReq.builder()
                .referenceNo("WJTEST1234")
                .shippingMethod("IML-RU")
                .warehouseCode("RUS2")
                .verify(0)
                .receiverInfo(ThirdWarehouseCreateOutboundReq.ReceiverInfo.builder()
                        .name("mark")
                        .phone("12345678910")
                        .countryCode("RU")
                        .province("newyork")
                        .city("hs")
                        .address1("13")
                        .zipcode("123456")
                        .build())
                .items(Arrays.asList(ThirdWarehouseCreateOutboundReq.Item.builder()
                        .productSku("1764")
                        .quantity(1)
                        .build()))
                .build();
        ApiResult<String> code = thirdWarehouseService.createOutboundBill(createOutboundReq,"1726457716430561281");
        System.out.println(code);
        System.out.println(code.getData());
    }

    @Test
    public void testCancelOutboundBill() {
        ThirdWarehouseCancelOutboundReq cancelOutboundReq = ThirdWarehouseCancelOutboundReq.builder()
                .orderCode("86526-231130-2195")
                .build();
        ApiResult<String> code = thirdWarehouseService.cancelOutboundBill(cancelOutboundReq,"1726457716430561281");
        System.out.println(code);
        System.out.println(code.getData());
    }

}
