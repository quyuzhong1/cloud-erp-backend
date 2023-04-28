package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
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
 * @description: TODO
 * @date 2023/4/20 11:12
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_purchase_price_change_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_PURCHASE_PRICE_CHANGE)
public class KingdeePurchasePriceChangeConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.PUR_PAT.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "CGTJ23042800005"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FId,FPUR_PATENTRY_FEntryID,FMaterialId.FNumber,FSrcEntryID";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1,0);
        System.out.println(queryList);

       /* LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        viewMap.put("Number","CGDD-230413-8806");
        JSONObject viewJson = apiUtils.getViewJson(JSONUtil.toJsonStr(viewMap));
        System.out.println(viewJson);
*/
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(Map<String, Object> map) {
        //模块类型
        Integer type = ApiModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode();
        //业务id
        String  businessId = String.valueOf(map.get("id"));

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.PUR_PAT.getCode());

        //根据录入值和字段配置生成JSONObject
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(),type);

        //未配置发送字段
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, businessId,"","未配置同步字段",type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }

        //判断金蝶系统是否已存在该数据
        SaveParam param = new SaveParam(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils,(String)map.get("syncKingdeeId"),(String)map.get("code"));
        } catch (Exception e) {

            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
            return;
        }

        //查找到数据后，判断其审核状态
        String documentStatus = (String)model.get("DocumentStatus");

        String id = String.valueOf(model.get("Id")) ;
        Boolean flag = Boolean.FALSE;

        //审核中或已审核则要先反审
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            flag = kingdeeCommonService.unAudit(platformEntity, map, apiUtils, id, type);
        }
        //创建状态则直接修改、删除
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            //给修改json对象赋值ID
            setQueryJSONObject(id,apiUtils,platformEntity,map,type,json);
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList)Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
        }
    }

    /**
     * 给修改json对象赋值ID
     */
    private void setQueryJSONObject (String id, KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type,JSONObject json) {
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FId = '%s'", id));
        String filterStr = String.join(" and ", queryFilters);
        //查询子单据id
        String fieldKeys = "FPUR_PATENTRY_FEntryID,FMaterialId.FNumber";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 1000, 1, 0);
        if (CollectionUtils.isEmpty(queryList)) {
            //错误日志
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")),filterStr,"未查询到子单据id",type,ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        //主单据id
        KingdeeUtils.makeFieldJson(json,"FId",".", id);
        //比较
        for (Map<String, Object> queryMap: queryList) {
            JSONArray obj = (JSONArray)json.get("FPUR_PATENTRY") ;
            JSONArray removeObj = new JSONArray();
            JSONArray addObj = new JSONArray();
            for (Object o : obj) {
                JSONObject jsonObject = JSONUtil.parseObj(JSONUtil.toJsonStr(o));
                JSONObject newJson = new JSONObject(new LinkedHashMap<>());
                Object o1 = queryMap.get("FMaterialId.FNumber");
                JSONObject o2 = (JSONObject)jsonObject.get("FMaterialId");
                Object fNumber = o2.get("FNumber");
                if (o1.equals(fNumber)) {
                    newJson.set("FEntryId",queryMap.get("FPUR_PATENTRY_FEntryID"));
                }
                newJson.putAll(jsonObject);
                removeObj.set(o);
                addObj.set(newJson);
            }
            obj.removeAll(removeObj);
            obj.addAll(addObj);
        }

    }

}
