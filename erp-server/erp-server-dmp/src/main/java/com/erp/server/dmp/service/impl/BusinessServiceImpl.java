package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.*;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.BusinessHandlerRegistry;
import com.common.business.handler.IBusinessHandler;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.Md5Util;
import com.common.message.constant.RocketMqTopic;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.dto.OmsMongoDTO;
import com.erp.server.dmp.enums.CleanDataTableEnum;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.DmpPullTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
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
    private CfgSettingService cfgSettingService;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private DmpPullTaskService dmpPullTaskService;
    private static final int size = 100;

    /**
     * 业务处理
     *
     * @param <T>             业务类型
     * @param <R>             业务返回类型
     * @param <>              业务数据类型
     * @param category        业务类型
     * @param platform        平台类型
     * @param business        业务类型
     * @param data            业务数据
     * @param platformApiEnum
     */
//    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public <T extends CleanBaseDTO,R extends UniqueDto> void pullProcessBusiness(String category, String platform, String business, JobTaskDTO data, PlatformApiEnum platformApiEnum) {
        IBusinessHandler<T,R> handler = (IBusinessHandler<T,R>) registry.getHandler(category, platform, business);
        if (handler != null) {
            PlatformDataDTO<T, R> platformData = handler.pullHandle(data);

            String targetPlatform = handler.getTargetPlatform();
            Boolean isSendMq = handler.getIsSendMq();
            // 保存mongo 并发送mq
            List<R> toMqList = compareAndSaveMongo(isSendMq, category, platform, business, targetPlatform, platformData, RocketMqTopic.PLATFORM_PULL_DATA_TOPIC, platformApiEnum);
        } else {
            // Handle the case when no handler is found
            throw new RuntimeException("No handler found for category: " + category + ", platform: " + platform + ", business: " + business);
        }

    }

    /**
     * 批量处理业务
     *
     * @param <T>             业务类型
     * @param <R>             业务返回类型
     * @param <>              业务数据类型
     * @param category        业务类型
     * @param platform        平台类型
     * @param business        业务类型
     * @param data            业务数据
     * @param platformApiEnum
     */
//    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
//    @Transactional(rollbackFor = Exception.class)
    public <T extends CleanBaseDTO,R extends UniqueDto> void batchPullProcessBusiness(String category, String platform, String business, JobTaskDTO data, PlatformApiEnum platformApiEnum, Integer batchSendMqSize) {
        IBusinessHandler<T,R> handler = (IBusinessHandler<T,R>) registry.getHandler(category, platform, business);
        if (handler != null) {
            PlatformDataDTO<T, R> platformData = handler.pullHandle(data);

            String targetPlatform = handler.getTargetPlatform();
            Boolean isSendMq = handler.getIsSendMq();
            // 保存mongo 并发送mq
            List<R> toMqList = batchCompareAndSaveMongo(isSendMq, category, platform, business, targetPlatform, platformData, RocketMqTopic.PLATFORM_PULL_DATA_TOPIC, platformApiEnum, batchSendMqSize);
        } else {
            // Handle the case when no handler is found
            throw new RuntimeException("No handler found for category: " + category + ", platform: " + platform + ", business: " + business);
        }

    }

    /**
     * 业务处理
     *
     * @param <T>                      业务类型
     * @param <R>                      业务返回类型
     * @param <>                       业务数据类型
     * @param category                 业务类型
     * @param platform                 平台类型
     * @param business                 业务类型
     * @param platformApiEnum          任务类型
     * @param clearCheckDownloadStatus
     */
