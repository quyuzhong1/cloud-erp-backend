package com.erp.server.dmp.pull.service.dmp;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.constant.UrlContant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.KingdeeParamDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.core.security.HmacSHA256Utils;
import com.common.core.utils.HttpCommonUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.erp.server.dmp.pull.service.mabang.*;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.utils.MabangApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 *
 * @Author Cloud
 * @Date 2023/2/6 12:04
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
@Slf4j
public class MabangPullServiceTest {
    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Autowired
    private MabangDeliveryServiceImpl deliveryService;

    @Test
    public void testtt() {
        //379801
        //模块类型
        Integer type = ApiModuleTypeEnum.CUSTOMER_INFO.getCode();
        Map<String, Object> map = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_CUSTOMER.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "CGTJ23050500001"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FId,FPUR_PATENTRY_FEntryID,FMaterialId.FNumber,FSrcEntryID,FIsPriceListPush";
        map.put("groupName", "测试分组");
        map.put("id", "379804");
        map.put("Ids", "");

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), type);
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
//        kingdeeCommonService.queryGroupInfo(apiUtils, map.get("Ids").toString());
        kingdeeCommonService.customerGroupDelete(apiUtils, (String)map.get("syncKingdeeId"),(String)map.get("groupName"));


//        Boolean aBoolean = kingdeeCommonService.customerGroupSaveOrUpdate(platformEntity, map, apiUtils, json, param, type);

       /* LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        viewMap.put("Number","CGDD-230413-8806");
        JSONObject viewJson = apiUtils.getViewJson(JSONUtil.toJsonStr(viewMap));
        System.out.println(viewJson);
*/
    }


    @Test
    public void pullOrderTest(){
        MabangOrderInfoServiceImpl orderService = new MabangOrderInfoServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        PlatformApiEnum apiEnum = PlatformApiEnum.ORDER_GET_ORDER_LIST;
        jobTaskDTO.setApiCode(apiEnum.getTaskName());
        jobTaskDTO.setPlatformApiId("5");
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId("30");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2023-10-09 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2023-10-09 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setDictPlatform("1");
        jobTaskDTO.setStatus(1);
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
        jobTaskDTO.setPlatformApiId("5");
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId("30");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2023-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2023-01-01 00:00:01", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setDictPlatform("1");
        jobTaskDTO.setStatus(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(apiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
//            orderService.pullDataSave(requestDTO);
            LocalDateTime nextTime = requestDTO.getJobTaskDTO().getNextTime();
            List<OrderEntity> entityList = MabangApiUtils.queryHistorySalesList(requestDTO.getPlatformApiEnum().getTaskName(), nextTime);
            System.out.println(JSONUtil.toJsonStr(entityList));
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
        jobTaskDTO.setPlatformApiId("5");
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId("30");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2023-03-22 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2023-03-22 01:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setDictPlatform("1");
        jobTaskDTO.setStatus(1);
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
        jobTaskDTO.setPlatformApiId("5");
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId("30");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2023-03-21 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2023-03-22 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setDictPlatform("1");
        jobTaskDTO.setStatus(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(apiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            orderService.pullDataSave(requestDTO);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询仓库列表
     * sys-get-warehouse-list
     */
    public static void main(String[] args) {
        HashMap<String, Object> params = new HashMap<>(6);
        Map<String, Object> paramMap = new HashMap(16);
        paramMap.put("api", "sys-get-warehouse-list");
        paramMap.put("appkey", "200780");
        paramMap.put("version", 1);
        params.put("type", 9);
        paramMap.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        paramMap.put("data",params);
        String paramStr = JSONUtil.toJsonStr(paramMap);
        String sign = HmacSHA256Utils.hmacSHA256(paramStr, "13c324fa18feaaeb0ebcc8a7746ebfca");
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", sign);
        com.alibaba.fastjson.JSONObject responseMap = HttpCommonUtil.sendOkhttp(UrlContant.MABANG_HOST, paramStr, null, headerMap, RequestMethod.POST);
        if (Objects.equals(responseMap.getInteger("code"), 200)) {
//            log.error("MB获取仓库列表失败，失败原因：{}", responseMap.getString("msg"));
            JSONArray jsonArray = responseMap.getJSONObject("data").getJSONArray("data");
            Long id = 1669528950398783506l;
            for (int i = 0; i < jsonArray.size(); i++) {
                com.alibaba.fastjson.JSONObject jsonObject = jsonArray.getJSONObject(i);
                String type = jsonObject.getString("type");
                String isDefault = jsonObject.getString("isDefault");
                isDefault = "1".equals(isDefault) ? "true" : "false";
                if(StrUtil.isBlank(type)){
                    type = "";
                }else if("1".equals(type)){
                    type = "PRIVATE";
                }else if("2".equals(type)){
                    type = "THIRD_PARTY";
                }else if("3".equals(type)){
                    type = "FBA";
                }
                log.info("INSERT INTO \"public\".\"dmp_warehouse_mapping\" (\"id\" , \"warehouse_code\", \"warehouse_name\", \"type\", \"default\", \"source_id\", \"platform_sign\",\"status\") VALUES(\'{}\',\'{}\',\'{}\',\'{}\',\'{}\',\'{}\',\'{}\',\'{}\');",(id+i)+"", jsonObject.getString("finance_code"), jsonObject.getString("name"),
                        type, isDefault,jsonObject.getString("id"),"马帮",jsonObject.getString("status"));
            }
        } else {
            log.info("MB获取仓库列表成功，返回结果：{}", responseMap.getString("data"));
        }

    }

    @Test
    public void pullFbaDeliveryTest(){
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        PlatformApiEnum apiEnum = PlatformApiEnum.MABANG_DELIVERY;
        jobTaskDTO.setApiCode(apiEnum.getTaskName());
        jobTaskDTO.setPlatformApiId("5");
        jobTaskDTO.setApiName("获取FBA发货单列表");
        jobTaskDTO.setId("30");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2023-06-29 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2023-06-29 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setDictPlatform("4");
        jobTaskDTO.setStatus(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(apiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            deliveryService.pullDataSave(requestDTO);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


}
