package com.sdk.wms.antu.service;

import cn.hutool.json.JSONUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.sdk.wms.antu.dto.request.*;
import com.sdk.wms.antu.dto.response.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= AntuService.class)
public class SptServerTest {

    public SptServerTest(){
        Map<String,Object> authMap = new HashMap<>();
        //生产
//        authMap.put("appToken","bee4c5e37009b3f4d8a18f7ce44a2365");
//        authMap.put("appKey","674f5f45f2ffdaa6705d88d38d5d04a3");
        //测试
        authMap.put("appToken","b704c637a0ecfed5dbda799a987291d4");
        authMap.put("appKey","ae60ef5fd2ba671c510d3eb1ae788046");
        ThirdWarehouseContext.setAuthMap(authMap);
    }

    @Resource
    private AntuService antuService;

    @Test
    public void getWarehouseTest() {
        AntuGetProductReq antuProductReq = AntuGetProductReq.builder().page(1)
                .pageSize(100)
                .build();
        AntuResponse<List<AntuWarehouseResp>> response = antuService.getWarehouse(antuProductReq, OmsPlatformEnum.OMS_SPT);
        System.out.println(response);
    }

    @Test
    public void getTransferWarehouseTest() {
        AntuGetProductReq antuProductReq = AntuGetProductReq.builder().page(1)
                .pageSize(100)
                .build();
        AntuResponse<List<AntuWarehouseResp>> response = antuService.getTransferWarehouse(antuProductReq, OmsPlatformEnum.OMS_SPT);
        System.out.println(response);
    }

    @Test
    public void getSkuListTest() {
        AntuGetProductReq antuProductReq = AntuGetProductReq.builder()
                .page(1)
                .pageSize(100)
//                .updateStartTime("2024-06-25 15:47:18")
//                .updateEndTime("2024-06-25 15:47:18")
                .build();
        AntuResponse<List<AntuProductResp>> response = antuService.getSkuList(antuProductReq, OmsPlatformEnum.OMS_SPT);
        System.out.println(JSONUtil.toJsonStr(response));
    }
    @Test
    public void getReceivingRegionTest() {
        AntuResponse<List<AntuRegionResp>> response = antuService.getReceivingRegion(OmsPlatformEnum.OMS_SPT);
        System.out.println(JSONUtil.toJsonStr(response));
    }

    @Test
    public void getProductInventoryTest() {
        AntuGetInventoryReq req = AntuGetInventoryReq.builder()
                .page(1)
                .pageSize(100)
                .build();
        AntuResponse<List<AntuInventoryResp>> response = antuService.getProductInventory(req, OmsPlatformEnum.OMS_SPT);
        System.out.println(response);
    }

    @Test
    public void getReceiptBatchTest() {
        AntuGetReceiptReq req = AntuGetReceiptReq.builder()
                .page(1)
                .pageSize(100)
                .receivingCode("RVVIJIM-250226-0005")
//                .receivingCodeArr(Arrays.asList("RVVIJIM-250226-0005"))
                .build();
        AntuResponse<List<AntuReceiptResp>> response = antuService.getReceiptBatch(req, OmsPlatformEnum.OMS_SPT);
        System.out.println(response);
    }

    @Test
    public void getOutboundBatchTest() {
        AntuGetOutboundReq req = AntuGetOutboundReq.builder()
                .page(1)
                .pageSize(100)
//                .modifyDateFrom(LocalDateTime.parse("2024-08-26T18:30:11"))
//                .modifyDateTo(LocalDateTime.parse("2024-08-28T18:40:11"))
                .build();
        AntuResponse<List<AntuOutboundResp>> response = antuService.getOutboundBatch(req, OmsPlatformEnum.OMS_SPT);
        System.out.println(response);
    }

    @Test
    public void getShippingMethodTest() {
        AntuResponse<List<AntuLogisticsProductsResp>> response = antuService.getShippingMethod(null, OmsPlatformEnum.OMS_SPT);
        System.out.println(JSONUtil.toJsonStr(response));
    }

