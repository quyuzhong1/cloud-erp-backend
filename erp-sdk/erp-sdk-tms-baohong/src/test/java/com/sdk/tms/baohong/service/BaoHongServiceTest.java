package com.sdk.tms.baohong.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.business.threadlocal.TransferLogisticsContext;
import com.sdk.tms.baohong.api.asn.ASNData;
import com.sdk.tms.baohong.api.asn.ReceivingInfo;
import com.sdk.tms.baohong.api.asn.ReceivingItemsType;
import com.sdk.tms.baohong.api.order.CreateOrderInfo;
import com.sdk.tms.baohong.api.order.OrderDataArr;
import com.sdk.tms.baohong.api.order.ProductDeatil;
import com.sdk.tms.baohong.api.order.SmRow;
import com.sdk.tms.baohong.api.product.DataRow;
import com.sdk.tms.baohong.api.product.ProductRow;
import com.sdk.tms.baohong.api.product.RecordItemRequest;
import com.sdk.tms.baohong.api.product.RecordItemResponse;
import com.sdk.tms.baohong.dto.response.BaoHongResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//生产 customerCode: E3138 appToken : 386E6532DEA4EC65 appKey 2c5bd44acfe6f61c7421c800190781f8
//测试 customerCode: E0207 appToken:  BAAC60E49804C53A appKey 98f8fd9bb9edfa770bc0a317b8203fc3
@RunWith(SpringRunner.class)
@SpringBootTest(classes= BaoHongService.class)
public class BaoHongServiceTest {

    @Resource
    private BaoHongService baoHongService;

    public BaoHongServiceTest(){
        Map<String,String> authMap = new HashMap<>();
        //a39ab99c1437c991ec07fad4e1f78f8f
        authMap.put("appToken","BAAC60E49804C53A");
        //f7e4102f9b0b983e58bed3140dc22f1a
        authMap.put("appKey","98f8fd9bb9edfa770bc0a317b8203fc3");
        authMap.put("customerCode","E0207");
        TransferLogisticsContext.setAuthMap(authMap);
    }


    @Test
    public void getShippingMethodListTest(){
        BaoHongResponse<List<SmRow>> response = baoHongService.getShippingMethodList();
        System.out.println(response);
        System.out.println(response.getData());
    }

    @Test
    public void getAllProductInfo(){
        BaoHongResponse<List<DataRow>> response = baoHongService.getAllProductInfo();
        List<DataRow> dataRowList = response.getData().stream().filter(v->v.getProductSku().equals("kktwo")).collect(Collectors.toList());
        System.out.println(dataRowList);
        System.out.println(response.getData());
    }

    @Test
    public void getProductInfo(){
        BaoHongResponse<ProductRow> response = baoHongService.getProductInfo("2460");
        System.out.println(response);
        System.out.println(response.getData());
        long now = System.currentTimeMillis();
        for(int i = 0;i<=800;i++){
            BaoHongResponse<ProductRow> response2 = baoHongService.getProductInfo("2460");
            System.out.println(response2);
        }
        long end = System.currentTimeMillis();
        System.out.println(end - now);
    }

    @Test
    public void filingProduct(){
        RecordItemRequest recordItemRequest = RecordItemRequest.builder()
                .sku("2461")
                .name("三脚架")
                .englishName("Tripod")
                .unit("007")
                .currencyCode("RMB")
                .declaredValue(14.4400f)
                .weight(342)
                .length(9.5f)
                .width(5.5f)
                .height(18f)
                .hasBattery(0)
                .hsName("三脚架")
                .hsCode("9620009000")
                .hsElement("1|0|品牌:Ulanzi|相机拍摄用|型号:MT-40|cas|w")
                .firstQauntity(342)
                .build();
        BaoHongResponse<RecordItemResponse> response = baoHongService.filingProduct(recordItemRequest);
        System.out.println(response);
        System.out.println(response.getData());
    }

