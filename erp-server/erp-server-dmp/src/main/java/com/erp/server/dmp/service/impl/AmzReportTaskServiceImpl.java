package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.DataIdempotent;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.AmzReportCreatedMethodEnum;
import com.erp.model.dmp.enums.AmzReportTaskStatusEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportDocument;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiReportUtils;
import com.erp.server.dmp.convert.DmpReportConverter;
import com.erp.server.dmp.factory.AmzReportHandlerFactory;
import com.erp.server.dmp.mapper.AmzReportTaskMapper;
import com.erp.server.dmp.service.*;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private AmzReportInfoService amzReportInfoService;
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
        if (reportTypeConfig.getHasPreTask() && null != taskEntity) {
            if (!AmzReportTaskStatusEnum.FINISH.getCode().equalsIgnoreCase(taskEntity.getStatus()) && !AmzReportTaskStatusEnum.STOP.getCode().equalsIgnoreCase(taskEntity.getStatus())) {
                // 上次任务未完成
                log.info("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 存在上一次未完成任务: group={}, shopId={}, reportType={}", groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
                XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 存在上一次未完成任务: group={}, shopId={}, reportType={}",
                        groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
                return;
            }
        }
        // 是否是增量报告(取上一次执行任务的时间)
        if (!reportTypeConfig.getIsFullUpdate() && null != taskEntity) {
            if (!AmzReportTaskStatusEnum.FINISH.getCode().equalsIgnoreCase(taskEntity.getStatus())) {
                // 上次任务未完成
                log.info("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 增量报告：存在上一次未完成任务: group={}, shopId={}, reportType={}", groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
                XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 增量报告：存在上一次未完成任务:：group={}, shopId={}, reportType={}",
                        groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
                return;
            }
            // 查询对应报告
            AmzReportInfoEntity amzReportInfo = amzReportInfoService.getByReportId(taskEntity.getReportId(), Report.ProcessingStatusEnum.DONE.getValue());
            if (null == amzReportInfo) {
                throw new ServiceException("未找到报告信息, reportId=" + taskEntity.getReportId());
            }
            // 解析时间
            reqDataStartTime = amzReportInfo.getDataEndTime();
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
            throw new RuntimeException(StrUtil.format("发送创建报告待请求记录MQ数据异常，{}", JSONUtil.toJsonStr(result)));
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
                AmzReportTaskStatusEnum.MANUAL_STOP.getCode().equalsIgnoreCase(taskEntity.getStatus());
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
                throw new RuntimeException(StrUtil.format("发送查询报告待请求记录MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
            return;
        }
        // 报告创建方式
        AmzReportCreatedMethodEnum createdMethodEnum = AmzReportCreatedMethodEnum.getBySubscribedType(recordTypeConfig.getSubscribedType());

        // 记录报告信息
        AmzReportInfoEntity amzReportInfoEntity = DmpReportConverter.INSTANCE.newReportInfoEntity(report, entity, createdMethodEnum.getCode());
        if (!amzReportInfoService.save(amzReportInfoEntity)) {
            throw new ServiceException("保存报告信息失败");
        }

        // 报告创建失败
        if (Report.ProcessingStatusEnum.FATAL.equals(processingStatus)) {
            // 创建失败设置为待请求重新推送
            AmzReportTaskEntity newEntity = this.updateStatus("", entity, AmzReportTaskStatusEnum.CREATED, LocalDateTime.now(), null, null, null);

            // 处理中, 重推队列等待
            SendResult result = mqProducerService.syncClassMsgWithDelayLevel(
                    RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
                    RocketMqTagEnum.AMZ_REPORT_CREATE_TAG.getName(),
                    newEntity,
                    StrUtil.format("{}_{}", newEntity.getId(), newEntity.getStatus()),
                    recordTypeConfig.getCreatedDelayLevel());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                throw new RuntimeException(StrUtil.format("发送查询报告待请求记录MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
            return;
        }
        // 报告创建成功
        if (Report.ProcessingStatusEnum.DONE.equals(processingStatus)) {
            // 更新待下载状态
            AmzReportTaskEntity newEntity = this.updateStatus(report.getReportId(), entity, AmzReportTaskStatusEnum.DOWNLOAD, null, LocalDateTime.now(), null, null);

            // 添加到延时队列3末端
            SendResult result = mqProducerService.syncClassMsgWithDelayLevel(
                    RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
                    RocketMqTagEnum.AMZ_REPORT_DOWNLOAD_TAG.getName(),
                    newEntity,
                    StrUtil.format("{}_{}", newEntity.getId(), newEntity.getStatus()),
                    recordTypeConfig.getDownloadDelayLevel());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                throw new RuntimeException(StrUtil.format("发送报告下载MQ数据异常，{}", JSONUtil.toJsonStr(result)));
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
        CfgAmzReportTypeEntity config = cfgAmzReportTypeService.getByRecordType(entity.getReportType());
        if (null == config) {
            throw new ServiceException("未找到报告类型配置：recordType=" + entity.getReportType());
        }
        // 是否检查先前任务
        if (config.getHasPreTask()) {
            // 检查当前类型的历史是否有未完成的记录
            List<AmzReportTaskEntity> historyList = this.findNotFinishOrStop(entity.getShopId(), entity.getReportType());
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
        // 更新下次计划任务下次执行时间
        amzReportScheduleService.updateNextTime(entity.getMainId());

        // 请求创建报告
        String reportId = amzReportHandleService.createAmzReport(entity);

        // 更新任务状态
        AmzReportTaskEntity newEntity = this.updateStatus(reportId, entity, AmzReportTaskStatusEnum.QUERY, LocalDateTime.now(), null, null, null);

        // 添加到延时队列2末端
        SendResult result = mqProducerService.syncClassMsgWithDelayLevel(
                RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
                RocketMqTagEnum.AMZ_REPORT_QUERY_TAG.getName(),
                newEntity,
                StrUtil.format("{}_{}", newEntity.getId(), newEntity.getStatus()),
                config.getQueryDelayLevel());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送查询报告待请求记录MQ数据异常，{}", JSONUtil.toJsonStr(result)));
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
        CfgAmzReportTypeEntity config = cfgAmzReportTypeService.getByRecordType(entity.getReportType());
        if (null == config) {
            throw new ServiceException("未找到报告类型配置：recordType=" + entity.getReportType());
        }
        // 查询当前已有的报告信息
        AmzReportInfoEntity reportInfo = amzReportInfoService.getByReportId(entity.getReportId(), Report.ProcessingStatusEnum.DONE.getValue());
        if (null == reportInfo) {
            throw new ServiceException("数据异常:未找到成功的报告信息: report=" + entity.getReportId());
        }
        if (StringUtils.isBlank(reportInfo.getReportDocumentId())) {
            throw new ServiceException("数据异常:报告文档ID为空: report=" + entity.getReportId());
        }
        // 查询文档信息
        ReportDocument reportDocument = amzReportHandleService.queryAmzReportDocument(reportInfo, entity);

        // 根据url下载到FastDFS
        String compressionAlgorithm = null == reportDocument.getCompressionAlgorithm() ? "" : reportDocument.getCompressionAlgorithm().getValue();
        String fileName = StrUtil.subBetween(reportDocument.getUrl(), ".com/", "?");
        String fastDFSUrl = AmazonSpApiReportUtils.downloadAndUploadFastDFS(reportDocument.getUrl(), compressionAlgorithm, fileName, reportDocument.getReportDocumentId(), reportInfo.getReportType());

        // 更新报告信息
        reportInfo.setReportUrl(reportDocument.getUrl());
        reportInfo.setFilePath(fastDFSUrl);
        if (!amzReportInfoService.updateById(reportInfo)) {
            throw new ServiceException("更新报告信息失败:reportId=" + reportInfo.getReportId());
        }
        // 更新任务状态
        AmzReportTaskEntity newEntity = this.updateStatus(null, entity, AmzReportTaskStatusEnum.PARSE, null, null, LocalDateTime.now(), null);

        // 添加到延时队列4末端
        SendResult result = mqProducerService.syncClassMsgWithDelayLevel(
                RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
                RocketMqTagEnum.AMZ_REPORT_PARSE_TAG.getName(),
                newEntity,
                StrUtil.format("{}_{}", newEntity.getId(), newEntity.getStatus()),
                config.getParseDelayLevel());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送报告解析MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataIdempotent(keyIdName = "reportRedissonKey")
    public void consumerReportParse(String reportRedissonKey, AmzReportTaskEntity entity) {
        // 检查当前记录是否已完成或终止?
        boolean hasFinishOrStop = this.checkFinishOrStop(entity.getId());
        if (hasFinishOrStop) {
            log.warn("步骤4：报告解析消费结束:任务已完成或终止, id={}", entity.getId());
            return;
        }
        if (!AmzReportTaskStatusEnum.PARSE.getCode().equalsIgnoreCase(entity.getStatus())) {
            String msg = StrUtil.format("任务状态非parse:id={}, status={}", entity.getId(), entity.getStatus());
            throw new ServiceException(msg);
        }
        // 查询报告信息
        AmzReportInfoEntity reportInfo = amzReportInfoService.getByReportId(entity.getReportId(), Report.ProcessingStatusEnum.DONE.getValue());
        if (null == reportInfo) {
            throw new ServiceException("数据异常:未找到成功的报告信息: report=" + entity.getReportId());
        }
        // 查询报告配置map<报告列表名, mongo保存字段名>
        Map<String, String> columnMap = cfgAmzReportFieldService.mayByReportType(entity.getReportType());
        if (columnMap.isEmpty()) {
            throw new ServiceException("报告类型列表配置不存在, recordType=" + entity.getReportType());
        }

        // 更新任务状态
        AmzReportTaskEntity newEntity = this.updateStatus(null, entity, AmzReportTaskStatusEnum.FINISH, null, null, null, LocalDateTime.now());

        // 下载和解析文件内容
        String fullFileUrl = reportInfo.getFilePath();
        if (StringUtils.isBlank(reportInfo.getFilePath())) {
            throw new ServiceException("解析失败, 文件路径为空=" + reportInfo.getFilePath());
        }
        newEntity.setReqDataEndTime(reportInfo.getDataEndTime());
        // 从FastDFS下载后解析
        JSONArray jsonArray = AmazonSpApiReportUtils.downloadFromFastDFSAndParse(fullFileUrl, columnMap, newEntity.getReportType());
        // 业务处理
        AmzReportHandlerFactory.createHandler(reportInfo.getReportType())
                .businessHandler(newEntity, reportInfo, jsonArray);

        // 检查缓存是否已删除
        this.checkAndDelHistory(newEntity);
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
        // 报告类型配置
        CfgAmzReportTypeEntity recordTypeConfig = cfgAmzReportTypeService.getByRecordType(entity.getReportType());

        if (!AmzReportTaskStatusEnum.DIRECT_QUERY.getCode().equalsIgnoreCase(entity.getStatus())) {
            String msg = StrUtil.format("任务状态非direct_query:id={}, status={}", entity.getId(), entity.getStatus());
            throw new ServiceException(msg);
        }
        // 查询最新成功的报告
        Report report = amzReportHandleService.directQueryAmzReportInfo(entity);
        Report.ProcessingStatusEnum processingStatus = report.getProcessingStatus();

        // 查询结果异常
        if (!Report.ProcessingStatusEnum.DONE.equals(processingStatus)) {
            log.warn("报告结果异常：响应的报告非完成：{}", JSONUtil.toJsonStr(report));
            return;
        }
        // 检查报告是否已存在
        AmzReportInfoEntity reportInfo = amzReportInfoService.getByReportId(report.getReportId(), null);
        if (null != reportInfo){
            // 更新待下载状态
            this.updateStatus(report.getReportId(), entity, AmzReportTaskStatusEnum.EXIST_STOP, null, LocalDateTime.now(), null, null);
            return;
        }

        // 报告创建方式
        AmzReportCreatedMethodEnum createdMethodEnum = AmzReportCreatedMethodEnum.getBySubscribedType(recordTypeConfig.getSubscribedType());

        // 记录报告信息
        AmzReportInfoEntity amzReportInfoEntity = DmpReportConverter.INSTANCE.newReportInfoEntity(report, entity, createdMethodEnum.getCode());
        if (!amzReportInfoService.save(amzReportInfoEntity)) {
            throw new ServiceException("保存报告信息失败");
        }

        // 报告创建成功
        // 更新待下载状态
        AmzReportTaskEntity newEntity = this.updateStatus(report.getReportId(), entity, AmzReportTaskStatusEnum.DOWNLOAD, null, LocalDateTime.now(), null, null);

        // 添加到延时队列3末端
        SendResult result = mqProducerService.syncClassMsgWithDelayLevel(
                RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
                RocketMqTagEnum.AMZ_REPORT_DOWNLOAD_TAG.getName(),
                newEntity,
                StrUtil.format("{}_{}", newEntity.getId(), newEntity.getStatus()),
                recordTypeConfig.getDownloadDelayLevel());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("直接查询发送报告下载MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    public List<AmzReportTaskEntity> findNotFinishOrStop(String shopId, String reportType) {
        return lambdaQuery()
                .eq(AmzReportTaskEntity::getShopId, shopId)
                .eq(AmzReportTaskEntity::getReportType, reportType)
                .in(AmzReportTaskEntity::getStatus, AmzReportTaskStatusEnum.notFinishOrStopList())
                .list();
    }

    @Override
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
                                            LocalDateTime reportParseTime) {
        AmzReportTaskEntity oldEntity = this.getByIdOpt(entity.getId())
                .orElseThrow(() -> new ServiceException("未找到任务记录id" + entity.getId()));

        if (StringUtils.isNotBlank(reportId)) {
            oldEntity.setReportId(reportId);
        }
        oldEntity.setStatus(statusEnum.getCode());
        oldEntity.setStatusDesc(statusEnum.getName());
        if (AmzReportTaskStatusEnum.FINISH.equals(statusEnum)){
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
        if (reportTypeConfig.getHasPreTask() && null != taskEntity) {
            if (!AmzReportTaskStatusEnum.FINISH.getCode().equalsIgnoreCase(taskEntity.getStatus()) && !AmzReportTaskStatusEnum.STOP.getCode().equalsIgnoreCase(taskEntity.getStatus())) {
                // 上次任务未完成
                log.info("[检查最新【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 存在上一次未完成任务: group={}, shopId={}, reportType={}", groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
                XxlJobHelper.log("[检查最新【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 存在上一次未完成任务: group={}, shopId={}, reportType={}",
                        groupId, reportSchedule.getShopId(), reportSchedule.getReportType());
                return;
            }
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
            AmzReportInfoEntity amzReportInfo = amzReportInfoService.getByReportId(taskEntity.getReportId(), Report.ProcessingStatusEnum.DONE.getValue());
            if (null == amzReportInfo) {
                throw new ServiceException("未找到报告信息, reportId=" + taskEntity.getReportId());
            }
            // 解析时间
            reqDataStartTime = amzReportInfo.getDataEndTime();
        }
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
            throw new RuntimeException(StrUtil.format("发送报告直接查询MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        Object newReportObj = redisUtil.get(queryKey);
        if (null != newReportObj) {
            redisUtil.del(directQueryKey);
        }
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
}
