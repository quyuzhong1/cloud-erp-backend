package com.sdk.wms.iml.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.sdk.wms.iml.dto.request.*;
import com.sdk.wms.iml.dto.response.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= ImlService.class)
public class ImlServerTest {

    @Resource
    private ImlService imlServer;

    public ImlServerTest(){
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appToken","bee4c5e37009b3f4d8a18f7ce44a2365");
        authMap.put("appKey","674f5f45f2ffdaa6705d88d38d5d04a3");
        ThirdWarehouseContext.setAuthMap(authMap);
    }


    @Test
    public void getSkuList() {
        ImlGetProductReq imlProductReq = ImlGetProductReq.builder()
                .page(1)
                .pageSize(50)
//                .productSku("0044")
//                .productSkuArr(Arrays.asList("0044"))
                .updateStartTime("2000-01-01 11:11:11")
                .updateEndTime("2024-01-01 11:11:11")
                .build();
        ImlResponse<List<ImlProductResp>> response = imlServer.getSkuList(imlProductReq);
        System.out.println(response);
    }

    @Test
    public void getWarehouseTest() {
        ImlGetProductReq imlProductReq = ImlGetProductReq.builder()
//                .page(1)
//                .pageSize(50)
                .build();
        ImlResponse<List<ImlWarehouseResp>> response = imlServer.getWarehouse(imlProductReq);
        System.out.println(response);
    }

    @Test
    public void getReceivingRegionTest() {
        ImlResponse<List<ImlRegionResp>> response = imlServer.getReceivingRegion();
        System.out.println(response);
    }

    @Test
    public void getReceiptBatchTest() {
        ImlGetReceiptReq imlGetReceiptReq = ImlGetReceiptReq.builder()
                .page(1)
                .pageSize(2)
//                .receivingCode("RV86526-230919-0005")
                .receivingCodeArr(Arrays.asList("1234564"))
                .build();
        ImlResponse<List<ImlReceiptResp>> response = imlServer.getReceiptBatch(imlGetReceiptReq);
        System.out.println(response);
    }

    @Test
    public void getProductInventoryTest() {
        ImlGetInventoryReq imlGetInventoryReq = ImlGetInventoryReq.builder()
                .page(1)
                .pageSize(1000)
//                .receivingCode("RV86526-230919-0005")
                .build();
        ImlResponse<List<ImlInventoryResp>> response = imlServer.getProductInventory(imlGetInventoryReq);
        System.out.println(response);
    }

    @Test
    public void getShippingMethodTest() {
        ImlResponse<List<ImlInventoryLogisticsProductsResp>> response = imlServer.getShippingMethod("");
        System.out.println(response);
    }

    @Test
    public void createInboundBillTest() {
        ImlCreateInboundReq imlGetReceiptReq = ImlCreateInboundReq.builder()
                .referenceNo("wjtest20231205")
                .warehouseCode("UAW1")
                .verify(0)
                .items(Arrays.asList(ImlCreateInboundReq.Item.builder()
                                .productSku("2898")
                                .boxNo(1)
                                .quantity(1)
                        .build()))
                .build();
        ImlResponse<String> response = imlServer.createInboundBill(imlGetReceiptReq);
        System.out.println(response);
    }
    @Test
    public void editInboundBillTest() {
        ImlCreateInboundReq imlGetReceiptReq = ImlCreateInboundReq.builder()
                .receivingCode("RV86526-231222-0009")
                .referenceNo("FHD23122200011")
                .warehouseCode("RUS2")
                .verify(1)
                .items(Arrays.asList(ImlCreateInboundReq.Item.builder()
                        .productSku("86526-2503A")
                        .boxNo(1)
                        .quantity(13)
                        .build()))
                .build();
        ImlResponse<String> response = imlServer.editInboundBill(imlGetReceiptReq);
        System.out.println(response);
    }
    @Test
    public void cancelInboundBillTest() {
        ImlResponse<String> response = imlServer.cancelInboundBill("RV86526-231205-0001");
        System.out.println(response);
    }

    @Test
    public void createOutboundBillTest() {
        ImlCreateOutboundReq imlCreateOutboundReq;
        String json = " {\n" +
                "                    \"platform\":\"OTHER\",\n" +
                "                    \"allocated_auto\":\"0\",\n" +
                "                    \"warehouse_code\":\"RUS2\",\n" +
                "                    \"shipping_method\":\"AE-HUB-3000566871\",\n" +
                "                    \"reference_no\":\"5292418095450107\",\n" +
                "                    \"aliexpress_order_no\":\"8000777788889999\",\n" +
                "                    \"order_desc\":\"\\u8ba2\\u5355\\u63cf\\u8ff0\",\n" +
                "                    \"country_code\":\"RU\",\n" +
                "                    \"province\":\"province\",\n" +
                "                    \"city\":\"city\",\n" +
                "                    \"address1\":\"address1\",\n" +
                "                    \"address2\":\"address2\",\n" +
                "                    \"address3\":\"address3\",\n" +
                "                    \"zipcode\":\"142970\",\n" +
                "                    \"doorplate\":\"doorplate\",\n" +
                "                    \"company\":\"company\",\n" +
                "                    \"name\":\"name\",\n" +
                "                    \"phone\":\"123456789124\",\n" +
                "                    \"cell_phone\":\"cell_phone\",\n" +
                "                    \"email\":\"email\",\n" +
                "                    \"is_order_cod\":\"1\",\n" +
                "                    \"order_cod_price\":\"99\",\n" +
                "                    \"order_cod_currency\":\"RMB\",\n" +
                "                    \"order_age_limit\":\"22\",\n" +
                "                    \"is_signature\":\"0\",\n" +
                "                    \"is_insurance\":\"0\",\n" +
                "                    \"insurance_value\":\"0\",\n" +
                "                    \"items\":[\n" +
                "                        {\n" +
                "                            \"product_sku\":2898\"\",\n" +
                "                            \"product_name_en\":\"Product Name\",\n" +
                "                            \"product_declared_value\":\"5.000\",\n" +
                "                            \"quantity\":\"1\"\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"tracking_no\":\"123\",\n" +
                "                    \"label\":{\n" +
                "                        \"file_type\":\"png\",\n" +
                "                        \"file_data\":\"hVJPjUP4+yHjvKErt5PuFfvRhd...\"\n" +
                "                    },\n" +
                "                    \"lp_code\":\"AEOWH000034596\"\n" +
                "                }";
        imlCreateOutboundReq = JSONObject.parseObject(json,new TypeReference<ImlCreateOutboundReq>() {}.getType());
        ImlResponse<String> response = imlServer.createOutboundBill(imlCreateOutboundReq);
        System.out.println(response);
    }

    @Test
    public void cancelOutboundBillTest() {
        ImlResponse<String> response = imlServer.cancelOutboundBill("86526-231116-2374",null);
        System.out.println(response);
    }
    @Test
    public void getOutboundBillTest() {
        // 假设您有一个时间戳
        long timestamp = 1710175364977l; // 2021-03-09T00:00:00Z

        // 使用时间戳创建Instant对象
        Instant instant = Instant.ofEpochSecond(timestamp);

        // 将Instant对象转换为本地日期时间(LocalDateTime)，需要提供时区信息
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        // 打印转换后的本地日期时间
        System.out.println("LocalDateTime: " + localDateTime);
    }
}