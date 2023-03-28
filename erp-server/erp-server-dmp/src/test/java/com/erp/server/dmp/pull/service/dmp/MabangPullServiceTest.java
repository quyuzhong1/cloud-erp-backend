package com.erp.server.dmp.pull.service.dmp;

import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.kingdee.KingdeeEccShopEntity;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.erp.server.dmp.pull.service.gyy.GyyDeliveryDetailServiceImpl;
import com.erp.server.dmp.pull.service.gyy.GyyOrderInfoServiceImpl;
import com.erp.server.dmp.pull.service.kingdee.KingdeeEccShopServiceImpl;
import com.erp.server.dmp.pull.service.mabang.MabangHistoryOrderInfoServiceImpl;
import com.erp.server.dmp.pull.service.mabang.MabangOrderInfoServiceImpl;
import com.erp.server.dmp.pull.service.mabang.MabangRefundServiceImpl;
import com.erp.server.dmp.pull.service.mabang.MabangReturnOrderInfoServiceImpl;
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
public class MabangPullServiceTest {


    @Test
    public void pullOrderTest(){
        MabangOrderInfoServiceImpl orderService = new MabangOrderInfoServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        PlatformApiEnum apiEnum = PlatformApiEnum.ORDER_GET_ORDER_LIST;
        jobTaskDTO.setApiCode(apiEnum.getTaskName());
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2023-01-05 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2023-01-06 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(apiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            orderService.pullDataSave(requestDTO);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void pullHistoryOrderTest(){
        MabangHistoryOrderInfoServiceImpl orderService = new MabangHistoryOrderInfoServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        PlatformApiEnum apiEnum = PlatformApiEnum.GET_HISTORY_ORDER_LIST;
        jobTaskDTO.setApiCode(apiEnum.getTaskName());
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2022-09-05 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2022-09-06 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(apiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            orderService.pullDataSave(requestDTO);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void pullRefundTest(){
        MabangRefundServiceImpl orderService = new MabangRefundServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        PlatformApiEnum apiEnum = PlatformApiEnum.ORDER_GET_REFUND_LIST;
        jobTaskDTO.setApiCode(apiEnum.getTaskName());
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2023-03-22 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2023-03-22 01:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(apiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            orderService.pullDataSave(requestDTO);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void pullReturnTest(){
        MabangReturnOrderInfoServiceImpl orderService = new MabangReturnOrderInfoServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        PlatformApiEnum apiEnum = PlatformApiEnum.ORDER_GET_RETURN_ORDER_LIST;
        jobTaskDTO.setApiCode(apiEnum.getTaskName());
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2023-03-21 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2023-03-22 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(apiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            orderService.pullDataSave(requestDTO);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


}
