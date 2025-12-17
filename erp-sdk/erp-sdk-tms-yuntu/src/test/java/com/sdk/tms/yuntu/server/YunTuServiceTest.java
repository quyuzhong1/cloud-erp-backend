package com.sdk.tms.yuntu.server;

import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.sdk.tms.yuntu.dto.request.*;
import com.sdk.tms.yuntu.dto.response.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=YunTuService.class)
public class YunTuServiceTest {

    @Resource
    private YunTuService yunTuService;

    private Map<String, String> authMap = new HashMap<>();

    public YunTuServiceTest(){
        //生产
        authMap.put("clientId","CNH1896658");
        authMap.put("clientSecret","58d89eba9f63431f9883de6800bd98df");
        authMap.put("url","http://oms.api.yunexpress.com");

        //测试
        authMap.put("clientId","ITC0893791");
        authMap.put("clientSecret","axzc2utvPbfc9UbJDOh+7w==");
        authMap.put("url","http://omsapi.uat.yunexpress.com");
    }

    @Test
    public void getAllChannel() {
        YunTuResponse<List<YunTuChannel>> response = yunTuService.getAllChannel(authMap);
        System.out.println(response);
    }

    @Test
    public void createOrder() {
        YunTuCreateOrderRequest request = YunTuCreateOrderRequest.builder()
                .customerOrderNumber("ceshi123456")
                .shippingMethodCode("HKTHZXR-RMB")
                .iossCode("IOSS0690112210251452600")
                .height(1)
                .length(1)
                .width(1)
                .applicationType(4)
                .returnOption(false)
                .tariffPrepay(false)
                .insuranceOption(0)
                .sourceCode("soB2c")
                .packageCount(1)
                .weight(new BigDecimal("12"))
                .platform(YunTuCreateOrderRequest.Platform.builder()
                        .platformCode("Shopify")
                        .platformName("Shopify")
                        .build())
                .receiver(YunTuCreateOrderRequest.Receiver.builder()
                        .countryCode("JP")
                        .firstName("海斗 園田")
                        .company("test")
                        .state("Hyōgo")
                        .zip("658-0022")
                        .phone("5869098233")
                        .houseNumber("1")
                        .email("12345@qq.com")
                        .street("67700 Lockwood-Jolon Road")
                        .city("Lockwood")
                        .build())
                .sender(YunTuCreateOrderRequest.Sender.builder()
                        .countryCode("US")
                        .firstName("test")
                        .lastName("ming")
                        .company("test gs")
                        .street("207 TELLURIDE DR")
                        .city("GEORGETOWN")
                        .state("TX")
                        .zip("78626-7163")
                        .phone("58690982363")
                        .build())
                .parcels(Arrays.asList(YunTuCreateOrderRequest.Parcels.builder()
                        .eName("Phone Holder")
                        .quantity(1)
                                .hsCode("9620009000")
                        .unitPrice(new BigDecimal("4.9672"))
                        .unitWeight(new BigDecimal("0.134"))
                        .cName("三脚架")
                        .sku("M065")
                        .remark(" 商 品")
                        .invoiceRemark("ceshi 1 pcs")
                        .build()))
                .childOrders(Arrays.asList(YunTuCreateOrderRequest.ChildOrders.builder()
                        .boxNumber("TEST")
                        .length(1)
                        .width(1)
                        .height(1).boxWeight(new BigDecimal("1"))
                        .childDetails(Arrays.asList(YunTuCreateOrderRequest.ChildOrders.ChildDetails.builder()
                                .sku("sku1001")
                                .quantity(1)
                                .build()))
                        .build()))
                .orderExtra(Arrays.asList(YunTuCreateOrderRequest.OrderExtra.builder()
                        .extraCode("V1")
                        .extraName("云途预缴")
                        .build()))
                .build();
        YunTuResponse<List<YunTuCreateOrder>> response = yunTuService.createOrder(Arrays.asList(request),authMap);
        System.out.println(response);
    }


    @Test
    public void getTrackingNumber() {
        YunTuGetTrackingNumRequest request = YunTuGetTrackingNumRequest.builder()
                .customerOrderNumber("WEIJI2023110901001")
                .build();
        YunTuResponse<List<YunTuTrackingNumber>> response = yunTuService.getTrackingNumber(request,authMap);
        System.out.println(response);
    }

    @Test
    public void getPrintLabel() {
        YunTuPrintLabelRequest request = YunTuPrintLabelRequest.builder()
                .orderNumbers(Arrays.asList("YT2335621272000004"))
                .build();
        YunTuResponse<List<YunTuPrintLabel>> response = yunTuService.getPrintLabel(request,authMap);
        System.out.println(response);
    }

    @Test
    public void interceptOrder() {
        YunTuInterceptOrderRequest request = YunTuInterceptOrderRequest.builder()
                .orderType(2)
                .orderNumber("WEIJI2023110901001")
                .remark("客户要求拦截")
                .build();
        YunTuResponse<YunTuInterceptOrder> response = yunTuService.interceptOrder(request,authMap);
        System.out.println(response);
    }

    @Test
    public void cancelOrder() {
        YunTuCancelOrderRequest request = YunTuCancelOrderRequest.builder()
                .orderType(2)
                .orderNumber("ceshi123")
                .build();
        YunTuResponse<YunTuCancelOrder> response = yunTuService.cancelOrder(request,authMap);
        System.out.println(response);
    }
    @Test
    public void updateWeight() {
        YunTuUpdateWeightRequest request = YunTuUpdateWeightRequest.builder()
                .weight(new BigDecimal("10.1"))
                .orderNumber("17369383218351200541")
                .build();
        YunTuResponse<String> response = yunTuService.updateWeight(request,authMap);
        System.out.println(response);
    }
}