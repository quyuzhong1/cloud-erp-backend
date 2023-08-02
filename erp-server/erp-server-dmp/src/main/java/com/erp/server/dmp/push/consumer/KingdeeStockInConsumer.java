package com.erp.server.dmp.push.consumer;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
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
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.SaveParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对接金蝶入库单
 * @Author Luo_WG
 * @Date 2023/4/23 19:47
 **/
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_purchase_stock_in_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_PURCHASE_STOCK_IN,consumeMode = ConsumeMode.ORDERLY)
public class KingdeeStockInConsumer implements RocketMQListener<Map<String, Object>> {
    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;


    private String formId;
    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.STK_INSTOCK.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "CGRK2879153"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FStockLocId.FF100014.FNumber,FInStockEntry_FEntryID,FSRCBILLTYPEID,FSRCBillNo,FSRCRowId,FMaterialId.FNumber,FPOOrderNo,FPOORDERENTRYID,FInStockEntry_Link_FSBillId";
        System.out.println(filterStr);
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1,3);
        for (Map<String, Object> map : queryList) {
            System.out.println(map);
        }
        queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "CGRK23071200003"));
        filterStr = String.join(" and ", queryFilters);
        fieldKeys = "FParentRowId,FInStockEntry_FEntryID,FSRCBILLTYPEID,FSRCBillNo,FSRCRowId,FMaterialId.FNumber,FPOOrderNo,FPOORDERENTRYID,FSRCRowId,FInStockEntry_Link_FSBillId";
        System.out.println(filterStr);
        queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1,3);
        for (Map<String, Object> map : queryList) {
            System.out.println(map);
        }

