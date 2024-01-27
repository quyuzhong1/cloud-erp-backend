package com.erp.server.tms.service.transfer;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.threadlocal.TransferLogisticsContext;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateInboundReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.server.tms.ErpServerTmsApplication;
import com.erp.server.tms.handler.TransferLogisticsRegistry;
import com.erp.server.tms.service.TransferLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class BaoHongTransferHandlerImplTest {

    @Resource
    private TransferLogisticsRegistry transferLogisticsRegistry;

    @Resource
    private BaoHongTransferHandlerImpl baoHongTransferHandler;

    private TransferLogisticsService thirdWarehouseService;

    public BaoHongTransferHandlerImplTest(){
        Map<String,String> authMap = new HashMap<>();
        //a39ab99c1437c991ec07fad4e1f78f8f
        authMap.put("appToken","BAAC60E49804C53A");
        //f7e4102f9b0b983e58bed3140dc22f1a
        authMap.put("appKey","98f8fd9bb9edfa770bc0a317b8203fc3");
        authMap.put("customerCode","E0207");
        TransferLogisticsContext.setAuthMap(authMap);
    }

    @PostConstruct
    public void init(){
        thirdWarehouseService = transferLogisticsRegistry.getHandler(LogisticsPlatformEnum.BAO_HONG.getCode());
    }

    @Test
    public void getShippingMethodList() {
    }

    @Test
    public void getAllProductInfo() {
        baoHongTransferHandler.getAllProductInfo();
    }

    @Test
    public void createOrder() {
        TransferLogisticsCreateOrderReq createOrderReq = TransferLogisticsCreateOrderReq.builder()
                .trackingNumber("wjtest12345")
                .country("CN")
                .shippingCode("TY-DHL")
                .name("唯迹")
                .referenceNo("wjtest12345")
                .deliveryAddress("深圳龙岗")
                .streetAddress("深圳龙岗")
                .state("广东")
                .city("深圳")
                .postcode("001")
                .phone("123456")
                .orderStatus("2")
                .iossNo("ioss1")
                .serialNo("wjNo")
                .grossWeight(new BigDecimal("10.11"))
                .buyInsurance(0)
                .productDetailList(Arrays.asList(TransferLogisticsCreateOrderReq.ProductDetail.builder()
                                .skuNo("SHANZHONGLAOREN")
                                .qty(1)
                                .productTitleEn("E0207")
                                .purposeDeclaredValue("1")
                        .build(),TransferLogisticsCreateOrderReq.ProductDetail.builder()
                        .skuNo("HUOSANG")
                        .qty(2)
                        .productTitleEn("HUOSANG")
                        .purposeDeclaredValue("1")
                        .build()))
                .build();
        System.out.println(thirdWarehouseService.createOrder(createOrderReq,"1750367777970982913"));
    }

    @Test
    public void getOrderByCode() {
        System.out.println(thirdWarehouseService.getOrderByCode("SOE02070222796","1750367777970982913"));
    }

    @Test
    public void createInbound() {
        TransferLogisticsCreateInboundReq transferLogisticsCreateInboundReq = TransferLogisticsCreateInboundReq.builder()
                .referenceCode("wj20240127")
                .isDelivery(true)
                .packQty(1)
                .grossWeight(new BigDecimal("10.12"))
                .receivingStatus("2")
                .receiveItemList(Arrays.asList(TransferLogisticsCreateInboundReq.ReceiveItem.builder()
                                .orderCode("SOE02070222822")
                                .grossWeight(new BigDecimal("10"))
                        .build()))
                .build();
        System.out.println(thirdWarehouseService.createInbound(transferLogisticsCreateInboundReq,"1750367777970982913"));
    }

    @Test
    public void printLabel() {
        System.out.println(thirdWarehouseService.printLabel("SOE02070222822","1750367777970982913"));
    }
}