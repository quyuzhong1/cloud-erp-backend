package com.sdk.wms.antu.service;

import cn.hutool.json.JSONUtil;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.sdk.wms.antu.dto.request.*;
import com.sdk.wms.antu.dto.response.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= AntuService.class)
public class AntuServerTest {

    public AntuServerTest(){
        Map<String,Object> authMap = new HashMap<>();
        //生产
//        authMap.put("appToken","bee4c5e37009b3f4d8a18f7ce44a2365");
//        authMap.put("appKey","674f5f45f2ffdaa6705d88d38d5d04a3");
        //测试
        authMap.put("appToken","d836186400053ed9ac13b2ab0211d8fa");
        authMap.put("appKey","2dd02ab336824a7833041d29cbe22c06");
        ThirdWarehouseContext.setAuthMap(authMap);
    }

    @Resource
    private AntuService antuService;

    @Test
    public void getWarehouseTest() {
        AntuGetProductReq antuProductReq = AntuGetProductReq.builder()
                .build();
        AntuResponse<List<AntuWarehouseResp>> response = antuService.getTransferWarehouse(antuProductReq);
        System.out.println(response);
    }

    @Test
    public void getSkuListTest() {
        AntuGetProductReq antuProductReq = AntuGetProductReq.builder()
                .page(1)
                .pageSize(100)
                .updateStartTime("2024-06-25 15:47:18")
                .updateEndTime("2024-06-25 15:47:18")
                .build();
        AntuResponse<List<AntuProductResp>> response = antuService.getSkuList(antuProductReq);
        System.out.println(JSONUtil.toJsonStr(response));
    }
    @Test
    public void getReceivingRegionTest() {
        AntuResponse<List<AntuRegionResp>> response = antuService.getReceivingRegion();
        System.out.println(JSONUtil.toJsonStr(response));
    }

    @Test
    public void getProductInventoryTest() {
        AntuGetInventoryReq req = AntuGetInventoryReq.builder()
                .page(1)
                .pageSize(100)
                .build();
        AntuResponse<List<AntuInventoryResp>> response = antuService.getProductInventory(req);
        System.out.println(response);
    }

    @Test
    public void getReceiptBatchTest() {
        AntuGetReceiptReq req = AntuGetReceiptReq.builder()
                .page(1)
                .pageSize(100)
                .receivingCodeArr(Arrays.asList("RVA091-240702-0001"))
                .build();
        AntuResponse<List<AntuReceiptResp>> response = antuService.getReceiptBatch(req);
        System.out.println(response);
    }

    @Test
    public void getOutboundBatchTest() {
        AntuGetOutboundReq req = AntuGetOutboundReq.builder()
                .page(1)
                .pageSize(100)
                .modifyDateFrom(LocalDateTime.parse("2024-06-21T11:11:11"))
                .modifyDateTo(LocalDateTime.parse("2024-06-22T11:11:11"))
                .build();
        AntuResponse<List<AntuOutboundResp>> response = antuService.getOutboundBatch(req);
        System.out.println(response);
    }

    @Test
    public void getShippingMethodTest() {
        AntuResponse<List<AntuLogisticsProductsResp>> response = antuService.getShippingMethod(null);
        System.out.println(response);
    }

    @Test
    public void createOutboundBillTest() {
        AntuCreateOutboundReq antuCreateOutboundReq = AntuCreateOutboundReq.builder()
                .referenceNo("WJTEST20240821008")
                .swOrderNumber("14465312644131564841")
                .shippingMethod("PAC")
                .verify(1)
                .warehouseCode("BR01")
                .countryCode("BR")
                .province("PR")
                .city("Curitiba")
                .district("")
                .isInsurance(1)
                .address1("********el;Rua Bororós 1416 ***1")
                .address2("***1")
                .address3("********el;Rua Bororós 1416")
                .zipcode("80320260")
                .license("07309700945")
                .name("Bruno Mendes")
                .phone("41912345776")
                .doorplate("123")
                .email("")
                .items(Arrays.asList(
                        AntuCreateOutboundReq.Item.builder()
                                .productSku("3PL-1C-TEST")
                                .quantity(1)
                                .build()
                ))
                .build();
        AntuResponse<String> response = antuService.createOutboundBill(antuCreateOutboundReq);
        System.out.println(response);
    }

    @Test
    public void cancelOutboundBillTest() {
        AntuResponse<String> response = antuService.cancelOutboundBill("A001-240621-0003","平台拦截");
        System.out.println(response);
    }

    @Test
    public void createInboundBillTest() {
        AntuCreateInboundReq antuGetReceiptReq = AntuCreateInboundReq.builder()
                .referenceNo("wjtest20231205")
                .incomeType(1)
                .receivingType("T")
                .smCode("PAC")
                .contacter("张三")
                .contactPhone("123456789")
                .warehouseCode("BR01")
                .customerType("Y")
                .regionIdLevel0(6)
                .regionIdLevel1(79)
                .regionIdLevel2(733)
                .street("广东省东莞市塘厦镇环市南路24号塘联工业~汇胜科创园2栋楼3号")
                .verify(0)
                .items(Arrays.asList(AntuCreateInboundReq.Item.builder()
                        .productSku("3PL-1C-TEST")
                        .boxNo(1)
                        .quantity(1)
                        .build()))
                .build();
        System.out.println(JSONUtil.toJsonStr(antuGetReceiptReq));
        AntuResponse<String> response = antuService.createInboundBill(antuGetReceiptReq);
        System.out.println(response);
    }

    @Test
    public void editInboundBillTest() {
        AntuCreateInboundReq antuGetReceiptReq = AntuCreateInboundReq.builder()
                .receivingCode("RVA001-240826-0003")
                .verify(1)
                .items(Arrays.asList(AntuCreateInboundReq.Item.builder()
                        .productSku("3PL-1C-TEST")
                        .boxNo(1)
                        .quantity(14)
                        .build()))
                .build();
        AntuResponse<String> response = antuService.editInboundBill(antuGetReceiptReq);
        System.out.println(response);
    }
    @Test
    public void cancelInboundBillTest() {
        AntuResponse<String> response = antuService.cancelInboundBill("RVA001-240826-0003");
        System.out.println(response);
    }
}