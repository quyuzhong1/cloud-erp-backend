package com.erp.server.dmp.push.consumer;

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
import com.erp.server.dmp.push.service.business.KingdeeSoOutstockConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.kingdee.impl.KingdeeCommonServiceImpl;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 对接金蝶销售出库
 *
 * @Author Luo_WG
 * @Date 2023/6/1 14:45
 **/
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC,
        selectorExpression = "kingdee_so_outstock_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SO_OUTSTOCK,
        consumeMode = ConsumeMode.CONCURRENTLY,
        consumeThreadNumber = 5)
public class KingdeeSoOutstockConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private KingdeeSoOutstockConsumerService kingdeeSoOutstockConsumerService;

    @Resource
    private DmpPushTaskService dmpPushTaskService;


    public static void main(String[] args) {
        //模块类型
        Integer type = ApiModuleTypeEnum.SO_OUTSTOCK.getCode();
        KingdeeCommonService kingdeeCommonService = new KingdeeCommonServiceImpl();
        Map<String, Object> map = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SAL_OUTSTOCK.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "XSCKD7212897"));
        String filterStr = String.join(" and ", queryFilters);//5814757
        String fieldKeys = "FID,FBillTypeID,FBillTypeID.FName,FBillNo,FSoOrDerNo,FDate,FSaleOrgId,FSaleOrgId.FName,FCarriageNO,FStockerID.FNumber,FStockerID.FName," +
                "FCustomerID,FCustomerID.FName,FCustomerID.FNumber,FSaleDeptID.FName,FSalesManID,FSalesManID.FName,FSalesManID.FNumber,FReceiverID.FName," +
                "FTransferBizType.FName,F_ulz_BaseProperty2,F_ulz_BaseProperty2.FNumber,FLinkPhone,FLinkMan,FBussinessType,FDocumentStatus," +
                "FNote,FReceiveAddress,FCreatorId.FName,FCreateDate,FModifierId.FName,FModifyDate,FApproverID.FName," +
                "FApproveDate,FCancelStatus,FGYDATE,FLogisticsNos,F_ulz_Text3,FSettleCurrID.FCode,FExchangeRate,FISGENFORIOS," +
                "FEntity_FENTRYID,FBillAllAmount,FBillAllAmount_LC,FAllAmount,FAllAmount_LC,FAmount_LC,FTaxAmount,FTaxAmount_LC,FBillTaxAmount,FEntryTaxAmount," +
                "FSrcBillNo,FCustMatName,F_ulz_BaseProperty1,FMaterialID,FMaterialID.FNumber,FMaterialID.FName,FStockLocID," +
                "FBarcode,FMateriaModel,FMateriaType,FRealQty,FUnitID.FName,FPrice,FIsFree,FArrivalStatus,FArrivalDate," +
                "FAmount,FStockStatusID,FStockStatusID.FName,FStockID.FName,FStockID.FNumber,F_ulz_Text1,FEntryCostAmount,FEntrynote,FSrcType,FTaxPrice," +
                "FCostPrice,FCostAmount_LC,FSalCostPrice,F_ULZ_data_sources,FETHIRDBILLNO";
        map.put("FCustMatID.FNumber", "XSCKD01_SYS，XSCKD07_SYS");
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 11);
        System.out.println(queryList);

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
        kingdeeSoOutstockConsumerService.executeConsumer(map);
        return ApiResult.success();
    }

}
