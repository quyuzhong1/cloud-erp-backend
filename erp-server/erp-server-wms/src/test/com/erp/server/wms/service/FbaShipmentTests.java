package com.erp.server.wms.service;

import cn.hutool.json.JSONUtil;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.server.wms.ErpServerWmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class FbaShipmentTests {

    @Resource
    private FbaShipmentService fbaShipmentService;



    @Test
    public void delFind() {
        FbaShipmentEntity entity = fbaShipmentService.getByFbaShipmentIdAndIsDelete("FBA16GQW2VDC", null);
        System.out.println(JSONUtil.toJsonStr(entity));
    }





}
