package com.erp.server.tms.service.impl;

import cn.hutool.json.JSONUtil;
import com.erp.model.tms.dto.CfgLogisticsAuthFieldDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.server.tms.ErpServerTmsApplication;
import com.erp.server.tms.service.LogisticsChannelService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class CfgLogisticsAuthFieldServiceImplTest {

    @Resource
    private CfgLogisticsAuthFieldServiceImpl cfgLogisticsAuthFieldService;
    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Test
    public void add() {
        CfgLogisticsAuthFieldDTO.AddDTO addDTO = new CfgLogisticsAuthFieldDTO.AddDTO();
        addDTO.setLogisticsPlatform("iml");
        addDTO.setLogisticsPlatformName("艾姆勒");
        addDTO.setFieldCode("appToken");
        addDTO.setFieldName("AppToken");
        cfgLogisticsAuthFieldService.add(addDTO);
    }

    @Test
    public void test(){
        LogisticsChannelDTO.SignShipDTO dto = logisticsChannelService.getScaleChannelByChannelById("1736654735097729028", "Amazon");
        System.out.println(JSONUtil.toJsonStr(dto));
    }
}