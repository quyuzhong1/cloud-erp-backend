package com.erp.server.plm.service.impl;

import com.common.core.utils.ObjectUtils;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.entity.BusinessProcessEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.workflow.dto.ProcessNodeDTO;
import com.erp.model.workflow.dto.StartProcessDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.enums.BusinessProcessEnum;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.enums.TaskTypeEnum;
import com.erp.server.plm.service.BusinessProcessService;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.TaskOperateStrategy;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 开始任务
 *
 * @Classname
 * @Description TODO
 * @Date 2022-10-18 15:57
 * @Created by yl
 */
public class StartTask implements TaskOperateStrategy {

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private BusinessProcessService businessProcessService;





    /**
     * 开始任务
     *
     * @param taskIds
     * @param productId
     * @return
     */
    @Override
    @Transactional
    public Boolean updateTaskState(List<String> taskIds, String  productId,String userId) {
        //获取所有的任务列表
        List<ProjectTaskEntity> list = projectTaskService.getByTaskIds(taskIds);
        Integer ingCode = TaskStateEnum.ING.getCode();
        int size = list.stream().filter(t -> t.getStatus() > ingCode).collect(Collectors.toList()).size();
        if (size > 0) {
            throw new ServiceException(ApiError.ERROR_95031);
        }
        //一般任务
        Integer generalTaskCode = TaskTypeEnum.GENERAL_TASK.getCode();
        //审核任务
        Integer reviewTaskCode = TaskTypeEnum.REVIEW_TASK.getCode();

        //一般任务 列表  都是将任务状态改为进行中
        List<ProjectTaskEntity> generalTasks = list.stream().filter(t -> generalTaskCode.equals(t.getType())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(generalTasks)) {
            List<String> taskIdList = generalTasks.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            projectTaskService.updateTaskState(taskIdList, ingCode, new Date(), null);
        }

        /**
         * 审核任务要 启动流程 任务评审流程
         */
        List<ProjectTaskEntity> reviewList = list.stream().filter(t -> reviewTaskCode.equals(t.getType())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(reviewList)) {
            String businessType = BusinessProcessEnum.REVIEW_TASK.getBusinessType();
            BusinessProcessEntity processEntity = businessProcessService.getProcessByBusinessType(businessType);
            //该流程是 任务负责人会签审核的
            if (!Objects.isNull(processEntity)) {
                Date nowDate = new Date();
                Integer waitConfirmCode = TaskStateEnum.WAIT_CONFIRM.getCode();

                for (ProjectTaskEntity review : reviewList) {
                    String chargeId = review.getChargeId();
                    if (StringUtils.isNotBlank(chargeId)) {
                        StartProcessDTO start = new StartProcessDTO();
                        start.setBusinessKey(processEntity.getBusinessKey());
                        start.setProcessDefinitionKey(processEntity.getProcessDefinitionKey());
                        start.setUserId(userId);
                        Map<String, Object> parameterMap = new HashMap<>();
                        //taskChargeIds
                        parameterMap.put("taskChargeIds", Arrays.asList(chargeId.split(",")));
                        start.setParameterMap(parameterMap);
                        ProcessNodeDTO process = workflowFeign.startProcess(start);
                        //这个是流程Id
                        String processId = process.getProcessId();
                        //流程id 不为空 表示成功
                        if (StringUtils.isNotBlank(processId)) {
                            review.setProcessId(processId);

                        }
                        review.setRealityStartTime(nowDate);
                        review.setStatus(waitConfirmCode);
                        //更改 时间 很流程id
                        projectTaskService.updateById(review);

                    }
                }
            }


        }
        return true;
    }
}