    @Test
    public void getCreateOrder(){
        String json = "{\n" +
                "  \"buyInsurance\":0,\n" +
                "  \"channel\":1,\n" +
                "  \"deliveryAddress\":\"Rua Humberto I 928\",\n" +
                "  \"grossWeight\":\"152\",\n" +
                "  \"iossNo\":\"\",\n" +
                "  \"oabCity\":\"São Paulo\",\n" +
                "  \"oabCountry\":\"BR\",\n" +
                "  \"oabName\":\"Rafaela Caixeta\",\n" +
                "  \"oabPhone\":\"+5534996757065\",\n" +
                "  \"oabPostcode\":\"04018032\",\n" +
                "  \"oabState\":\"SP\",\n" +
                "  \"oabStreetAddress1\":\"Rua Humberto I 928\",\n" +
                "  \"orderMode\":1,\n" +
                "  \"orderProduct\":[\n" +
                "    {\n" +
                "      \"opQuantity\":1,\n" +
                "      \"productSku\":\"kktwo\",\n" +
                "      \"purposeDeclaredValue\":\"8.14\",\n" +
                "      \"productTitleEn\":\"microphone\",\n" +
                "    }\n" +
                "  ],\n" +
                "  \"orderStatus\":\"2\",\n" +
                "  \"referenceNo\":\"XSTEST240902471\",\n" +
                "  \"serialNo\":\"\",\n" +
                "  \"smCode\":\"ZY-KJWS\",\n" +
                "  \"trackingNumber\":\"WStest120456365\",\n" +
                "  \"warehouseCode\":\"sz01\"\n" +
                "}";
        CreateOrderInfo createOrderInfo = JSONObject.parseObject(json,new TypeReference<CreateOrderInfo>() {}.getType());
        System.out.println(json);
//        CreateOrderInfo createOrderInfo = CreateOrderInfo.builder()
//                .oabCountry("CN")
//                .smCode("TY-DHL")
//                .orderProduct(Arrays.asList(
//                        ProductDeatil.builder()
//                                .productSku("484654-6")
//                                .opQuantity(1)
//                                .purposeDeclaredValue("8.14")
//                                .build()
//                ))
//                .orderStatus("2")
//                .trackingNumber("314r131122")
//                .oabName("wj")
//                .referenceNo("wj2022432121")
//                .deliveryAddress("深圳龙岗坂田")
//                .oabStreetAddress1("深圳龙岗坂田")
//                .build();
        BaoHongResponse<String> response = baoHongService.createOrder(createOrderInfo);
        System.out.println(response);
        System.out.println(response.getData());
    }

    @Test
    public void cancelOrder(){
        BaoHongResponse<String> response = baoHongService.cancelOrder("SOE02070223645","平台发货异常");
        System.out.println(response);
    }

    @Test
    public void getOrderByCodeTest(){
        BaoHongResponse<OrderDataArr> response = baoHongService.getOrderByCode("SOE02070223440");
        System.out.println(response);
        System.out.println(response.getData());
    }

    @Test
    public void createReceiving(){
        ReceivingInfo receivingInfo = ReceivingInfo.builder()
                .refCode("SOE02070238021")
                .wrapType("1")
                .packNo("10")
                .roughWeight("10.12")
                .iePort("5349")
                .isDelivery("1")
                .receivingItems(Arrays.asList(ReceivingItemsType.builder()
                                .orderCode("SOE02070222879")
                                .groossWeight("100")
                        .build()))
                .build();
        String json = JSONObject.toJSONString(receivingInfo);

        BaoHongResponse<String> response = baoHongService.createReceiving(receivingInfo);
        System.out.println(response);
        System.out.println(response.getData());
    }

    @Test
    public void getReceiving(){
        ReceivingInfo receivingInfo = ReceivingInfo.builder()
                .refCode("SOE02070238021")
                .wrapType("1")
                .packNo("10")
                .roughWeight("10.12")
                .iePort("5349")
                .isDelivery("1")
                .receivingItems(Arrays.asList(ReceivingItemsType.builder()
                        .orderCode("SOE02070222879")
                        .groossWeight("100")
                        .build()))
                .build();
        String json = JSONObject.toJSONString(receivingInfo);

        BaoHongResponse<ASNData> response = baoHongService.getReceiving("SOE02070238021");
        System.out.println(response);
        System.out.println(response.getData());
    }

    @Test
    public void printLabel(){
        BaoHongResponse<String> response = baoHongService.printLabel("wjtestserial001");
        System.out.println(response);
        System.out.println(response.getData());
    }
}