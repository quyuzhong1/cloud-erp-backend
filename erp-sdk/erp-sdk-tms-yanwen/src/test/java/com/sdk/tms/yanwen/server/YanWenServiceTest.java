package com.sdk.tms.yanwen.server;

import com.common.core.utils.FileUtil;
import com.sdk.tms.yanwen.dto.request.YanWenCancelOrderRequest;
import com.sdk.tms.yanwen.dto.request.YanWenCreateWayBillRequest;
import com.sdk.tms.yanwen.dto.request.YanWenGetLabelRequest;
import com.sdk.tms.yanwen.dto.request.YanWenQueryOrderRequest;
import com.sdk.tms.yanwen.dto.response.YanWenGetLabel;
import com.sdk.tms.yanwen.dto.response.YanWenResponse;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=YanWenService.class)
class YanWenServiceTest {

    @Resource
    private YanWenService yanWenService;

    private Map<String, String> authMap = new HashMap<>();

    /**
     * 生产环境：
     * url:Https://open.yw56.com.cn
     * clientId:30114979
     * clientSercet:62854821B3B261983589ACFE94C7D652
     *
     * UAT:
     * url:
     * clientId:100000
     * clientSercet:D6140AA383FD8515B09028C586493DDB
     */
    public YanWenServiceTest(){
//        authMap.put("url","Https://open.yw56.com.cn");
        authMap.put("clientId","30114979");
        authMap.put("clientSecret","62854821B3B261983589ACFE94C7D652");
    }
    @Test
    public void getAllChannel() {
        System.out.println(yanWenService.getAllChannel(authMap));
    }

    @Test
    public void createWayBill() {
        YanWenCreateWayBillRequest yanWenCreateWayBillRequest = YanWenCreateWayBillRequest.builder()
                .orderNumber("weiji1233223")
                .channelId("481")
                .orderSource("weijiERP")
                .receiverInfo(YanWenCreateWayBillRequest.ReceiverInfo.builder()
                        .name("Arthur")
                        .country("115")
                        .address("jia xing wu liu yuan A22-28")
                        .phone("153442")
                        .state("广东")
                        .city("汕头")
                        .zipCode("12201")
                        .build())
                .parcelInfo(YanWenCreateWayBillRequest.ParcelInfo.builder()
                        .hasBattery(1)
                        .currency("USD")
                        .totalPrice(new BigDecimal("12"))
                        .totalQuantity(3)
                        .totalWeight(10)
                        .productList(Collections.singletonList(YanWenCreateWayBillRequest.ParcelInfo.Product.builder()
                                .goodsNameCh("摩托车居家服")
                                .goodsNameEn("Motorcycle Homewear")
                                .price(new BigDecimal("21.5"))
                                .quantity(123)
                                .weight(53)
                                .build()))
                        .build())
                .build();
        System.out.println(yanWenService.createWayBill(yanWenCreateWayBillRequest,authMap));
    }

    @Test
    public void getLabel() {
        YanWenGetLabelRequest request = YanWenGetLabelRequest.builder()
                .waybillNumber("UJ000087568YP")
                .printRemark(1)
                .build();
        YanWenResponse<YanWenGetLabel> response = yanWenService.getLabel(request,authMap);
        String base64 = response.getData().getBase64String();
        FileUtil.base64ToFile(base64,"wayBill1.pdf","C:\\Users\\Administrator\\Desktop");
        System.out.println(response);
    }

    @Test
    public void cancelOrder() {
        YanWenCancelOrderRequest request = YanWenCancelOrderRequest.builder()
                .waybillNumber("LR083592414CN")
                .build();
        System.out.println(yanWenService.cancelOrder(request,authMap));
    }

    @Test
    public void queryOrder() {
        YanWenQueryOrderRequest request = YanWenQueryOrderRequest.builder()
                .listNumber(Arrays.asList("XSDS24041500001"))
                .build();
        System.out.println(yanWenService.queryOrder(request,authMap).getData());
    }
}