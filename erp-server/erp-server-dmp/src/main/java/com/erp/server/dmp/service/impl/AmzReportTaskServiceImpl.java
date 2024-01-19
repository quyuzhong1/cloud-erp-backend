package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.DataIdempotent;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.AmzReportTaskStatusEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.server.dmp.convert.DmpReportConverter;
import com.erp.server.dmp.mapper.AmzReportTaskMapper;
import com.erp.server.dmp.pull.service.mabang.MabangDeliveryDetailServiceImpl;
import com.erp.server.dmp.service.AmzReportInfoService;
import com.erp.server.dmp.service.AmzReportTaskService;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    private MQProducerService mQProducerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlerCreateReportTask(String groupKey, List<ShopInfoEntity> shopList, Integer size, OffsetDateTime currentDateTime, List<AmzReportScheduleEntity> scheduleEntityList, Map<String, List<CfgAmzReportTypeEntity>> reportTypeConfigMap, Map<String, CfgAmzReportTypeEntity> reportTypeMap) {
        List<String> shopIds = shopList.stream().map(ShopInfoEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtil.isEmpty(scheduleEntityList)) {
            log.info("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束,无需要更新的店铺报告计划={}", JSONUtil.toJsonStr(shopIds));
            XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束,无需要更新的店铺报告计划={}", JSONUtil.toJsonStr(shopIds));
            return;
        }
        // 过滤获取当前店铺执行的任务
        List<AmzReportScheduleEntity> currentScheduleEntityList = scheduleEntityList.stream().filter(e -> shopIds.contains(e.getShopId())).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(currentScheduleEntityList)) {
            log.info("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束,当前无需要更新的店铺报告计划: group={}, shopIds={}", groupKey, JSONUtil.toJsonStr(shopIds));
            XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束,当前无需要更新的店铺报告计划:: group={}, shopIds={}", groupKey, JSONUtil.toJsonStr(shopIds));
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
                    // 只执行分组第一个
                    // 添加任务
                    this.createTask(groupKey, reportSchedule, currentDateTime, reportTypeMap);
                    log.info("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 创建待请求任务成功: group={}, shopId={}, reportType={}",
                            groupKey, reportSchedule.getShopId(), reportSchedule.getReportType());
                    XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 创建待请求任务成功: group={}, shopId={}, reportType={}",
                            groupKey, reportSchedule.getShopId(), reportSchedule.getReportType());
                } catch (Exception e) {
                    log.error("[创建【亚马逊报告】亚马逊-ERP] 创建亚马逊报表计划失败：group={}, shopId={}, reportType={}, error={}",
                            groupKey,
                            reportSchedule.getShopId(),
                            reportSchedule.getReportType(),
                            ExceptionUtil.stacktraceToString(e, 1000));
                    XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 创建亚马逊报表计划失败：group={}, shopId={},reportType={}, error={}",
                            groupKey,
                            reportSchedule.getShopId(),
                            reportSchedule.getReportType(),
                            ExceptionUtil.stacktraceToString(e, 1000)
                    );
                }
            }
        });
    }

    @Override
    @DataIdempotent(keyIdName = "groupKey", waitTime = 60)
    @Transactional(rollbackFor = Exception.class)
    public void createTask(String groupKey, AmzReportScheduleEntity reportSchedule, OffsetDateTime currentDateTime, Map<String, CfgAmzReportTypeEntity> reportTypeMap) {
        // 请求参数数据开始时间
        LocalDateTime reqDataStartTime = null;
        // 请求参数数据结束时间
        LocalDateTime reqDataEndTime = null;
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
                log.info("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 存在上一次未完成任务: group={}, shopId={}, reportType={}", groupKey, reportSchedule.getShopId(), reportSchedule.getReportType());
                XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 存在上一次未完成任务: group={}, shopId={}, reportType={}",
                        groupKey, reportSchedule.getShopId(), reportSchedule.getReportType());
                return;
            }
        }
        // 是否是增量报告(取上一次执行任务的时间)
        if (!reportTypeConfig.getIsFullUpdate() && null != taskEntity) {
            if (!AmzReportTaskStatusEnum.FINISH.getCode().equalsIgnoreCase(taskEntity.getStatus())) {
                // 上次任务未完成
                log.info("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 增量报告：存在上一次未完成任务: group={}, shopId={}, reportType={}", groupKey, reportSchedule.getShopId(), reportSchedule.getReportType());
                XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 当前线程任务结束, 增量报告：存在上一次未完成任务:：group={}, shopId={}, reportType={}",
                        groupKey, reportSchedule.getShopId(), reportSchedule.getReportType());
                return;
            }
            // 查询对应报告
            AmzReportInfoEntity amzReportInfo = amzReportInfoService.getByReportId(taskEntity.getReportId(), Report.ProcessingStatusEnum.DONE.getValue());
            if (null == amzReportInfo) {
                throw new ServiceException("未找到报告信息, reportId=" + taskEntity.getReportId());
            }
            // TODO 解析时间
//            reqDataStartTime = amzReportInfo.getDataStartTime();
//            reqDataEndTime = amzReportInfo.getDataEndTime();
        }
        // 创建报告待请求记录
        AmzReportTaskEntity newTaskEntity = DmpReportConverter.INSTANCE.initScheduleEntityToTask(reportSchedule, reqDataStartTime, reqDataEndTime);
        if (!this.save(newTaskEntity)){
            throw new ServiceException("保存创建报告待请求记录失败");
        }
        // 添加到延时队列1末端
        SendResult result = mQProducerService.syncClassMsgWithDelayLevel(
                RocketMqTopic.AMZ_REPORT_TASK_TOPIC,
                RocketMqTagEnum.MABANG_DELIVERY_ORDER_TAG.getName(),
                newTaskEntity,
                newTaskEntity.getId(),
                3);
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
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
