package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BaseStatusEnum;
import com.common.business.enums.SkuApproveConfigureEnum;
import com.common.business.enums.WorkflowBusinessEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.TaskRelationshipEnum;
import com.erp.model.plm.vo.*;
import com.erp.model.sys.dto.SysCalendarDTO;
import com.erp.model.sys.vo.SysCalendarListVO;
import com.erp.model.workflow.dto.*;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.model.workflow.vo.ProcessCurrentAuditorVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.ProjectPlanConstant;
import com.erp.server.plm.constant.SearchType;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.controller.AuditParamDTO;
import com.erp.server.plm.mapper.ProjectPlanMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目计划表(ProjectPlan)表服务实现类
 *
 * @author yl
 * @since 2023-02-03 15:14:29
 */
@Service
public class ProjectPlanServiceImpl extends ServiceImpl<ProjectPlanMapper, ProjectPlanEntity> implements ProjectPlanService {

    @Resource
    private ProjectTaskService taskService;

    @Resource
    private ProjectPlanTaskService projectPlanTaskService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CommonService commonService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private TaskDeliveryService taskDeliveryService;


    @Value("${pmoCharge}")
    private String pmoCharge;

    @Resource
    private NoticeMessageService noticeMessageService;

    @Resource
    private PreTaskService preTaskService;
    @Resource
    private ProjectTaskService projectTaskService;
    @Resource
    private TaskChargeDistributionService taskChargeDistributionService;

