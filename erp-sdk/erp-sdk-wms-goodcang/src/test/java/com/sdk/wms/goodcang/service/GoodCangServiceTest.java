package com.sdk.wms.goodcang.service;


import cn.hutool.json.JSONUtil;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.sdk.wms.goodcang.dto.request.*;
import com.sdk.wms.goodcang.dto.response.*;
import com.sdk.wms.goodcang.enums.GoodCangEnums;
import com.sdk.wms.goodcang.utils.GoodCangUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

//生产 url : https://oms.goodcang.net  appToken : a39ab99c1437c991ec07fad4e1f78f8f appKey f7e4102f9b0b983e58bed3140dc22f1a
//测试 url : https://uat-oms.eminxing.com appToken:  7013991264f611e98ea200e01b680258 appKey 6ff50abf64f611e98ea200e01b680258
@RunWith(SpringRunner.class)
@SpringBootTest(classes={GoodCangService.class, GoodCangUtils.class})
@TestPropertySource(properties = {"warehouse.goodcang.url=https://uat-oms.eminxing.com"})
public class GoodCangServiceTest {
    @Resource
    private GoodCangService goodCangService;

    public GoodCangServiceTest(){
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appToken","7013991264f611e98ea200e01b680258");
        authMap.put("appKey","6ff50abf64f611e98ea200e01b680258");
        ThirdWarehouseContext.setAuthMap(authMap);
    }

    @Test
    public void authorizationTest() {
    }

    @Test
    public void getSkuListTest() {
        GoodCangGetSkuReq goodCangGetSkuReq = GoodCangGetSkuReq.builder()
                .page(1)
                .pageSize(100)
                .productUpdateTimeFrom("2023-09-26 15:00:00")
                .productUpdateTimeTo("2023-09-26 16:00:00")
//                .productSku("YDXN5C-001300UK")
//                .productSkuArr(Arrays.asList("M032GBB1","L055GBA1"))
                .build();
        List<GoodCangSkuResp> respList = new ArrayList<>();
        GoodCangResponse<List<GoodCangSkuResp>> goodCangResponse = goodCangService.getSkuList(goodCangGetSkuReq);
        System.out.println(goodCangResponse);
        respList = respList.stream().filter(v->v.getProductStatus().equals(GoodCangEnums.OpenApiProductStatusEnum.AVAILABLE.getCode())).collect(Collectors.toList());
        System.out.println(respList.size());
    }

    @Test
    public void getWarehouseTest() {
        GoodCangResponse<List<GoodCangWarehouseResp>> response = goodCangService.getWarehouse();
        System.out.println(response);
    }

    @Test
    public void getInboundDetailTest() {
        GoodCangResponse<GoodCangReceiptBatchResp> response = goodCangService.getInboundDetail("RVG2199-231222-0001");
        System.out.println(response);
    }
//    @Test
//    public void getReceiptBatchTest() {
//        GoodCangResponse<GoodCangReceiptBatchResp> response = goodCangService.getReceiptBatch("RVG1149-230613-0002");
//        System.out.println(response);
//    }

    @Test
    public void getOutboundTest() {
        GoodCangGetOutBoundReq goodCangGetOutBoundReq = GoodCangGetOutBoundReq.builder()
                .modifyDateFrom(LocalDateTime.of(2018,11,20, 0, 0, 0))
                .modifyDateTo(LocalDateTime.of(2018,12,20, 0, 0, 0))
//                .orderCode("G1149-240515-0008")
                .page(1)
                .pageSize(20)
                .build();
        GoodCangResponse<List<GoodCangOutboundResp>> response = goodCangService.getOutboundBatch(goodCangGetOutBoundReq);
        System.out.println(response);
    }

    @Test
    public void getSmCodeTwcToWarehouseTest() {
        GoodCangResponse<GoodCangLogisticsAndWarehouseResp> response = goodCangService.getSmCodeTwcToWarehouse();
        System.out.println(response);
    }


    @Test
    public void getProductInventoryTest() {
        GoodCangGetInventoryReq goodCangGetSkuReq = GoodCangGetInventoryReq.builder()
                .page(1)
                .pageSize(200)
//                .productSkuArr(Arrays.asList("TEST180717"))
//                .warehouseCode("USEA")
                .build();
        GoodCangResponse<List<GoodCangInventoryResp>> response = goodCangService.getProductInventory(goodCangGetSkuReq);
        System.out.println(response);
        System.out.println(response.getData());
    }


