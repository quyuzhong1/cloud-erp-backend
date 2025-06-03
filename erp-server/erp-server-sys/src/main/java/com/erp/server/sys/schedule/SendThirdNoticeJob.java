package com.erp.server.sys.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.common.business.constant.ThirdConstants;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.common.business.validator.ValidList;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskFollowerEntity;
import com.erp.model.plm.enums.NoticeItemPeopleEnum;
import com.erp.model.sys.dto.ThirdNoticePushRecordDTO;
import com.erp.model.sys.entity.CfgThirdNoticeEntity;
import com.erp.model.sys.entity.ThirdNoticePushRecordEntity;
import com.erp.model.sys.enums.CfgThirdNoticeMethodEnum;
import com.erp.model.sys.enums.ThirdNoticePushRecordNoticeTypeEnum;
import com.erp.model.sys.enums.ThirdNoticePushRecordStatusEnum;
import com.erp.model.sys.vo.SendThirdNoticeConsumerDTO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.AuditorHandleDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.enums.CfgApproveSyncSyncPlatformEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.ProcessTaskManagementFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.sys.service.CfgThirdNoticeService;
import com.erp.server.sys.service.ThirdNoticePushRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.executor.CronExpression;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 售后申请单同步旺店通
 * @Author jack
 * @Date 2025-04-10
 **/
@Component
@Slf4j
@EnableScheduling
public class SendThirdNoticeJob {

    private String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");

    @Resource
    private FsService fsService;

    @Resource
    private CfgThirdNoticeService cfgThirdNoticeService;

    @Resource
    private ThirdNoticePushRecordService thirdNoticePushRecordService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private ProcessTaskManagementFeign processTaskManagementFeign;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private SysUserFeign sysUserFeign;

    //------tms------
    //在途异常 com.erp.server.tms.schedule.FmLogisticWarnJob.sendFmLogisticWarnJob
    //备案通知 com.erp.server.tms.service.impl.ProductRegistrationServiceImpl.sendMsgWhenNotRegistration

    //------wms------
    //质检通知 -新品 com.erp.server.wms.service.impl.QcResultServiceImpl.sendQcResultMsg
    //质检通知 -老品 com.erp.server.wms.service.impl.QcResultServiceImpl.sendQcResultMsg

