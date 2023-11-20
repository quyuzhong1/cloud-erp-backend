package com.erp.server.wms.service.impl;


import cn.hutool.json.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.ThirdWarehouseService;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class})
public class GoodCangServiceImplTest {

    @Resource
    ThirdWarehouseRegistry overseasWarehouseRegistry;

    @Test
    public void getChannel() {
        ThirdWarehouseService warehouseService = overseasWarehouseRegistry.getHandler(OmsPlatformEnum.OMS_GOOD_CANG.getCode());
        JSONObject authJson = new JSONObject();
        authJson.set("appToken","7013991264f611e98ea200e01b680258");
        authJson.set("appKey","6ff50abf64f611e98ea200e01b680258");
        System.out.println(warehouseService.authorize(authJson));
    }

}