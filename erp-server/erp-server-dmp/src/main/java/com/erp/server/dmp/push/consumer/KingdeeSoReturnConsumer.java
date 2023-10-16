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
import com.erp.server.dmp.push.service.business.KingdeeSoReturnConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.kingdee.impl.KingdeeCommonServiceImpl;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
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
        selectorExpression = "kingdee_so_return_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SO_RETURN,
        consumeMode = ConsumeMode.ORDERLY)
public class KingdeeSoReturnConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private KingdeeSoReturnConsumerService kingdeeSoReturnConsumerService;

    @Resource
    private DmpPushTaskService dmpPushTaskService;


    public static void main(String[] args) {
        //模块类型
        Integer type = ApiModuleTypeEnum.SO_RETURN.getCode();
        KingdeeCommonService kingdeeCommonService = new KingdeeCommonServiceImpl();
        Map<String, Object> map = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SAL_RETURNSTOCK.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
//        queryFilters.add(String.format("FDocumentStatus = '%s'", "C"));
        queryFilters.add(String.format("FBillNo = '%s'", "XSTHD12791749"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FBillNo,FID";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 2);
        System.out.println(queryList);
    }


    @Override
    public void updateSyncTaskStatus(String syncTaskId, SyncStatusEnum code, String msg) {
        dmpPushTaskService.updateStatus(new DmpSyncMqDTO.ParamDTO(syncTaskId, code.getCode(), msg));
    }

    @Override
    public ApiResult<?> handle(Object ext) {
        Map<String, Object> map = JSONUtil.parseObj(ext);
        kingdeeSoReturnConsumerService.executeConsumer(map);
        return ApiResult.success();
    }


}
