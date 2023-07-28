package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.utils.FastJsonUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.kingdee.impl.KingdeeCommonServiceImpl;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.SaveParam;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
    private KingdeeCommonService kingdeeCommonService;

    public static void main(String[] args) {
        //模块类型
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

        System.out.println(queryList);

        K3CloudApi client = new K3CloudApi();
/*
        JSONObject json = JSONUtil.parseObj("{\n" +
                "    \"FCompanyType\":\"BD_Customer\",\n" +
                "    \"FCompany\":{\n" +
                "        \"FNumber\":\"CUST0020\"\n" +
                "    },\n" +
                "    \"FBizLocNumber\":\"test\",\n" +
                "    \"FBizLocation\":\"Q9R8+88W, 68 MIDDLE PIRERBAG KAMAL SARANI, DHAKA, BANGLADESH\",\n" +
                "    \"FBizAddress\":\"Q9R8+88W, 68 MIDDLE PIRERBAG KAMAL SARANI, DHAKA, BANGLADESH\",\n" +
                "    \"FEmail\":\"\",\n" +
                "    \"FMobile\":\"\",\n" +
                "    \"FName\":\"RAJIB MISTRY\",\n" +
                "    \"FNumber\":\"\",\n" +
                "    \"FPost\":\"\",\n" +
                "    \"FCONTACTID\":\"1303337\"\n" +
                "}");

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
        }*/

    }

    @Override
    public void onMessage(Map<String, Object> map) {
        //模块类型
        Integer type = ApiModuleTypeEnum.CUSTOMER_CONTACT.getCode();

        //业务id
        String  businessId = String.valueOf(map.get("id"));

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_COMMONCONTACT.getCode());

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), type);

        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId,"","未配置同步字段",type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }


        //地址编号
        String addressCode = String.valueOf(map.get("addressCode"));
        //联系人金蝶id
        String syncKingdeeId = String.valueOf(map.get("syncKingdeeId"));
        //联系人编号
        String code = String.valueOf(map.get("code"));
        //如果所有编码都没有无法同步，需要手动设置好编号
        if (StringUtils.isBlank(addressCode) && StringUtils.isBlank(syncKingdeeId) && StringUtils.isBlank(code)) {
            //错误日志
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId,"","地址编码、联系人金蝶id、联系人编号都是空，同步金蝶失败，请手动维护数据",type, ApiSendStatusEnum.FAILURE.getCode());
        }

        //判断金蝶系统是否已存在该数据
        SaveParam param = new SaveParam(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils,platformEntity.getId(), map);
        } catch (Exception e) {
            //更新数据
            Boolean flag = kingdeeCommonService.saveOrUpdateCustomerContact(platformEntity, map, apiUtils, json, param, type);
            if (flag) {
                //启用、禁用
                excuteOperation(apiUtils,platformEntity,map,type);
            }
            return;
        }
        String id = String.valueOf(model.get("Id"));
        String forbidStatus = String.valueOf(model.get("ForbidStatus")) ;
        StringBuffer allKey = FastJsonUtil.getAllKey(json);
        Boolean flag = Boolean.FALSE;
        //查找到数据后，判断其审核状态
        String documentStatus = (String) model.get("DocumentStatus");

        //审核中或已审核则要先反审
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            flag = kingdeeCommonService.unAudit(platformEntity, map, apiUtils, id, type);
        }
        //给修改json对象赋值ID
        KingdeeUtils.makeFieldJson(json,"FCONTACTID",".", id);
        ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
        param.setNeedUpDateFields(apiFieldList);
        //更新数据
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            kingdeeCommonService.saveOrUpdateCustomerContact(platformEntity,map,apiUtils,json,param,type);
            if ((forbidStatus.equals("B") && Boolean.valueOf(map.get("disabled").toString()) == Boolean.FALSE) || (forbidStatus.equals("A") && Boolean.valueOf(map.get("disabled").toString()))) {
                //启用、禁用
                excuteOperation(apiUtils,platformEntity,map,type);
            }
        }
    }

    /**
     * 启用、禁用
     */
    private void excuteOperation(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {
        //仓库状态 true禁用,false启用
        Object disabled = map.get("disabled");
        if (ObjectUtils.isEmpty(disabled)) {
            return;
        }

        String code = (String) map.get("code");
        String operate = null;
        //启用
        if (!(Boolean) disabled) {
            operate = SyncKingdeeOperateEnum.OPERATE_ENABLE.getCode();
        }
        //禁用
        if ((Boolean) disabled) {
            operate = SyncKingdeeOperateEnum.OPERATE_DISABLE.getCode();
        }
        if (StringUtils.isNotBlank(operate)) {
            kingdeeCommonService.excuteOperation(apiUtils,platformEntity,map,type,code,operate);
        }
    }
}