    /**
     * 提交项目计划
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-03 17:08
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submitSchedule(HandleTaskScheduleDTO dto) {
        List<String> taskIds = dto.getTaskIdList();
        if (CollectionUtils.isEmpty(taskIds)) {
            return false;
        }
        List<ProjectTaskEntity> taskList = taskService.getByTaskIds(taskIds);
        String productId = dto.getProductId();
        String userName = commonService.getUserInfo().getUserName();
        checkAuditor();
        if (CollectionUtils.isNotEmpty(taskIds)) {
            checkTaskTime(taskList);
            checkTaskStatus(taskList);

            //检查任务审核人不能为空
            checkTaskAuditor(taskIds);
        }
        String phaseName = "立项阶段";
        long approvalTaskCount = taskList.stream().filter(t -> phaseName.equals(t.getPhaseName())).count();
        long projectTaskCount = taskList.stream().filter(t -> !phaseName.equals(t.getPhaseName())).count();
        String phase = "all";
        if (approvalTaskCount > 0 && projectTaskCount == 0) {
            phase = "projectApproval";
        }
        if (approvalTaskCount == 0 && projectTaskCount > 0) {
            phase = "project";
        }
        ProjectPlanEntity projectPlan = new ProjectPlanEntity();
        String id = IdWorker.getIdStr();
        projectPlan.setProductId(productId);
        projectPlan.setType("initial");
        projectPlan.setTaskQuantity(dto.getTaskIdList().size());
        projectPlan.setPhase(phase);
        projectPlan.setId(id);
        Boolean saveResult = this.save(projectPlan);
        if (saveResult) {
            projectPlanTaskService.savePlanTask(id, dto.getProductId(), taskList);
            //发起流程啊
            startScheduleTaskProcess(id);
            //给第一个人发信息
            noticeMessageService.scheduleTaskAuditor(userName, taskList, productId, Arrays.asList(pmoCharge));

        }
        //异步发送消息
        noticeMessageService.scheduleTaskSubmit(userName, taskList, productId);
        return saveResult;
    }


    /**
     * 检查任务审核人不能为空
     *
     * @param taskIdList
     */
    private void checkTaskAuditor(List<String> taskIdList) {
        if (CollectionUtils.isNotEmpty(taskIdList)) {
            Integer source = MathUtil.THREE;
            //这个是任务列表 对应的审核人
            List<TaskChargeDistributionEntity> distributionList = taskChargeDistributionService.listBySourceAndTaskIdList(source, taskIdList);
            //这个是获取任务是不是有 审核人 大于0 就是没有
            long count = distributionList.stream().filter(d -> StringUtils.isBlank(d.getChargeIds())).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_95148);
            }
        }
    }


    /**
     * 检查任务状态
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-02-08 17:37
     */
    private void checkTaskStatus(List<ProjectTaskEntity> taskList) {
        List<String> statusList = new ArrayList<>(2);
        String waitSubmit = BaseStatusEnum.WAIT_SUBMIT.getStatus();
        String cancel = BaseStatusEnum.CANCEL.getStatus();
        statusList.add(waitSubmit);
        statusList.add(cancel);
        long count = taskList.stream().filter(t -> !statusList.contains(t.getScheduleStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95116);
        }
    }


    /**
     * 检查任务计划时间是否设置
     *
     * @param taskList
     * @return void
     * @author yl
     * @date 2023-02-08 17:31
     */
    public void checkTaskTime(List<ProjectTaskEntity> taskList) {
        long count = taskList.stream().
                filter(t -> Objects.isNull(t.getPlanEndTime()) || Objects.isNull(t.getPlanStartTime())).
                count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95115);
        }
        long blankChargeIdCount = taskList.stream().filter(t -> StringUtils.isBlank(t.getChargeId())).count();
        if (blankChargeIdCount > 0) {
            throw new ServiceException(ApiError.ERROR_95097);
        }
    }

    /**
     * 取消排期
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-03 17:35
     */
    @Override
    public Boolean cancelSchedule(List<String> ids) {
        List<ProjectPlanEntity> planList = this.getByIds(ids);
        if (CollectionUtils.isEmpty(planList)) {
            throw new ServiceException(ApiError.ERROR_95122);
        }
        List<String> statusList = planList.stream().map(ProjectPlanEntity::getStatus).collect(Collectors.toList());
        String waitAudit = BaseStatusEnum.WAIT_AUDIT.getStatus();
        if (!statusList.contains(waitAudit)) {
            throw new ServiceException(ApiError.ERROR_95121);
        }
        String userId = commonService.getUserInfo().getUid();
        //变更
        String change = ProjectPlanConstant.PROJECT_PLAN_CHANGE;
        //初次
        String initial = ProjectPlanConstant.PROJECT_PLAN_INITIAL;

        /**
         *  这个是初次提交 且是待审核的
         *  取消后那么它的状态该成 取消
         *  然后任务状态 也是已取消
         */
        List<ProjectPlanEntity> initialList = planList.stream().filter(p ->
                initial.equals(p.getType()) && waitAudit.equals(p.getStatus())).collect(Collectors.toList());

        Boolean initialResult = initialPlanCancel(initialList, userId);

        /**
         * 这个是变更的 状态为待提交
         * @author yl
         * @date 2023-02-27 19:18
         * @param ids
         * @return java.lang.Boolean
         */
        List<ProjectPlanEntity> changeList = planList.stream().filter(p ->
                change.equals(p.getType()) && waitAudit.equals(p.getStatus())).collect(Collectors.toList());

        Boolean changeResult = changePlanCancel(changeList, userId);
        return initialResult && changeResult;
    }

    /**
     * 初始排期 是待审核的取消
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-02-27 19:05
     */
    public Boolean initialPlanCancel(List<ProjectPlanEntity> planList, String userId) {
        if (CollectionUtils.isNotEmpty(planList)) {
            //初次
            String initial = ProjectPlanConstant.PROJECT_PLAN_INITIAL;
            String cancelStatus = BaseStatusEnum.CANCEL.getStatus();
            planList.forEach(plan ->
                    plan.setStatus(cancelStatus)
            );
            List<String> ids = planList.stream().map(ProjectPlanEntity::getId).collect(Collectors.toList());
            WithDrawProcessBusinessDTO withDrawProcess = new WithDrawProcessBusinessDTO();
            withDrawProcess.setUserId(userId);
            withDrawProcess.setBusinessTableIdList(ids);
            Boolean result = workflowFeign.withDrawByBusiness(withDrawProcess);
            if (result) {
                this.updateBatchById(planList);
                String productId = planList.get(0).getProductId();
                List<ProjectPlanTaskEntity> initialTaskList = projectPlanTaskService.getByProjectPlanIdList(ids);
                List<String> initialTaskIds = initialTaskList.stream().map(ProjectPlanTaskEntity::getTaskId).collect(Collectors.toList());
                //更改任务状态
                taskService.updateScheduleStatus(productId, initialTaskIds, cancelStatus, initial);
            }

            return result;
        }
        return true;

    }


    /**
     * 变更排期 是待审核的取消
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-02-27 19:05
     */
    public Boolean changePlanCancel(List<ProjectPlanEntity> planList, String userId) {
        if (CollectionUtils.isNotEmpty(planList)) {
            //变更
            String change = ProjectPlanConstant.PROJECT_PLAN_CHANGE;
            String auditPass = BaseStatusEnum.AUDIT_PASS.getStatus();
            planList.forEach(plan ->
                    plan.setStatus(BaseStatusEnum.CANCEL.getStatus())
            );
            List<String> ids = planList.stream().map(ProjectPlanEntity::getId).collect(Collectors.toList());
            WithDrawProcessBusinessDTO withDrawProcess = new WithDrawProcessBusinessDTO();
            withDrawProcess.setUserId(userId);
            withDrawProcess.setBusinessTableIdList(ids);
            Boolean result = workflowFeign.withDrawByBusiness(withDrawProcess);
            if (result) {
                this.updateBatchById(planList);
                String productId = planList.get(0).getProductId();
                List<ProjectPlanTaskEntity> changeTaskList = projectPlanTaskService.getByProjectPlanIdList(ids);
                List<String> changeTaskIds = changeTaskList.stream().map(ProjectPlanTaskEntity::getTaskId).collect(Collectors.toList());
                //更改任务状态
                taskService.updateScheduleStatus(productId, changeTaskIds, auditPass, change);
            }
            return result;
        }

        return true;

    }


    /**
     * 根据表id 获取实体
     *
     * @param projectPlanIds
     * @return java.util.List<com.erp.model.plm.entity.ProjectPlanEntity>
     * @author yl
     * @date 2023-02-09 11:15
     */
    @Override
    public List<ProjectPlanEntity> getByIds(List<String> projectPlanIds) {
        if (CollectionUtils.isNotEmpty(projectPlanIds)) {
            LambdaQueryWrapper<ProjectPlanEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ProjectPlanEntity::getId, projectPlanIds);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();

    }

    /**
     * 排期详情
     *
     * @param id
     * @return com.erp.model.plm.vo.ProjectPlanDetailsVO
     * @author yl
     * @date 2023-02-09 17:07
     */
    @Override
    public ProjectPlanDetailsVO details(String id) {
        ProjectPlanDetailsVO vo = new ProjectPlanDetailsVO();
        ProjectPlanEntity plan = this.getById(id);
        if (Objects.isNull(plan)) {
            throw new ServiceException(ApiError.ERROR_95122);
        }
        String productId = plan.getProductId();
        vo.setProductId(productId);
        Map<String, Object> productMap = taskService.getProductMapByProductId(productId);
        vo.setProductName(productMap.get("productName").toString());
        vo.setTotalTaskCount(Integer.valueOf(productMap.get("taskCount").toString()));
        //获取任务
        List<ProjectPlanTaskEntity> planTaskList = projectPlanTaskService.getByProjectPlanIdList(Arrays.asList(id));
        if (CollectionUtils.isEmpty(planTaskList)) {
            vo.setTaskList(new ArrayList<>());
            return vo;
        }

        //交付文档列表
        List<TaskDeliveryDocsEntity> deliveryDocsList = taskDeliveryService.getByProductId(productId);

        List<String> taskIdList = planTaskList.stream().map(ProjectPlanTaskEntity::getTaskId).collect(Collectors.toList());

        //最小计划开始时间
        Date minStartTime = planTaskList.stream().filter(p -> p.getChangeStartTime() != null).min(Comparator.comparing(ProjectPlanTaskEntity::getChangeStartTime)).map(ProjectPlanTaskEntity::getChangeStartTime).get();
        //最大计划结束时间
        Date maxEndTime = planTaskList.stream().filter(obj -> obj.getChangeEndTime() != null).max(Comparator.comparing(ProjectPlanTaskEntity::getChangeEndTime)).map(ProjectPlanTaskEntity::getChangeEndTime).get();
        String fmt = DateUtil.fmt_day;
        vo.setScheduleStartTine(DateUtil.conversionDate(minStartTime, fmt));
        vo.setScheduleEndTine(DateUtil.conversionDate(maxEndTime, fmt));
        //相差多少天
        Integer durationDay = DateUtil.getDiffDay(minStartTime, maxEndTime) + 1;
        vo.setDurationDay(durationDay);
        vo.setWaitAuditTaskCount(planTaskList.size());

        List<ScheduleTaskDetailsVO> taskList = new ArrayList<>(20);

        List<FindUserDTO> userList = sysUserFeign.getUserList();
        /**
         * 获取是变更的任务
         */
        List<ScheduleTaskDetailsVO> changeTaskList = projectPlanTaskService.getTaskByPlanType(productId, ProjectPlanConstant.PROJECT_PLAN_CHANGE);
        List<String> changeTaskIdList = changeTaskList.stream().map(ScheduleTaskDetailsVO::getTaskId).collect(Collectors.toList());
        taskIdList.addAll(changeTaskIdList);
        //获取对应的任务
        List<ProjectTaskEntity> taskEntityList = taskService.getByTaskIds(taskIdList);
        Map<String, List<PreTaskVO>> preTaskGourpTaskIdMap = MapUtil.empty();
        if (CollectionUtil.isNotEmpty(taskEntityList)) {
            preTaskGourpTaskIdMap = preTaskService.listByTaskIds(taskEntityList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList()));
        }
        for (ProjectPlanTaskEntity item : planTaskList) {
            ScheduleTaskDetailsVO task = new ScheduleTaskDetailsVO();
            String chargeId = item.getChangeChargeId();
            String taskId = item.getTaskId();
            String taskName = taskEntityList.stream().filter(t -> t.getId().equals(taskId)).findFirst().
                    flatMap(data -> Optional.ofNullable(data.getName())).orElse("");

            task.setTaskName(taskName);
            task.setChargeId(item.getChangeChargeId());
            task.setChargeName(getNameByIds(chargeId, userList));
            task.setPlanEndTime(item.getChangeEndTime());
            task.setPlanStartTime(item.getChangeStartTime());
            task.setTaskId(taskId);
            task.setId(IdWorker.getIdStr());
            task.setWorkPeriod(item.getWorkPeriod());
            List<String> docsNameList = deliveryDocsList.stream().filter(d -> d.getTaskId().equals(taskId))
                    .map(TaskDeliveryDocsEntity::getDocsName).collect(Collectors.toList());
            task.setDeliveryDocsNames(String.join(",", docsNameList));
            task.setPreTaskList(preTaskGourpTaskIdMap.get(task.getId()));


            List<ScheduleTaskDetailsVO> historyList = changeTaskList.stream().filter(c -> c.getTaskId().equals(taskId)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(historyList)) {
                for (ScheduleTaskDetailsVO hi : historyList) {
                    String hiChargeId = hi.getChargeId();
                    hi.setChargeName(getNameByIds(hiChargeId, userList));

                    String hiTaskName = taskEntityList.stream().filter(t -> t.getId().equals(hi.getTaskId())).findFirst().
                            flatMap(data -> Optional.ofNullable(data.getName())).orElse("");
                    hi.setTaskName(hiTaskName);
                    hi.setId(IdWorker.getIdStr());

                    List<String> docsList = deliveryDocsList.stream().filter(d -> d.getTaskId().equals(taskId))
                            .map(TaskDeliveryDocsEntity::getDocsName).collect(Collectors.toList());
                    hi.setDeliveryDocsNames(String.join(",", docsList));
                }
            }
            task.setHistoryList(historyList);
            taskList.add(task);
        }


        vo.setTaskList(taskList);
        return vo;
    }


    /**
     * 排期审核分页
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO<java.util.List < com.erp.model.plm.vo.SchedulePagingVO>>
     * @author yl
     * @date 2023-02-10 9:15
     */
    @Override
    public PagingVO<List<SchedulePagingVO>> paging(PagingDTO<SearchPagingDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        SearchPagingDTO params = dto.getParams();
        params.setParam(dto.getParam());
        String searchType = params.getSearchType();
        List<String> idList = new ArrayList<>();
        List<String> statusList = new ArrayList<>();
        //待审核
        if (SearchType.WAIT_AUDIT.equals(searchType)) {
            String userId = commonService.getUserInfo().getUid();
            //获取我的待办信息
            List<MyToDoTaskVO> myToDoTasks = workflowFeign.getMyToDoTasks(userId);
            idList = myToDoTasks.stream().map(MyToDoTaskVO::getBusinessTableId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(idList)) {
                IPage pageData = new Page();
                return new PagingVO(pageData);
            }
            statusList.add(BaseStatusEnum.WAIT_AUDIT.getStatus());
            statusList.add(BaseStatusEnum.AUDIT_ING.getStatus());
        }
        IPage pageData = baseMapper.paging(query, params, idList, statusList);
        List<SchedulePagingVO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        for (SchedulePagingVO item : list) {
            item.setStatusName(BaseStatusEnum.getName(item.getStatus()));
        }


        return new PagingVO(pageData);
    }


    public String getNameByIds(String userId, List<FindUserDTO> userList) {
        if (StringUtils.isNotBlank(userId)) {
            List<String> names = new ArrayList<>();
            String[] userIds = userId.split(",");
            for (String user : userIds) {
                FindUserDTO findUser = userList.stream().filter(u -> user.equals(u.getUserId())).findFirst().orElse(null);
                if (findUser != null) {
                    names.add(findUser.getUserName());
                } else {
                    names.add("");
                }
            }
            return StringUtils.join(names, ",");
        }
        return "";
    }

    /**
     * 重启提交
     * 审核不通过重新提交
     *
     * @param id
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-03 17:35
     */
    @Override
    @Transactional
    public Boolean restartSchedule(String id) {
        ProjectPlanEntity plan = this.getById(id);
        if (Objects.isNull(plan)) {
            throw new ServiceException(ApiError.ERROR_95122);
        }
        String status = plan.getStatus();
        if (!BaseStatusEnum.AUDIT_NO_PASS.getStatus().equals(status)) {
            throw new ServiceException(ApiError.ERROR_95119);
        }

        checkAuditor();
        String waitAuditStatus = BaseStatusEnum.WAIT_AUDIT.getStatus();
        plan.setStatus(waitAuditStatus);
        Boolean result = this.updateById(plan);
        if (result) {
            //这里要发起流程
            startScheduleTaskProcess(id);
            String productId = plan.getProductId();
            List<ProjectPlanTaskEntity> taskList = projectPlanTaskService.getByProjectPlanIdList(Arrays.asList(id));
            projectPlanTaskService.updateTaskInfo(Arrays.asList(id));
            List<String> taskIds = taskList.stream().map(ProjectPlanTaskEntity::getTaskId).collect(Collectors.toList());
            //更改任务状态
            taskService.updateScheduleStatus(productId, taskIds, waitAuditStatus, plan.getType());
        }

        return result;

    }


    /**
     * 变更排期
     * 添加一个变更流程 保存数据
     *
     * @param
     * @return
     */
    @Override
    public Boolean changeSchedule(String productId, List<ChangeTaskScheduleDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        //检查审核人
        checkAuditor();
        List<String> taskIds = list.stream().map(ChangeTaskScheduleDTO::getTaskId).collect(Collectors.toList());
        List<ProjectTaskEntity> taskList = taskService.getByTaskIds(taskIds);
        long approvalTaskCount = taskList.stream().filter(t -> TaskConstant.APPROVAL_TASK.equals(t.getProperty())).count();
        long projectTaskCount = taskList.stream().filter(t -> TaskConstant.PROJECT_TASK.equals(t.getProperty())).count();
        String phase = "all";
        if (approvalTaskCount > 0 && projectTaskCount == 0) {
            phase = "projectApproval";
        }
        if (approvalTaskCount == 0 && projectTaskCount > 0) {
            phase = "project";
        }
        ProjectPlanEntity projectPlan = new ProjectPlanEntity();
        String id = IdWorker.getIdStr();
        projectPlan.setProductId(productId);
        String type = ProjectPlanConstant.PROJECT_PLAN_CHANGE;
        projectPlan.setType(type);
        projectPlan.setTaskQuantity(taskIds.size());
        projectPlan.setPhase(phase);
        projectPlan.setId(id);
        Boolean saveResult = this.save(projectPlan);
        if (saveResult) {
            projectPlanTaskService.saveChangePlanTask(id, productId, taskList, list);

            //发起流程
            startScheduleTaskProcess(id);

            String userName = commonService.getUserInfo().getUserName();
            //给第一个人发信息
            noticeMessageService.scheduleTaskAuditor(userName, taskList, productId, Arrays.asList(pmoCharge));

            //更改任务状态
            taskService.updateScheduleStatus(productId, taskIds, BaseStatusEnum.WAIT_AUDIT.getStatus(), type);

        }
        return saveResult;

    }


    /**
     * 启动一个流程
     *
     * @param id
     * @return
     * @author yl
     * @date 2023-02-11 10:04
     */

    @Override
    public void startScheduleTaskProcess(String id) {
        FindProcessDTO findProcess = new FindProcessDTO();
        String userId = commonService.getUserInfo().getUid();
        String businessType = WorkflowBusinessEnum.SCHEDULE_TASK.getBusinessType();
        String platform = WorkflowBusinessEnum.SCHEDULE_TASK.getPlatform();
        findProcess.setBusinessType(businessType);
        findProcess.setPlatform(platform);
        //获取到业务的信息
        BusinessInfoDTO business = workflowFeign.getBusiness(findProcess);
        if (business != null) {
            StartProcessDTO startProcess = new StartProcessDTO();
            startProcess.setUserId(userId);
            startProcess.setProcessDefinitionKey(business.getProcessDefinitionKey());
            startProcess.setBusinessKey(business.getBusinessKey());
            Map<String, Object> parameterMap = new HashMap<>();

            if (StringUtils.isBlank(pmoCharge)) {
                throw new ServiceException(ApiError.ERROR_9036);
            }
            parameterMap.put("pmoCharge", pmoCharge);

            List<String> departmentHeadList = productDetailService.getApproveLead(SkuApproveConfigureEnum.FIVE_APPROVE.getDesc());
            if (CollectionUtils.isEmpty(departmentHeadList)) {
                throw new ServiceException(ApiError.ERROR_9032);
            }
            parameterMap.put("departmentHead", departmentHeadList.get(0));
            startProcess.setParameterMap(parameterMap);
            //启动流程
            ProcessNodeDTO processResult = workflowFeign.startProcess(startProcess);
            //流程id
            String processId = processResult.getProcessId();
            if (StringUtils.isNotBlank(processId)) {

                WorkflowBusinessProcessDTO businessProcess = new WorkflowBusinessProcessDTO();
                businessProcess.setBusinessId(business.getId());
                businessProcess.setCreateTime(LocalDateTime.now());
                businessProcess.setBusinessTableId(id);
                businessProcess.setCreateUserId(userId);
                businessProcess.setProcessId(processId);
                //保存业务与流程的信息
                workflowFeign.saveBusinessProcess(businessProcess);
            }

        }
    }


    /**
     * 审核通过
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-01-29 18:55
     */
    @Override
    @Transactional
    public Boolean approvalPass(AuditParamDTO dto) {
        String id = dto.getId();
        ProjectPlanEntity plan = this.getById(id);
        if (Objects.isNull(plan)) {
            throw new ServiceException(ApiError.ERROR_95122);
        }
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        String userName = loginUser.getUserName();
        //是不是第一次审核
        Boolean isFirst = false;

        //已存在的审核状态
        String dbStatus = plan.getStatus();
        //意见
        String comment = dto.getComment();
        String status = BaseStatusEnum.AUDIT_ING.getStatus();
        //当不是审核通过的时候
        if (!status.equals(dbStatus)) {
            isFirst = true;
            plan.setStatus(status);
        }
        plan.setRemark(dto.getComment());

        BusinessTableDTO tableDTO = new BusinessTableDTO();
        tableDTO.setBusinessTableId(id);
        tableDTO.setUserId(userId);
        //获取到用户该业务表的待办任务
        MyToDoTaskVO processTask = workflowFeign.getByBusinessTableId(tableDTO);
        if (Objects.isNull(processTask)) {
            throw new ServiceException(ApiError.ERROR_94005);
        }

        //审核
        ApproveProcessDTO approveProcess = new ApproveProcessDTO();
        approveProcess.setTaskId(processTask.getTaskId());
        approveProcess.setProcessInstanceId(processTask.getProcessInstanceId());
        approveProcess.setUserId(userId);
        approveProcess.setComment(comment);

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("agree", true);
        approveProcess.setParameterMap(parameterMap);

        Boolean result = this.updateById(plan);

        ProcessNodeDTO node = workflowFeign.taskPass(approveProcess);
        //表示成功
        if (node != null) {
            if (result) {
                List<ProjectPlanTaskEntity> taskList = projectPlanTaskService.getByProjectPlanIdList(Arrays.asList(id));
                List<String> taskIdList = taskList.stream().map(ProjectPlanTaskEntity::getTaskId).collect(Collectors.toList());
                //当是第一次审核的时候才更新状态
                if (isFirst) {
                    taskService.updateScheduleStatus(plan.getProductId(), taskIdList, status, "");
                }

                //给审核人发消息
                List<ProjectTaskEntity> taskEntityList = taskService.getByTaskIds(taskIdList);
                ProcessCurrentAuditorVO auditorVO = workflowFeign.getProcessNextAudit(id);
                List<String> auditorList = auditorVO.getHandleUserIdList();
                if (CollectionUtils.isNotEmpty(auditorList)) {
                    noticeMessageService.scheduleTaskAuditor(userName, taskEntityList, plan.getProductId(), auditorList);
                }
            }
        } else {
            throw new ServiceException(ApiError.ERROR_94005);
        }

        return true;
    }

    /**
     * 审核不通过
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-01-29 18:53
     */
    @Override
    public Boolean approvalNoPass(AuditParamDTO dto) {

        String id = dto.getId();
        ProjectPlanEntity plan = this.getById(id);
        if (Objects.isNull(plan)) {
            throw new ServiceException(ApiError.ERROR_95122);
        }
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        String userName = loginUser.getUserName();
        //意见
        String comment = dto.getComment();
        String status = BaseStatusEnum.AUDIT_NO_PASS.getStatus();
        plan.setStatus(status);
        plan.setRemark(dto.getComment());
        plan.setApprovalFinishTime(new Date());
        BusinessTableDTO tableDTO = new BusinessTableDTO();
        tableDTO.setBusinessTableId(id);
        tableDTO.setUserId(userId);
        //获取到用户该业务表的待办任务
        MyToDoTaskVO processTask = workflowFeign.getByBusinessTableId(tableDTO);
        if (Objects.isNull(processTask)) {
            throw new ServiceException(ApiError.ERROR_94005);
        }

        //审核
        ApproveProcessDTO approveProcess = new ApproveProcessDTO();
        approveProcess.setTaskId(processTask.getTaskId());
        approveProcess.setProcessInstanceId(processTask.getProcessInstanceId());
        approveProcess.setUserId(userId);
        approveProcess.setComment(comment);
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("agree", false);
        approveProcess.setParameterMap(parameterMap);
        ProcessNodeDTO node = workflowFeign.taskNoPass(approveProcess);
        //表示成功
        if (node != null) {
            Boolean result = this.updateById(plan);
            if (result) {
                List<ProjectPlanTaskEntity> taskList = projectPlanTaskService.getByProjectPlanIdList(Arrays.asList(id));
                List<String> taskIdList = taskList.stream().map(ProjectPlanTaskEntity::getTaskId).collect(Collectors.toList());
                taskService.updateScheduleStatus(plan.getProductId(), taskIdList, status, "");

                //异步发送 审核通知消息
                List<ProjectTaskEntity> taskEntityList = taskService.getByTaskIds(taskIdList);
                noticeMessageService.scheduleTaskAudit(userName, taskEntityList, plan.getProductId(), status, comment);
            }
        } else {
            throw new ServiceException(ApiError.ERROR_94005);
        }
        return true;

    }


    /**
     * 最终审核通过
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-02-11 11:11
     */
    @Override
    public void processPass(ProcessPassDTO dto) {
        String id = dto.getBusinessTableId();
        ProjectPlanEntity plan = this.getById(id);
        LoginUser loginUser = commonService.getUserInfo();
        String userName = loginUser.getUserName();
        if (plan != null) {
            String status = BaseStatusEnum.AUDIT_PASS.getStatus();
            plan.setStatus(status);
            plan.setApprovalFinishTime(new Date());
            Boolean result = this.updateById(plan);
            if (result) {
                List<ProjectPlanTaskEntity> taskList = projectPlanTaskService.getByProjectPlanIdList(Arrays.asList(id));
                List<String> taskIdList = taskList.stream().map(ProjectPlanTaskEntity::getTaskId).collect(Collectors.toList());
                List<ProjectTaskEntity> taskEntityList = taskService.getByTaskIds(taskIdList);
                noticeMessageService.scheduleTaskAudit(userName, taskEntityList, plan.getProductId(), status, "");
                /**
                 * 当是变更的情况 就要去更改数据
                 * @author yl
                 * @date 2023-02-11 16:39
                 * @param dto
                 * @return void
                 */
                if (ProjectPlanConstant.PROJECT_PLAN_CHANGE.equals(plan.getType())) {
                    // 排期任务变动 发送通知
                    taskService.updateScheduleTask(taskList, status, loginUser, plan.getProductId());
                    noticeMessageService.changeScheduleTask(userName, taskEntityList, plan.getProductId());
                } else {
                    /**
                     * 如果是初始排期 审核通过后
                     * 就要
                     * 如果是自动发布的任务 就要发布
                     */
                    taskService.initialScheduleTaskPass(loginUser, plan.getProductId(), taskIdList, status);

                }

            }
        }
    }


    /**
     * 审核情况
     *
     * @param id
     * @return void
     * @author yl
     * @date 2023-02-08 9:00
     */
    @Override
    public List<ApproveNodeRecordVO> auditInfo(String id) {
        if (StringUtils.isNotBlank(id)) {
            List<ApproveNodeRecordVO> list = workflowFeign.getHistoryTaskByBusinessTableId(id);
            return list;
        }
        return new ArrayList<>();


    }


    /**
     * 检查审核人是否为空
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-02-11 9:57
     */
    @Override
    public void checkAuditor() {

        if (StringUtils.isBlank(pmoCharge)) {
            throw new ServiceException(ApiError.ERROR_9036);
        }

        //研发中心负责人
        List<String> departmentHeadList = productDetailService.getApproveLead(SkuApproveConfigureEnum.FIVE_APPROVE.getDesc());
        if (CollectionUtils.isEmpty(departmentHeadList)) {
            throw new ServiceException(ApiError.ERROR_9032);
        }
    }

    /**
     * 获取排期任务状态
     *
     * @param
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @author yl
     * @date 2023-02-20 11:48
     */
    @Override
    public List<Map<String, Object>> getSubmitSchedule() {
        List<Map<String, Object>> list = new ArrayList<>(10);
        String cancel = BaseStatusEnum.CANCEL.getStatus();
        for (BaseStatusEnum item : BaseStatusEnum.values()) {
            if (!item.getStatus().equals(cancel)) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", item.getName());
                map.put("status", item.getStatus());
                list.add(map);
            }

        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectTaskPlanAutoVO autoSchedule(ProjectPlanTaskDTO.AutoDTo dto) {
        List<ProjectPlanTaskDTO.AutoDateDTO> list = dto.getList();
        if (CollectionUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_1017);
        }
        // 校验第一条是否有开始时间
        List<ProjectPlanTaskDTO.AutoDateDTO> autoPlanTaskOrderList = list.stream()
                .sorted(Comparator.comparing(ProjectPlanTaskDTO.AutoDateDTO::getId))
                .collect(Collectors.toList());
        ProjectPlanTaskDTO.AutoDateDTO autoDTO = autoPlanTaskOrderList.get(0);
        if (null == autoDTO.getStartDate()){
           throw new ServiceException(ApiError.ERROR_95144);
        }
        // 查询所有休息日
        SysCalendarDTO.ListDTO listDTO = new SysCalendarDTO.ListDTO();
        listDTO.setIsWorkDay(Boolean.FALSE);
        List<SysCalendarListVO> sysCalendarList = sysUserFeign.listCalendar(listDTO);
        List<LocalDate> dateList = sysCalendarList.stream().map(SysCalendarListVO::getCalendarDate).collect(Collectors.toList());

        List<ProjectTaskPlanAutoVO.ScheduleVO> errorList = new ArrayList<>();
        List<ProjectTaskPlanAutoVO.ScheduleDateVO> sucessList = new ArrayList<>();
        // 避免重复关联
        LinkedList<String> exitList = new LinkedList<>();
        // 查询对应任务
        Map<String, ProjectPlanTaskDTO.AutoDateDTO> dtoMap = list.stream().collect(Collectors.toMap(ProjectPlanTaskDTO.AutoDateDTO::getId, e -> e));
        List<String> planIdList = new ArrayList<>(dtoMap.keySet());
        // 查询计划
        List<PlanTaskNameDTO> planEntityList = null;
        Integer type = dto.getType();
        if (1== type){
            List<ProjectTaskEntity> taskEntityList = taskService.listByTaskIds(planIdList);
            planEntityList = taskEntityList.stream().map(PlanTaskNameDTO::new).collect(Collectors.toList());
        }else {
            planEntityList = projectPlanTaskService.listByPlanId(planIdList);
        }
        if(CollectionUtil.isEmpty(planEntityList)){
            throw new ServiceException(ApiError.ERROR_95145);
        }
        // 查询任务列表
        Map<String, PlanTaskNameDTO> taskIdMap = planEntityList.stream().collect(Collectors.toMap(PlanTaskNameDTO::getId, e -> e));

        // 对当前日期进行排期-校验是否存在冲突重复id
       Iterator<ProjectPlanTaskDTO.AutoDateDTO> iterator = list.iterator();
       while (iterator.hasNext()) {
           ProjectPlanTaskDTO.AutoDateDTO autoEntity = iterator.next();

           PlanTaskNameDTO planTaskNameDTO = taskIdMap.get(autoEntity.getId());
           if (checkData(dto.getType(), errorList, exitList, autoEntity, planTaskNameDTO)) {
               continue;
           }
           if(null == autoEntity.getStartDate() || null == autoEntity.getEndDate()){
               errorList.add(new ProjectTaskPlanAutoVO.ScheduleVO(planTaskNameDTO.getTaskName(), "链路起点任务开始时间结束时间不能为空"));
               continue;
           }
           // 更新任务工期
           LocalDate endDate;
           LocalDate startDate;
           if(1 == type){
               ProjectTaskEntity updateTaskEntity = new ProjectTaskEntity(planTaskNameDTO, autoEntity.getStartDate(), dateList);
               projectTaskService.updateById(updateTaskEntity);
               endDate = LocalDateUtil.date2LocalDate(updateTaskEntity.getPlanEndTime());
               startDate = LocalDateUtil.date2LocalDate(updateTaskEntity.getPlanStartTime());
               sucessList.add(new ProjectTaskPlanAutoVO.ScheduleDateVO(autoEntity.getId(), updateTaskEntity.getPlanStartTime(), updateTaskEntity.getPlanEndTime()));
           }else {
               ProjectPlanTaskEntity updateTaskEntity = new ProjectPlanTaskEntity(planTaskNameDTO, autoEntity.getStartDate(), dateList);
               projectPlanTaskService.updateById(updateTaskEntity);
               endDate = LocalDateUtil.date2LocalDate(updateTaskEntity.getChangeEndTime());
               startDate = LocalDateUtil.date2LocalDate(updateTaskEntity.getChangeStartTime());
               sucessList.add(new ProjectTaskPlanAutoVO.ScheduleDateVO(autoEntity.getId(), updateTaskEntity.getChangeStartTime(), updateTaskEntity.getChangeEndTime()));
           }
           // 对下一个节点进行排期
           sonNodeSchedule(dto.getType(), dateList, errorList, sucessList, exitList, dtoMap, taskIdMap, autoEntity.getId(), startDate, endDate);
       }
        return new ProjectTaskPlanAutoVO(errorList, sucessList);
    }

    /**
     * 递归处理子节点
     * @param type
     * @param dateList
     * @param errorList
     * @param sucessList
     * @param exitList
     * @param dtoMap
     * @param taskIdMap
     * @param taskId
     * @param endDate
     */
    private void sonNodeSchedule(Integer type,List<LocalDate> dateList, List<ProjectTaskPlanAutoVO.ScheduleVO> errorList, List<ProjectTaskPlanAutoVO.ScheduleDateVO> sucessList,
                                 LinkedList<String> exitList, Map<String, ProjectPlanTaskDTO.AutoDateDTO> dtoMap, Map<String, PlanTaskNameDTO> taskIdMap,
                                 String taskId, LocalDate startDate, LocalDate endDate) {
        // 查询下一个节点
        List<ProjectChildTaskDTO> childrenTaskList = preTaskService.listChildrenTaskOneByTaskId(taskId);
        Map<String, ProjectChildTaskDTO> childTaskMap = childrenTaskList.stream().collect(Collectors.toMap(ProjectChildTaskDTO::getId, e -> e));
        ArrayList<String> childTaskIds = new ArrayList<>(childTaskMap.keySet());

        Set<String> taskIds = taskIdMap.keySet();
        childTaskIds.retainAll(taskIds);
        if(CollectionUtils.isEmpty(childrenTaskList) || CollectionUtils.isEmpty(taskIds)){
            return;
        }
        // 对子节点进行 更新
        for (String x : childTaskIds) {
            // 下级信息
            ProjectChildTaskDTO projectChildTaskDTO = childTaskMap.get(x);
            PlanTaskNameDTO planTask = taskIdMap.get(x);
            ProjectPlanTaskDTO.AutoDateDTO autoPlanTask = dtoMap.get(x);
            if (checkData(type, errorList, exitList, autoPlanTask, planTask)) {
                return;
            }
            // 更新任务工期
            LocalDate planEndTime;
            LocalDate planStartTime;
            if(1 == type){
                ProjectTaskEntity taskEntity = new ProjectTaskEntity(planTask, startDate, endDate, projectChildTaskDTO, dateList);
                projectTaskService.updateById(taskEntity);
                planEndTime = LocalDateUtil.date2LocalDate(taskEntity.getPlanEndTime());
                planStartTime = LocalDateUtil.date2LocalDate(taskEntity.getPlanStartTime());
                sucessList.add(new ProjectTaskPlanAutoVO.ScheduleDateVO(taskEntity.getId(), taskEntity.getPlanStartTime(), taskEntity.getPlanEndTime()));
            }else {
                ProjectPlanTaskEntity updateTaskEntity = new ProjectPlanTaskEntity(planTask, startDate, endDate, projectChildTaskDTO, dateList);
                projectPlanTaskService.updateById(updateTaskEntity);
                planEndTime = LocalDateUtil.date2LocalDate(updateTaskEntity.getChangeEndTime());
                planStartTime = LocalDateUtil.date2LocalDate(updateTaskEntity.getChangeStartTime());
                sucessList.add(new ProjectTaskPlanAutoVO.ScheduleDateVO(planTask.getId(), updateTaskEntity.getChangeStartTime(), updateTaskEntity.getChangeEndTime()));
            }
            sonNodeSchedule(type,dateList,errorList,sucessList, exitList,dtoMap,taskIdMap,planTask.getId(), planStartTime,planEndTime);
        }
    }

    /**
     * 数据问题性校验
     * @param type
     * @param errorList
     * @param exitList
     * @param autoEntity
     * @param planTaskNameDTO
     * @return
     */
    private static boolean checkData(Integer type, List<ProjectTaskPlanAutoVO.ScheduleVO> errorList, LinkedList<String> exitList, ProjectPlanTaskDTO.AutoDateDTO autoEntity, PlanTaskNameDTO planTaskNameDTO) {
        if (null == planTaskNameDTO || null == planTaskNameDTO.getTaskName()) {
            errorList.add(new ProjectTaskPlanAutoVO.ScheduleVO(planTaskNameDTO.getTaskName(),"计划数据不存在或项目任务数据不不存在"));
            return true;
        }
        if (exitList.contains(autoEntity.getId())) {
//            errorList.add(new ProjectTaskPlanAutoVO.ScheduleVO(planTaskNameDTO.getTaskName(),"前后置任务冲突请手动排期"));
            return true;
        }
        exitList.add(autoEntity.getId());
        if (1 == type && !(BaseStatusEnum.WAIT_SUBMIT.getStatus().equals(planTaskNameDTO.getScheduleStatus()) || BaseStatusEnum.AUDIT_NO_PASS.getStatus().equals(planTaskNameDTO.getScheduleStatus()))) {
            errorList.add(new ProjectTaskPlanAutoVO.ScheduleVO(planTaskNameDTO.getTaskName(), "当前状态不允许修改排期"));
            return true;
        }
        return false;
    }

}
