package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeCustomerContactConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.kingdee.impl.KingdeeCommonServiceImpl;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.kingdee.bos.webapi.entity.SaveParam;
import com.kingdee.bos.webapi.entity.SaveResult;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 对接金蝶仓库
 * @author Lambda
 * @Classname KingdeeWarehouseConsumer

 * @Date 2023-04-25 14:29
 * @Created by yl
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_customer_contact_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_CUSTOMER_CONTACT, consumeMode = ConsumeMode.ORDERLY)
public class KingdeeCustomerContactConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeCustomerContactConsumerService kingdeeCustomerContactConsumerService;

    public static void main(String[] args) {
/*        //模块类型
        Integer type = ApiModuleTypeEnum.CUSTOMER_CONTACT.getCode();
        KingdeeCommonService kingdeeCommonService = new KingdeeCommonServiceImpl();
        Map<String, Object> map = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_COMMONCONTACT.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FNumber = '%s'", "CXR006655"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FForbidStatus,FNumber,FCONTACTID";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 2);

        System.out.println(queryList);*/

        K3CloudApi client = new K3CloudApi();
        JSONObject json = JSONUtil.parseObj("{ \"FCompanyType\" : \"BD_Customer\", \"FCompany\" :{ \"FNumber\" : \"CUST0320\" }, \"FCustId\" :{ \"FNUMBER\" : \"CUST0320\" },\n" +
                "\"FBizLocNumber\" : \"BIZ20230720120215\",\n" +
                "\"FBizLocation\" : \"Fotonordic Oy,  Kempeleentie 5, 90400 Oulu,Filand\",\n" +
                "\"FBizAddress\" : \"Fotonordic Oy,  Kempeleentie 5, 90400 Oulu,Filand\",\n" +
                "\"FEmail\" : \"\",\n" +
                "\"FMobile\" : \"358442592400\",\n" +
                "\"FName\" : \"Juuso\",\n" +
                "\"FNumber\" : \"CXR021025\",\n" +
                "\"FPost\" : \"\",\n" +
                "\"FCONTACTID\" : \"1415337\"}");

        //判断金蝶系统是否已存在该数据
        SaveParam param = new SaveParam(json);
        SaveResult result;
        try {
            result = client.save(KingdeePushModuleEnum.BD_COMMONCONTACT.getCode(), param);
            if (!result.isSuccessfully()) {
                throw new RuntimeException("【保存】出错:" + JSONUtil.toJsonStr(result.getResult().getResponseStatus().getErrors()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeeCustomerContactConsumerService.executeConsumer(map);
        }catch (Exception e){
            log.error("KingdeeCustomerContactConsumer>>>onMessage>>>map ={}>>>e={}", map, e);
        }



    }


}
