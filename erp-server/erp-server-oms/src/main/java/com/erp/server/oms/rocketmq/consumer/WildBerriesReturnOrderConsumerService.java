package com.erp.server.oms.rocketmq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractRestCloudPlatformConsumerHandler;
import com.erp.model.oms.entity.*;
import com.erp.server.oms.service.ListingInfoService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: wtr
 * @Date: 2025/9/29 15:41
 * @Param:
 * @Return:
 * @Description:
 **/

@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.RESTCLOUD_PLATFORM_RETURN_ORDER_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.RESTCLOUD_PLATFORM_RETURN_ORDER_TO_OMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.RESTCLOUD_PLATFORM_RETURN_ORDER_TO_OMS_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class WildBerriesReturnOrderConsumerService extends AbstractRestCloudPlatformConsumerHandler {

    @Resource
    private ListingInfoService listingInfoService;

    @Resource
    private NewPlatformReturnOrderConsumerService newPlatformReturnOrderConsumerService;

    @Override
    public String getBizName() {
        return "WildBerries平台退货订单";
    }

    @Override
    public void handle(String data) {
        newPlatformReturnOrderConsumerService.handle(skuMapping(data));
    }

    public String skuMapping(String data) {
        // 解析原始JSON数据
        JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();

        // 处理detailList数组
        JsonArray detailList = jsonObject.getAsJsonArray("detailList");
        if (detailList != null) {
            JsonArray newDetailList = new JsonArray();
            // 用于批量查询，减少数据库访问次数
            Map<String, ListingInfoEntity> skuMappingCache = new HashMap<>();

            // 收集所有platformSkuId
            List<String> platformSkuIds = new ArrayList<>();
            for (JsonElement element : detailList) {
                JsonObject detail = element.getAsJsonObject();
                if (detail.has("platformSkuId")) {
                    platformSkuIds.add(detail.get("platformSkuId").getAsString());
                }
            }

            // 批量查询映射关系
            if (!platformSkuIds.isEmpty()) {
                List<ListingInfoEntity> listingInfos = listingInfoService.list(
                        new LambdaQueryWrapper<ListingInfoEntity>()
                                .in(ListingInfoEntity::getPlatformSkuId, platformSkuIds)
                                .eq(ListingInfoEntity::getIsDeleted, Boolean.FALSE)
                );

                // 构建缓存映射
                for (ListingInfoEntity entity : listingInfos) {
                    skuMappingCache.put(entity.getPlatformSkuId(), entity);
                }
            }

            // 处理每个detail
            for (JsonElement element : detailList) {
                JsonObject detail = element.getAsJsonObject();
                if (detail.has("platformSkuId")) {
                    String platformSkuIdValue = detail.get("platformSkuId").getAsString();
                    ListingInfoEntity listingInfoEntity = skuMappingCache.get(platformSkuIdValue);

                    // 如果没有找到映射关系
                    if (listingInfoEntity == null) {
                        log.warn("退货商品未映射，平台SKU: {}", platformSkuIdValue);
                        throw new ServiceException("退货商品未映射，平台SKU: " + platformSkuIdValue);
                    }

                    // 移除platformSkuId字段
                    detail.remove("platformSkuId");
                    // 添加platformSkuNo字段
                    detail.addProperty("platformSkuNo", listingInfoEntity.getPlatformSkuNo());
                }
                newDetailList.add(detail);
            }
            jsonObject.add("detailList", newDetailList);
        }

        return jsonObject.toString();
    }
}

