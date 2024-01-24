package com.sdk.tms.baohong.service;

import com.alibaba.fastjson.JSONObject;
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
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= BaoHongService.class)
public class BaoHongServiceTest {

    @Resource
    private BaoHongService baoHongService;

    @Test
    public void getShippingMethodListTest(){
        BaoHongResponse<List<SmRow>> response = baoHongService.getShippingMethodList();
        System.out.println(response);
        System.out.println(response.getData());
    }

    @Test
    public void getAllProductInfo(){
        BaoHongResponse<List<DataRow>> response = baoHongService.getAllProductInfo();
        System.out.println(response);
        System.out.println(response.getData());
    }

    @Test
    public void getProductInfo(){
        BaoHongResponse<ProductRow> response = baoHongService.getProductInfo("20240120310-1");
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
        BaoHongResponse<OrderDataArr> response = baoHongService.getOrderByCode("SOE02070222802");
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
        BaoHongResponse<String> response = baoHongService.printLabel("SOE02070222806");
        System.out.println(response);
        System.out.println(response.getData());
    }
}