    @Test
    public void createOutboundBillTest() {
        AntuCreateOutboundReq antuCreateOutboundReq = AntuCreateOutboundReq.builder()
                .referenceNo("WJTEST20240821008")
                .swOrderNumber("14465312644131564841")
                .shippingMethod("PAC")
                .verify(1)
                .warehouseCode("CNLG")
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
                                .productSku("D014")
                                .quantity(1)
                                .build()
                ))
                .build();
        AntuResponse<String> response = antuService.createOutboundBill(antuCreateOutboundReq, OmsPlatformEnum.OMS_SPT);
        System.out.println(response);
    }

    @Test
    public void cancelOutboundBillTest() {
        AntuResponse<String> response = antuService.cancelOutboundBill("A001-240621-0003","平台拦截", OmsPlatformEnum.OMS_SPT);
        System.out.println(response);
    }

    @Test
    public void createInboundBillTest() {
        AntuCreateInboundReq antuGetReceiptReq = AntuCreateInboundReq.builder()
                .referenceNo("wjtest20231206")
                .incomeType(0)
//                .transitWarehouseCode("CNLG")
                .receivingType("D")
                .smCode("PAC")
                .contacter("张三")
                .contactPhone("123456789")
                .warehouseCode("CNLG")
                .customerType("Y")
                .verify(0)
                .items(Arrays.asList(AntuCreateInboundReq.Item.builder()
                        .productSku("D014")
                        .boxNo(1)
                        .quantity(1)
                        .build()))
                .build();
        System.out.println(JSONUtil.toJsonStr(antuGetReceiptReq));
        AntuResponse<String> response = antuService.createInboundBill(antuGetReceiptReq, OmsPlatformEnum.OMS_SPT);
        System.out.println(response);
    }

    @Test
    public void editInboundBillTest() {
        AntuCreateInboundReq antuGetReceiptReq = AntuCreateInboundReq.builder()
                .referenceNo("wjtest20231206")
                .receivingCode("RVVIJIM-250226-0005")
                .warehouseCode("CNLG")
                .verify(1)
                .items(Arrays.asList(AntuCreateInboundReq.Item.builder()
                        .productSku("D014")
                        .boxNo(1)
                        .quantity(14)
                        .build()))
                .build();
        AntuResponse<String> response = antuService.editInboundBill(antuGetReceiptReq, OmsPlatformEnum.OMS_SPT);
        System.out.println(response);
    }
    @Test
    public void cancelInboundBillTest() {
        AntuResponse<String> response = antuService.cancelInboundBill("RVA001-240826-0003", OmsPlatformEnum.OMS_SPT);
        System.out.println(response);
    }
    @Test
    public void getReturnInstockTest() {
        AntuGetReturnReq antuGetReturnReq = AntuGetReturnReq.builder()
                .page(1)
                .pageSize(100)
                .build();
        AntuResponse<List<AntuReturnResp>> response = antuService.getReturnInstock(antuGetReturnReq, OmsPlatformEnum.OMS_SPT);
        System.out.println(JSONUtil.toJsonStr(response));
    }

    @Test
    public void getCalculateFeeBatch() {
        AntuCalculateFeeReq antuCalculateFeeReq = AntuCalculateFeeReq.builder()
                .warehouseCode("HRBW")
                .countryCode("RU")
                .shippingMethod(Arrays.asList("FF","US"))
                .postcode("456145615")
                .weight(0.2F)
//                .length(1F)
//                .width(1F)
//                .height(1F)
                .build();
        AntuResponse<List<AntuCalculateFeeResp>> response = antuService.getCalculateFeeBatch(antuCalculateFeeReq, OmsPlatformEnum.OMS_SPT);
        System.out.println(JSONUtil.toJsonStr(response));
    }
}