    /**
     * 售后申请单同步旺店通
     * @Author jack
     * @Date 2025-04-10
     **/
    @XxlJob("SendThirdNoticeJob")
    public ReturnT<String> SendThirdNoticeJob() {
        XxlJobHelper.log("====SendThirdNoticeJob 开始任务=====");
        long start = System.currentTimeMillis();

        List<CfgThirdNoticeEntity> cfgThirdNoticeEntities = cfgThirdNoticeService.listByMethod(CfgThirdNoticeMethodEnum.SUMMARY.getCode());
        if(CollUtil.isNotEmpty(cfgThirdNoticeEntities)){
            LocalDateTime now = LocalDateTime.now();
            for (CfgThirdNoticeEntity noticeEntity : cfgThirdNoticeEntities) {
                String roleType = noticeEntity.getRoleType();
                String specificPerson = noticeEntity.getSpecificPerson();
                if (StringUtils.isBlank(roleType) && StringUtils.isBlank(specificPerson)) {
                    continue;
                }

                List<String> taskIdList = new ArrayList<>();
                ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList  = new ValidList<>();
                List<String> userIdList = getSetNotice(roleType, specificPerson, "","" ,"",taskIdList ,dtoList);
                if (CollUtil.isEmpty(userIdList)) {
                    continue;
                }

                //延迟等级
                int delayLevel = -1;
                String cron = noticeEntity.getCron();
                // 校验cron表达式
                boolean isValid = CronExpression.isValidExpression(cron);
                if(Boolean.FALSE.equals(isValid)){
                    //cron表达式不合法 记录错误日志
                    log.error("cron表达式不合法，Cron：{}", cron);
                    continue;
                }else {
                    //解析cron表达式，获取下次执行时间
                    try {
                        CronExpression cronExpression = new CronExpression(cron);
                        LocalDateTime nextExecutionTime = cronExpression.getNextValidTimeAfter(new Date()).toInstant()
                                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                        log.info("Cron表达式解析成功，下次执行时间为：{}", nextExecutionTime);

                        // 根据下次执行时间，发送延迟消息到RocketMQ
                        if (nextExecutionTime == null) continue;
                        Duration delay = Duration.between(now, nextExecutionTime);
                        delayLevel = mapDelayLevel(delay);
                        if (delayLevel == -1) continue;
                    } catch (Exception e) {
                        log.error("解析Cron表达式失败，Cron：{}", cron);
                        continue;
                    }
                }

                //根据通知方式查找人员 目前只有飞书
                String noticeMethod = noticeEntity.getNoticeMethod();
                if (StringUtils.isNotBlank(noticeMethod)) {
                    List<String> noticeMethodList = Arrays.asList(noticeMethod.split(","));
                    for (String str : noticeMethodList) {
                        //获取飞书的unionid 与用户关系
                        if (CfgApproveSyncSyncPlatformEnum.FEISHU.getCode().equals(str)) {
                            List<ThirdUnionDTO> unionList = sysUserFeign.getThirdByUserIds(ThirdpartyPlatformEnum.FS.getCode() , userIdList);
                            Map<String, ThirdUnionDTO> unionMap = unionList.stream().collect(Collectors.toMap(ThirdUnionDTO::getUserId, e -> e));

                            //根据用户id + businessType + noticeMethod + noticeType 判断是否已经在发送中。
                            ThirdNoticePushRecordDTO.ParamsDTO paramsDTO = new ThirdNoticePushRecordDTO.ParamsDTO();
                            paramsDTO.setBusinessType(noticeEntity.getBusinessType());
                            paramsDTO.setNoticeMethod(noticeMethod);
                            paramsDTO.setNoticeType(ThirdNoticePushRecordNoticeTypeEnum.MESSAGEPUSH.getCode());
                            paramsDTO.setStatus(ThirdNoticePushRecordStatusEnum.SENDING.getCode());
                            paramsDTO.setUserIds(userIdList);
                            paramsDTO.setSendTime(now);//只查询当天日期的
                            List<ThirdNoticePushRecordEntity> listSendingRecord = thirdNoticePushRecordService.listSendingRecord(paramsDTO);
                            Map<String, ThirdNoticePushRecordEntity> sendingMap = listSendingRecord.stream().collect(Collectors.toMap(ThirdNoticePushRecordEntity::getReceiverId, e -> e, (o1, o2) -> o1));

                            for (String userId : userIdList) {
                                //上一次的发送中，则跳过
                                if(sendingMap.containsKey(userId)){
                                    continue;
                                }
                                ThirdNoticePushRecordEntity recordEntity = new ThirdNoticePushRecordEntity();
                                recordEntity.setCfgThirdNoticeId(noticeEntity.getId());
                                recordEntity.setNoticeType(ThirdNoticePushRecordNoticeTypeEnum.MESSAGEPUSH.getCode());
                                recordEntity.setBusinessType(noticeEntity.getBusinessType());
                                recordEntity.setNoticeMethod(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
                                recordEntity.setReceiverId(userId);
                                if(unionMap.containsKey(userId) &&  StringUtils.isNotBlank(unionMap.get(userId).getUserName())){
                                    recordEntity.setReceiverName(unionMap.get(userId).getUserName());
                                }
                                recordEntity.setSendTime(now);
                                recordEntity.setTitle(noticeEntity.getTitle());
                                recordEntity.setStatus(ThirdNoticePushRecordStatusEnum.SENDING.getCode());
                                if(!(unionMap.containsKey(userId) &&  StringUtils.isNotBlank(unionMap.get(userId).getThirdUnionId()))){
                                    recordEntity.setStatus(ThirdNoticePushRecordStatusEnum.FAILED.getCode());
                                    recordEntity.setErrorReason("飞书未绑定");
                                }
                                boolean save = thirdNoticePushRecordService.save(recordEntity);
                                if(Boolean.TRUE.equals(save)){
                                    if(unionMap.containsKey(userId) &&  StringUtils.isNotBlank(unionMap.get(userId).getThirdUnionId())){
                                        String messageId = recordEntity.getId();
                                        SendThirdNoticeConsumerDTO sendMessage = new SendThirdNoticeConsumerDTO();
                                        sendMessage.setUnionIds(Collections.singletonList(unionMap.get(userId).getThirdUnionId()));
                                        //通知标题
                                        String title = noticeEntity.getTitle();
                                        //通知主题
                                        StringBuffer sb = new StringBuffer();
                                        String noticeType = noticeEntity.getNoticeType();
                                        sb.append("通知类型：");
                                        sb.append(noticeType);
                                        sb.append("\n");
                                        String content = sb.toString();
                                        //跳转URL
                                        String url = noticeEntity.getUrl();
                                        Map<String, Object> contentMap = fsService.getCardMessageMap(title, content, url);
                                        sendMessage.setContentMap(contentMap);
                                        sendMessage.setMessageId(messageId);
                                        mqProducerService.asyncClassMsgByDelayLevel(RocketMqTopic.SEND_THIRD_NOTICE_SYS_TOPIC, RocketMqTagEnum.SYS_SEND_THIRD_NOTICE_TAG.getName(), sendMessage, IdUtil.simpleUUID(), delayLevel);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====SendThirdNoticeJob 结束任务=====");
        return ReturnT.SUCCESS;
    }


    /**
     * 获取系统设置的通知人员(已去重)
     * @param
     * @return java.util.List<java.lang.String>
     * @author jack
     * @date 2025-05-30
     */
    private List<String> getSetNotice(String roleType,
                                             String specificPerson,
                                             String createUserId,
                                             String projectChargeId ,
                                             String productChargeId,
                                             List<String> taskIdList ,
                                             ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList) {
        List<String> resultList = new ArrayList<>();

        //具体人员
        if(StringUtils.isNotBlank(specificPerson)){
            List<String> otherPeopleIds = Arrays.asList(specificPerson.split(","));
            resultList.addAll(otherPeopleIds);
        }

        if(StringUtils.isNotBlank(roleType)){
            List<String> itemPeopleList = Arrays.asList(roleType.split(","));

            //这个是项目经理
            if (itemPeopleList.contains(NoticeItemPeopleEnum.ITEM_MANAGER.getFlag()) && StringUtils.isNotBlank(projectChargeId)) {
                List<String> projectChargeIdList = Arrays.asList(projectChargeId.split(","));
                resultList.addAll(projectChargeIdList);
            }

            //这个是产品经理
            if (itemPeopleList.contains(NoticeItemPeopleEnum.PRODUCT_MANAGER.getFlag()) && StringUtils.isNotBlank(productChargeId)) {
                List<String> productChargeIdList = Arrays.asList(productChargeId.split(","));
                resultList.addAll(productChargeIdList);
            }

            //这个是审核人
            if (itemPeopleList.contains(NoticeItemPeopleEnum.AUDITOR.getFlag())) {
                if(CollUtil.isNotEmpty(taskIdList)){//产品
                    List<ProjectTaskEntity> taskEntityList = plmTaskFeign.listProjectTaskByTaskIds(taskIdList);
                    if (CollectionUtils.isNotEmpty(taskEntityList)) {
                        for (ProjectTaskEntity taskEntity : taskEntityList) {
                            List<AuditorHandleDTO> historyTaskByProcessId = workflowFeign.getHistoryTaskByProcessId(taskEntity.getProcessId());
                            List<String> userIdList = historyTaskByProcessId.stream().map(AuditorHandleDTO::getHandleUserId).distinct().collect(Collectors.toList());
                            resultList.addAll(userIdList);
                        }
                    }
                }

                if(CollUtil.isNotEmpty(dtoList)){
                    ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.curApprover(dtoList);
                    if(Objects.nonNull(listApiResult) && CollUtil.isNotEmpty(listApiResult.getData())){
                        List<String> userIdList = listApiResult.getData().stream().map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                        resultList.addAll(userIdList);
                    }
                }
            }

            //这个是关注人
            if (itemPeopleList.contains(NoticeItemPeopleEnum.FOLLOWER.getFlag()) && CollectionUtils.isNotEmpty(taskIdList)) {
                List<TaskFollowerEntity> taskConcernEntities = plmTaskFeign.listTaskFollowerByTaskIds(taskIdList);
                if (CollectionUtils.isNotEmpty(taskConcernEntities)) {
                    List<String> userIdList = taskConcernEntities.stream().map(TaskFollowerEntity::getUserId).distinct().collect(Collectors.toList());
                    resultList.addAll(userIdList);
                }
            }

            //店铺负责人
            if (itemPeopleList.contains(NoticeItemPeopleEnum.SHOP_CHARGE.getFlag())){

            }

            //创建人
            if (itemPeopleList.contains(NoticeItemPeopleEnum.CREATOR.getFlag()) && StringUtils.isNotBlank(createUserId)){
                resultList.add(createUserId);
            }
        }
        return resultList.stream().distinct().collect(Collectors.toList());
    }


    //延迟等级映射
    public static int mapDelayLevel(Duration delay) {
        long seconds = delay.getSeconds();
        if (seconds <= 1) return 1;
        if (seconds <= 5) return 2;
        if (seconds <= 10) return 3;
        if (seconds <= 30) return 4;
        if (seconds <= 60) return 5;
        if (seconds <= 120) return 6;
        if (seconds <= 180) return 7;
        if (seconds <= 240) return 8;
        if (seconds <= 300) return 9;
        if (seconds <= 360) return 10;
        if (seconds <= 600) return 11;
        if (seconds <= 1200) return 12;
        return -1; // 超出最大延迟等级，忽略
    }

}
