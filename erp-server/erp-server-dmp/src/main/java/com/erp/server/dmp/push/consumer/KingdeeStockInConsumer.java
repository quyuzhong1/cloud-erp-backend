package com.erp.server.dmp.push.consumer;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeStockInConsumerService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.kingdee.bos.webapi.entity.SaveParam;
import com.kingdee.bos.webapi.entity.SaveResult;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 对接金蝶入库单
 *
 * @Author Luo_WG
 * @Date 2023/4/23 19:47
 **/
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC,
        selectorExpression = "kingdee_purchase_stock_in_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_PURCHASE_STOCK_IN,
        consumeMode = ConsumeMode.ORDERLY)
public class KingdeeStockInConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {
    @Resource
    private KingdeeStockInConsumerService kingdeeStockInConsumerService;

    @Resource
    private DmpPushTaskService dmpPushTaskService;


    public static void main(String[] args) {
/*
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
        }*/

        //模块类型
        Integer type = ApiModuleTypeEnum.PURCHASE_STOCK_IN.getCode();
        K3CloudApi client = new K3CloudApi();

        JSONObject json = JSONUtil.parseObj("{ \"FBillTypeID\" :{ \"FNumber\" : \"RKD01_SYS\" },\n" +
                "\"FBillNo\" : \"CGRK23090500013\",\n" +
                "\"FDate\" : \"2023-09-05\",\n" +
                "\"FStockOrgId\" :{ \"FNumber\" : \"113\" },\n" +
                "\"FPurchaseOrgId\" :{ \"FNumber\" : \"113\" },\n" +
                "\"FPurchaseDeptId\" :{ \"FNumber\" : \"BM00077\" },\n" +
                "\"FPurchaserId\" :{ \"FName\" : \"\",\n" +
                "\"FNumber\" : \"\" },\n" +
                "\"FProviderContactID\" :{ \"FName\" : \"\" },\n" +
                "\"FSupplierId\" :{ \"FName\" : \"COCO测试供应商\",\n" +
                "\"FNumber\" : \"GYS23072400001\" },\n" +
                "\"FSupplyAddress\" : \"雅宝\",\n" +
                "\"F_ulz_Combo\" : 2,\n" +
                "\"FInStockEntry\" :[{ \"FInStockEntry_FEntryID\" : \"\",\n" +
                "\"FMaterialId\" :{ \"FNumber\" : \"P012CNB1DZ\" },\n" +
                "\"FStockId\" :{ \"FName\" : \"COCO测试仓--简拍\",\n" +
                "\"FNumber\" : \"autotest1\" },\n" +
                "\"F_ULZ_TEXT1\" : \"\",\n" +
                "\"FStockLocId\" :{ \"FSTOCKLOCID__FF100014\" :{ \"FNumber\" : \"\" }},\n" +
                "\"FNote\" : \"\",\n" +
                "\"FRealQty\" : 1,\n" +
                "\"FTAXPRICE\" : 200,\n" +
                "\"FPriceBaseQty\" : 1,\n" +
                "\"FUnitID\" :{ \"FNumber\" : \"Pcs\" },\n" +
                "\"FPriceUnitID\" :{ \"FNumber\" : \"ge\" },\n" +
                "\"FRemainInStockUnitId\" :{ \"FNumber\" : \"Pcs\" },\n" +
                "\"FPOOrderNo\" : \"PO23090500007\",\n" +
                "\"FMustQty\" : 100,\n" +
                "\"FPOORDERENTRYID\" : \"179859\",\n" +
                "\"FSRCBILLTYPEID\" : \"PUR_PurchaseOrder\",\n" +
                "\"FSRCBillNo\" : \"PO23090500007\",\n" +
                "\"FInStockEntry_Link\" :[{ \"FInStockEntry_Link_FRuleId\" : \"PUR_PurchaseOrder-STK_InStock\",\n" +
                "\"FInStockEntry_Link_FSTableName\" : \"t_PUR_POOrderEntry\",\n" +
                "\"FInStockEntry_Link_FSBillId\" : \"129501\",\n" +
                "\"FInStockEntry_Link_FSId\" : \"179859\" }]}],\n" +
                "\"FId\" : \"3347719\"}");

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
        }
    }


    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpPushTaskService.updateStatus(paramDTO);
    }

    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {

    }
    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        dmpPushTaskService.sendWarnMsg(syncTaskId);
    }

    @Override
    public ApiResult<?> handle(Object ext) {
        Map<String, Object> map = JSONUtil.parseObj(ext);
        kingdeeStockInConsumerService.executeConsumer(map);
        return ApiResult.success();
    }

}