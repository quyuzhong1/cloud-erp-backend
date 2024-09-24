package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.dmp.push.service.business.KingdeeProductDetailConsumerService;
import com.erp.server.dmp.service.DmpPushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: 金蝶物料同步
 * @date 2023/3/9 16:24
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC,
        selectorExpression = "kingdee_product_detail_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_PRODUCT_DETAIL,
        consumeMode = ConsumeMode.ORDERLY)
public class KingdeeProductDetailConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private KingdeeProductDetailConsumerService kingdeeProductDetailConsumerService;

    @Resource
    private DmpPushTaskService dmpPushTaskService;


    public static void main(String[] args) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_MATERIAL.getCode());
        LinkedHashMap<String, Object> viewMap = new LinkedHashMap<>();
        // viewMap.put("id", "391315");
           viewMap.put("number", "DZ1601");

        log.info("view方法数据查询,viewJson = {}", JSONUtil.toJsonStr(viewMap));

        JSONObject model = apiUtils.getViewJson(JSONUtil.toJsonStr(viewMap));
        JSONObject createOrgId = (JSONObject) model.get("CreateOrgId");
        System.out.println(model);
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
        kingdeeProductDetailConsumerService.executeConsumer(map);
        return ApiResult.success();
    }

}