//    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public <T extends CleanBaseDTO,R extends UniqueDto> void cleanProcessBusiness(String category, String platform, String business, PlatformApiEnum platformApiEnum, Boolean clearCheckDownloadStatus) {
        IBusinessHandler<T,R> handler = (IBusinessHandler<T,R>) registry.getHandler(category, platform, business);
        if (handler != null) {
            List<T> sourceDataList = getCleanData(category,platform, business, clearCheckDownloadStatus);
            PlatformDataDTO<T, R> platformData = handler.cleanHandle(sourceDataList);

            String targetPlatform = handler.getTargetPlatform();
            // 保存mongo 并发送mq
            List<R> toMqList = compareAndSaveMongo(true, category, platform, business, targetPlatform, platformData, RocketMqTopic.PLATFORM_PULL_DATA_TOPIC, platformApiEnum);
        } else {
            // Handle the case when no handler is found
            throw new RuntimeException("No handler found for category: " + category + ", platform: " + platform + ", business: " + business);
        }

    }

    private <T extends CleanBaseDTO> List<T> getCleanData(String category, String platform, String business, Boolean clearCheckDownloadStatus) {
        String tableName = StrUtil.format("{}_{}_{}", category, platform, business);
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO;
        if (null != clearCheckDownloadStatus && clearCheckDownloadStatus){
            orderMongoDTO = OrderMongoDTO.getByIsCleanDateStrWithDownloadStatus(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        } else {
            // 查询isClean = 0
            orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        }
        Class tClass = Objects.requireNonNull(CleanDataTableEnum.getByName(tableName)).getTClass();
        List<T> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, tableName, tClass);
        if (CollectionUtil.isEmpty(mongoData)) {
            return Collections.EMPTY_LIST;
        }else {
            return mongoData;
        }
    }

    private <R extends UniqueDto, T extends CleanBaseDTO> List<R> compareAndSaveMongo(Boolean isSendMq, String category, String platform, String business, String targetPlatform, PlatformDataDTO<T, R> platformData, String topic, PlatformApiEnum platformApiEnum) {
        // 保存数据到mongodb 并推送到mq
        List<T> sourceData = platformData.getSourceData();
        if(CollectionUtil.isEmpty(sourceData)){
            return Collections.EMPTY_LIST;
        }
        List<R> pushToMqList = new ArrayList<>();
        Class<T> tClass = (Class<T>) sourceData.get(0).getClass();
        List<String> uniqueIds = new ArrayList<>();
        // 根据定义的类型表名
        String tableName = StrUtil.format("{}_{}_{}", category, platform, business);
        if (null != platformApiEnum && StringUtils.isNotBlank(platformApiEnum.getMongoTableName())) {
            tableName = platformApiEnum.getMongoTableName();
        }
        String tag = StrUtil.format("{}_{}", category, business) + "_tag";
        // 保存或更新到mongo
        handleSaveOrUpdateMongo(sourceData, tableName, tClass, uniqueIds);
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
        String finalTableName = tableName;
//        pushToMqList.stream().peek(msg ->{
        pushToMqList.forEach(msg ->{
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
            log.info("MQ消息发送成功：{} {} {} {}", topic, tag, msg, msg.getUniqueId());
            if (!SendStatus.SEND_OK.equals(cleanResult.getSendStatus())){
                throw new RuntimeException(StrUtil.format("发送业务模块 MQ数据异常，{}", JSONUtil.toJsonStr(cleanResult)));
            }else {
                OmsMongoDTO updateDto = new OmsMongoDTO(msg.getUniqueId());
                MapUtil mapUtil = new MapUtil();
                mapUtil.put("isClean", 1);
                mongoService.updateMongoData(updateDto, mapUtil, finalTableName, tClass);
            }
        });
//        }).collect(Collectors.toList());

        return pushToMqList;
    }

    public  <T extends CleanBaseDTO> void handleSaveOrUpdateMongo(List<T> sourceData, String tableName, Class<T> tClass, List<String> uniqueIds) {
        List<T> insertList = new ArrayList<>();
        for (T item : sourceData) {
//            OrderMongoDTO orderMongoDTO =  OrderMongoDTO.getUniqId(item.getUniqueId());
            UniqueDto uniqueDto = UniqueDto.getUniqId(item.getUniqueId());
            // 指定数量理论返回
            List<T> mongoData = mongoService.findMongoData(uniqueDto, 0, 100, tableName, tClass);
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
            // 更新
            Map<String, Object> updateFieldMap = item.getUpdateFieldMap();
            MapUtil mapUtil;
            if (CollectionUtil.isEmpty(updateFieldMap)){
                // 无指定字段更新所有
                mapUtil = JSONObject.parseObject(JSONObject.toJSONString(item), MapUtil.class);
            } else {
                // 根据指定字段更新
                mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
                mapUtil.putAll(updateFieldMap);
            }
            OmsMongoDTO updateDto = new OmsMongoDTO(mongoDatum.getUniqueId());
            mongoService.updateMongoData(updateDto, mapUtil, tableName, tClass);
            uniqueIds.add(item.getUniqueId());
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, tableName);
        }
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
        BusinessTypeEnum businessType = BusinessTypeEnum.getByCodeAndThrow(business);

        Class<T> tClass = (Class<T>) sourceDto.getClass();
        String tableName = StrUtil.format("{}_{}_{}", category, platform, business);
        // 修改数据
        UniqueDto updateDto = UniqueDto.getUniqId(dto.getUniqueId());
        MapUtil mapUtil =JSONObject.parseObject(JSONObject.toJSONString(sourceDto), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, tableName, tClass);

        String modelTaskId = dmpPullTaskService.saveOrUpdateDmpSyncTask(new DmpPullTaskEntity(platform, businessType.getSourceType().getCode(), targetPlatform, topic, tag, dto));
        // 异步推送到MQ
        dto.setDmpSyncTaskId(modelTaskId);
        log.info("详情发送队列前：{}", JSONUtil.toJsonStr(dto));
        SendResult cleanResult = mqProducerService.syncClassMsg(topic, tag, dto, dto.getUniqueId());
        log.info("详情发送队列结果：{}", JSONUtil.toJsonStr(cleanResult));
        if (!SendStatus.SEND_OK.equals(cleanResult.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送业务模块 MQ数据异常，{}", JSONUtil.toJsonStr(cleanResult)));
        }
    }

    private <R extends UniqueDto, T extends CleanBaseDTO> List<R> batchCompareAndSaveMongo(Boolean isSendMq, String category, String platform, String business, String targetPlatform, PlatformDataDTO<T, R> platformData, String topic, PlatformApiEnum platformApiEnum, Integer batchSendMqSize) {
        // 保存数据到mongodb 并推送到mq
        List<T> sourceData = platformData.getSourceData();
        if(CollectionUtil.isEmpty(sourceData)){
            return Collections.emptyList();
        }
        List<R> pushToMqList = new ArrayList<>();
        Class<T> tClass = (Class<T>) sourceData.get(0).getClass();
        List<String> uniqueIds = new ArrayList<>();
        // 根据定义的类型表名
        String tableName = StrUtil.format("{}_{}_{}", category, platform, business);
        if (null != platformApiEnum && StringUtils.isNotBlank(platformApiEnum.getMongoTableName())) {
            tableName = platformApiEnum.getMongoTableName();
        }
        String tag = StrUtil.format("{}_{}", category, business) + "_tag";
        // 保存或更新到mongo
        handleSaveOrUpdateMongo(sourceData, tableName, tClass, uniqueIds);
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
            return Collections.emptyList();
        }
        BusinessTypeEnum businessType = BusinessTypeEnum.getByCode(business);
        if (null == businessType){
            throw new ServiceException(StrUtil.format("业务类型business = {} 不存在", business));
        }
        SourceTypeEnum sourceType = businessType.getSourceType();
        if (ObjectUtil.isEmpty(sourceType)){
            throw new ServiceException(StrUtil.format("来源类型business = {} 不存在", business));
        }
        // 根据唯一规则去重
        List<DmpPullTaskEntity> allList = new ArrayList<>(pushToMqList.stream()
                .map(msg -> new DmpPullTaskEntity(platform, sourceType.getCode(), targetPlatform, topic, tag, msg))
                .collect(Collectors.toMap(
                        DmpPullTaskEntity::uniqueKey,
                        obj -> obj,
                        (existing, replacement) -> existing
                ))
                .values());

        // 批量保存和更新
        List<DmpPullTaskEntity> allResultList = dmpPullTaskService.batchCheckSaveAndUpdate(allList, platform, sourceType.getCode(), targetPlatform, topic, tag);
        Map<String, String> unqueIdAndTaskIdMap = allResultList.stream().collect(Collectors.toMap(DmpPullTaskEntity::uniqueKey, DmpPullTaskEntity::getId));
        // 设置taskId到消息体
        pushToMqList.forEach(e -> {
            String uniqueKey = StrUtil.format("{}_{}_{}_{}_{}_{}_{}",
                    sourceType.getCode(),
                    e.getUniqueId(),
                    e.getUniqueId(),
                    platform,
                    targetPlatform,
                    topic,
                    tag);
            String taskId = unqueIdAndTaskIdMap.get(uniqueKey);
            if (StringUtils.isBlank(taskId)){
                throw new ServiceException("处理异常:未找到DmpPullTaskEntity的Id， sourceId=" + e.getUniqueId());
            }
            e.setDmpSyncTaskId(taskId);
        });

        List<R> collect = pushToMqList.stream().peek(msg -> {
            SendResult cleanResult = mqProducerService.syncClassMsg(topic, tag, JSONUtil.toJsonStr(msg), msg.getUniqueId());
            log.debug("发送业务模块 MQ数据结果：UniqueId={}, resultMsg={}", msg.getUniqueId(), JSONUtil.toJsonStr(cleanResult));
            if (!SendStatus.SEND_OK.equals(cleanResult.getSendStatus())) {
                throw new RuntimeException(StrUtil.format("发送业务模块 MQ数据异常，{}", JSONUtil.toJsonStr(cleanResult)));
            }
        }).collect(Collectors.toList());
        return pushToMqList;
    }

    /**
     * 批量处理业务
     *
     * @param <T>             业务类型
     * @param <R>             业务返回类型
     * @param <>              业务数据类型
     * @param category        业务类型
     * @param platform        平台类型
     * @param business        业务类型
     * @param data            业务数据
     * @param platformApiEnum
     */
//    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
//    @Transactional(rollbackFor = Exception.class)
    public <T extends CleanBaseDTO,R extends UniqueDto> void batchCheckAndInsert(String category, String platform, String business, JobTaskDTO data, PlatformApiEnum platformApiEnum, Integer batchSendMqSize) {
        IBusinessHandler<T,R> handler = (IBusinessHandler<T,R>) registry.getHandler(category, platform, business);
        if (handler != null) {
            PlatformDataDTO<T, R> platformData = handler.pullHandle(data);

            String targetPlatform = handler.getTargetPlatform();
            Boolean isSendMq = handler.getIsSendMq();
            // 保存mongo 并发送mq
            List<R> toMqList = batchCompareAndSaveMongo(isSendMq, category, platform, business, targetPlatform, platformData, RocketMqTopic.PLATFORM_PULL_DATA_TOPIC, platformApiEnum, batchSendMqSize);
        } else {
            // Handle the case when no handler is found
            throw new RuntimeException("No handler found for category: " + category + ", platform: " + platform + ", business: " + business);
        }

    }
}
