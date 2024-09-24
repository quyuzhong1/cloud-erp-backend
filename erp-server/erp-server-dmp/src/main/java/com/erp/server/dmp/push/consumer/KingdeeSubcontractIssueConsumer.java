package com.erp.server.dmp.push.consumer;


import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.dmp.push.service.business.KingdeeOtherInstockConsumerService;
import com.erp.server.dmp.push.service.business.KingdeeSubcontractIssueConsumerService;
import com.erp.server.dmp.service.DmpPushTaskService;
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
 * @description: 委外发料单
 * @author Will
 * @date: 2024/1/26 18:12
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC,
        selectorExpression = "kingdee_subcontract_issue_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SUBCONTRACT_ISSUE,
        consumeMode = ConsumeMode.ORDERLY)
public class KingdeeSubcontractIssueConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private KingdeeSubcontractIssueConsumerService kingdeeSubcontractIssueConsumerService;

    @Resource
    private DmpPushTaskService dmpPushTaskService;

    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SUB_PICKMTRL.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "WWLL00000025"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FID,FSrcBillType";
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
        kingdeeSubcontractIssueConsumerService.executeConsumer(map);
        return ApiResult.success();
    }

}