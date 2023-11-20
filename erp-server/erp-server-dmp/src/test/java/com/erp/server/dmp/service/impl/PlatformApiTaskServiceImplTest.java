package com.erp.server.dmp.service.impl;

import cn.hutool.json.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.erp.model.dmp.dto.ThirdWarehouseTaskDTO;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class PlatformApiTaskServiceImplTest {

    @Resource
    private PlatformApiTaskService platformApiTaskService;

    @Test
    public void createThirdWarehouseTask() throws JsonProcessingException {
        JSONObject json = new JSONObject();
        json.set("AppToken","7013991264f611e98ea200e01b680258");
        json.set("AppKey","6ff50abf64f611e98ea200e01b680258");
        platformApiTaskService.createThirdWarehouseTask(new ThirdWarehouseTaskDTO.AddDTO("1",json, OmsPlatformEnum.OMS_GOOD_CANG.getCode()));

    }
}