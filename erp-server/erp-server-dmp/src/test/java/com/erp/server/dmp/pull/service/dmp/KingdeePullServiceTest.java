package com.erp.server.dmp.pull.service.dmp;

import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.model.dmp.kingdee.KingdeeEccShopEntity;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.erp.server.dmp.pull.service.kingdee.KingdeeDeliveryDetailServiceImpl;
import com.erp.server.dmp.pull.service.kingdee.KingdeeEccShopServiceImpl;
import com.erp.server.dmp.pull.service.kingdee.KingdeeOrderInfoServiceImpl;
import com.erp.server.dmp.pull.service.kingdee.KingdeeReturnOrderInfoImpl;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.kingdee.impl.KingdeeCommonServiceImpl;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
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

    @Test
    public void pullKingdeeDeliveryDetailTest(){
        KingdeeDeliveryDetailServiceImpl deliveryDetailService = new KingdeeDeliveryDetailServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(PlatformApiEnum.SAL_OUTSTOCK.getTaskName());
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2023-07-13 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2023-07-14 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(PlatformApiEnum.SAL_OUTSTOCK);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            List<KingdeeDeliveryDetailEntity> entityList = deliveryDetailService.pullDate(requestDTO);
            System.out.println("entityList = " + entityList);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void pullKingdeeReturnOrderTest(){
        KingdeeReturnOrderInfoImpl returnOrderService = new KingdeeReturnOrderInfoImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(PlatformApiEnum.SAL_RETURNSTOCK.getTaskName());
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2023-03-27 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2023-03-28 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(PlatformApiEnum.SAL_RETURNSTOCK);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            List<KingdeeReturnOrderEntity> entityList = returnOrderService.pullDate(requestDTO);
            System.out.println("entityList = " + entityList);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void Test(){
        //模块类型
        Integer type = ApiModuleTypeEnum.SO_OUTSTOCK.getCode();
        KingdeeCommonService kingdeeCommonService = new KingdeeCommonServiceImpl();
        Map<String, Object> map = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SAL_OUTSTOCK.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "XSCKD4064982"));
        String filterStr = String.join(" and ", queryFilters);//5814757
        String fieldKeys = "FModifyDate,FDocumentStatus,FApproveDate";
        map.put("FCustMatID.FNumber", "XSCKD01_SYS，XSCKD07_SYS");
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1,11);
        System.out.println(queryList);
    }

}