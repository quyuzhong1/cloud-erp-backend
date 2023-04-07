package com.erp.server.plm.rocketmq.sync.kingdee.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.common.message.enums.AssistantDataEnum;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeCategoryService;
import com.erp.server.plm.service.BasicCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/4 12:25
 */
@Slf4j
@Service
public class SyncKingdeeCategoryServiceImpl implements SyncKingdeeCategoryService {

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private BasicCategoryService basicCategoryService;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    public void syncDataToKingdee(BasicCategoryEntity entity) {
        Map<String, Object> resultMap = new HashMap<>(MathUtil.FIVE);

        //是否存在上级
        boolean isExistParent = !MathUtil.ZERO.toString().equals(entity.getPid());
        resultMap.put("isExistParent", isExistParent);
        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //名称
        resultMap.put("name",entity.getName());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());

        //模块类型
        Integer moduleType = ApiModuleTypeEnum.ONE_LEVEL_CATEGORY.getCode();
        //辅助资料类型编码
        String fNumber = AssistantDataEnum.ONE_LEVEL_CATEGORY.getCode();

        //二级分类
        if (isExistParent) {
            moduleType = ApiModuleTypeEnum.SECOND_LEVEL_CATEGORY.getCode();
            fNumber = AssistantDataEnum.SECOND_LEVEL_CATEGORY.getCode();
            //查询上级分类编码
            BasicCategoryEntity parent = basicCategoryService.lambdaQuery().eq(BasicCategoryEntity::getId, entity.getPid()).one();
            if (ObjectUtils.isEmpty(parent)) {
                log.error("未找到上级分类，pid = {}",entity.getPid());
              throw new ServiceException(new ApiResult(1,"未找到上级分类"));
            }
            //上级编码
            resultMap.put("parentCode",parent.getCode());
            //二级编码
            resultMap.put("code",parent.getCode().concat(entity.getCode()));
        }
        resultMap.put("moduleType",moduleType);
        resultMap.put("fNumber", fNumber);
        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_CATEGORY_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return basicCategoryService.updateSyncKingdeeStatus(entity.getId(), SyncKingdeeStatusEnum.IN_SYNC.getCode(),"");
            }
            return Boolean.TRUE;
        });
    }
}
