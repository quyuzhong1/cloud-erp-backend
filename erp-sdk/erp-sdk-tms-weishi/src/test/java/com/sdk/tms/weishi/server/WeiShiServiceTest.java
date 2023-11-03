package com.sdk.tms.weishi.server;


import com.sdk.tms.weishi.dto.request.WeiShiCancelOrderRequest;
import com.sdk.tms.weishi.dto.request.WeiShiCreateOrderRequest;
import com.sdk.tms.weishi.dto.request.WeiShiGetLabelUrlRequest;
import com.sdk.tms.weishi.dto.request.WeiShiInterceptOrderRequest;
import com.sdk.tms.weishi.dto.response.WeiShiChannel;
import com.sdk.tms.weishi.dto.response.WeiShiCreateOrder;
import com.sdk.tms.weishi.dto.response.WeiShiGetLabelUrl;
import com.sdk.tms.weishi.dto.response.WeiShiResponse;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=WeiShiService.class)
class WeiShiServiceTest {

    @Resource
    private WeiShiService weiShiService;

    @Test
    void getAllChannel() {
        WeiShiResponse<List<WeiShiChannel>> channel= weiShiService.getAllChannel();
        System.out.println(channel.getData());
    }

    @Test
    void createOrder() {
        WeiShiCreateOrderRequest weiShiCreateOrderRequest = WeiShiCreateOrderRequest.builder()
                .referenceNo("TEST2019102800132")
                .shippingMethod("MX1001")
                .countryCode("MX")
                .orderWeight(1.2F)
                .consignee(WeiShiCreateOrderRequest.Consignee.builder()
                        .consigneeStreet("纽约")
                        .consigneeName("mask")
                        .consigneePostcode("11510")
                        .consigneeCity("MIGUEL ")
                        .consigneeMobile("123456789")
                        .consigneeProvince("纽约")
                        .build())
                .shipper(WeiShiCreateOrderRequest.Shipper.builder()
                        .shipperCountrycode("CN")
                        .shipperProvince("广东")
                        .shipperCity("汕头")
                        .shipperStreet("唯迹")
                        .shipperPostcode("wj")
                        .shipperName("亚瑟")
                        .shipperTelephone("321423135")
                        .shipperMobile("123456789")
                        .build())
                .itemArr(Arrays.asList(WeiShiCreateOrderRequest.ItemArr.builder()
                        .invoiceEnname("Shoes")
                        .invoiceWeight(1.52F)
                        .invoiceQuantity(10)
                        .invoiceUnitcharge(1.53F)
                        .build()))
                .build();
        WeiShiCreateOrder response = weiShiService.createOrder(weiShiCreateOrderRequest);
        System.out.println(response);
    }

    @Test
    void getLabelUrl() {
        WeiShiGetLabelUrlRequest weiShiGetLabelUrlRequest = WeiShiGetLabelUrlRequest.builder()
                .referenceNo("TEST2019102800132")
                .lableType("2")
                .build()
                ;
        WeiShiGetLabelUrl weiShiGetLabelUrl = weiShiService.getLabelUrl(weiShiGetLabelUrlRequest);
        System.out.println(weiShiGetLabelUrl);
    }

    @Test
    void interceptOrder() {
        WeiShiInterceptOrderRequest weiShiGetLabelUrlRequest = WeiShiInterceptOrderRequest.builder()
                .referenceNo("TEST2019102800132")
                .build()
                ;
        WeiShiResponse response = weiShiService.interceptOrder(weiShiGetLabelUrlRequest);
        System.out.println(response);
    }

    @Test
    void cancelOrder() {
        WeiShiCancelOrderRequest weiShiCancelOrderRequest = WeiShiCancelOrderRequest.builder()
                .referenceNo("TEST2019102800132")
                .build()
                ;
        WeiShiResponse response = weiShiService.cancelOrder(weiShiCancelOrderRequest);
        System.out.println(response);
    }
}