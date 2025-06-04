package com.erp.server.sys.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.constant.NoticeMsgConstant;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.enums.NoticeItemPeopleEnum;
import com.erp.model.sys.dto.ThirdNoticePushRecordDTO;
import com.erp.model.sys.entity.CfgThirdNoticeEntity;
import com.erp.model.sys.entity.SysPostUserEntity;
import com.erp.model.sys.entity.ThirdNoticePushRecordEntity;
import com.erp.model.sys.enums.CfgThirdNoticeMethodEnum;
import com.erp.model.sys.enums.ThirdNoticePushRecordNoticeTypeEnum;
import com.erp.model.sys.enums.ThirdNoticePushRecordStatusEnum;
import com.erp.model.sys.vo.SendThirdNoticeConsumerDTO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.enums.FmLogisticTrackStatusEnum;
import com.erp.model.workflow.enums.CfgApproveSyncSyncPlatformEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysPostFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.TmsFirstMileLogisticFeign;
import com.erp.rpc.tms.feign.TmsProductRegistrationFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
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
import java.time.format.DateTimeFormatter;
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

    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private TmsFirstMileLogisticFeign tmsFirstMileLogisticFeign;

    @Resource
    private SysPostFeign sysPostFeign;
    @Resource
    private TmsProductRegistrationFeign tmsProductRegistrationFeign;
    @Resource
    private WmsTaskFeign wmsTaskFeign;

    //------tms------
    //在途异常 com.erp.server.tms.schedule.FmLogisticWarnJob.sendFmLogisticWarnJob
    //备案通知 com.erp.server.tms.service.impl.ProductRegistrationServiceImpl.sendMsgWhenNotRegistration /com.erp.server.tms.schedule.ProductRegistrationJob.syncProductRegistrationInfo

    //------wms------
    //质检通知 com.erp.server.wms.schedule.CfgSettingJob.fsQcNotice

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
                //目前只支持3种单据
                String businessType = noticeEntity.getBusinessType();
                if(SourceTypeEnum.QC_INFO.getCode().equals(businessType)
                        || SourceTypeEnum.LOGISTICS_BILL.getCode().equals(businessType)
                        ||SourceTypeEnum.TRANSFER_LOGISTICS_CREATE_PRODUCT.getCode().equals(businessType)){
                }else {
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

                //通知标题
                String title = noticeEntity.getTitle();
                //通知主题
                StringBuffer sb = new StringBuffer();
                String noticeType = noticeEntity.getNoticeType();
                sb.append("通知类型：");
                sb.append(noticeType);
                sb.append("\n");
                String content = sb.toString();
                if (SourceTypeEnum.QC_INFO.getCode().equals(businessType)) {//质检单--质检通知
                    sendFsQcNotice(noticeEntity, roleType, specificPerson, title, now, content, delayLevel);
                } else if (SourceTypeEnum.LOGISTICS_BILL.getCode().equals(businessType)) { //物流单--在途异常
                    sendFmLogisticWarn(noticeEntity, roleType, specificPerson, title, now, content, delayLevel);
                } else if (SourceTypeEnum.TRANSFER_LOGISTICS_CREATE_PRODUCT.getCode().equals(businessType)) { //备案管理
                    sendWhenNotRegistration(noticeEntity, roleType, specificPerson, title, content, now, delayLevel);
                }
            }
        }
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====SendThirdNoticeJob 结束任务=====");
        return ReturnT.SUCCESS;
    }

    private void sendFsQcNotice(CfgThirdNoticeEntity noticeEntity, String roleType, String specificPerson, String title, LocalDateTime now, String content, int delayLevel) {
        List<String> userIdList = getPostUserList(roleType, specificPerson);
        if (CollUtil.isEmpty(userIdList)) {
            return;
        }
        title = wmsTaskFeign.getFsQcNoticeTitle(title);
        content = CharSequenceUtil.format(content,"质检通知", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        foreachSendByNoticeMethod(noticeEntity, userIdList, now, title, content, delayLevel);

    }

    private void sendWhenNotRegistration(CfgThirdNoticeEntity noticeEntity, String roleType, String specificPerson, String title, String content, LocalDateTime now, int delayLevel) {
        List<ProductRegistrationEntity> registrationEntities = tmsProductRegistrationFeign.listByRegistered();
        List<String> userIdList = getPostUserList(roleType, specificPerson);
        if (CollUtil.isEmpty(userIdList)) {
            return;
        }
        Map<String,List<ProductRegistrationEntity>> map = registrationEntities.stream().collect(Collectors.groupingBy(ProductRegistrationEntity::getDeclareSupplierName));
        for (Map.Entry<String, List<ProductRegistrationEntity>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<ProductRegistrationEntity> value = entry.getValue();
            title = CharSequenceUtil.format(title, key,value.size());
            List<String> skuNoList = value.stream().map(v->v.getSkuNo()).collect(Collectors.toList());
            int size = skuNoList.size();
            if(size<=10){
                content = content + "备案SKU："+ skuNoList;
            }else{
                skuNoList = skuNoList.subList(0,10);
                content = content + "备案SKU："+ skuNoList + "...+"+(size-10);
            }
            foreachSendByNoticeMethod(noticeEntity, userIdList, now, title, content, delayLevel);
        }
    }

    private void sendFmLogisticWarn(CfgThirdNoticeEntity noticeEntity, String roleType, String specificPerson, String title, LocalDateTime now, String content, int delayLevel) {
        List<TmsFirstMileLogisticDTO.PagingVO> pagingVOS = tmsFirstMileLogisticFeign.hasWarnPaging(new TmsFirstMileLogisticDTO.PagingParamDTO());
        pagingVOS = pagingVOS.stream().filter(v-> Objects.nonNull(v.getWarnHour()) && v.getWarnHour() < 0 && !FmLogisticTrackStatusEnum.SIGN.getCode().equals(v.getLogisticsStatus())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(pagingVOS)){
            List<String> userIdList = getPostUserList(roleType, specificPerson);
            if (CollUtil.isEmpty(userIdList)) {
                return;
            }
            //今日超期
            List<TmsFirstMileLogisticDTO.PagingVO> todayPagingVOS = pagingVOS.stream().filter(v-> v.getWarnHour() > -24).collect(Collectors.toList());
            //预警发送人员分为两部分，一部分是销售店铺负责人，一部分是抄送人
            //处理抄送人消息发送
            int totalWarnCount = pagingVOS.size();
            int todayCount = todayPagingVOS.size();
            title = CharSequenceUtil.format(title, totalWarnCount,todayCount);
            foreachSendByNoticeMethod(noticeEntity, userIdList, now, title, content, delayLevel);

            //处理店铺负责人消息推送
            List<String> shopIdList = pagingVOS.stream().map(TmsFirstMileLogisticDTO.PagingVO::getShopId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
            if(CollectionUtils.isEmpty(shopIdList)){
                return;
            }
            //封装负责人id
            List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIdList);
            for (TmsFirstMileLogisticDTO.PagingVO pagingVO : pagingVOS) {
                TmsFirstMileLogisticDTO.MsgDTO msgDTO = new TmsFirstMileLogisticDTO.MsgDTO();
                ShopInfoEntity shopInfoEntity = shopInfoEntityList.stream().filter(v->v.getId().equals(pagingVO.getShopId())).findFirst().orElse(null);
                if(Objects.nonNull(shopInfoEntity) && StringUtils.isNotBlank(shopInfoEntity.getChargeId())){
                    pagingVO.setChargeId(shopInfoEntity.getChargeId());
                }
            }
            pagingVOS = pagingVOS.stream().filter(v->StringUtils.isNotBlank(v.getChargeId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(pagingVOS)){
                return;
            }
            Map<String,List<TmsFirstMileLogisticDTO.PagingVO>> pagingMap = pagingVOS.stream().collect(Collectors.groupingBy(TmsFirstMileLogisticDTO.PagingVO::getChargeId));

            for (Map.Entry<String, List<TmsFirstMileLogisticDTO.PagingVO>> entry : pagingMap.entrySet()) {
                String chargeId = entry.getKey();
                List<TmsFirstMileLogisticDTO.PagingVO> value = entry.getValue();
                List<TmsFirstMileLogisticDTO.PagingVO> todayWarnByCharge = value.stream().filter(v-> v.getWarnHour() > -24).collect(Collectors.toList());
                int totalWarnCountByCharge = value.size();
                int todayCountByCharge = todayWarnByCharge.size();
                title = CharSequenceUtil.format(title, totalWarnCountByCharge,todayCountByCharge);
                foreachSendByNoticeMethod(noticeEntity, Collections.singletonList(chargeId), now, title, content, delayLevel);
            }
        }
    }

    private void foreachSendByNoticeMethod(CfgThirdNoticeEntity noticeEntity, List<String> userIdList, LocalDateTime now, String title, String content, int delayLevel) {
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


    /**
     * 获取系统设置的岗位人员(未去重)
     * @param
     * @return java.util.List<java.lang.String>
     * @author jack
     * @date 2025-05-30
     */
    private List<String> getPostUserList(String post,String specificPerson) {
        List<String> resultList = new ArrayList<>();

        //具体人员
        if(StringUtils.isNotBlank(specificPerson)){
            List<String> otherPeopleIds = Arrays.asList(specificPerson.split(","));
            resultList.addAll(otherPeopleIds);
        }

        if(StringUtils.isNotBlank(post)){
            List<String> postIdList = Arrays.asList(post.split(","));

            //岗位id
            List<SysPostUserEntity> userEntityList = sysPostFeign.getUserIdByPostIds(postIdList);
            if(CollectionUtils.isNotEmpty(userEntityList)){
                resultList.addAll(userEntityList.stream().map(SysPostUserEntity::getUserId).distinct().collect(Collectors.toList()));
            }
        }
        return resultList;
    }



    /**
     * 获取系统设置的店铺负责人(未去重)
     * @param
     * @return java.util.List<java.lang.String>
     * @author jack
     * @date 2025-05-30
     */
    private List<String> getShopChargeList(String roleType , List<String> shopIdList) {
        List<String> resultList = new ArrayList<>();
        if(StringUtils.isNotBlank(roleType)){
            List<String> itemPeopleList = Arrays.asList(roleType.split(","));
            //店铺负责人
            if (itemPeopleList.contains(NoticeItemPeopleEnum.SHOP_CHARGE.getFlag()) && CollUtil.isNotEmpty(shopIdList)) {
                //封装负责人id
                List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIdList);
                if (CollUtil.isNotEmpty(shopInfoEntityList)) {
                    shopInfoEntityList.stream().filter(v -> StringUtils.isNotBlank(v.getChargeId())).forEach(v -> resultList.add(v.getChargeId()));
                }
            }
        }
        return resultList;
    }
/*
    *//**
     * 获取系统设置的通知人员(未去重)
     * @param
     * @return java.util.List<java.lang.String>
     * @author jack
     * @date 2025-05-30
     *//*
    private List<String> getSetNotice(String roleType,
                                             String specificPerson,
                                             String createUserId,
                                             String projectChargeId ,
                                             String productChargeId,
                                             List<String> taskIdList ,
                                             ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList,
                                             List<String> shopIdList
    ) {
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
            if (itemPeopleList.contains(NoticeItemPeopleEnum.SHOP_CHARGE.getFlag()) && CollUtil.isNotEmpty(shopIdList)){
                //封装负责人id
                List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIdList);
                if(CollUtil.isNotEmpty(shopInfoEntityList)){
                    shopInfoEntityList.stream().filter(v->StringUtils.isNotBlank(v.getChargeId())).forEach(v->resultList.add(v.getChargeId()));
                }
            }

            //创建人
            if (itemPeopleList.contains(NoticeItemPeopleEnum.CREATOR.getFlag()) && StringUtils.isNotBlank(createUserId)){
                resultList.add(createUserId);
            }
        }
        return resultList;
    }*/


}
