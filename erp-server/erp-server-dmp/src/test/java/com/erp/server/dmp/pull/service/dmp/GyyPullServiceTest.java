package com.erp.server.dmp.pull.service.dmp;

import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.erp.server.dmp.pull.service.gyy.GyyDeliveryDetailServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

/**
 * TODO
 *
 * @Author Cloud
 * @Date 2023/2/6 12:04
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class GyyPullServiceTest {


    @Test
    void pullDeliveryTest(){
        GyyDeliveryDetailServiceImpl gyyOrderInfoService = new GyyDeliveryDetailServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.GY_ERP_TRADE_DELIVERY_GET;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setApiId(7);
        jobTaskDTO.setApiName("管易云查询订单列表");
        jobTaskDTO.setId(32L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.now().minusHours(1));
        jobTaskDTO.setNextTime(LocalDateTime.now());
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            gyyOrderInfoService.pullDataSave(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void pullKingdeeDeliveryTest(){
        GyyDeliveryDetailServiceImpl gyyOrderInfoService = new GyyDeliveryDetailServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.SAL_OUTSTOCK;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setApiId(7);
        jobTaskDTO.setApiName("管易云查询订单列表");
        jobTaskDTO.setId(32L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.now().minusHours(1));
        jobTaskDTO.setNextTime(LocalDateTime.now());
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            gyyOrderInfoService.pullDataSave(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