    @Test
    public void getShippingMethodTest() {
        GoodCangGetInventoryReq goodCangGetSkuReq = GoodCangGetInventoryReq.builder()
                .page(1)
                .pageSize(200)
                .build();
        GoodCangResponse<List<GoodCangLogisticsProductsResp>> response = goodCangService.getShippingMethod("");
        System.out.println(response);
        System.out.println(response.getData());
    }
    @Test
    public void createInboundBillTest() {
        GoodCangCreateInboundReq goodCangCreateInboundReq = GoodCangCreateInboundReq.builder()
                .transitType(0)
                .receivingShippingType(0)
                .warehouseCode("USEA")
                .etaDate(LocalDate.parse("2024-01-20", DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .trackingNumber("wjtest321123")
                .verify(0)
                .items(Arrays.asList(GoodCangCreateInboundReq.Item.builder()
                        .boxNo("1")
                        .box_detailList(Arrays.asList(GoodCangCreateInboundReq.Item.BoxDetail.builder()
                                .productSku("QC6SHR-031500US")
                                .quantity(10)
                                .build()))
                        .build()))
                .build();
        GoodCangResponse<String> response = goodCangService.createInboundBill(goodCangCreateInboundReq);
        System.out.println(response);
        System.out.println(response.getData());
    }

    @Test
    public void editInboundBillTest() {
        GoodCangCreateInboundReq goodCangCreateInboundReq = GoodCangCreateInboundReq.builder()
                .receivingCode("RVG1149-231227-0011")
                .transitType(0)
                .receivingShippingType(0)
                .warehouseCode("USEA")
                .etaDate(LocalDate.parse("2024-01-20", DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .trackingNumber("wjtest321123")
                .verify(1)
                .items(Arrays.asList(GoodCangCreateInboundReq.Item.builder()
                        .boxNo("1")
                        .box_detailList(Arrays.asList(GoodCangCreateInboundReq.Item.BoxDetail.builder()
                                .productSku("H1226")
                                .quantity(12)
                                .build()))
                        .build()))
                .build();
        GoodCangResponse<String> response = goodCangService.editInboundBill(goodCangCreateInboundReq);
        System.out.println(response);
        System.out.println(response.getData());
    }
    @Test
    public void cancelInboundBillTest() {
        GoodCangResponse<String> response = goodCangService.cancelInboundBill("RVG1149-231116-0004");
        System.out.println(response);
        System.out.println(response.getData());
    }

    @Test
    public void createOutboundBillTest() {
        GoodCangCreateOutboundReq goodCangCreateOutboundReq = GoodCangCreateOutboundReq.builder()
                .name("mark")
                .city("newyork")
                .warehouseCode("USWE")
                .shippingMethod("MORKRM-DUOBOX")
                .address1("address")
                .countryCode("US")
                .zipcode("22162")
                .province("ca")
                .verify(1)
                .itemList(Arrays.asList(GoodCangCreateOutboundReq.Item.builder()
                                .productSku("USWR410242")
                                .quantity(1)
                        .build()))
                .build();
        GoodCangResponse<String> response = goodCangService.createOutboundBill(goodCangCreateOutboundReq);
        System.out.println(response);
        System.out.println(response.getData());
    }

    @Test
    public void getOutboundCodeTest() {
        GoodCangResponse<String> response = goodCangService.getOutboundCode("XSDS24012000006");
        System.out.println(response);
        System.out.println(response.getData());
    }
    @Test
    public void cancelOutboundBillTest() {
        GoodCangResponse<String> response = goodCangService.cancelOutboundBill("G1149-231116-005",null);
        System.out.println(response);
        System.out.println(response.getData());
    }

    @Test
    public void getReturnInstockTest() {
        GoodCangGetReturnInstockReq goodCangGetReturnInstockReq = GoodCangGetReturnInstockReq
                .builder()
                .currentPage(1)
                .pageSize(5)
//                .asroStatus(5)
                .build();
        GoodCangResponse<List<GoodCangReturnInstockResp>> response = goodCangService.getReturnInstock(goodCangGetReturnInstockReq);
        System.out.println(response);
        System.out.println(JSONUtil.toJsonStr(response.getData()));
    }

}