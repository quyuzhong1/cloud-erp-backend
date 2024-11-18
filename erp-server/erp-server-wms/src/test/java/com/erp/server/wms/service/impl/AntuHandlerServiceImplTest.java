package com.erp.server.wms.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.third.*;
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
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestPropertySources;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
@TestPropertySources({@TestPropertySource("classpath:bootstrap-dev.yml"),@TestPropertySource("classpath:bootstrap.yml")})
@EnableDiscoveryClient
public class AntuHandlerServiceImplTest {

    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;

    private ThirdWarehouseService thirdWarehouseService;

    @PostConstruct
    public void init(){
        thirdWarehouseService = thirdWarehouseRegistry.getHandler(OmsPlatformEnum.OMS_ANTU.getCode());
    }

    @Test
    public void getSkuList() {
        ThirdWarehouseProductReq productReq = new ThirdWarehouseProductReq();
        productReq.setSkuNoList(Arrays.asList("YDXN5C-001300UK","YDXN5C-001200UK"));
        ApiResult<List<ThirdWarehouseSkuResp>> code = thirdWarehouseService.getSkuList(productReq,"1726456935660867586");
        System.out.println(code);
    }

    @Test
    public void testCreateInboundBill() {
//        String json = "{\"receivingCode\":\"RVG1149-231206-0020\",\"referenceNo\":\"FHD23120500007\",\"incomeType\":\"selfHeadway\",\"transitType\":\"selfHeadway\",\"warehouseCode\":\"USEA\",\"transitWarehouseCode\":\"\",\"smCode\":\"\",\"receivingShippingType\":\"express\",\"trackingNumber\":\"6666668\",\"etaDate\":1703865600000,\"verify\":\"0\",\"customsType\":\"\",\"collectingService\":\"\",\"deliveryCode\":\"\",\"shiperInfo\":{\"contacterName\":\"张三\",\"phone\":\"13222222222\",\"countryCode\":\"CN\",\"stateName\":\"广东\",\"cityName\":\"深圳\",\"region\":\"龙岗区\",\"address1\":\"CN\"},\"collect\":{\"contacterName\":\"\",\"contacterFirstName\":\"\",\"contacterLastName\":\"\",\"contactPhone\":\"\",\"collectStreet\":\"\",\"collectCountryCode\":\"CN\",\"collectStateName\":\"\",\"collectCityName\":\"\",\"collectZipcode\":\"\"},\"items\":[{\"productSku\":\"QC6SHR-045700EU-ML001\",\"boxNo\":1,\"quantity\":100},{\"productSku\":\"QC6SHR-045700EU-ML003\",\"boxNo\":1,\"quantity\":100},{\"productSku\":\"QC6SHR-045700EU\",\"boxNo\":1,\"quantity\":100},{\"productSku\":\"QC6SHR-045700EU-ML000\",\"boxNo\":1,\"quantity\":100}]}";
//        ThirdWarehouseCreateInboundReq createInboundReq = JSONObject.parseObject(json,new TypeReference<ThirdWarehouseCreateInboundReq>() {}.getType());
        final ThirdWarehouseCreateInboundReq createInboundReq = ThirdWarehouseCreateInboundReq.builder()
                .referenceNo("wj132123412123")
                .incomeType(OverseasDeliveryModeEnum.COLLECT_AT_HOME.getCode())
                .receivingCode(OverseasInstockTypeEnum.SELF_HEADWAY.getCode())
                .transitType(OverseasInstockTypeEnum.SELF_HEADWAY.getCode())
                .receivingShippingType(LogisticsMethodEnum.AIRFREIGHT.getCode())
                .customsType(OverseasCustomsTypeNewEnum.AGENCY_CUSTOMS_DECLARATION.getCode())
                .collectingService(OverseasDeliveryModeEnum.COLLECT_AT_HOME.getCode())
                .trackingNumber("123")
                .warehouseCode("USEA")
                .etaDate(LocalDateTime.now())
                .verify("0")
                .transitWarehouseCode("DG")
                .smCode("USEAAIRFREIGHT6000D")
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
        String json = "{\"receivingCode\":\"RVG1149-231206-0020\",\"referenceNo\":\"FHD23120500007\",\"incomeType\":\"selfHeadway\",\"transitType\":\"selfHeadway\",\"warehouseCode\":\"USEA\",\"transitWarehouseCode\":\"\",\"smCode\":\"\",\"receivingShippingType\":\"express\",\"trackingNumber\":\"6666668\",\"etaDate\":1703865600000,\"verify\":\"0\",\"customsType\":\"\",\"collectingService\":\"\",\"deliveryCode\":\"\",\"shiperInfo\":{\"contacterName\":\"张三\",\"phone\":\"13222222222\",\"countryCode\":\"CN\",\"stateName\":\"广东\",\"cityName\":\"深圳\",\"region\":\"龙岗区\",\"address1\":\"CN\"},\"collect\":{\"contacterName\":\"\",\"contacterFirstName\":\"\",\"contacterLastName\":\"\",\"contactPhone\":\"\",\"collectStreet\":\"\",\"collectCountryCode\":\"CN\",\"collectStateName\":\"\",\"collectCityName\":\"\",\"collectZipcode\":\"\"},\"items\":[{\"productSku\":\"QC6SHR-045700EU-ML001\",\"boxNo\":1,\"quantity\":100},{\"productSku\":\"QC6SHR-045700EU-ML003\",\"boxNo\":1,\"quantity\":100},{\"productSku\":\"QC6SHR-045700EU\",\"boxNo\":1,\"quantity\":100},{\"productSku\":\"QC6SHR-045700EU-ML000\",\"boxNo\":1,\"quantity\":100}]}";
        ThirdWarehouseCreateInboundReq createInboundReq = JSONObject.parseObject(json,new TypeReference<ThirdWarehouseCreateInboundReq>() {}.getType());
//        final ThirdWarehouseCreateInboundReq createInboundReq = ThirdWarehouseCreateInboundReq.builder()
//                .receivingCode("RVG1149-231129-0004")
//                .referenceNo("wj132123412")
//                .transitType("0")
//                .receivingShippingType("2")
//                .trackingNumber("123")
//                .warehouseCode("USEA")
//                .etaDate(LocalDateTime.now())
//                .verify("1")
//                .transitWarehouseCode("DG")
//                .smCode("USEAAIRFREIGHT6000D")
//                .customsType("1")
//                .collectingService("1")
//                .deliveryCode("deliveryCode")
//                .shiperInfo(ThirdWarehouseCreateInboundReq.ShiperInfo.builder()
//                        .contacterName("mark")
//                        .phone("123")
//                        .countryCode("CN")
//                        .stateName("广东")
//                        .cityName("深圳")
//                        .region("龙岗")
//                        .address1("星河")
//                        .build())
//                .collect(ThirdWarehouseCreateInboundReq.Collect.builder()
//                        .contacterFirstName("1")
//                        .contacterLastName("2")
//                        .contactPhone("123")
//                        .collectCountryCode("CN")
//                        .collectStateName("广东")
//                        .collectCityName("深圳")
//                        .collectZipcode("13214564")
//                        .collectStreet("21")
//                        .build())
//                .items(Collections.singletonList(ThirdWarehouseCreateInboundReq.Item.builder()
//                        .productSku("BGQH2N-000900EU")
//                        .boxNo(1)
//                        .quantity(1)
//                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
//                        .productSku("BGQH2N-000900EU")
//                        .boxNo(1)
//                        .quantity(3)
//                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
//                        .productSku("USRAD01454-C")
//                        .boxNo(3)
//                        .quantity(10)
//                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
//                        .productSku("BGQH2N-000900EU")
//                        .boxNo(2)
//                        .quantity(11)
//                        .build()))
//                .build();
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
                .referenceNo("WJTEST20240822001")
                .platformCode("14465312644131564841")
                .shippingMethod("PAC")
                .warehouseCode("BR01")
                .verify(1)
                .receiverInfo(ThirdWarehouseCreateOutboundReq.ReceiverInfo.builder()
                        .name("JOSUE SANTANNA")
                        .phone("61998913002")
                        .countryCode("BR")
                        .province("Goiás")
                        .city("Valparaíso de Goiás")
                        .address1("Rua 19 Quadra 27 Casa 16")
                        .address2("Jardim Oriente")
                        .zipcode("72870-263")
                        .email("jofersant@gmail.com")
                        .taxNumber("83143025115")
                        .build())
                .items(Arrays.asList(ThirdWarehouseCreateOutboundReq.Item.builder()
                        .productSku("3PL-1C-TEST")
                        .quantity(5)
                        .build()))
                .build();
        ApiResult<String> code = thirdWarehouseService.createOutboundBill(createOutboundReq,"1826513110852497409");
        System.out.println(code);
        System.out.println(code.getData());
    }

    @Test
    public void testCancelOutboundBill() {
        ThirdWarehouseCancelOutboundReq cancelOutboundReq = ThirdWarehouseCancelOutboundReq.builder()
                .orderCode("G1149-231129-0051")
                .build();
        ApiResult<String> code = thirdWarehouseService.cancelOutboundBill(cancelOutboundReq,"1726456935660867586");
        System.out.println(code);
        System.out.println(code.getData());
    }

    @Test
    public void testHasWarehouse() {
    }

    @Test
    public void testHasWarehouse_GoodCangServiceReturnsNoItems() {
    }

}
