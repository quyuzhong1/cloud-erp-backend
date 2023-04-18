package com.erp.server.dmp.push.consumer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.core.enums.ApiError;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.SaveParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: 金蝶辅助资料同步（产品分类、）
 * @date 2023/3/13 14:45
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_assistant_data_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_ASSISTANT_DATA)
public class KingdeeAssistantDataDetailConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeCommonService kingdeeCommonService;


    public static void main(String[] args) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BOS_ASSISTANTDATA_DETAIL.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FNumber = '%s'", "SouthChina"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FEntryId,FNumber,FDataValue,FId,FId.FNumber,FId.FName,FParentId";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1,1);

        LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        viewMap.put("number","SouthChina");
        JSONObject viewJson = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
        System.out.println(queryList);
        System.out.println(viewJson);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(Map<String, Object> map) {

        //模块类型
        Integer type = (Integer)map.get("moduleType");
        //业务id
        String  businessId = String.valueOf(map.get("id"));

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BOS_ASSISTANTDATA_DETAIL.getCode());

        //map中设置父级id
        setPid( platformEntity, apiUtils, map, businessId, type);

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(),ApiModuleTypeEnum.ASSISTANT_DATA.getCode());

        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId,"","未配置同步字段",type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        //判断金蝶系统是否已存在该数据
        JSONObject model;
        SaveParam param = new SaveParam(json);
        try {
            model = kingdeeCommonService.view(apiUtils,(String)map.get("syncKingdeeId"),(String)map.get("code"));
        } catch (Exception e) {
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
            return;
        }
        //查找到数据后，判断其审核状态
        String documentStatus = (String)model.get("DocumentStatus");
        String id = (String) model.get("Id");
        Boolean flag = Boolean.FALSE;

        //操作项
        String operate = (String) map.get("operate");
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //审核中或已审核则要先反审
             flag = kingdeeCommonService.unAudit(platformEntity, map, apiUtils, id, type);
        }
        //创建状态则直接修改
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            //删除
            if (SyncKingdeeOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
                String code = (String) map.get("code");
                kingdeeCommonService.delete(apiUtils,platformEntity,map,type,code);
                return;
            }
            //主单据id
            KingdeeUtils.makeFieldJson(json,"FEntryId",".", id);
            ArrayList<String> apiFieldList = (ArrayList<String>) json.keySet().stream().collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
        }
    }

    /**
     * 设置父级id
     */
    private void setPid (PlatformEntity platformEntity,KingdeeApiUtils apiUtils,Map<String, Object> map,String businessId,Integer type) {
        //判断是否存在上级
        Boolean isExistParent = (Boolean)map.get("isExistParent");
        if (isExistParent) {
            //根据上级编码查询上级id
            String parentCode = (String)map.get("parentCode");
            LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
            viewMap.put("number",parentCode);
            JSONObject model;
            try {
                model = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
            } catch (Exception e) {
                //更新数据
                kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId,JSONArray.toJSONString(viewMap),"未找到上级辅助资料",type, ApiSendStatusEnum.FAILURE.getCode());
                return;
            }
            String id = (String) model.get("Id");
            map.put("pid",id);
        }

    }
}
