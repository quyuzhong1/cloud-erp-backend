package com.sdk.tms.baohong.service;

import com.alibaba.fastjson.JSONObject;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.business.threadlocal.TransferLogisticsContext;
import com.sdk.tms.baohong.api.asn.ReceivingInfo;
import com.sdk.tms.baohong.api.asn.ReceivingItemsType;
import com.sdk.tms.baohong.api.order.CreateOrderInfo;
import com.sdk.tms.baohong.api.order.OrderDataArr;
import com.sdk.tms.baohong.api.order.ProductDeatil;
import com.sdk.tms.baohong.api.order.SmRow;
import com.sdk.tms.baohong.api.product.DataRow;
import com.sdk.tms.baohong.api.product.ProductRow;
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
        BaoHongResponse<ProductRow> response = baoHongService.getProductInfo("20221121013-1");
        System.out.println(response);
        System.out.println(response.getData());
    }

    @Test
    public void getCreateOrder(){

        CreateOrderInfo createOrderInfo = CreateOrderInfo.builder()
                .oabCountry("CN")
                .smCode("TY-DHL")
                .orderProduct(Arrays.asList(
                        ProductDeatil.builder()
                                .productSku("484654-6")
                                .opQuantity(1)
                                .build()
                ))
                .trackingNumber("123456781011")
                .oabName("wj")
                .referenceNo("wj202401211")
                .deliveryAddress("深圳龙岗坂田")
                .oabStreetAddress1("深圳龙岗坂田")
                .build();
        BaoHongResponse<String> response = baoHongService.createOrder(createOrderInfo);
        System.out.println(response);
        System.out.println(response.getData());
    }


    @Test
    public void getOrderByCodeTest(){
        BaoHongResponse<OrderDataArr> response = baoHongService.getOrderByCode("SOE02070222796");
        System.out.println(response);
        System.out.println(response.getData());
    }

    @Test
    public void createReceiving(){
        ReceivingInfo receivingInfo = ReceivingInfo.builder()
                .refCode("SOE02070222802")
                .wrapType("1")
                .packNo("10")
                .roughWeight("10.12")
                .iePort("5349")
                .receivingItems(Arrays.asList(ReceivingItemsType.builder()
                                .orderCode("SOE02070222802")
                                .groossWeight("100")
                        .build()))
                .build();
        String json = JSONObject.toJSONString(receivingInfo);

        BaoHongResponse<String> response = baoHongService.createReceiving(receivingInfo);
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