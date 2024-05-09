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
        queryFilters.add(StrUtil.format("FBillNo in ({})", "'1101846843208509'," +
                "'1101860234099481'," +
                "'1101903503801116'," +
                "'1101906720316572'," +
                "'1101909745414170'," +
                "'1101914710291046'," +
                "'1101916936227088'," +
                "'1101917099185489'," +
                "'1101924617334285'," +
                "'1101927988240011'," +
                "'1101929992437966'," +
                "'1101940254195574'," +
                "'1101944696828783'," +
                "'1101946956517235'," +
                "'1101952700327235'," +
                "'1101955954976340'," +
                "'1101973160024441'," +
                "'1101982176163416'," +
                "'1101982463961558'," +
                "'1102005280289426'," +
                "'1102031567130892'," +
                "'1102038882620592'," +
                "'1102050907248517'," +
                "'1102064096721379'," +
                "'1102064096721379'," +
                "'1102064096721379'," +
                "'1102068178569637'," +
                "'1102069440573643'," +
                "'1102070076596587'," +
                "'1102073127780439'," +
                "'1102075938855663'," +
                "'1102076016861675'," +
                "'1102081425167384'," +
                "'1102085346622762'," +
                "'1102090399214910'," +
                "'1102097334813871'," +
                "'1102099381272766'," +
                "'1102103632475019'," +
                "'1102119699128204'," +
                "'1102121337656213'," +
                "'1102121518549722'," +
                "'1102123397355957'," +
                "'1102152775209200'," +
                "'1102166084272229'," +
                "'1102178538680943'," +
                "'1102178538680943'," +
                "'1102195516992182'," +
                "'1102210175416767'," +
                "'1102221878431892'," +
                "'1102232644801520'," +
                "'3028131896456572'," +
                "'3028198147626572'," +
                "'3028244991962624'," +
                "'3028332243264120'," +
                "'3028415369124263'," +
                "'3028502171083895'," +
                "'3028514049835593'," +
                "'3028514049835593'," +
                "'3028716754761489'," +
                "'5276441030723914'"));
        String filterStr = String.join(" and ", queryFilters);

        String fieldKeys = "FID,FBillNo,FDate,FBillTypeId.FName,FBillTypeId.FNumber,FBillTypeId," +
                "FDocumentStatus,FCustId.FName,FCustId.FNumber,FSaleDeptId.FName,FSalerId.FName,FReceiveAddress,FLinkMan,FLinkPhone," +
                "FApproverId.FName,FApproveDate,FCloseStatus,FCloseDate,FCancelStatus,FChangerId," +
                "FReceiveId.FName,FNote,FHeadDeliveryWay,FHEADLOCID,FCorrespondOrgId,FSaleGroupId," +
                "FChangeReason,FBusinessType,FReceiveContact,FChargeId,FCreatorId,FCreateDate,FModifierId,FModifierId.FName," +
                "FModifyDate,FSaleOrgId,FSaleOrgId.FName,FVersionNo,FSignStatus,FSOFrom,F_SK_Date,F_SHGJ1,FExchangeRate,FSettleCurrId.FCode";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 0);
        System.out.println(queryList);


    }

    @Override
    public void updateSyncTaskStatus(String syncTaskId, SyncStatusEnum code, String msg) {
        dmpPushTaskService.updateStatus(new DmpSyncMqDTO.ParamDTO(syncTaskId, code.getCode(), msg));
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
