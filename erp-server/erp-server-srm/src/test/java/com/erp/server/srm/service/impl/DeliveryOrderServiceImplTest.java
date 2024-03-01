package com.erp.server.srm.service.impl;

import com.erp.server.srm.ErpServerSrmApplication;
import com.erp.server.srm.service.DeliveryOrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerSrmApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class DeliveryOrderServiceImplTest {

    @Resource
    private DeliveryOrderService deliveryOrderService;

    @Test
    public void confirmReceiveStatus() {
        deliveryOrderService.confirmReceiveStatus(Arrays.asList("1750363303529418754","1750722479596138498"));
    }
}