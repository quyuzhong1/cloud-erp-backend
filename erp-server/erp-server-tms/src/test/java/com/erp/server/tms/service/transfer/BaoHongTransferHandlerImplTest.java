package com.erp.server.tms.service.transfer;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.threadlocal.TransferLogisticsContext;
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
        TransferLogisticsService thirdWarehouseService = transferLogisticsRegistry.getHandler(LogisticsPlatformEnum.BAO_HONG.getCode());
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
    }

    @Test
    public void getOrderByCode() {
    }

    @Test
    public void createInbound() {
    }

    @Test
    public void printLabel() {
    }
}