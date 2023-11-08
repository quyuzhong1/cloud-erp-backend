package com.sdk.tms.yanwen.server;

import com.common.core.utils.FileUtil;
import com.sdk.tms.yanwen.dto.request.YanWenCancelOrderRequest;
import com.sdk.tms.yanwen.dto.request.YanWenCreateWayBillRequest;
import com.sdk.tms.yanwen.dto.request.YanWenGetLabelRequest;
import com.sdk.tms.yanwen.dto.request.YanWenQueryOrderRequest;
import com.sdk.tms.yanwen.dto.response.YanWenGetLabel;
import com.sdk.tms.yanwen.dto.response.YanWenResponse;
import jodd.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=YanWenService.class)
class YanWenServiceTest {

    @Resource
    private YanWenService yanWenService;

    @Test
    public void getAllChannel() {
        System.out.println(yanWenService.getAllChannel());
    }

    @Test
    public void createWayBill() {
        YanWenCreateWayBillRequest yanWenCreateWayBillRequest = YanWenCreateWayBillRequest.builder()
                .orderNumber("weiji1233211")
                .channelId("155")
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
        System.out.println(yanWenService.createWayBill(yanWenCreateWayBillRequest));
    }

    @Test
    public void getLabel() {
        YanWenGetLabelRequest request = YanWenGetLabelRequest.builder()
                .waybillNumber("LR084318011CN")
                .build();
        YanWenResponse<YanWenGetLabel> response = yanWenService.getLabel(request);
        String base64 = response.getData().getBase64String();
        FileUtil.base64ToFile(base64,"wayBill.pdf","C:\\Users\\Administrator\\Desktop");
        System.out.println(yanWenService.getLabel(request));
    }

    @Test
    public void cancelOrder() {
        YanWenCancelOrderRequest request = YanWenCancelOrderRequest.builder()
                .waybillNumber("LR083592414CN")
                .build();
        System.out.println(yanWenService.cancelOrder(request));
    }

    @Test
    public void queryOrder() {
        YanWenQueryOrderRequest request = YanWenQueryOrderRequest.builder()
                .listNumber(Arrays.asList("LR083592414CN","weiji1233211"))
                .build();
        System.out.println(yanWenService.queryOrder(request).getData());
    }
}