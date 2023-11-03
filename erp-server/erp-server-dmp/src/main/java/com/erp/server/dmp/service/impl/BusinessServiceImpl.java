package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.*;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.BusinessHandlerRegistry;
import com.common.business.handler.IBusinessHandler;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.DmpPullTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 业务处理服务类
 * @author Cloud
 */
@Slf4j
@Service
public class BusinessServiceImpl {
    @Resource
    private BusinessHandlerRegistry registry;
    @Resource
    private MongoService mongoService;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private DmpPullTaskService dmpPullTaskService;

    /**
     * 业务处理
     * @param category 业务类型
     * @param platform 平台类型
     * @param business 业务类型
     * @param data     业务数据
     * @param <T>      业务类型
     * @param <R>      业务返回类型
     * @param <>      业务数据类型
     */
//    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public <T extends CleanBaseDTO,R extends UniqueDto> void pullProcessBusiness(String category, String platform, String business, JobTaskDTO data) {
        IBusinessHandler<T,R> handler = (IBusinessHandler<T,R>) registry.getHandler(category, platform, business);
        if (handler != null) {
            PlatformDataDTO<T, R> platformData = handler.pullHandle(data);

            String targetPlatform = handler.getTargetPlatform();
            Boolean isSendMq = handler.getIsSendMq();
            // 保存mongo 并发送mq
            List<R> toMqList = compareAndSaveMongo(isSendMq, category, platform, business, targetPlatform, platformData, RocketMqTopic.PLATFORM_PULL_DATA_TOPIC);
        } else {
            // Handle the case when no handler is found
            throw new RuntimeException("No handler found for category: " + category + ", platform: " + platform + ", business: " + business);
        }

    }

    private <R extends UniqueDto, T extends CleanBaseDTO> List<R> compareAndSaveMongo(Boolean isSendMq, String category, String platform, String business,String targetPlatform, PlatformDataDTO<T, R> platformData, String topic) {
        // 保存数据到mongodb 并推送到mq
        List<T> sourceData = platformData.getSourceData();
        if(CollectionUtil.isEmpty(sourceData)){
            return Collections.EMPTY_LIST;
        }
        List<T> insertList = new ArrayList<>();
        List<R> pushToMqList = new ArrayList<>();
        Class<T> tClass = (Class<T>) sourceData.get(0).getClass();
        List<String> uniqueIds = new ArrayList<>();
        String tableName = StrUtil.format("{}_{}_{}", category, platform, business);
        String tag = StrUtil.format("{}_{}", category, business) + "_tag";
        for (T item : sourceData) {
            OrderMongoDTO orderMongoDTO =  OrderMongoDTO.getUniqId(item.getUniqueId());
            List<T> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, tableName, tClass);
            item.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            item.setDownloadTime(LocalDateTime.now().toString());
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add((T) item);
                uniqueIds.add(item.getUniqueId());
                continue;
            }
            T mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(item.toString())) {
                continue;
            }
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(item), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.getUniqueId());
            mongoService.updateMongoData(updateDto, mapUtil, tableName, tClass);
            uniqueIds.add(item.getUniqueId());
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, tableName);
        }
        // 不发送MQ
        if (!isSendMq){
            return pushToMqList;
        }
        List<R> targetData = platformData.getTargetData();
        for (R targetDatum : targetData) {
            uniqueIds.add(targetDatum.getUniqueId());
        }
        targetData.stream().filter(item -> uniqueIds.contains(item.getUniqueId())).forEach(pushToMqList::add);
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("平台数据下载 tag =【{}】, 无需推送到MQ dto={}", tag, JSONUtil.toJsonStr(platformData));
            return Collections.EMPTY_LIST;
        }
        // 异步推送到MQ
        pushToMqList.stream().peek(msg ->{
            BusinessTypeEnum businessType = BusinessTypeEnum.getByCode(business);
            if (ObjectUtil.isEmpty(businessType)){
                throw new ServiceException(StrUtil.format("业务类型business = {} 不存在", business));
            }
            SourceTypeEnum sourceType = businessType.getSourceType();
            if (ObjectUtil.isEmpty(sourceType)){
                throw new ServiceException(StrUtil.format("来源类型business = {} 不存在", business));
            }
            String modelTaskId = dmpPullTaskService.saveOrUpdateDmpSyncTask(new DmpPullTaskEntity(platform, sourceType.getCode(), targetPlatform, topic, tag, msg));
            msg.setDmpSyncTaskId(modelTaskId);
            SendResult cleanResult = mqProducerService.syncClassMsg(topic, tag, msg, msg.getUniqueId());
            if (!SendStatus.SEND_OK.equals(cleanResult.getSendStatus())){
                throw new RuntimeException(StrUtil.format("发送业务模块 MQ数据异常，{}", JSONUtil.toJsonStr(cleanResult)));
            }
        }).collect(Collectors.toList());

        return pushToMqList;
    }

    /**
     * 详情处理业务处理
     * @param category 业务类型
     * @param platform 平台类型
     * @param business 业务类型
     * @param <T>      业务类型
     * @param <R>      业务返回类型
     */
    @Transactional(rollbackFor = Exception.class)
    public <T extends CleanBaseDTO, R extends UniqueDto> void pullDetailProcess(T sourceDto,  R dto, String category, String platform, String business) {
        IBusinessHandler<T,R> handler = (IBusinessHandler<T,R>) registry.getHandler(category, platform, business);
        String targetPlatform = handler.getTargetPlatform();
        String topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC;
        String tag = StrUtil.format("{}_{}", category, business) + "_tag";

        Class<T> tClass = (Class<T>) sourceDto.getClass();
        String tableName = StrUtil.format("{}_{}_{}", category, platform, business);
        // 修改数据
        OrderMongoDTO updateDto = OrderMongoDTO.getUniqId(dto.getUniqueId());
        MapUtil mapUtil =JSONObject.parseObject(JSONObject.toJSONString(dto), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, tableName, tClass);

        // 异步推送到MQ
        String modelTaskId = dmpPullTaskService.saveOrUpdateDmpSyncTask(new DmpPullTaskEntity(platform, business, targetPlatform, topic, tag, dto));
        dto.setDmpSyncTaskId(modelTaskId);
        SendResult cleanResult = mqProducerService.syncClassMsg(topic, tag, dto, dto.getUniqueId());
        if (!SendStatus.SEND_OK.equals(cleanResult.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送业务模块 MQ数据异常，{}", JSONUtil.toJsonStr(cleanResult)));
        }
    }
}
