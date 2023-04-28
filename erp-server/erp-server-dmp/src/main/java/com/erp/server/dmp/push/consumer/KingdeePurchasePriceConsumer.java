package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.utils.FastJsonUtil;
import com.common.core.utils.MathUtil;
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
import java.math.BigDecimal;
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
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_purchase_price_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_PURCHASE_PRICE)
public class KingdeePurchasePriceConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.PUR_PRICECATEGORY.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FNumber = '%s'", "CGJM23042700003"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FPriceListEntry_FEntryID,FFROMQTY,FToQty,FDisablerId";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1,0);
        System.out.println(queryList);
/*
        LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        viewMap.put("Number","CGJM23042700003");
        JSONObject viewJson = apiUtils.getViewJson(JSONUtil.toJsonStr(viewMap));
        System.out.println(viewJson);*/
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(Map<String, Object> map) {
        //模块类型
        Integer type = ApiModuleTypeEnum.PURCHASE_PRICE.getCode();
        //业务id
        String  businessId = String.valueOf(map.get("id"));

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.PUR_PRICECATEGORY.getCode());


        //操作项，分录禁用
        String operate = (String) map.get("operate");
        if (SyncKingdeeOperateEnum.OPERATE_SUB_EFFECTIVE.getCode().equals(operate) || SyncKingdeeOperateEnum.OPERATE_SUB_UN_EFFECTIVE.getCode().equals(operate)) {
            excuteOperation(apiUtils,map,operate);
            return;
        }

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
            //禁用启用
            excuteOperation(apiUtils,map,operate);
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
            //禁用启用
            excuteOperation(apiUtils,map,operate);
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
        String fieldKeys = "FPriceListEntry_FEntryID,FMaterialId.FNumber,FFROMQTY,FToQty";
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
            JSONArray obj = (JSONArray)json.get("FPriceListEntry") ;
            JSONArray removeObj = new JSONArray();
            JSONArray addObj = new JSONArray();
            for (Object o : obj) {
                JSONObject jsonObject = JSONUtil.parseObj(JSONUtil.toJsonStr(o));
                JSONObject newJson = new JSONObject(new LinkedHashMap<>());
                Object o1 = queryMap.get("FMaterialId.FNumber");
                JSONObject o2 = (JSONObject)jsonObject.get("FMaterialId");
                Object fNumber = o2.get("FNumber");
                BigDecimal minQty = MathUtil.valueOf(jsonObject.get("FFROMQTY"));
                BigDecimal maxQty = MathUtil.valueOf(jsonObject.get("FToQty"));
                BigDecimal fMinQty = MathUtil.valueOf(queryMap.get("FFROMQTY"));
                BigDecimal fMaxQty = MathUtil.valueOf(queryMap.get("FToQty"));
                if (o1.equals(fNumber) && MathUtil.compareTo(minQty,fMinQty) == MathUtil.ZERO && MathUtil.compareTo(maxQty,fMaxQty) == MathUtil.ZERO ) {
                    newJson.set("FEntryId",queryMap.get("FPriceListEntry_FEntryID"));
                }
                newJson.putAll(jsonObject);
                removeObj.set(o);
                addObj.set(newJson);
            }
            obj.removeAll(removeObj);
            obj.addAll(addObj);
        }

    }

    /**
     * 启用、禁用
     */
    private void excuteOperation (KingdeeApiUtils apiUtils,Map<String, Object> map,String operate) {

        JSONArray list = JSONUtil.parseArray(map.get("list"));
        String id = (String)map.get("syncKingdeeId");


        List<String> disabledList = new ArrayList<>();
        List<String> unDisabledList = new ArrayList<>();
        for (Object obj : list ) {
            JSONObject jsonObject = JSONUtil.parseObj(JSONUtil.toJsonStr(obj));

            //同步数据时禁用,需要考虑既有禁用又有启用的情况
            Boolean disabled = (Boolean)jsonObject.get("disabled");
            if (ObjectUtils.isNotEmpty(disabled)) {
                //禁用
                if (disabled) {
                    operate = SyncKingdeeOperateEnum.OPERATE_DISABLE.getCode();
                } else {
                    operate = SyncKingdeeOperateEnum.OPERATE_ENABLE.getCode();
                }
            }
            if (StringUtils.isBlank(id)) {
                id = (String)jsonObject.get("syncKingdeeId");
            }

            String skuNo = (String)jsonObject.get("skuNo");
            BigDecimal minQty = MathUtil.valueOf(jsonObject.get("minQty")) ;
            BigDecimal maxQty = MathUtil.valueOf(jsonObject.get("maxQty"));

            LinkedList<String> queryFilters = new LinkedList<>();
            queryFilters.add(String.format("FId = '%s'", id));
            String filterStr = String.join(" and ", queryFilters);
            //查询子单据id
            String fieldKeys = "FPriceListEntry_FEntryID,FMaterialId.FNumber,FFROMQTY,FToQty,FDisablerId";
            List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 1000, 1, 0);
            //比较
            for (Map<String, Object> queryMap: queryList) {
                String number = (String)queryMap.get("FMaterialId.FNumber");
                String detailId =  (String)queryMap.get("FPriceListEntry_FEntryID");
                BigDecimal fMinQty = MathUtil.valueOf(queryMap.get("FFROMQTY"));
                BigDecimal fMaxQty = MathUtil.valueOf(queryMap.get("FToQty"));
                String disablerId = (String)queryMap.get("FDisablerId");
                if (StringUtils.equals(skuNo,number) && MathUtil.compareTo(minQty,fMinQty) == MathUtil.ZERO && MathUtil.compareTo(maxQty,fMaxQty) == MathUtil.ZERO ) {
                    //禁用
                    if (SyncKingdeeOperateEnum.OPERATE_SUB_UN_EFFECTIVE.getCode().equals(operate) && StringUtils.equals("0",disablerId)) {
                        disabledList.add(detailId);
                    }
                   //启用
                    if (SyncKingdeeOperateEnum.OPERATE_SUB_EFFECTIVE.getCode().equals(operate) && !StringUtils.equals("0",disablerId)) {
                        unDisabledList.add(detailId);
                    }
                }
            }

        }
        //禁用
        if (CollectionUtils.isNotEmpty(disabledList)) {
            excuteOperation(apiUtils,disabledList,id,SyncKingdeeOperateEnum.OPERATE_SUB_UN_EFFECTIVE.getName());
        }
        //启用
        if (CollectionUtils.isNotEmpty(unDisabledList)) {
            excuteOperation(apiUtils,unDisabledList,id,SyncKingdeeOperateEnum.OPERATE_SUB_EFFECTIVE.getName());
        }

    }

    /**
     * @description: 启用或禁用
     * @author Will
     * @date: 2023/4/28 11:36
     * @param apiUtils
     * @param list
     * @param id
     * @param operate
     */
    private void excuteOperation(KingdeeApiUtils apiUtils, List<String> list,String id,String operate) {
        JSONObject viewMap = new JSONObject(new LinkedHashMap<>());
        JSONObject newObj = new JSONObject();
        JSONArray pkEntryIds = new JSONArray();
        newObj.set("id",id);
        newObj.set("EntryIds",String.join(",",list));
        pkEntryIds.put(newObj);
        viewMap.set("PkEntryIds",pkEntryIds);
        //启用禁用
        apiUtils.excuteOperation(operate,JSONUtil.toJsonStr(viewMap));
    }

}
