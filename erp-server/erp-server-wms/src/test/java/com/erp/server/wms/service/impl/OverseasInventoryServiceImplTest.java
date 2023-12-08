package com.erp.server.wms.service.impl;

import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.OverseasInventoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class})
public class OverseasInventoryServiceImplTest {

    @Resource
    private OverseasInventoryService overseasInventoryService;
    @Test
    public void handleNotMapping() {
        overseasInventoryService.handleNotMapping("goodcang");
    }
}