package com.erp.server.oms.service.impl;

import com.erp.server.oms.ErpServerOmsApplication;
import com.erp.server.oms.service.SoB2cLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.math.BigDecimal;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerOmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
class SoB2cLogisticsServiceImplTest {

    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;
    @Test
    void updateWeight() {
        soB2cLogisticsService.updateWeight("1780914177058017281","1780914177171263490",new BigDecimal("123456789123.1234472542446464345457"), "");
    }
}