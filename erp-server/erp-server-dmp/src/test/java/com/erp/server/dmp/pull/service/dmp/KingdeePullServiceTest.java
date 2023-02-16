package com.erp.server.dmp.pull.service.dmp;

import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.kingdee.KingdeeEccShopEntity;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.erp.server.dmp.pull.service.gyy.GyyDeliveryDetailServiceImpl;
import com.erp.server.dmp.pull.service.gyy.GyyOrderInfoServiceImpl;
import com.erp.server.dmp.pull.service.kingdee.KingdeeEccShopServiceImpl;
import com.erp.server.dmp.pull.service.kingdee.KingdeeOrderInfoServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

/**
 * TODO
 *
 * @Author Cloud
 * @Date 2023/2/6 12:04
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class KingdeePullServiceTest {


    @Test
    public void pullDeliveryTest(){
        KingdeeOrderInfoServiceImpl gyyOrderInfoService = new KingdeeOrderInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.SAL_SALEORDER;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setApiId(7);
        jobTaskDTO.setApiName("管易云查询订单列表");
        jobTaskDTO.setId(32L);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2022-12-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2022-12-31 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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
    public void pullKingdeeEccShopTest(){
        KingdeeEccShopServiceImpl shopService = new KingdeeEccShopServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(PlatformApiEnum.ECC_SHOP.getTaskName());
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2021-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2023-05-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(PlatformApiEnum.ECC_SHOP);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            List<KingdeeEccShopEntity> kingdeeEccShopEntities = shopService.pullDate(requestDTO);
            System.out.println("kingdeeEccShopEntities = " + kingdeeEccShopEntities);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
