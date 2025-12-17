package com.erp.server.dmp.service.impl;


import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.DataIdempotent;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.MongoSuperDTO;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.message.constant.RedisKeyConstant;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.AmazonCreateReportResultDTO;
import com.erp.model.dmp.entity.AmzReportScheduleEntity;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.model.dmp.entity.CfgAmzReportTypeEntity;
import com.erp.model.dmp.entity.DmpAmzReportInfoEntity;
import com.erp.model.dmp.enums.AmzReportCreatedMethodEnum;
import com.erp.model.dmp.enums.AmzReportTaskStatusEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.sdk.oms.amz.spapi.dto.ReportSuperMongoDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.CreateReportScheduleSpecification;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportDocument;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiExceptionUtils;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiReportUtils;
import com.erp.server.dmp.convert.DmpReportConverter;
import com.erp.server.dmp.mapper.AmzReportTaskMapper;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.AmzReportHandleService;
import com.erp.server.dmp.service.AmzReportScheduleService;
import com.erp.server.dmp.service.AmzReportTaskService;
import com.erp.server.dmp.service.CfgAmzReportFieldService;
import com.erp.server.dmp.service.CfgAmzReportTypeService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.DmpAmzReportInfoService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.google.common.collect.Lists;
import com.xxl.job.core.context.XxlJobHelper;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 亚马逊报告请求记录 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
 */
@Slf4j
@Service
public class AmzReportTaskServiceImpl extends SuperServiceImpl<AmzReportTaskMapper, AmzReportTaskEntity> implements AmzReportTaskService {

    @Resource
    private DmpAmzReportInfoService dmpAmzReportInfoService;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private CfgAmzReportTypeService cfgAmzReportTypeService;
    @Resource
    private AmzReportHandleService amzReportHandleService;
    @Resource
    private AmzReportScheduleService amzReportScheduleService;
    @Resource
    private CfgAmzReportFieldService cfgAmzReportFieldService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private PlatformApiTaskService platformApiTaskService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private MongoService mongoService;
    @Lazy
    @Resource
    private AmzReportTaskService amzReportTaskService;
    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlerCreateReportTask(String groupId, List<ShopInfoEntity> shopList, Integer size, OffsetDateTime currentDateTime, List<AmzReportScheduleEntity> scheduleEntityList, Map<String, List<CfgAmzReportTypeEntity>> reportTypeConfigMap, Map<String, CfgAmzReportTypeEntity> reportTypeMap) {
        List<String> shopIds = shopList.stream().map(ShopInfoEntity::getId).distinct().collect(Collectors.toList());

        // 过滤获取当前店铺执行的任务
        List<AmzReportScheduleEntity> currentScheduleEntityList = scheduleEntityList.stream().filter(e -> shopIds.contains(e.getShopId())).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(currentScheduleEntityList)) {
            log.info("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束,当前无需要更新的店铺报告计划: group={}, shopIds={}", groupId, JSONUtil.toJsonStr(shopIds));
            XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束,当前无需要更新的店铺报告计划:: group={}, shopIds={}", groupId, JSONUtil.toJsonStr(shopIds));
            return;
        }

        // 分组
        // Map<店铺ID, List<计划任务>(取每一个分组的第一个) >>
        Map<String, List<AmzReportScheduleEntity>> scheduleGroup = currentScheduleEntityList
                .stream()
                .collect(Collectors.groupingBy(
                        AmzReportScheduleEntity::getShopId,
                        Collectors.collectingAndThen(Collectors.toList(), list -> groupByReportTypeGroup(list, reportTypeConfigMap))
                ));

