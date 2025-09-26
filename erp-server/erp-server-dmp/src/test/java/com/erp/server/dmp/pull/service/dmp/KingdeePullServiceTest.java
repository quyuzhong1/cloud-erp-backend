package com.erp.server.dmp.pull.service.dmp;

import cn.hutool.json.JSONObject;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.model.dmp.kingdee.KingdeeEccShopEntity;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.erp.server.dmp.pull.service.kingdee.KingdeeDeliveryDetailServiceImpl;
import com.erp.server.dmp.pull.service.kingdee.KingdeeEccShopServiceImpl;
import com.erp.server.dmp.pull.service.kingdee.KingdeeOrderInfoServiceImpl;
import com.erp.server.dmp.pull.service.kingdee.KingdeeReturnOrderInfoImpl;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Cloud
 * @Date 2023/2/6 12:04
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class KingdeePullServiceTest {


    @Test
    public void pullDeliveryTest() {
        KingdeeOrderInfoServiceImpl gyyOrderInfoService = new KingdeeOrderInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.SAL_SALEORDER;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setPlatformApiId("7");
        jobTaskDTO.setApiName("管易云查询订单列表");
        jobTaskDTO.setId("32");
        jobTaskDTO.setLastTime(LocalDateTime.parse("2022-12-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2022-12-31 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setDictPlatform("1");
        jobTaskDTO.setStatus(1);
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
    public void pullKingdeeEccShopTest() {
        KingdeeEccShopServiceImpl shopService = new KingdeeEccShopServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(PlatformApiEnum.ECC_SHOP.getTaskName());
        jobTaskDTO.setPlatformApiId("5");
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId("30");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2021-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2023-05-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setDictPlatform("1");
        jobTaskDTO.setStatus(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(PlatformApiEnum.ECC_SHOP);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            List<KingdeeEccShopEntity> kingdeeEccShopEntities = shopService.pullDate(requestDTO);
            System.out.println("kingdeeEccShopEntities = " + kingdeeEccShopEntities);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void pullKingdeeDeliveryDetailTest() {
        KingdeeDeliveryDetailServiceImpl deliveryDetailService = new KingdeeDeliveryDetailServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(PlatformApiEnum.SAL_OUTSTOCK.getTaskName());
        jobTaskDTO.setPlatformApiId("5");
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId("30");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2024-02-27 17:40:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2024-02-27 17:50:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setDictPlatform("KINGDEE_PULL_DATA_TASK");
        jobTaskDTO.setStatus(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(PlatformApiEnum.SAL_OUTSTOCK);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            List<KingdeeDeliveryDetailEntity> entityList = deliveryDetailService.pullDate(requestDTO);
            System.out.println("entityList = " + entityList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void pullKingdeeReturnOrderTest() {
        KingdeeReturnOrderInfoImpl returnOrderService = new KingdeeReturnOrderInfoImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(PlatformApiEnum.SAL_RETURNSTOCK.getTaskName());
        jobTaskDTO.setPlatformApiId("5");
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId("30");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2024-04-01 17:40:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2024-04-02 17:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setDictPlatform("KINGDEE_PULL_DATA_TASK");
        jobTaskDTO.setStatus(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(PlatformApiEnum.SAL_RETURNSTOCK);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            List<KingdeeReturnOrderEntity> entityList = returnOrderService.pullDate(requestDTO);
            System.out.println("entityList = " + entityList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Test
    public void Test() {
        //模块类型
        Integer type = ApiModuleTypeEnum.SO_INFO.getCode();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SAL_SALEORDER.getCode());
        Map<String, Object> map = new HashMap<>();
        map.put("code", "XSD23082300002");
        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());

        JSONObject model =kingdeeCommonService.view(apiUtils,platformEntity.getId(),map);
        System.out.println(model);
    }


    /**
     * 近7天数据拉取
     */
    @Test
    public void pullOrderTest() {
        KingdeeOrderInfoServiceImpl kingdeeOrderInfoService = new KingdeeOrderInfoServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(PlatformApiEnum.SAL_SALEORDER.getTaskName());
        jobTaskDTO.setPlatformApiId("5");
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId("30");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.now().minusDays(5));
        jobTaskDTO.setNextTime(LocalDateTime.now());
        jobTaskDTO.setDictPlatform("1");
        jobTaskDTO.setStatus(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(PlatformApiEnum.SAL_SALEORDER);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            kingdeeOrderInfoService.pullDataSave(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}