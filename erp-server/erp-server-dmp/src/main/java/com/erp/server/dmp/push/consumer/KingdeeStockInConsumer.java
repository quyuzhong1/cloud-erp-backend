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
import com.erp.server.dmp.push.service.business.KingdeeStockInConsumerService;
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
 *
 * @Author Luo_WG
 * @Date 2023/4/23 19:47
 **/
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_purchase_stock_in_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_PURCHASE_STOCK_IN, consumeMode = ConsumeMode.ORDERLY)
public class KingdeeStockInConsumer implements RocketMQListener<Map<String, Object>> {
    @Resource
    private KingdeeStockInConsumerService kingdeeStockInConsumerService;




    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.STK_INSTOCK.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "CGRK2879153"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FStockLocId.FF100014.FNumber,FInStockEntry_FEntryID,FSRCBILLTYPEID,FSRCBillNo,FSRCRowId,FMaterialId.FNumber,FPOOrderNo,FPOORDERENTRYID,FInStockEntry_Link_FSBillId";
        System.out.println(filterStr);
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 3);
        for (Map<String, Object> map : queryList) {
            System.out.println(map);
        }
        queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "CGRK23071200003"));
        filterStr = String.join(" and ", queryFilters);
        fieldKeys = "FParentRowId,FInStockEntry_FEntryID,FSRCBILLTYPEID,FSRCBillNo,FSRCRowId,FMaterialId.FNumber,FPOOrderNo,FPOORDERENTRYID,FSRCRowId,FInStockEntry_Link_FSBillId";
        System.out.println(filterStr);
        queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 3);
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
        try {
            kingdeeStockInConsumerService.executeConsumer(map);
        }catch (Exception e){
            log.error("KingdeeStockInConsumer>>>onMessage>>>map ={}", map, e);
        }

    }

}