        // 根据size指定每个分组执行的个数
        // 多线程?
        scheduleGroup.entrySet().stream().limit(size).forEach(entry -> {
            for (AmzReportScheduleEntity reportSchedule : entry.getValue()) {
                try {
                    // 当前类型报告处理中锁key
                    String reportRedissonKey = StrUtil.format(RedisCacheConstants.AMZ_REPORT_HANDLE_PREFIX, groupId);
                    // 只执行分组第一个
                    // 添加任务
                    this.createTask(reportRedissonKey, groupId, reportSchedule, currentDateTime, reportTypeMap);
                    log.info("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 创建待请求任务成功: group={}, shopId={}, reportType={}",
                            groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
                    XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 创建待请求任务成功: group={}, shopId={}, reportType={}",
                            groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
                } catch (Exception e) {
                    log.error("[创建【亚马逊报告】亚马逊-ERP] 创建亚马逊报表计划失败：group={}, shopId={}, reportType={}, error={}",
                            groupId,
                            reportSchedule.getShopId(),
                            reportSchedule.getReportType(),
                            ExceptionUtil.stacktraceToString(e, 1000));
                    XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 创建亚马逊报表计划失败：group={}, shopId={},reportType={}, error={}",
                            groupId,
                            reportSchedule.getShopId(),
                            reportSchedule.getReportType(),
                            ExceptionUtil.stacktraceToString(e, 1000)
                    );
                }
            }
        });
    }

    @Override
    @DataIdempotent(keyIdName = "reportRedissonKey", waitTime = 120)
    @Transactional(rollbackFor = Exception.class)
    public void createTask(String reportRedissonKey, String groupId, AmzReportScheduleEntity reportSchedule, OffsetDateTime currentDateTime, Map<String, CfgAmzReportTypeEntity> reportTypeMap) {
        // 请求参数数据开始时间
        String reqDataStartTime = "";
        // 请求参数数据结束时间
        String reqDataEndTime = "";
        // 报告配置
        CfgAmzReportTypeEntity reportTypeConfig = reportTypeMap.get(reportSchedule.getReportType());
        if (null == reportTypeConfig) {
            throw new ServiceException("未找到报告类型配置：" + reportSchedule.getReportType());
        }
        // 查询上一次记录
        AmzReportTaskEntity taskEntity = this.findLastTask(reportSchedule);
        // 是否是检查上一次执行任务
        if (reportTypeConfig.getHasPreTask() && null != taskEntity && !AmzReportTaskStatusEnum.FINISH.getCode().equalsIgnoreCase(taskEntity.getStatus())
                && !AmzReportTaskStatusEnum.STOP.getCode().equalsIgnoreCase(taskEntity.getStatus())
                && !AmzReportTaskStatusEnum.CANCELLED.getCode().equalsIgnoreCase(taskEntity.getStatus())
        ) {
            // 上次任务未完成
            log.info("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 存在上一次未完成任务: group={}, shopId={}, reportType={}", groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
            XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 存在上一次未完成任务: group={}, shopId={}, reportType={}",
                    groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
            return;
        }
        // 数据开始时间
        OffsetDateTime dataOffsetDateTime = reportSchedule.getDataStartTime().atOffset(BusinessCommonConstants.systemZoneOffset);
        // 数据间隔时间枚举
        CreateReportScheduleSpecification.PeriodEnum dataPeriodEnum = CreateReportScheduleSpecification.PeriodEnum.getByCode(reportSchedule.getDataPeriod());
        // 下次数据开始时间=数据结束时间
        OffsetDateTime nextDataOffsetDateTime = dataPeriodEnum.plusPeriod(dataOffsetDateTime);

        // 是否是增量报告(取上一次执行任务的时间)
        if (!reportTypeConfig.getIsFullUpdate()) {
            OffsetDateTime utcStartTime = dataOffsetDateTime.withOffsetSameInstant(ZoneOffset.UTC);
            reqDataStartTime = utcStartTime.toString();
            // utc 数据结束时间
            OffsetDateTime utcEndTime = nextDataOffsetDateTime.withOffsetSameInstant(ZoneOffset.UTC);
            reqDataEndTime = utcEndTime.toString();
        }

        // 创建报告待请求记录
        AmzReportTaskEntity newTaskEntity = DmpReportConverter.INSTANCE.initScheduleEntityToTask(reportSchedule, reqDataStartTime, reqDataEndTime, groupId);
        if (!this.save(newTaskEntity)) {
            throw new ServiceException("保存创建报告待请求记录失败");
        }

        // 添加到延时队列1末端
        SendResult result = mqProducerService.syncClassMsgWithDelayLevel(
                RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
                RocketMqTagEnum.AMZ_REPORT_CREATE_TAG.getName(),
                newTaskEntity,
                StrUtil.format("{}_{}", newTaskEntity.getId(), newTaskEntity.getStatus()),
                reportTypeConfig.getCreatedDelayLevel());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new ServiceException(StrUtil.format("发送创建报告待请求记录MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    public AmzReportTaskEntity findLastTask(AmzReportScheduleEntity reportSchedule) {
        return lambdaQuery()
                .eq(AmzReportTaskEntity::getReportType, reportSchedule.getReportType())
                .eq(AmzReportTaskEntity::getShopId, reportSchedule.getShopId())
                .eq(AmzReportTaskEntity::getMarketplaceIds, reportSchedule.getMarketplaceIds())
                .eq(AmzReportTaskEntity::getMainId, reportSchedule.getId())
                .orderByDesc(AmzReportTaskEntity::getCreateTime)
                .last(" LIMIT 1")
                .one();
    }

    @Override
    public boolean checkFinishOrStop(String id) {
        AmzReportTaskEntity taskEntity = getByIdOpt(id).orElseThrow(() -> new ServiceException("未找任务记录：id=" + id));
        return AmzReportTaskStatusEnum.FINISH.getCode().equalsIgnoreCase(taskEntity.getStatus()) ||
                AmzReportTaskStatusEnum.STOP.getCode().equalsIgnoreCase(taskEntity.getStatus()) ||
                AmzReportTaskStatusEnum.MANUAL_STOP.getCode().equalsIgnoreCase(taskEntity.getStatus()) ||
                AmzReportTaskStatusEnum.CANCELLED.getCode().equalsIgnoreCase(taskEntity.getStatus())
                ;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataIdempotent(keyIdName = "reportRedissonKey", waitTime = 120)
    public void consumerReportQuery(String reportRedissonKey, AmzReportTaskEntity entity) {
        // 检查当前记录是否已完成或终止?
        boolean hasFinishOrStop = this.checkFinishOrStop(entity.getId());
        if (hasFinishOrStop) {
            log.warn("步骤2:报告查询消费结束:任务已完成或终止, id={}", entity.getId());
            return;
        }
        // 报告类型配置
        CfgAmzReportTypeEntity recordTypeConfig = cfgAmzReportTypeService.getByRecordType(entity.getReportType());

        boolean allow = cfgAmzReportTypeService.checkCountryList(recordTypeConfig, entity.getMarketplaceIds().split(",")[0]);
        if (!allow) {
            this.stopByErrorMsg(entity, "根据报告类型配置当前市场不支持停止");
            return;
        }

        if (!AmzReportTaskStatusEnum.QUERY.getCode().equalsIgnoreCase(entity.getStatus())) {
            String msg = StrUtil.format("任务状态非query:id={}, status={}", entity.getId(), entity.getStatus());
            throw new ServiceException(msg);
        }
        String reportId = entity.getReportId();
        if (StringUtils.isBlank(reportId)) {
            throw new ServiceException("数据异常:报告ID为空:id=" + entity.getId());
        }
        Report report = amzReportHandleService.queryAmzReportInfo(entity);
        Report.ProcessingStatusEnum processingStatus = report.getProcessingStatus();

        // 处理中, 重推队列等待
        if (Report.ProcessingStatusEnum.IN_PROGRESS.equals(processingStatus) || Report.ProcessingStatusEnum.IN_QUEUE.equals(processingStatus)) {
            SendResult result = mqProducerService.syncClassMsgWithDelayLevel(
                    RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
                    RocketMqTagEnum.AMZ_REPORT_QUERY_TAG.getName(),
                    entity,
                    StrUtil.format("{}_{}", entity.getId(), entity.getStatus()),
                    recordTypeConfig.getQueryDelayLevel());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                throw new ServiceException(StrUtil.format("发送查询报告待请求记录MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
            return;
        }

        if (Report.ProcessingStatusEnum.CANCELLED.equals(processingStatus)) {
            // 亚马逊报告自动取消=完成(亚马逊报告数据为空状态是取消)
            this.updateStatus("", entity, AmzReportTaskStatusEnum.CANCELLED, null, null, null, null, LocalDateTime.now(), true);
            return;
        }

        // 报告创建方式
        AmzReportCreatedMethodEnum createdMethodEnum = AmzReportCreatedMethodEnum.getBySubscribedType(recordTypeConfig.getSubscribedType());

        // 记录报告信息
        DmpAmzReportInfoEntity dmpAmzReportInfoEntity = DmpReportConverter.INSTANCE.newReportInfoEntity(report, entity, createdMethodEnum.getCode());
        if (!dmpAmzReportInfoService.save(dmpAmzReportInfoEntity)) {
            throw new ServiceException("保存报告信息失败");
        }

        // 报告创建失败
        if (Report.ProcessingStatusEnum.FATAL.equals(processingStatus)) {
            // 检查是否停止:并更新状态
            boolean stop = this.checkStopAndUpdateTask(entity);
            if (stop) {
                log.warn("亚马逊查询报告消费:因触发停止规则结束,taskId={}", entity.getId());
                return;
            }

            // 检查缓存是否已删除
            String key = StrUtil.format(RedisCacheConstants.AMZ_REPORT_RESULT_PREFIX, entity.getId(), AmzReportTaskStatusEnum.CREATED.getCode());
            Object reportIdObj = redisUtil.get(key);
            if (null != reportIdObj) {
                redisUtil.del(key);
            }

            // 创建失败设置为待请求重新推送,并添加创建失败次数
            AmzReportTaskEntity newEntity = this.updateStatus("", entity, AmzReportTaskStatusEnum.CREATED, LocalDateTime.now(), null, null, null, null, true);

            // 处理中, 重推队列等待
            SendResult result = mqProducerService.syncClassMsgWithDelayLevel(
                    RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
                    RocketMqTagEnum.AMZ_REPORT_CREATE_TAG.getName(),
                    newEntity,
                    StrUtil.format("{}_{}", newEntity.getId(), newEntity.getStatus()),
                    recordTypeConfig.getCreatedDelayLevel());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                throw new ServiceException(StrUtil.format("发送查询报告待请求记录MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
            return;
        }
        // 报告创建成功
        if (Report.ProcessingStatusEnum.DONE.equals(processingStatus)) {
            // 更新待下载状态
            AmzReportTaskEntity newEntity = this.updateStatus(report.getReportId(), entity, AmzReportTaskStatusEnum.DOWNLOAD, null, LocalDateTime.now(), null, null, null, true);

            // 添加到延时队列3末端
            SendResult result = mqProducerService.syncClassMsgWithDelayLevel(
                    RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
                    RocketMqTagEnum.AMZ_REPORT_DOWNLOAD_TAG.getName(),
                    newEntity,
                    StrUtil.format("{}_{}", newEntity.getId(), newEntity.getStatus()),
                    recordTypeConfig.getDownloadDelayLevel());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                throw new ServiceException(StrUtil.format("发送报告下载MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataIdempotent(keyIdName = "reportRedissonKey")
    public void consumerReportCreate(String reportRedissonKey, AmzReportTaskEntity entity) {
        // 检查当前记录是否已完成或终止?
        boolean hasFinishOrStop = this.checkFinishOrStop(entity.getId());
        if (hasFinishOrStop) {
            log.warn("步骤1:报告创建消费结束:任务已完成或终止, id={}", entity.getId());
            // 检查缓存是否已删除
            String key = StrUtil.format(RedisCacheConstants.AMZ_REPORT_RESULT_PREFIX, entity.getId(), entity.getStatus());
            Object reportIdObj = redisUtil.get(key);
            if (null != reportIdObj) {
                redisUtil.del(key);
            }
            return;
        }

        if (!AmzReportTaskStatusEnum.CREATED.getCode().equalsIgnoreCase(entity.getStatus())) {
            String msg = StrUtil.format("任务状态非created:id={}, status={}", entity.getId(), entity.getStatus());
            throw new ServiceException(msg);
        }
        // 查询报告类型配置
        CfgAmzReportTypeEntity config = cfgAmzReportTypeService.checkCountryAndGetByRecordType(entity);
        if (null == config) {
            this.stopByErrorMsg(entity, "根据报告类型配置当前市场不支持停止");
            return;
        }

        // 是否检查先前任务
        if (config.getHasPreTask()) {
            // 检查当前类型的历史是否有未完成的记录
            List<AmzReportTaskEntity> historyList = this.findNotFinishOrStop(entity.getShopId(), entity.getReportType(), entity.getId());
            if (!CollectionUtil.isEmpty(historyList)) {
                historyList.forEach(e -> {
                    e.setStatus(AmzReportTaskStatusEnum.STOP.getCode());
                    e.setStatusDesc(AmzReportTaskStatusEnum.STOP.getName());
                    e.setErrorMsg("新任务丢弃之前未完成或未终止任务");
                    this.checkAndDelHistory(e);
                });
                if (!this.updateBatchById(historyList)) {
                    throw new ServiceException("批量更新历史任务失败");
                }
            }
        }
        // 查询是否有处理中的报告(计算预估等待时间：0=不等待)
        AmazonCreateReportResultDTO resultDTO = amzReportHandleService.checkAndCreateAmzReport(entity, config.getReportGroup());
        if (!resultDTO.isCreatedSuccess()) {
            // 根据预估时间 添加到延时队列1末端
            SendResult result = mqProducerService.syncClassMsgWithDelayLevel(
                    RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
                    RocketMqTagEnum.AMZ_REPORT_CREATE_TAG.getName(),
                    entity,
                    StrUtil.format("{}_{}", entity.getId(), entity.getStatus()),
                    // 等待时间转换延时等级
                    mqProducerService.convertSecondsToDelayLevel(resultDTO.getEstimatedWaitSecond())
            );
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                throw new ServiceException(StrUtil.format("发送创建报告待请求记录MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
            return;
        }


        // 请求创建报告
        String reportId = resultDTO.getReportId();

        // 固定位置：防止授权异常后所有任务后, 导致当前时间任务停止
        // 更新下次计划任务下次执行时间
        amzReportScheduleService.updateNextTime(entity.getMainId(), config);

        // 更新任务状态
        AmzReportTaskEntity newEntity = this.updateStatus(reportId, entity, AmzReportTaskStatusEnum.QUERY, LocalDateTime.now(), null, null, null, null, true);

        // 添加到延时队列2末端
        SendResult result = mqProducerService.syncClassMsgWithDelayLevel(
                RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
                RocketMqTagEnum.AMZ_REPORT_QUERY_TAG.getName(),
                newEntity,
                StrUtil.format("{}_{}", newEntity.getId(), newEntity.getStatus()),
                config.getQueryDelayLevel());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new ServiceException(StrUtil.format("发送查询报告待请求记录MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataIdempotent(keyIdName = "reportRedissonKey")
    public void consumerReportDownload(String reportRedissonKey, AmzReportTaskEntity entity) {
        // 检查当前记录是否已完成或终止?
        boolean hasFinishOrStop = this.checkFinishOrStop(entity.getId());
        if (hasFinishOrStop) {
            log.warn("步骤3:报告下载消费结束:任务已完成或终止, id={}", entity.getId());
            return;
        }

        if (!AmzReportTaskStatusEnum.DOWNLOAD.getCode().equalsIgnoreCase(entity.getStatus())) {
            String msg = StrUtil.format("任务状态非download:id={}, status={}", entity.getId(), entity.getStatus());
            throw new ServiceException(msg);
        }
        // 查询报告类型配置
        CfgAmzReportTypeEntity config = cfgAmzReportTypeService.checkCountryAndGetByRecordType(entity);
        if (null == config) {
            this.stopByErrorMsg(entity, "根据报告类型配置当前市场不支持停止");
            return;
        }

        // 查询当前已有的报告信息
        DmpAmzReportInfoEntity reportInfo = dmpAmzReportInfoService.getByReportId(entity.getReportId(), Report.ProcessingStatusEnum.DONE.getValue());
        if (null == reportInfo) {
            throw new ServiceException("数据异常:未找到成功的报告信息: report=" + entity.getReportId());
        }
        if (StringUtils.isBlank(reportInfo.getReportDocumentId())) {
            throw new ServiceException("数据异常:报告文档ID为空: report=" + entity.getReportId());
        }
        // 查询文档信息
        ReportDocument reportDocument = amzReportHandleService.queryAmzReportDocument(reportInfo.getShopId(), reportInfo.getReportDocumentId(), entity.getId(), entity.getStatus());

        // 根据url下载到FastDFS
        String compressionAlgorithm = null == reportDocument.getCompressionAlgorithm() ? "" : reportDocument.getCompressionAlgorithm().getValue();
        String fileName = StrUtil.subBetween(reportDocument.getUrl(), ".com/", "?");
        String fastDFSUrl = AmazonSpApiReportUtils.downloadAndUploadFastDFS(reportDocument.getUrl(), compressionAlgorithm, fileName, reportDocument.getReportDocumentId(), reportInfo.getReportType());

        // 更新报告信息
        reportInfo.setReportUrl(reportDocument.getUrl());
        reportInfo.setFilePath(fastDFSUrl);
        if (!dmpAmzReportInfoService.updateById(reportInfo)) {
            throw new ServiceException("更新报告信息失败:reportId=" + reportInfo.getReportId());
        }
        // 更新任务状态
        AmzReportTaskEntity newEntity = this.updateStatus(null, entity, AmzReportTaskStatusEnum.PARSE, null, null, LocalDateTime.now(), null, null, false);

        // 添加到延时队列4末端
        SendResult result = mqProducerService.syncClassMsgWithDelayLevel(
                RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
                RocketMqTagEnum.AMZ_REPORT_PARSE_TAG.getName(),
                newEntity,
                StrUtil.format("{}_{}", newEntity.getId(), newEntity.getStatus()),
                config.getParseDelayLevel());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new ServiceException(StrUtil.format("发送报告解析MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
        result.getMsgId();
    }

    @Override
    @DataIdempotent(keyIdName = "reportRedissonKey")
    public void consumerReportParse(String reportRedissonKey, AmzReportTaskEntity entity) {
        // 检查当前记录是否已完成或终止?
        AmzReportTaskEntity newQueryEntity = getByIdOpt(entity.getId()).orElseThrow(() -> new ServiceException("未找任务记录：id=" + entity.getId()));
        boolean hasFinishOrStop = AmzReportTaskStatusEnum.FINISH.getCode().equalsIgnoreCase(newQueryEntity.getStatus()) ||
                AmzReportTaskStatusEnum.STOP.getCode().equalsIgnoreCase(newQueryEntity.getStatus()) ||
                AmzReportTaskStatusEnum.MANUAL_STOP.getCode().equalsIgnoreCase(newQueryEntity.getStatus());
        if (hasFinishOrStop) {
            log.warn("步骤4：报告解析消费结束:任务已完成或终止, id={}", entity.getId());
            return;
        }

        if (!AmzReportTaskStatusEnum.PARSE.getCode().equalsIgnoreCase(entity.getStatus())) {
            String msg = StrUtil.format("任务状态非parse:id={}, status={}", entity.getId(), entity.getStatus());
            throw new ServiceException(msg);
        }

        // 查询报告信息
        DmpAmzReportInfoEntity reportInfo = dmpAmzReportInfoService.getByReportId(entity.getReportId(), Report.ProcessingStatusEnum.DONE.getValue());
        if (null == reportInfo) {
            throw new ServiceException("数据异常:未找到成功的报告信息: report=" + entity.getReportId());
        }

        // 查询报告配置map<报告列表名, mongo保存字段名>
        Map<String, String> columnMap = cfgAmzReportFieldService.mayByReportType(entity.getReportType());
        if (columnMap.isEmpty()) {
            throw new ServiceException("报告类型列表配置不存在, recordType=" + entity.getReportType());
        }

        // 查询报告类型配置
        CfgAmzReportTypeEntity config = cfgAmzReportTypeService.checkCountryAndGetByRecordType(entity);
        if (null == config) {
            this.stopByErrorMsg(entity, "根据报告类型配置当前市场不支持停止");
            return;
        }

        // 下载和解析文件内容
        String fullFileUrl = reportInfo.getFilePath();
        if (StringUtils.isBlank(reportInfo.getFilePath())) {
            throw new ServiceException("解析失败, 文件路径为空=" + reportInfo.getFilePath());
        }
        // 当前亚马逊账号
        String platformShopCode = entity.parsePlatformShopCode();

        // 从FastDFS下载后解析
        JSONArray jsonArray = AmazonSpApiReportUtils.downloadFromFastDFSAndParse(fullFileUrl, columnMap, entity.getReportType());

        // 当前报告类型
        AmazonReportRecordTypeEnum recordType = AmazonReportRecordTypeEnum.getByRecordType(entity.getReportType());
        // 根据报告类型获取解析的实体
        Class<? extends ReportSuperMongoDTO> mongoDTOClass = recordType.getAndCheckMongoDTOClass();
        // 解析对应报告内容
        List<? extends ReportSuperMongoDTO> mongoDTOList = JSONUtil.toList(jsonArray, mongoDTOClass);
        // 填充报告信息和生成唯一键
        List<? extends ReportSuperMongoDTO> allMongoDTOList = ReportSuperMongoDTO.fillReportData(mongoDTOList, reportInfo, recordType, platformShopCode, entity.getFirstMarketplace());
        // 跳过已解析的数量
        List<? extends ReportSuperMongoDTO> handleDTOList = allMongoDTOList.stream().skip(newQueryEntity.getParseRowIndex()).collect(Collectors.toList());
        // 按配置数量分组
        List<? extends List<? extends ReportSuperMongoDTO>> partitionList = Lists.partition(handleDTOList, config.getParseRowCount());

        // 记录每次解析数量
        AmzReportTaskEntity curTask = newQueryEntity;
        for (List<? extends ReportSuperMongoDTO> curList : partitionList) {
            // 记录首次解析开始时间
            if (0 == curTask.getParseRowIndex()) {
                curTask.setReportParseTime(LocalDateTime.now(ZoneId.systemDefault()));
            }
            // 保存mongo处理
            curTask = amzReportTaskService.saveMongoAndUpdateRowIndex(recordType.getMongoInfoEnum().getMongoTableName(), curList, curTask);
        }

        // 更新任务状态和完成时间
        amzReportTaskService.updateStatus(null, entity, AmzReportTaskStatusEnum.FINISH, null, null, null, null, LocalDateTime.now(ZoneId.systemDefault()), false);

        // 检查缓存是否已删除
        amzReportTaskService.checkAndDelHistory(entity);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataIdempotent(keyIdName = "reportRedissonKey")
    public void consumerReportDirectQuery(String reportRedissonKey, AmzReportTaskEntity entity) {
        // 检查当前记录是否已完成或终止?
        boolean hasFinishOrStop = this.checkFinishOrStop(entity.getId());
        if (hasFinishOrStop) {
            log.warn("步骤1/步骤2:报告直接查询消费结束:任务已完成或终止, id={}", entity.getId());
            return;
        }
        // 查询报告类型配置
        CfgAmzReportTypeEntity recordTypeConfig = cfgAmzReportTypeService.checkCountryAndGetByRecordType(entity);
        if (null == recordTypeConfig) {
            this.stopByErrorMsg(entity, "根据报告类型配置当前市场不支持停止");
            return;
        }

        if (!AmzReportTaskStatusEnum.DIRECT_QUERY.getCode().equalsIgnoreCase(entity.getStatus())) {
            String msg = StrUtil.format("任务状态非direct_query:id={}, status={}", entity.getId(), entity.getStatus());
            throw new ServiceException(msg);
        }
        // 查询最新成功的报告
        Report report = amzReportHandleService.directQueryAmzReportInfo(entity);
        if (null == report) {
            // 更新状态
            this.updateStatus(null, entity, AmzReportTaskStatusEnum.NULL_STOP, null, LocalDateTime.now(), null, null, null, false);
            return;
        }
        Report.ProcessingStatusEnum processingStatus = report.getProcessingStatus();

        // 查询结果异常
        if (!Report.ProcessingStatusEnum.DONE.equals(processingStatus)) {
            log.warn("报告结果异常：响应的报告非完成：{}", JSONUtil.toJsonStr(report));
            return;
        }
        // 检查报告是否已存在
        DmpAmzReportInfoEntity reportInfo = dmpAmzReportInfoService.getByReportId(report.getReportId(), null);
        if (null != reportInfo) {
            // 移除缓存
            this.checkAndDelHistory(entity);

            // 更新待下载状态
            this.updateStatus(report.getReportId(), entity, AmzReportTaskStatusEnum.EXIST_STOP, null, LocalDateTime.now(), null, null, null, false);
            return;
        }

        // 报告创建方式
        AmzReportCreatedMethodEnum createdMethodEnum = AmzReportCreatedMethodEnum.getBySubscribedType(recordTypeConfig.getSubscribedType());

        // 记录报告信息
        DmpAmzReportInfoEntity dmpAmzReportInfoEntity = DmpReportConverter.INSTANCE.newReportInfoEntity(report, entity, createdMethodEnum.getCode());
        if (!dmpAmzReportInfoService.save(dmpAmzReportInfoEntity)) {
            throw new ServiceException("保存报告信息失败");
        }

        // 报告创建成功
        // 更新待下载状态
        AmzReportTaskEntity newEntity = this.updateStatus(report.getReportId(), entity, AmzReportTaskStatusEnum.DOWNLOAD, null, LocalDateTime.now(), null, null, null, false);

        // 添加到延时队列3末端
        SendResult result = mqProducerService.syncClassMsgWithDelayLevel(
                RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
                RocketMqTagEnum.AMZ_REPORT_DOWNLOAD_TAG.getName(),
                newEntity,
                StrUtil.format("{}_{}", newEntity.getId(), newEntity.getStatus()),
                recordTypeConfig.getDownloadDelayLevel());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new ServiceException(StrUtil.format("直接查询发送报告下载MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    public List<AmzReportTaskEntity> findNotFinishOrStop(String shopId, String reportType, String id) {
        return lambdaQuery()
                .eq(AmzReportTaskEntity::getShopId, shopId)
                .eq(AmzReportTaskEntity::getReportType, reportType)
                .in(AmzReportTaskEntity::getStatus, AmzReportTaskStatusEnum.notFinishOrStopList())
                .ne(AmzReportTaskEntity::getId, id)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateErrorMsgAndCount(AmzReportTaskEntity entity, String errorMsg, Integer createdRetryCount, Integer queryRetryCount, Integer downloadRetryCount, Integer parseRetryCount) {
        boolean update = this.lambdaUpdate()
                .set(StringUtils.isNotBlank(errorMsg), AmzReportTaskEntity::getErrorMsg, errorMsg)
                .set(null != createdRetryCount, AmzReportTaskEntity::getCreatedRetryCount, createdRetryCount)
                .set(null != queryRetryCount, AmzReportTaskEntity::getQueryRetryCount, queryRetryCount)
                .set(null != downloadRetryCount, AmzReportTaskEntity::getDownloadRetryCount, downloadRetryCount)
                .set(null != parseRetryCount, AmzReportTaskEntity::getParseRetryCount, parseRetryCount)
                .set(AmzReportTaskEntity::getVersion, entity.getVersion() + 1)
                .eq(AmzReportTaskEntity::getId, entity.getId())
                .update();
        if (!update) {
            log.warn("更新亚马逊报告任务失败:entity={}", JSONUtil.toJsonStr(entity));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AmzReportTaskEntity updateStatus(String reportId, AmzReportTaskEntity entity,
                                            AmzReportTaskStatusEnum statusEnum,
                                            LocalDateTime reportCreatedTime,
                                            LocalDateTime reportQueryTime,
                                            LocalDateTime reportDownloadTime,
                                            LocalDateTime reportParseTime,
                                            LocalDateTime completedTime,
                                            Boolean addCreatedRetryCount
    ) {
        AmzReportTaskEntity oldEntity = this.getByIdOpt(entity.getId())
                .orElseThrow(() -> new ServiceException("未找到任务记录id" + entity.getId()));

        if (StringUtils.isNotBlank(reportId)) {
            oldEntity.setReportId(reportId);
        }
        oldEntity.setStatus(statusEnum.getCode());
        oldEntity.setStatusDesc(statusEnum.getName());
        if (AmzReportTaskStatusEnum.FINISH.equals(statusEnum)) {
            oldEntity.setErrorMsg("");
        }
        if (null != reportCreatedTime) {
            oldEntity.setReportCreatedTime(reportCreatedTime);
        }
        if (null != reportQueryTime) {
            oldEntity.setReportQueryTime(reportQueryTime);
        }
        if (null != reportDownloadTime) {
            oldEntity.setReportDownloadTime(reportDownloadTime);
        }
        if (null != reportParseTime) {
            oldEntity.setReportParseTime(reportParseTime);
        }
        if (null != completedTime) {
            oldEntity.setCompletedTime(completedTime);
        }
        if (null != addCreatedRetryCount && addCreatedRetryCount) {
            oldEntity.setCreatedRetryCount(oldEntity.getCreatedRetryCount() + 1);
        }
        boolean update = this.updateById(oldEntity);
        if (!update) {
            String msg = StrUtil.format("【AmzReportTaskEntity】更新状态失败：{}", JSONUtil.toJsonStr(entity));
            throw new ServiceException(msg);
        }
        return oldEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlerCheckReport(String groupId, List<ShopInfoEntity> shopList, Integer size, OffsetDateTime currentDateTime, List<AmzReportScheduleEntity> scheduleEntityList, Map<String, List<CfgAmzReportTypeEntity>> reportTypeConfigMap, Map<String, CfgAmzReportTypeEntity> reportTypeMap) {
        List<String> shopIds = shopList.stream().map(ShopInfoEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtil.isEmpty(scheduleEntityList)) {
            log.info("[检查最新【亚马逊报告】亚马逊-ERP] 当前线程任务结束,无需要更新的店铺报告计划={}", JSONUtil.toJsonStr(shopIds));
            XxlJobHelper.log("[检查最新【亚马逊报告】亚马逊-ERP] 当前线程任务结束,无需要更新的店铺报告计划={}", JSONUtil.toJsonStr(shopIds));
            return;
        }
        // 过滤获取当前店铺执行的任务
        List<AmzReportScheduleEntity> currentScheduleEntityList = scheduleEntityList.stream().filter(e -> shopIds.contains(e.getShopId())).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(currentScheduleEntityList)) {
            log.info("[检查最新【亚马逊报告】亚马逊-ERP] 当前线程任务结束,当前无需要更新的店铺报告计划: group={}, shopIds={}", groupId, JSONUtil.toJsonStr(shopIds));
            XxlJobHelper.log("[检查最新【亚马逊报告】亚马逊-ERP] 当前线程任务结束,当前无需要更新的店铺报告计划:: group={}, shopIds={}", groupId, JSONUtil.toJsonStr(shopIds));
            return;
        }

        // 分组
        // Map<店铺ID, List<计划任务>(取每一个分组的第一个) >>
        Map<String, List<AmzReportScheduleEntity>> scheduleGroup = currentScheduleEntityList
                .stream()
                .collect(Collectors.groupingBy(
                        AmzReportScheduleEntity::getShopId,
                        Collectors.collectingAndThen(Collectors.toList(), list -> groupByReportTypeGroup(list, reportTypeConfigMap))
                ));

        // 根据size指定每个分组执行的个数
        // 多线程?
        scheduleGroup.entrySet().stream().limit(size).forEach(entry -> {
            for (AmzReportScheduleEntity reportSchedule : entry.getValue()) {
                try {
                    // 当前类型报告处理中锁key
                    String reportRedissonKey = StrUtil.format(RedisCacheConstants.AMZ_REPORT_HANDLE_PREFIX, groupId);
                    // 只执行分组第一个
                    // 添加任务
                    this.checkTask(reportRedissonKey, groupId, reportSchedule, currentDateTime, reportTypeMap);
                    log.info("[检查最新【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 创建待请求任务成功: group={}, shopId={}, reportType={}",
                            groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
                    XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 创建待请求任务成功: group={}, shopId={}, reportType={}",
                            groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
                } catch (Exception e) {
                    log.error("[检查最新【亚马逊报告】亚马逊-ERP] 创建亚马逊报表计划失败：group={}, shopId={}, reportType={}, error={}",
                            groupId,
                            reportSchedule.getShopId(),
                            reportSchedule.getReportType(),
                            ExceptionUtil.stacktraceToString(e, 1000));
                    XxlJobHelper.log("[检查最新【亚马逊报告】亚马逊-ERP] 创建亚马逊报表计划失败：group={}, shopId={},reportType={}, error={}",
                            groupId,
                            reportSchedule.getShopId(),
                            reportSchedule.getReportType(),
                            ExceptionUtil.stacktraceToString(e, 1000)
                    );
                }
            }
        });
    }

    @Override
    @DataIdempotent(keyIdName = "reportRedissonKey", waitTime = 120)
    @Transactional(rollbackFor = Exception.class)
    public void checkTask(String reportRedissonKey, String groupId, AmzReportScheduleEntity reportSchedule, OffsetDateTime currentDateTime, Map<String, CfgAmzReportTypeEntity> reportTypeMap) {
        // 请求参数数据开始时间
        String reqDataStartTime = "";
        // 请求参数数据结束时间
        String reqDataEndTime = "";
        // 报告配置
        CfgAmzReportTypeEntity reportTypeConfig = reportTypeMap.get(reportSchedule.getReportType());
        if (null == reportTypeConfig) {
            throw new ServiceException("直接查询报告:未找到报告类型配置：" + reportSchedule.getReportType());
        }
        // 查询上一次记录
        AmzReportTaskEntity taskEntity = this.findLastTask(reportSchedule);
        // 是否是检查上一次执行任务
        if (reportTypeConfig.getHasPreTask() && null != taskEntity && !AmzReportTaskStatusEnum.FINISH.getCode().equalsIgnoreCase(taskEntity.getStatus()) && !AmzReportTaskStatusEnum.STOP.getCode().equalsIgnoreCase(taskEntity.getStatus())) {
        	// 上次任务未完成
            log.info("[检查最新【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 存在上一次未完成任务: group={}, shopId={}, reportType={}", groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
            XxlJobHelper.log("[检查最新【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 存在上一次未完成任务: group={}, shopId={}, reportType={}",
                    groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
            return;
        }
        // 是否是增量报告(取上一次执行任务的时间)
        if (!reportTypeConfig.getIsFullUpdate() && null != taskEntity) {
            if (!AmzReportTaskStatusEnum.FINISH.getCode().equalsIgnoreCase(taskEntity.getStatus()) && !AmzReportTaskStatusEnum.EXIST_STOP.getCode().equalsIgnoreCase(taskEntity.getStatus())) {
                // 上次任务未完成
                log.info("[检查最新【亚马逊报告】亚马逊-ERP]  当前线程任务结束, 增量报告：存在上一次未完成任务: group={}, shopId={}, reportType={}", groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
                XxlJobHelper.log("[检查最新【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 增量报告：存在上一次未完成任务:：group={}, shopId={}, reportType={}",
                        groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
                return;
            }
            // 查询对应报告
            DmpAmzReportInfoEntity amzReportInfo = dmpAmzReportInfoService.getByReportId(taskEntity.getReportId(), Report.ProcessingStatusEnum.DONE.getValue());
            if (null == amzReportInfo) {
                throw new ServiceException("未找到报告信息, reportId=" + taskEntity.getReportId());
            }
            // 解析时间
            reqDataStartTime = amzReportInfo.getDataEndTime();
        }
        // 更新下次计划任务下次执行时间
        amzReportScheduleService.updateNextTime(reportSchedule.getId(), reportTypeConfig);

        // 创建报告待请求记录
        AmzReportTaskEntity newTaskEntity = DmpReportConverter.INSTANCE.initDirectQueryTask(reportSchedule, groupId, reqDataStartTime, reqDataEndTime);
        if (!this.save(newTaskEntity)) {
            throw new ServiceException("保存直接查询报告记录失败");
        }
        // 添加到延时队列5末端
        SendResult result = mqProducerService.syncClassMsgWithDelayLevel(
                RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
                RocketMqTagEnum.AMZ_REPORT_DIRECT_QUERY_TAG.getName(),
                newTaskEntity,
                StrUtil.format("{}_{}", newTaskEntity.getId(), newTaskEntity.getStatus()),
                reportTypeConfig.getDirectQueryDelayLevel());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new ServiceException(StrUtil.format("发送报告直接查询MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    public void checkAndDelHistory(AmzReportTaskEntity entity) {
        // 检查缓存是否已删除
        String key = StrUtil.format(RedisCacheConstants.AMZ_REPORT_RESULT_PREFIX, entity.getId(), AmzReportTaskStatusEnum.CREATED.getCode());
        Object reportIdObj = redisUtil.get(key);
        if (null != reportIdObj) {
            redisUtil.del(key);
        }
        String queryKey = StrUtil.format(RedisCacheConstants.AMZ_REPORT_RESULT_PREFIX, entity.getId(), AmzReportTaskStatusEnum.QUERY.getCode());
        Object reportObj = redisUtil.get(queryKey);
        if (null != reportObj) {
            redisUtil.del(queryKey);
        }
        String directQueryKey = StrUtil.format(RedisCacheConstants.AMZ_REPORT_RESULT_PREFIX, entity.getId(), AmzReportTaskStatusEnum.DIRECT_QUERY.getCode());
        Object newReportObj = redisUtil.get(directQueryKey);
        if (null != newReportObj) {
            redisUtil.del(directQueryKey);
        }
    }

    @Override
    public void stopByErrorMsg(AmzReportTaskEntity entity, String errorMsg) {
        boolean update = this.lambdaUpdate()
                .set(AmzReportTaskEntity::getErrorMsg, errorMsg)
                .set(AmzReportTaskEntity::getStatus, AmzReportTaskStatusEnum.STOP.getCode())
                .set(AmzReportTaskEntity::getStatusDesc, AmzReportTaskStatusEnum.STOP.getName())
                .set(AmzReportTaskEntity::getVersion, entity.getVersion() + 1)
                .eq(AmzReportTaskEntity::getId, entity.getId())
                .update();
        if (!update) {
            log.warn("更新亚马逊报告任务失败:entity={}", JSONUtil.toJsonStr(entity));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public boolean checkStopByUnAuthorized(Exception exception, AmzReportTaskEntity entity) {
        boolean unAuthorized = AmazonSpApiExceptionUtils.isUnauthorized(exception);
        if (unAuthorized) {
            // 亚马逊授权异常处理
            this.stopByErrorMsg(entity, "亚马逊店铺被禁用或授权异常停止");
            // 禁用店铺和任务
            platformApiTaskService.checkAndClosedPlatformShopByShopId(entity.getShopId());
        }
        return unAuthorized;
    }

    @Override
    public void sendReportWarnMsg(AmzReportTaskEntity entity, String errorMsg) {
        //查询redis,预警8小时发送一次
        String existKey = StrUtil.format(RedisKeyConstant.DMP_PUSH_TASK_WARN, entity.getId());
        boolean isHas = redisUtil.hasKey(existKey);
        if (isHas) {
            return;
        } else {
            //添加缓存
            redisUtil.set(existKey, entity, RedisService.EIGHT_HOURS_CACHE_TIME);
        }
        SourceTypeEnum sourceTypeEnum = SourceTypeEnum.AMZ_REPORT_CONSUMER;
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(sourceTypeEnum.getName());
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        String title = StrUtil.format("亚马逊报告消费异常:【{}_{}】从{}拉取至{}失败", entity.getId(), entity.getStatus(), PlatformDictEnum.AMAZON.getName(), "自研ERP");
        warnMsgInfo.setTitle(title);
        warnMsgInfo.setTableName(sourceTypeEnum.getTableName());
        warnMsgInfo.setTableId(entity.getId());
        warnMsgInfo.setKeyInfo(errorMsg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkStopAndUpdateTask(AmzReportTaskEntity entity) {
        Map<SettingEnum, String> configMap = cfgSettingService.getMap(SettingEnum.AMAZON_REPORT);
        // 检查历史的失败次数
        String checkCountStr = configMap.getOrDefault(SettingEnum.AMAZON_REPORT_CHECK_COUNT, "2");
        int checkCount = Integer.parseInt(checkCountStr);
        // 任务停止次数
        String stopCountStr = configMap.getOrDefault(SettingEnum.AMAZON_REPORT_STOP_COUNT, "4");
        int stopCount = Integer.parseInt(stopCountStr);
        // 符合检查历史的失败次数
        if (entity.getCreatedRetryCount() >= checkCount && entity.getCreatedRetryCount() <= stopCount) {
            // 请求亚马逊接口:检查历史是否有成功记录
            Report report = amzReportHandleService.directQueryAmzReportInfo(entity);
            // 无 停止所有
            if (null == report) {
                // 当前任务停止
                this.stopByErrorMsg(entity, StrUtil.format("超过配置的最大创建检查失败{}次数停止,并且亚马逊无历史成功报告停止", stopCount));
                // 停止计划任务
                amzReportScheduleService.cancelById(entity.getMainId());
                return true;
            }
        }
        // 符合任务停止次数
        if (entity.getCreatedRetryCount() >= stopCount) {
            // 当前任务停止
            this.stopByErrorMsg(entity, StrUtil.format("超过配置的最大创建失败{}次数停止", stopCount));
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryTask(AmzReportTaskEntity entity) {
        if (!AmzReportTaskStatusEnum.notFinishOrStopList().contains(entity.getStatus())) {
            log.info("[重试【亚马逊报告】任务] 当前任务执行完毕, taskId={},非运行中状态:{}", entity.getId(), entity.getStatus());
            XxlJobHelper.log("[重试【亚马逊报告】任务] 当前任务执行完毕, taskId={},非运行中状态:{}", entity.getId(), entity.getStatus());
            return;
        }
        // 当前分组报告处理中锁key
        String reportRedissonKey = StrUtil.format(RedisCacheConstants.AMZ_REPORT_HANDLE_PREFIX, entity.getShopId(), entity.getReportType());

        AmzReportTaskStatusEnum taskStatusEnum = AmzReportTaskStatusEnum.getByCode(entity.getStatus());
        switch (taskStatusEnum) {
            case CREATED:
                // 报告创建处理
                this.consumerReportCreate(reportRedissonKey, entity);
                return;
            case QUERY:
                // 报告查询处理
                this.consumerReportQuery(reportRedissonKey, entity);
                return;
            case DOWNLOAD:
                this.consumerReportDownload(reportRedissonKey, entity);
                return;
            case PARSE:
                this.consumerReportParse(reportRedissonKey, entity);
                return;
            case DIRECT_QUERY:
                this.consumerReportDirectQuery(reportRedissonKey, entity);
                return;
            default:
        }

    }

    @Override
    public boolean stopRetryCount(Integer retryCount) {
        Map<SettingEnum, String> configMap = cfgSettingService.getMap(SettingEnum.AMAZON_REPORT);
        // 获取停止次数
        String stopCountStr = configMap.getOrDefault(SettingEnum.AMAZON_REPORT_STOP_COUNT, "4");
        int stopCount = Integer.parseInt(stopCountStr);
        return retryCount > stopCount;
    }


    /**
     * 根据报告类型组把任务分组
     */
    private List<AmzReportScheduleEntity> groupByReportTypeGroup(List<AmzReportScheduleEntity> entityList, Map<String, List<CfgAmzReportTypeEntity>> reportTypeConfigMap) {
        // 来源任务
        // 结果
        List<AmzReportScheduleEntity> resultList = new ArrayList<>();
        // 在 reportTypeConfigMap 中找到匹配的 reportGroup
        for (Map.Entry<String, List<CfgAmzReportTypeEntity>> entry : reportTypeConfigMap.entrySet()) {
            List<String> reportTypeList = entry.getValue().stream().map(CfgAmzReportTypeEntity::getReportType).collect(Collectors.toList());
            // 找出任意一个
            AmzReportScheduleEntity entity = entityList.stream().filter(e -> reportTypeList.contains(e.getReportType())).findFirst().orElse(null);
            if (null == entity) {
                continue;
            }
            resultList.add(entity);
        }

        return resultList;
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public <T extends MongoSuperDTO> AmzReportTaskEntity saveMongoAndUpdateRowIndex(String mongoTableName, List<T> mongoList, AmzReportTaskEntity currentEntity) {

        // 保存或更新mongo
        Class<T> tClass = (Class<T>) mongoList.get(0).getClass();

        // 区分新数据还是历史数据
        checkAndSetIsAddOrUpdate(mongoList, mongoTableName, tClass);

        List<T> insertList = new ArrayList<>();
        for (T item : mongoList) {
            MongoSuperDTO uniqueDto = MongoSuperDTO.getUniqId(item.getUniqueId());
            List<T> mongoData = mongoService.findMongoData(uniqueDto, 0, 0, mongoTableName, tClass);
            item.setDownloadTime(LocalDateTime.now());
            if (CollectionUtil.isEmpty(mongoData)) {
                insertList.add(item);
                continue;
            }
            T mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(item.toString())) {
                continue;
            }
            // 无指定字段更新所有
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(item), MapUtil.class);
            mongoService.updateMongoData(uniqueDto, mapUtil, mongoTableName, tClass);
        }
        if (CollectionUtil.isNotEmpty(insertList)) {
            mongoService.saveMongoDataMult(insertList, mongoTableName);
        }
        // 更新解析的行数
        currentEntity.setParseRowIndex(currentEntity.getParseRowIndex() + mongoList.size());
        if (!this.updateById(currentEntity)) {
            throw new ServiceException("[AmzReportTaskEntity] 更新解析的行数失败");
        }
        return currentEntity;
    }

    /**
     * 根据业务唯一ID判断数量是新增还是历史已有
     */
    private <T extends MongoSuperDTO> void checkAndSetIsAddOrUpdate(List<T> mongoList, String mongoTableName, Class<T> tClass) {
        if (mongoList.stream().allMatch(e -> e.getUniqueId().equalsIgnoreCase(e.getBusinessUniqueKey()))) {
            // 都是新增
            return;
        }
        List<String> businessKeyList = mongoList.stream().map(MongoSuperDTO::getBusinessUniqueKey).distinct().collect(Collectors.toList());
        Query query = new Query();
        Criteria criteria = Criteria.where("businessUniqueKey").in(businessKeyList);
        query.addCriteria(criteria);
        List<T> existBusinessList = mongoTemplate.find(query, tClass, mongoTableName);
        if (CollectionUtil.isEmpty(existBusinessList)) {
            // 不存在
            return;
        }
        List<String> existbusinessKeyList = existBusinessList.stream().map(MongoSuperDTO::getBusinessUniqueKey).distinct().collect(Collectors.toList());
        mongoList.forEach(e ->
                // 设置已存在的数据为非新增或更新
                e.setIsAddOrUpdate(!existbusinessKeyList.contains(e.getBusinessUniqueKey()))
        );
    }

}