/*
        //模块类型
        Integer type = ApiModuleTypeEnum.PURCHASE_STOCK_IN.getCode();
        K3CloudApi client = new K3CloudApi();

        JSONObject json = JSONUtil.parseObj("{ \"FBillTypeID\" :{ \"FNumber\" : \"RKD01_SYS\" },\n" +
                "\"FBillNo\" : \"CGRK23071200003\",\n" +
                "\"FDate\" : \"2023-07-12\",\n" +
                "\"FStockOrgId\" :{ \"FNumber\" : \"113\" },\n" +
                "\"FPurchaseOrgId\" :{ \"FNumber\" : \"113\" },\n" +
                "\"FPurchaseDeptId\" :{ \"FNumber\" : \"BM000004\" },\n" +
                "\"FProviderContactID\" :{ \"FName\" : \"\" },\n" +
                "\"FPurchaserId\" :{ \"FName\" : \"祝梦彬\",\n" +
                "\"FNumber\" : \"\" },\n" +
                "\"FSupplyAddress\" : \"测试\",\n" +
                "\"FSupplierId\" :{ \"FNumber\" : \"GYS23070600001\",\n" +
                "\"FName\" : \"供应商1（测试专用勿动）\" },\n" +
                "\"F_ulz_Combo\" : 2,\n" +
                "\"FInStockEntry\" :[{ \"FInStockEntry_FEntryID\" : \"\",\n" +
                "\"FMaterialId\" :{ \"FNumber\" : \"test-sku\" },\n" +
                "\"FRealQty\" : 1,\n" +
                "\"FStockId\" :{ \"FNumber\" : \"test01\",\n" +
                "\"FName\" : \"测试仓（勿动）\" },\n" +
                "\"FStockLocId\" :{ \"FSTOCKLOCID__FF100014\" :{ \"FNumber\" : \"\" }},\n" +
                "\"F_ULZ_TEXT1\" : \"\",\n" +
                "\"FNote\" : \"\",\n" +
                "\"FPriceBaseQty\" : 1,\n" +
                "\"FUnitID\" :{ \"FNumber\" : \"Pcs\" },\n" +
                "\"FPriceUnitID\" :{ \"FNumber\" : \"ge\" },\n" +
                "\"FRemainInStockUnitId\" :{ \"FNumber\" : \"Pcs\" },\n" +
                "\"FPOOrderNo\" : \"PO23071200001\",\n" +
                "\"FRemainInStockQty\" : 1,\n" +
                "\"FSupplierId\" :{ \"FName\" : \"供应商1（测试专用勿动）\",\n" +
                "\"FNumber\" : \"GYS23070600001\" },\n" +
                "\"FSRCBillNo\" : \"PO23071200001\",\n" +
                "\"FSRCBILLTYPEID\" : \"PUR_PurchaseOrder\" },{ \"FInStockEntry_FEntryID\" : \"\",\n" +
                "\"FMaterialId\" :{ \"FNumber\" : \"test-sku\" },\n" +
                "\"FRealQty\" : 1,\n" +
                "\"FStockId\" :{ \"FNumber\" : \"test01\",\n" +
                "\"FName\" : \"测试仓（勿动）\" },\n" +
                "\"FStockLocId\" :{ \"FSTOCKLOCID__FF100014\" :{ \"FNumber\" : \"\" }},\n" +
                "\"F_ULZ_TEXT1\" : \"\",\n" +
                "\"FNote\" : \"\",\n" +
                "\"FPriceBaseQty\" : 1,\n" +
                "\"FUnitID\" :{ \"FNumber\" : \"Pcs\" },\n" +
                "\"FPriceUnitID\" :{ \"FNumber\" : \"ge\" },\n" +
                "\"FRemainInStockUnitId\" :{ \"FNumber\" : \"Pcs\" },\n" +
                "\"FPOOrderNo\" : \"PO23071200001\",\n" +
                "\"FRemainInStockQty\" : 1,\n" +
                "\"FSupplierId\" :{ \"FName\" : \"供应商1（测试专用勿动）\",\n" +
                "\"FNumber\" : \"GYS23070600001\" },\n" +
                "\"FSRCBillNo\" : \"PO23071200001\",\n" +
                "\"FSRCBILLTYPEID\" : \"PUR_PurchaseOrder\" },{ \"FInStockEntry_FEntryID\" : \"\",\n" +
                "\"FMaterialId\" :{ \"FNumber\" : \"test-sku02\" },\n" +
                "\"FRealQty\" : 1,\n" +
                "\"FStockId\" :{ \"FNumber\" : \"test01\",\n" +
                "\"FName\" : \"测试仓（勿动）\" },\n" +
                "\"FStockLocId\" :{ \"FSTOCKLOCID__FF100014\" :{ \"FNumber\" : \"\" }},\n" +
                "\"F_ULZ_TEXT1\" : \"\",\n" +
                "\"FNote\" : \"\",\n" +
                "\"FPriceBaseQty\" : 1,\n" +
                "\"FUnitID\" :{ \"FNumber\" : \"Pcs\" },\n" +
                "\"FPriceUnitID\" :{ \"FNumber\" : \"ge\" },\n" +
                "\"FRemainInStockUnitId\" :{ \"FNumber\" : \"Pcs\" },\n" +
                "\"FPOOrderNo\" : \"PO23071200001\",\n" +
                "\"FRemainInStockQty\" : 1,\n" +
                "\"FSupplierId\" :{ \"FName\" : \"供应商1（测试专用勿动）\",\n" +
                "\"FNumber\" : \"GYS23070600001\" },\n" +
                "\"FSRCBillNo\" : \"PO23071200001\",\n" +
                "\"FSRCBILLTYPEID\" : \"PUR_PurchaseOrder\" }]}");

        //判断金蝶系统是否已存在该数据
        SaveParam param = new SaveParam(json);
        SaveResult result;
        try {
            result = client.save(KingdeePushModuleEnum.STK_INSTOCK.getCode(), param);
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
        Integer type = ApiModuleTypeEnum.PURCHASE_STOCK_IN.getCode();

        //业务编码
        String code = (String) map.get("code");

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, type);
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.STK_INSTOCK.getCode());

        //操作项
        String operate = (String) map.get("operate");



        /**
         * 作废
         */
        if (SyncKingdeeOperateEnum.OPERATE_INVALID.getCode().equals(operate)) {
            operateInvalid(apiUtils,platformEntity,map,type,code,operate);
        }
        /**
         * 反审核
         */
        if (SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operate)) {
            operateDisapprove(apiUtils,platformEntity, map,type);
        }
        /**
         * 审核
         */
        if (SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode().equals(operate)) {
            operateApprove(apiUtils,platformEntity, map,type);
        }
    }

    private void operateInvalid(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type, String code,String operate) {
        //判断金蝶系统是否已存在该数据
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils,platformEntity.getId(),map);
        } catch (Exception e) {
            //更新业务表中的金蝶id
            kingdeeCommonService.updateBusinessSyncKingdeeStatus(type, String.valueOf(map.get("id")), SyncKingdeeStatusEnum.NO_NEED_SYNC.getCode(), "");
            return;
        }
        String documentStatus = (String)model.get("DocumentStatus");
        String id = String.valueOf(model.get("Id")) ;
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //反审核
            Boolean unAudit = kingdeeCommonService.unAudit(platformEntity, map, apiUtils, id, ApiModuleTypeEnum.PURCHASE_ORDER.getCode());
            if (!unAudit) {
                return;
            }
        }
        //作废
        kingdeeCommonService.excuteOperation(apiUtils,platformEntity,map,type,code,operate);
        return;
    }

    private void operateDisapprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {
        String syncKingdeeId = (String) map.get("syncKingdeeId");
        if (StringUtils.isBlank(syncKingdeeId)) {
            return;
        }
        //反审核
        kingdeeCommonService.unAudit(platformEntity, map, apiUtils, syncKingdeeId, type);
        return;
    }

    private void operateApprove(KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {
        //业务id
        String  businessId = String.valueOf(map.get("id"));

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
            model = kingdeeCommonService.view(apiUtils,platformEntity.getId(),map);
        } catch (Exception e) {
            /*Map<String, Object> pushMap = new HashMap<>();
            pushMap.put("ids", map.get("poSyncKingdeeId"));
            pushMap.put("EntryIds", map.get("poKingdeeDetailIds"));
            pushMap.put("RuleId", "PUR_PurchaseOrder-STK_InStock");
            pushMap.put("TargetFormId", KingdeePushModuleEnum.STK_INSTOCK.getCode());
            pushMap.put("CustomParams", json);
            //读取配置，初始化SDK
            KingdeeApiUtils sourceApiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.STK_INSTOCK.getCode());*/
            //更新数据
//            Boolean isAdd = kingdeeCommonService.push(platformEntity, map, sourceApiUtils, apiUtils, JSONUtil.parseObj(pushMap), param, type, json);

            Boolean isAdd = kingdeeCommonService.saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
            if (isAdd) {
                //给明细id赋值
                JSONArray jsonArray = setDetailIdForJSONObject(apiUtils,platformEntity, map, type);
                //更新明细id
                updateKingdeeDetailId(jsonArray);
            }
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
            KingdeeUtils.makeFieldJson(json,"FId",".", id);
            //更新数据不能传入库组织
            json.remove("FStockOrgId.FNumber");
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            //更新数据
            Boolean isAdd = kingdeeCommonService.saveOrUpdate(platformEntity,map,apiUtils,json,param,type);
            if (isAdd) {
                //给明细id赋值
                JSONArray jsonArray = setDetailIdForJSONObject(apiUtils,platformEntity, map, type);
                //更新明细id
                updateKingdeeDetailId(jsonArray);
            }
        }
    }

    /**
     * 给明细id赋值
     * @Author Luo_WG
     * @Date 2023/7/12 10:18
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     * @return cn.hutool.json.JSONArray
     **/
    private JSONArray setDetailIdForJSONObject (KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type) {

        JSONArray list = JSONUtil.parseArray(map.get("list"));
        String id = (String)map.get("syncKingdeeId");

        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FId = '%s'", id));
        String filterStr = String.join(" and ", queryFilters);
        //查询子单据id
        String fieldKeys = "FInStockEntry_FEntryID";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 1000, 1, 0);
        if (CollectionUtils.isEmpty(queryList)) {
            //错误日志
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), filterStr, "未查询到子单据id", type, ApiSendStatusEnum.FAILURE.getCode());
            return list;
        }
        JSONArray removeObj = new JSONArray();
        JSONArray addObj = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            JSONObject jsonObject = JSONUtil.parseObj(obj);
            JSONObject newJson = new JSONObject(new LinkedHashMap<>());
            if (list.size() >= queryList.size()) {
                //金蝶明细id赋值
                newJson.set("kingdeeDetailId",queryList.get(i).get("FInStockEntry_FEntryID"));
            }
            newJson.putAll(jsonObject);
            removeObj.set(obj);
            addObj.set(newJson);
        }
        list.removeAll(removeObj);
        list.addAll(addObj);
        return list;
    }


    /**
     * 更新明细id
     */
    private void updateKingdeeDetailId (JSONArray jsonArray) {
        //更新业务单据状态
        Map<String,Object> params = new HashMap<>(MathUtil.THREE);
        params.put("code",ApiModuleTypeEnum.PURCHASE_STOCK_IN.getCode().toString());
        params.put("details",jsonArray);
        wmsTaskFeign.updateBusinessSyncKingdeeStatus(params);
    }
}