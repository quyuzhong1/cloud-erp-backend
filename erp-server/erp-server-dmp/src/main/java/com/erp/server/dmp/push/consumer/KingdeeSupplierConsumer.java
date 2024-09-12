package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeSupplierConsumerService;
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
        selectorExpression = "kingdee_supplier_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SUPPLIER,
        consumeMode = ConsumeMode.ORDERLY)
public class KingdeeSupplierConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private KingdeeSupplierConsumerService kingdeeSupplierConsumerService;

    @Resource
    private DmpPushTaskService dmpPushTaskService;


    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_SUPPLIER.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FNumber = '%s'", "GYS23041400015"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FNumber,FFinanceInfo_FEntryID,FPayCondition.FNumber,FContact,FTel,FCommonContactId.FNumber,FsupplierId,FConForbidStatus";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 0);
        System.out.println(queryList);


        LinkedHashMap<String, Object> viewMap = new LinkedHashMap<>();
        viewMap.put("Number", "GYS23041400017");
        JSONObject viewJson = apiUtils.getViewJson(JSONUtil.toJsonStr(viewMap));
        System.out.println(viewJson);

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
        kingdeeSupplierConsumerService.executeConsumer(map);
        return ApiResult.success();
    }


}
