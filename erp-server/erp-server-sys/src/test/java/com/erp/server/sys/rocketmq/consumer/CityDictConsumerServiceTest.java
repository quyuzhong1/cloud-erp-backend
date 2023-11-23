package com.erp.server.sys.rocketmq.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.common.business.dto.PlatformCityDictDTO;
import com.common.business.enums.OmsPlatformEnum;
import com.erp.server.sys.ErpServerSysApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerSysApplication.class})
public class CityDictConsumerServiceTest {

    @Resource
    private CityDictConsumerService cityDictConsumerService;

    @Test
    public void handle() {
        PlatformCityDictDTO platformCityDictDTO = new PlatformCityDictDTO();
        platformCityDictDTO.setProvider(OmsPlatformEnum.OMS_IML.getCode());
        platformCityDictDTO.setRegionId("1");
        platformCityDictDTO.setRegionName("从化市");
        platformCityDictDTO.setParentRegionId("76");
        platformCityDictDTO.setRegionLevel("3");
        com.alibaba.fastjson2.JSONObject jsonObject = JSONObject.from(platformCityDictDTO);
        cityDictConsumerService.handle(jsonObject);
    }
}