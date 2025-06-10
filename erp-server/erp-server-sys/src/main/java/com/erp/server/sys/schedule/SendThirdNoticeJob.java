package com.erp.server.sys.schedule;

import com.erp.server.sys.service.ThirdNoticePushRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/**
 * 售后申请单同步旺店通
 * @Author jack
 * @Date 2025-04-10
 **/
@Component
@Slf4j
@EnableScheduling
public class SendThirdNoticeJob {

    @Resource
    private ThirdNoticePushRecordService thirdNoticePushRecordService;

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

        thirdNoticePushRecordService.sendThirdNoticeJob();

        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====SendThirdNoticeJob 结束任务=====");
        return ReturnT.SUCCESS;
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
