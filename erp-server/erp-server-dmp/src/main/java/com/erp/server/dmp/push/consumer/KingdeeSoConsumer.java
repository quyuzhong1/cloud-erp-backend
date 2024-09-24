package com.erp.server.dmp.push.consumer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeSoConsumerService;
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
 * @author Will
 * @version 1.0
 * @date 2023/4/20 11:12
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC,
        selectorExpression = "kingdee_so_info_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SO_INFO,
        consumeMode = ConsumeMode.ORDERLY)
public class KingdeeSoConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private KingdeeSoConsumerService kingdeeSoConsumerService;

    @Resource
    private DmpPushTaskService dmpPushTaskService;

    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SAL_SALEORDER.getCode(), 1);
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(StrUtil.format("FBillNo in ({})", "'XSD24053100007'"));
        String filterStr = String.join(" and ", queryFilters);

        String fieldKeys = "FID,FBillNo,FDate,FBillTypeId.FName,FBillTypeId.FNumber,FBillTypeId," +
                "FDocumentStatus,FCustId.FName,FCustId.FNumber,FSaleDeptId.FName,FSalerId.FName,FSalerId.FNumber,FReceiveAddress,FLinkMan,FLinkPhone," +
                "FApproverId.FName,FApproveDate,FCloseStatus,FCloseDate,FCancelStatus,FChangerId," +
                "FReceiveId.FName,FNote,FHeadDeliveryWay,FHEADLOCID,FCorrespondOrgId,FSaleGroupId," +
                "FChangeReason,FBusinessType,FReceiveContact,FChargeId,FCreatorId,FCreateDate,FModifierId,FModifierId.FName," +
                "FModifyDate,FSaleOrgId,FSaleOrgId.FName,FVersionNo,FSignStatus,FSOFrom,F_SK_Date,F_SHGJ1.FNumber,F_SHGJ1,FExchangeRate,FSettleCurrId.FCode," +
                "FDeliveryDate";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 0);
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
        kingdeeSoConsumerService.executeConsumer(map);
        return ApiResult.success();
    }


}
