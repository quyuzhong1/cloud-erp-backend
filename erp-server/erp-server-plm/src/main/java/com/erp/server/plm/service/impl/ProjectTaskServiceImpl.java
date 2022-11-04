package com.erp.server.plm.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.workflow.dto.ApproveProcessDTO;
import com.erp.model.workflow.dto.ProcessNodeDTO;
import com.erp.model.workflow.dto.StartProcessDTO;
import com.erp.model.workflow.dto.TaskShowDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.enums.BusinessProcessEnum;
import com.erp.server.plm.enums.TaskProcessTypeEnum;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.enums.TaskTypeEnum;
import com.erp.server.plm.mapper.ProjectTaskMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 产品任务表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
public class ProjectTaskServiceImpl extends ServiceImpl<ProjectTaskMapper, ProjectTaskEntity> implements ProjectTaskService {


    @Autowired
    private ProjectTaskSysService projectTaskSysService;


    @Autowired
    private TaskDeliveryService taskDeliveryService;

    @Autowired
    private TaskDocsFinishService finishService;

    @Autowired
    private ProjectPhaseService projectPhaseService;


    @Autowired
    private PreTaskService preTaskService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private ProductOperateRecordService productOperateRecordService;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private BusinessProcessService businessProcessService;

    @Autowired
    private TaskDocsNameService taskDocsNameService;


    @Autowired
    private TaskOperatorRecordService taskOperatorRecordService;
    @Autowired
    private TaskCommentService taskCommentService;

    @Autowired
    private TaskDocsFinishService taskDocsFinishService;


    @Autowired
    private ProjectMembersService projectMembersService;

    /**
     * 添加系统的产品任务
     * 只添加立项的
     *
     * @param productId
     * @return void
     * @author yl
     * @date 2022-09-17 10:32
     */
    @Transactional
    @Override
    public void addSysTask(String productId, List<TaskDocsNameEntity> taskDocsNameList) {
        // 这是立项任务任务
        List<ProjectTaskSysEntity> sysTaskList = projectTaskSysService.getListByProperty(TaskConstant.APPROVAL_TASK);
        //添加前置任务
        //添加立项阶段
        String taskPhaseId = projectPhaseService.saveTaskPhase(productId, TaskConstant.APPROVAL_TASK_NAME, IsConstant.YES);
        if (CollectionUtils.isNotEmpty(sysTaskList)) {
            List<CopySourceDTO> sourceList = new ArrayList<>();
            for (ProjectTaskSysEntity item : sysTaskList) {
                CopySourceDTO source = new CopySourceDTO();
                ProjectTaskEntity entity = new ProjectTaskEntity();
                BeanMapper.copy(item, entity);
                entity.setQuoteSysTaskId(item.getId());
                entity.setProductId(productId);
                entity.setPhaseId(taskPhaseId);
                entity.setPhaseName(TaskConstant.APPROVAL_TASK_NAME);
                String id = IdWorker.getIdStr();
                entity.setId(id);
                source.setNewCreateId(id);
                source.setDataId(item.getId());
                sourceList.add(source);
                if (IsConstant.NO.equals(item.getType())) {
                    entity.setStatus(TaskStateEnum.NOT_START.getCode());
                }
                boolean flag = this.save(entity);
                if (flag) {
                    taskDeliveryService.saveTaskDeliveryDocs(productId, entity.getId(), item.getId(), taskDocsNameList);
                }
                //新增产品操作日志
                ProductOperateRecordDTO productOperateRecordDTO = new ProductOperateRecordDTO();
                productOperateRecordDTO.setProductId(productId);
                List<String> remarkList = new ArrayList<>();
                remarkList.add("新增了一个任务：[" + entity.getName() + "]");
                productOperateRecordDTO.setRemark(JSONObject.toJSONString(remarkList));
                productOperateRecordService.saveOrUpdate(productOperateRecordDTO);
            }

            //处理前置任务
            List<String> sysTaskIds = sysTaskList.stream().map(ProjectTaskSysEntity::getId).collect(Collectors.toList());

            List<PreTaskEntity> sysPreTaskList = preTaskService.getSysPreTask(sysTaskIds);
            //以系统任务的id 分组
            Map<String, List<PreTaskEntity>> preMap = sysPreTaskList.parallelStream().
                    collect(Collectors.groupingBy(PreTaskEntity::getTaskId));
            List<PreTaskEntity> savePreList = new ArrayList<>();
            for (Map.Entry<String, List<PreTaskEntity>> item : preMap.entrySet()) {
                String sysTaskId = item.getKey();
                List<PreTaskEntity> sysPreTasks = item.getValue();
                CopySourceDTO source = sourceList.stream().filter(s -> s.getDataId().equals(sysTaskId))
                        .findFirst().orElse(null);
                if (source != null) {
                    for (PreTaskEntity sysPre : sysPreTasks) {
                        CopySourceDTO preTask = sourceList.stream().filter(s -> s.getDataId().equals(sysPre.getPreTaskId())).findFirst().orElse(null);
                        if (preTask != null) {
                            PreTaskEntity newPreTask = new PreTaskEntity();
                            newPreTask.setTaskId(source.getNewCreateId());
                            newPreTask.setPreTaskId(preTask.getNewCreateId());
                            newPreTask.setProductId(productId);
                            savePreList.add(newPreTask);
                        }
                    }
                }
            }
            if (savePreList.size() > 0) {
                preTaskService.saveBatch(savePreList);
            }
        }


    }


    /**
     * 根据 产品id 删除任务
     *
     * @param productId
     * @return void
     * @author yl
     * @date 2022-09-17 13:28
     */
    @Override
    public void removeTaskByProductId(String productId) {
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskEntity::getProductId, productId);
        this.remove(queryWrapper);

    }


    /**
     * 根据产品id集合 获取到对应任务
     *
     * @param productIds
     * @return void
     * @author yl
     * @date 2022-09-19 8:58
     */
    @Override
    public List<ProjectTaskEntity> getByProductIds(List<String> productIds) {
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProjectTaskEntity::getProductId, productIds);
        return list(queryWrapper);
    }

    /**
     * //根据产品很任务id 获取任务名
     *
     * @param productId
     * @return
     */
    @Override
    public List<ProjectTaskEntity> getByProductId(String productId) {
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskEntity::getProductId, productId);
        return list(queryWrapper);
    }


    /**
     * 复制任务列数据 根据产品id 项目id
     * 产品id 是要保存的
     *
     * @param saveProductId 需要保存的产品id
     * @param saveProjectId 需要保存的项目id
     * @param flagProjectId 查找的项目id
     * @return void
     * @author yl
     * @date 2022-09-20 18:07
     */
    @Override
    public void copyTaskByProject(String saveProductId, String saveProjectId, String flagProjectId) {
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskEntity::getProjectId, flagProjectId);
        queryWrapper.eq(ProjectTaskEntity::getProperty, TaskConstant.PROJECT_TASK);
        List<ProjectTaskEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            for (ProjectTaskEntity entity : list) {
                entity.setProductId(saveProductId);
                entity.setProjectId(saveProjectId);
                entity.setId(IdWorker.getIdStr());
                entity.setStatus(TaskStateEnum.TO_BE_RELEASED.getCode());
            }
            this.saveBatch(list);

        }

    }


    /**
     * 新建项目的话 需要查看系统是否设置了任务
     * 如果有就要复制项目任务
     *
     * @param saveProductId
     * @param saveProjectId
     * @return void
     * @author yl
     * @date 2022-09-21 9:08
     */
    @Override
    @Transactional
    public void copyTaskBySys(String saveProductId, String saveProjectId) {
        //从系统拿到 项目任务
        List<ProjectTaskSysEntity> sysTaskList = projectTaskSysService.getListByProperty(TaskConstant.PROJECT_TASK);
        //保存除了立项阶段的 阶段名
        List<CopySourceDTO> projectPhaseList = projectPhaseService.saveSysPhase(saveProductId);
        if (CollectionUtils.isNotEmpty(sysTaskList)) {
            List<CopySourceDTO> sourceList = new ArrayList<>(sysTaskList.size());
            List<TaskDocsNameEntity> docsNameList = taskDocsNameService.getDocsNameByProductId(saveProductId);
            for (ProjectTaskSysEntity item : sysTaskList) {
                CopySourceDTO phase = projectPhaseList.stream().filter(p -> p.getDataId().equals(item.getPhaseId())).findFirst().orElse(null);
                ProjectTaskEntity entity = new ProjectTaskEntity();
                BeanMapper.copy(item, entity);
                entity.setQuoteSysTaskId(item.getId());
                entity.setProductId(saveProductId);
                entity.setProjectId(saveProjectId);
                String id = IdWorker.getIdStr();
                entity.setId(id);
                CopySourceDTO source = new CopySourceDTO();
                source.setDataId(item.getId());
                source.setNewCreateId(id);
                sourceList.add(source);
                if (phase != null) {
                    entity.setPhaseId(phase.getNewCreateId());
                } else {
                    entity.setPhaseId("");
                }
                boolean flag = this.save(entity);
                if (flag) {
                    taskDeliveryService.saveTaskDeliveryDocs(saveProductId, entity.getId(), item.getId(), docsNameList);
                }
            }

            //处理前置任务
            List<String> sysTaskIds = sysTaskList.stream().map(ProjectTaskSysEntity::getId).collect(Collectors.toList());

            List<PreTaskEntity> sysPreTaskList = preTaskService.getSysPreTask(sysTaskIds);
            //以系统任务的id 分组
            Map<String, List<PreTaskEntity>> preMap = sysPreTaskList.parallelStream().
                    collect(Collectors.groupingBy(PreTaskEntity::getTaskId));
            List<PreTaskEntity> savePreList = new ArrayList<>();
            for (Map.Entry<String, List<PreTaskEntity>> item : preMap.entrySet()) {
                String sysTaskId = item.getKey();
                List<PreTaskEntity> sysPreTasks = item.getValue();
                CopySourceDTO source = sourceList.stream().filter(s -> s.getDataId().equals(sysTaskId))
                        .findFirst().orElse(null);
                if (source != null) {
                    for (PreTaskEntity sysPre : sysPreTasks) {
                        CopySourceDTO preTask = sourceList.stream().filter(s -> s.getDataId().equals(sysPre.getPreTaskId())).findFirst().orElse(null);
                        if (preTask != null) {
                            PreTaskEntity newPreTask = new PreTaskEntity();
                            newPreTask.setTaskId(source.getNewCreateId());
                            newPreTask.setPreTaskId(preTask.getNewCreateId());
                            newPreTask.setProductId(saveProductId);
                            savePreList.add(newPreTask);
                        }
                    }
                }
            }
            if (savePreList.size() > 0) {
                preTaskService.saveBatch(savePreList);
            }
        }


    }

    /**
     * 分页获取
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO
     * @author yl
     * @date 2022-09-21 14:51
     */
    @Override
    public PagingVO<List<TaskPagingShowDTO>> paging(PagingDTO<TaskPagingDTO> dto) {
        LoginUser loginUser = commonService.getUserInfo();
        String userId =loginUser.getUid();
        TaskPagingDTO params = dto.getParams();
        Integer taskFlag = params.getTaskFlag();
        String phaseId = params.getPhaseId();
        String productId = params.getProductId();
        String searchKeyword = params.getSearchKeyword();
        List<Integer> statusList = params.getStatusList();
        List<TaskSearchDTO> searchList = params.getSearchList();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = new Page();

        //这个是我完成的任务
        if (TaskConstant.MY_FINISH_TASK.equals(taskFlag)) {
//            statusList.add(TaskStateEnum.NOT_START.getCode());
//            statusList.add(TaskStateEnum.ING.getCode());
            pageData = baseMapper.paging(query, productId, phaseId, searchList, userId, searchKeyword, statusList);
        }
        //这个待我审核的任务
        if (TaskConstant.MY_APPROVAL_TASK.equals(taskFlag)) {
//            statusList.add(TaskStateEnum.WAIT_CONFIRM.getCode());
//            statusList.add(TaskStateEnum.FINISH_WAIT_CONFIRM.getCode());
            List<TaskShowDTO> myToDoList = workflowFeign.queryMyToDo(userId);
            //获取流程集合
            List<String> processIds = myToDoList.stream().map(TaskShowDTO::getProcessInstanceId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(processIds)) {
                pageData = baseMapper.myApprovalPaging(query, productId, phaseId, searchList, userId, searchKeyword, statusList, processIds);
                //要给流程任务的id
                List<TaskPagingShowDTO> list = pageData.getRecords();
                for (TaskPagingShowDTO show : list) {
                    TaskShowDTO showDTO = myToDoList.stream().filter(t -> t.getProcessInstanceId().equals(show.getProcessId())).findFirst().orElse(null);
                    if (showDTO != null) {
                        show.setProcessTaskId(showDTO.getTaskId());
                    }
                }
            }
        }
        //这个是全部
        if (TaskConstant.ALL_FINISH_TASK.equals(taskFlag)) {
            pageData = baseMapper.paging(query, productId, phaseId, searchList, null, searchKeyword, statusList);
            List<TaskShowDTO> myToDoList = workflowFeign.queryMyToDo(userId);
            //获取流程集合
            List<String> processIds = myToDoList.stream().map(TaskShowDTO::getProcessInstanceId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(processIds)) {
                //要给流程任务的id
                List<TaskPagingShowDTO> list = pageData.getRecords();
                for (TaskPagingShowDTO show : list) {
                    TaskShowDTO showDTO = myToDoList.stream().filter(t -> t.getProcessInstanceId().equals(show.getProcessId())).findFirst().orElse(null);
                    if (showDTO != null) {
                        show.setProcessTaskId(showDTO.getTaskId());
                    }
                }
            }
        }
        if (pageData != null) {
            List<TaskPagingShowDTO> list = pageData.getRecords();
            //获取到任务id 集合
            List<String> taskIds = list.stream().map(TaskPagingShowDTO::getId).collect(Collectors.toList());
            //获取总的任务文档数
            List<CountDTO> taskDocsCounts = taskDeliveryService.getTaskDocsCount(taskIds);
            List<TaskDocsFinishEntity> finishTasks = finishService.getByTaskIds(taskIds);
            Integer finish = TaskStateEnum.FINISH.getCode();
            //根据产品id 获取到所有的 任务信息
            List<TaskPagingShowDTO> allList = getAllChildrenList(productId);
            for (TaskPagingShowDTO item : list) {
                String taskId = item.getId();
                String quoteSysTaskId = item.getQuoteSysTaskId();
                if (StringUtils.isNotBlank(quoteSysTaskId)) {
                    item.setIsSysTask(true);
                }
                String warning = getWarning(item.getStatus(), finish, item.getPlanEndTime());
                item.setWarning(warning);
                Integer totalDocsCount = 0;
                CountDTO countDTO = taskDocsCounts.stream().filter(d -> d.getFlagId().equals(taskId)).findFirst().orElse(null);
                if (countDTO != null) {
                    totalDocsCount = countDTO.getCount();
                }
                item.setTotalDocsCount(totalDocsCount);
                Integer finishDocsCount = finishTasks.stream().filter(f -> taskId.equals(f.getTaskId())).collect(Collectors.toList()).size();
                item.setFinishDocsCount(finishDocsCount);
                item.setChildList(getChildrenList(item, allList));
            }
        }

        return new PagingVO(pageData);
    }


    /**
     * 递归获取到任务下面的子任务
     *
     * @param item
     * @param allList
     * @return java.util.List<com.erp.model.plm.dto.TaskPagingShowDTO>
     * @author yl
     * @date 2022-10-17 16:19
     */
    private List<TaskPagingShowDTO> getChildrenList(TaskPagingShowDTO item, List<TaskPagingShowDTO> allList) {
        List<TaskPagingShowDTO> collectList = allList.stream().
                filter(t -> item.getId().equals(t.getPid())).
                map(p -> {
                    p.setChildList(getChildrenList(p, allList));
                    return p;
                }).collect(Collectors.toList());
        return CollectionUtils.isNotEmpty(collectList) ? collectList : new ArrayList<>();
    }

    /**
     * 根据 产品id 获取到所有的任务信息
     *
     * @param productId
     * @return
     */
    public List<TaskPagingShowDTO> getAllChildrenList(String productId) {
        List<TaskPagingShowDTO> allChildrenList = baseMapper.allChildrenList(productId);
        if (CollectionUtils.isNotEmpty(allChildrenList)) {
            //获取到任务id 集合
            List<String> taskIds = allChildrenList.stream().map(TaskPagingShowDTO::getId).collect(Collectors.toList());
            //获取总的任务文档数
            List<CountDTO> taskDocsCounts = taskDeliveryService.getTaskDocsCount(taskIds);
            List<TaskDocsFinishEntity> finishTasks = finishService.getByTaskIds(taskIds);
            Integer finish = TaskStateEnum.FINISH.getCode();
            for (TaskPagingShowDTO item : allChildrenList) {
                String taskId = item.getId();
                String quoteSysTaskId = item.getQuoteSysTaskId();
                if (StringUtils.isNotBlank(quoteSysTaskId)) {
                    item.setIsSysTask(true);
                }
                String warning = getWarning(item.getStatus(), finish, item.getPlanEndTime());
                item.setWarning(warning);
                Integer totalDocsCount = 0;
                CountDTO countDTO = taskDocsCounts.stream().filter(d -> d.getFlagId().equals(taskId)).findFirst().orElse(null);
                if (countDTO != null) {
                    totalDocsCount = countDTO.getCount();
                }
                item.setTotalDocsCount(totalDocsCount);
                Integer finishDocsCount = finishTasks.stream().filter(f -> taskId.equals(f.getTaskId())).collect(Collectors.toList()).size();
                item.setFinishDocsCount(finishDocsCount);
            }
            return allChildrenList;
        } else {
            return new ArrayList();
        }
    }


    /**
     * 项目任务 保存任务
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-22 15:33
     */
    @Override
    @Transactional
    public Boolean save(ProjectTaskDTO dto) {
        checkTaskName(dto.getId(), dto.getProductId(), dto.getName());
        LoginUser loginUser = commonService.getUserInfo();
        ProjectTaskEntity taskEntity = new ProjectTaskEntity();
        BeanMapper.copy(dto, taskEntity);
        ProjectPhaseEntity phaseEntity = projectPhaseService.getById(dto.getPhaseId());
        String phaseName = "";
        if (phaseEntity != null) {
            phaseName = phaseEntity.getName();
        }
        if (TaskConstant.APPROVAL_TASK_NAME.equals(phaseName)) {
            taskEntity.setProperty(TaskConstant.APPROVAL_TASK);
            if (IsConstant.NO.equals(dto.getType())) {
                taskEntity.setStatus(TaskStateEnum.NOT_START.getCode());
            }

        }
        List<String> chargeId = dto.getChargeIds();
        String chargeNames = commonService.getNameByIds(chargeId);
        //自定义审核人
        List<UserInfoDTO> approvalUserIds = dto.getApprovalUserIds();
        if (CollectionUtils.isNotEmpty(approvalUserIds)) {
            List<String> approvalUserIdList = approvalUserIds.stream().map(UserInfoDTO::getUserId).collect(Collectors.toList());
            taskEntity.setApprovalUserId(String.join(",", approvalUserIdList));
        }
        taskEntity.setChargeId(String.join(",", chargeId));
        taskEntity.setChargeName(chargeNames);
        taskEntity.setPhaseName(phaseName);
        taskEntity.setCreateUserId(loginUser.getUid());
        taskEntity.setCreateUserName(loginUser.getUserName());
        //交付文档
        List<DocsDTO> deliveryDocsList = dto.getDeliveryDocsList();
        boolean flag = this.save(taskEntity);
        if (flag) {
            //保存交付文档
            taskDeliveryService.saveDeliveryDocs(taskEntity.getId(), dto.getProductId(), deliveryDocsList);
            //保存前置任务
            preTaskService.savePreTask(taskEntity.getId(), dto.getPreTaskIdList(), dto.getProductId());
        }
        return flag;
    }


    /**
     * 根据产品id 获取任务id 和名字
     *
     * @param productId
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @author yl
     * @date 2022-09-22 17:40
     */
    @Override
    public List<Map<String, Object>> getTaskListByProductId(String productId) {
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ProjectTaskEntity::getId, ProjectTaskEntity::getName);
        queryWrapper.eq(ProjectTaskEntity::getProductId, productId);
        return this.listMaps(queryWrapper);
    }


    /**
     * 删除项目任务
     *
     * @param taskId
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-22 18:00
     */
    @Override
    public Boolean removeTask(String taskId) {
        ProjectTaskEntity entity = this.getById(taskId);
        Integer IsFixed = entity.getIsFixed();
        //如果是固定任务
        if (IsConstant.YES.equals(IsFixed)) {
            throw new ServiceException(ApiError.ERROR_95014);
        }
        //检查是否是子任务
        checkTaskIfExistPid(taskId);
        Boolean flag = this.removeById(entity);
        if (flag) {
            taskDeliveryService.removeByTaskId(taskId);
            taskDocsFinishService.removeByTaskId(taskId);
        }
        return flag;
    }


    /**
     * 根据产品id 获取到成员任务处理情况
     *
     * @param productId
     * @return java.util.List<com.erp.model.plm.dto.TaskConductDTO>
     * @author yl
     * @date 2022-09-27 9:47
     */
    @Override
    public List<TaskConductDTO> getTaskConductList(String productId) {
        Date date = new Date();
        List<TaskConductDTO> resultList = new LinkedList<>();
        List<ProjectTaskEntity> list = this.getByProductId(productId);
        List<ProjectMembersEntity> membersList = projectMembersService.getListByProductId(productId);
        Integer finishTask = TaskStateEnum.FINISH.getCode();
        Integer approvalPass = TaskStateEnum.APPROVAL_PASS.getCode();
        Integer approvalNoPass = TaskStateEnum.APPROVAL_NO_PASS.getCode();

        for (ProjectMembersEntity item : membersList) {
            TaskConductDTO dto = new TaskConductDTO();
            dto.setMembersId(item.getMemberId());
            List<ProjectTaskEntity> taskList = list.stream().filter(t -> t.getChargeId().contains(item.getMemberId())).collect(Collectors.toList());
            //完成任务数
            int finishTaskCount = taskList.stream().filter(t -> finishTask.equals(t.getStatus()) || approvalPass.equals(t.getStatus()) || approvalNoPass.equals(t.getStatus())).collect(Collectors.toList()).size();
            //进行中
            int ingTaskCount = taskList.stream().filter(t -> TaskStateEnum.ING.getCode().equals(t.getStatus())).collect(Collectors.toList()).size();
            //总任务数
            int totalTaskCount = taskList.size();
            //延期的任务数
            int postponeTaskCount = 0;
            postponeTaskCount = taskList.stream().filter(t -> t.getPlanEndTime() != null && t.getRealityEndTime() != null && t.getRealityEndTime().compareTo(t.getPlanEndTime()) == 1).collect(Collectors.toList()).size();
            dto.setTotalTaskCount(totalTaskCount);
            dto.setFinishTaskCount(finishTaskCount);
            dto.setIngTaskCount(ingTaskCount);
            dto.setPostponeTaskCount(postponeTaskCount);
            resultList.add(dto);

        }

        return resultList;
    }


    /**
     * 根据产品id 获取到任务完成情况
     *
     * @param productId
     * @return java.util.List<com.erp.model.plm.dto.TaskConductDTO>
     * @author yl
     * @date 2022-09-27 9:47
     */
    @Override
    public TaskConductDTO getTaskConduct(String productId) {
        Date date = new Date();
        List<ProjectTaskEntity> list = this.getByProductId(productId);

        TaskConductDTO dto = new TaskConductDTO();
        Integer finish = TaskStateEnum.FINISH.getCode();
        Integer approvalPass = TaskStateEnum.APPROVAL_PASS.getCode();

        //完成任务数
        int finishTaskCount = list.stream().filter(t -> finish.equals(t.getStatus()) || approvalPass.equals(t.getStatus())).collect(Collectors.toList()).size();
        //进行中
        int ingTaskCount = list.stream().filter(t -> TaskStateEnum.ING.getCode().equals(t.getStatus())).collect(Collectors.toList()).size();
        //总任务数
        int totalTaskCount = list.size();
        //延期的任务数
        int postponeTaskCount = list.stream().filter(t -> t.getPlanEndTime() != null && t.getRealityEndTime() != null && t.getRealityEndTime().compareTo(t.getPlanEndTime()) == 1).collect(Collectors.toList()).size();
        dto.setTotalTaskCount(totalTaskCount);
        dto.setFinishTaskCount(finishTaskCount);
        dto.setIngTaskCount(ingTaskCount);
        dto.setPostponeTaskCount(postponeTaskCount);
        return dto;
    }


    /**
     * 根据产品id 集合获取到导出的任务集合
     *
     * @param productIds
     * @return java.util.List<com.erp.model.plm.dto.TaskExcelDTO>
     * @author yl
     * @date 2022-09-29 9:32
     */
    @Override
    public List<TaskExcelDTO> getExportTask(List<String> productIds) {
        List<TaskExcelDTO> list = baseMapper.getExportTask(productIds);
        for (TaskExcelDTO item : list) {
            String taskState = item.getTaskState();
            Integer state = Integer.parseInt(taskState);
            String stateName = TaskStateEnum.getName(state);
            item.setTaskState(stateName);
        }
        return list;
    }

    /**
     * 获取任务详情
     *
     * @param taskId
     * @return com.erp.model.plm.dto.ProjectTaskDetailsDTO
     * @author yl
     * @date 2022-10-11 11:24
     */
    @Override
    public ProjectTaskDetailsDTO getTaskDetails(String taskId) {
        ProjectTaskDetailsDTO detailsDTO = baseMapper.getTaskDetails(taskId);
        if (Objects.isNull(detailsDTO)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        ProjectTaskEntity taskEntity = getById(taskId);
        String businessProcessId = detailsDTO.getBusinessProcessId();
        if (StringUtils.isNotBlank(businessProcessId)) {
            BusinessProcessEntity processEntity = businessProcessService.getById(businessProcessId);
            if (!Objects.isNull(processEntity) &&
                    BusinessProcessEnum.DOCS_CHANGE.getBusinessType().equals(processEntity.getBusinessType())) {
                Integer taskState = detailsDTO.getTaskState();
                detailsDTO.setChangeDocsProcessState(TaskStateEnum.getName(taskState));
            }
        }

        StringBuffer planTime = new StringBuffer();
        if (detailsDTO.getPlanStartTime() != null) {
            planTime.append(DateUtil.conversionDate(detailsDTO.getPlanStartTime(), DateUtil.fmt_day));
        }
        planTime.append(" - ");
        if (detailsDTO.getPlanEndTime() != null) {
            planTime.append(DateUtil.conversionDate(detailsDTO.getPlanEndTime(), DateUtil.fmt_day));
        }
        detailsDTO.setPlanTime(planTime.toString());

        StringBuffer realityTime = new StringBuffer();
        if (detailsDTO.getRealityStartTime() != null) {
            realityTime.append(DateUtil.conversionDate(detailsDTO.getRealityStartTime(), DateUtil.fmt_day));
        }
        realityTime.append(" - ");
        if (detailsDTO.getRealityEndTime() != null) {
            realityTime.append(DateUtil.conversionDate(detailsDTO.getRealityEndTime(), DateUtil.fmt_day));
        }
        detailsDTO.setRealityTime(realityTime.toString());
        //前置任务id集合
        List<String> preTaskIdList = preTaskService.getPreTaskIdList(taskId);
        //前置任务
        List<RefTaskInfoDTO> preTasks = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(preTaskIdList)) {
            preTasks = getRefTask(preTaskIdList);
        }
        detailsDTO.setPreTasks(preTasks);
        //子任务
        List<RefTaskInfoDTO> childTasks = new ArrayList<>();
        //获取到当前任务id 的子任务
        List<String> childTaskIds = getChildTaskIds(taskId, detailsDTO.getProductId());
        if (CollectionUtils.isNotEmpty(childTaskIds)) {
            childTasks = getRefTask(childTaskIds);
        }
        detailsDTO.setChildTasks(childTasks);

        //获取交付文档
        List<DeliveryDocsDTO> docsList = taskDeliveryService.getByTaskId(taskId);
        //获取到任务的属性
        Integer taskProperty = getTaskProperty(taskEntity);
        Integer taskState = taskEntity.getStatus();
        Integer generalApproval = TaskProcessTypeEnum.GENERAL_APPROVAL_TASK.getCode();
        Integer reviewTask = TaskProcessTypeEnum.REVIEW_TASK.getCode();
        Boolean isApprovalPass = TaskStateEnum.APPROVAL_PASS.getCode().equals(taskEntity.getStatus());
        for (DeliveryDocsDTO docs : docsList) {
            //当审核通过
            if (isApprovalPass) {
                docs.setOldFileUrl(docs.getFileUrl());
                docs.setOldUploadType(docs.getUploadType());
            }

            // 如果任务类型是审核的
            if (taskProperty.equals(generalApproval) || taskProperty.equals(reviewTask)) {
                //如果审核通过可以变更
                if (taskState.equals(TaskStateEnum.APPROVAL_PASS.getCode())) {
                    docs.setChangeFlag(true);
                    docs.setDeleteFlag(false);
                }
                if (taskState.equals(TaskStateEnum.APPROVAL_NO_PASS.getCode())) {
                    docs.setChangeFlag(true);
                }
                if (taskState.equals(TaskStateEnum.APPROVAL_ING.getCode())) {
                    docs.setDeleteFlag(false);
                }
            }
        }

        detailsDTO.setOutputDocsList(docsList);
        return detailsDTO;
    }


    /**
     * 获取任务属性
     * 是一般任务 还是一般带审核  还是待审核
     *
     * @return
     */
    public Integer getTaskProperty(ProjectTaskEntity taskEntity) {
        Integer taskType = taskEntity.getType();
        //一般任务
        Integer generalTask = TaskTypeEnum.GENERAL_TASK.getCode();
        if (generalTask.equals(taskType)) {
            String processId = taskEntity.getProcessId();
            String businessProcessId = taskEntity.getBusinessProcessId();
            if (StringUtils.isNotBlank(processId) && StringUtils.isNotBlank(businessProcessId)) {
                return TaskProcessTypeEnum.GENERAL_APPROVAL_TASK.getCode();
            } else {
                return TaskProcessTypeEnum.GENERAL_TASK.getCode();
            }
        }

        return TaskProcessTypeEnum.REVIEW_TASK.getCode();
    }


    private List<String> getUpdateField(ProjectTaskDTO dto) {
        ProjectTaskEntity entity = new ProjectTaskEntity();
        BeanMapper.copy(dto, entity);
        List<String> list = new ArrayList<>();
        ProjectTaskEntity projectTaskEntity = this.getById(dto.getId());
        if (!projectTaskEntity.getName().equals(dto.getName())) {
            list.add("编辑任务字段[任务名]由[" + projectTaskEntity.getName() + "]改为[" + dto.getName() + "]");
        }

        if (dto.getType() != null) {
            if (!projectTaskEntity.getType().equals(dto.getType())) {
                String entityType = (projectTaskEntity.getType() == 0) ? "一般任务" : "审核任务";
                String dtoType = (dto.getType() == 0) ? "一般任务" : "审核任务";
                list.add("编辑任务字段[任务类型]由[" + entityType + "]改为[" + dtoType + "]");
            }
        }
        //负责人ids
        List<String> chargeIds = dto.getChargeIds();
        String chargeNames = commonService.getNameByIds(chargeIds);
        if (!projectTaskEntity.getChargeName().equals(chargeNames)) {
            list.add("编辑任务字段[产品负责人]由[" + projectTaskEntity.getChargeName() + "]改为[" + chargeNames + "]");
        }
        if (dto.getPlanStartTime() != null) {
            if (!projectTaskEntity.getPlanStartTime().equals(dto.getPlanStartTime())) {
                list.add("编辑任务字段[计划开始时间]由[" + projectTaskEntity.getPlanStartTime() + "]改为[" + dto.getPlanStartTime() + "]");
            }
        }
        if (dto.getPlanEndTime() != null) {
            if (!projectTaskEntity.getPlanEndTime().equals(dto.getPlanEndTime())) {
                list.add("编辑任务字段[计划结束时间]由[" + projectTaskEntity.getPlanEndTime() + "]改为[" + dto.getPlanEndTime() + "]");
            }
        }

        if (dto.getPriority() != null) {
            if (!projectTaskEntity.getPriority().equals(dto.getPriority())) {
                String entityPriority = (projectTaskEntity.getPriority() == 1) ? "低级" : (dto.getType() == 2) ? "中级" : "高级";
                String dtoPriority = (dto.getPriority() == 1) ? "低级" : (dto.getType() == 2) ? "中级" : "高级";
                list.add("编辑任务字段[任务优先级]由[" + entityPriority + "]改为[" + dtoPriority + "]");
            }
        }

        if (dto.getPhaseName() != null) {
            if (!projectTaskEntity.getPhaseName().equals(dto.getPhaseName())) {
                list.add("编辑任务字段[任务阶段名]由[" + projectTaskEntity.getPhaseName() + "]改为[" + dto.getPhaseName() + "]");
            }
        }

        if (dto.getDescription() != null) {
            if (!projectTaskEntity.getDescription().equals(dto.getDescription())) {
                list.add("编辑任务字段[任务描述]由[" + projectTaskEntity.getDescription() + "]改为[" + dto.getDescription() + "]");
            }
        }


        return list;
    }

    /**
     * 修改 任务信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-13 9:25
     */
    @Override
    @Transactional
    public Boolean updateTask(ProjectTaskDTO dto) {
        checkTaskName(dto.getId(), dto.getProductId(), dto.getName());
        ProjectTaskEntity taskEntity = new ProjectTaskEntity();
        BeanMapper.copy(dto, taskEntity);
        ProjectPhaseEntity phaseEntity = projectPhaseService.getById(dto.getPhaseId());
        String phaseName = "";
        if (phaseEntity != null) {
            phaseName = phaseEntity.getName();
        }
        if (TaskConstant.APPROVAL_TASK_NAME.equals(phaseName)) {
            taskEntity.setProperty(TaskConstant.APPROVAL_TASK);
        } else {
            taskEntity.setProperty(TaskConstant.PROJECT_TASK);
        }

        //自定义审核人
        List<UserInfoDTO> approvalUserIds = dto.getApprovalUserIds();
        if (CollectionUtils.isNotEmpty(approvalUserIds)) {
            List<String> approvalUserIdList = approvalUserIds.stream().map(UserInfoDTO::getUserId).collect(Collectors.toList());

            taskEntity.setApprovalUserId(String.join(",", approvalUserIdList));
        }
        List<String> chargeId = dto.getChargeIds();
        String chargeName = commonService.getNameByIds(chargeId);
        taskEntity.setChargeId(String.join(",", chargeId));
        taskEntity.setChargeName(chargeName);
        taskEntity.setPhaseName(phaseName);
        //交付文档
        List<DocsDTO> deliveryDocsList = dto.getDeliveryDocsList();
        boolean flag = this.updateById(taskEntity);
        if (flag) {
            //保存交付文档
            taskDeliveryService.saveDeliveryDocs(taskEntity.getId(), dto.getProductId(), deliveryDocsList);
            //保存前置任务
            preTaskService.savePreTask(taskEntity.getId(), dto.getPreTaskIdList(), dto.getProductId());

            List<String> updateField = getUpdateField(dto);
            if (updateField.size() > 0) {
                //新增产品操作日志
                ProductOperateRecordDTO productOperateRecordDTO = new ProductOperateRecordDTO();
                productOperateRecordDTO.setProductId(dto.getProductId());
                productOperateRecordDTO.setRemark(JSONObject.toJSONString(updateField));
                productOperateRecordService.saveOrUpdate(productOperateRecordDTO);
            }
        }
        return flag;
    }

    /**
     * 根据产品id 获取任务数量信息
     *
     * @param productId
     * @return com.erp.model.plm.dto.ProductTaskCountDTO
     * @author yl
     * @date 2022-10-13 16:51
     */
    @Override
    public ProductTaskCountDTO getProductTaskCount(String productId, Date date) {
        List<ProjectTaskEntity> taskList = this.getByProductId(productId);
        Integer finishState = TaskStateEnum.FINISH.getCode();
        Integer approvalPassState = TaskStateEnum.APPROVAL_PASS.getCode();
        //完成任务数
        int finishTaskCount = taskList.stream().filter(t -> finishState.equals(t.getStatus()) || approvalPassState.equals(t.getStatus())).collect(Collectors.toList()).size();
        //未完成任务数
        int unfinishedTaskCount = taskList.stream().filter(t -> !finishState.equals(t.getStatus()) && !approvalPassState.equals(t.getStatus())).collect(Collectors.toList()).size();
        //总任务数
        int totalTaskCount = taskList.size();
        //延期的任务数
        int postponeTaskCount = taskList.stream().filter(t -> t.getPlanEndTime() != null && t.getRealityEndTime() != null && t.getRealityEndTime().compareTo(t.getPlanEndTime()) == 1).collect(Collectors.toList()).size();
        ProductTaskCountDTO taskCountDTO = new ProductTaskCountDTO();
        taskCountDTO.setFinishTaskCount(finishTaskCount);
        taskCountDTO.setUnfinishedTaskCount(unfinishedTaskCount);
        taskCountDTO.setTotalTaskCount(totalTaskCount);
        taskCountDTO.setPostponeTaskCount(postponeTaskCount);
        return taskCountDTO;
    }

    /**
     * 更改任务基本信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-13 17:34
     */
    @Override
    public Boolean updateBaseTask(UpdateTaskDTO dto) {
        ProjectTaskEntity taskEntity = this.getById(dto.getTaskId());
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        //任务名
        String name = dto.getName();
        Date planStartTime = dto.getPlanStartTime();
        taskEntity.setPlanStartTime(planStartTime);
        //结束时间
        Date planEndTime = dto.getPlanEndTime();
        taskEntity.setPlanEndTime(planEndTime);
        String chargeId = dto.getChargeId();
        if (StringUtils.isNotBlank(name)) {
            checkTaskName(taskEntity.getId(), taskEntity.getProductId(), name);
            taskEntity.setName(name);
        }
        if (StringUtils.isNotBlank(chargeId)) {
            taskEntity.setChargeId(chargeId);
            String chargeName = commonService.getNameById(chargeId);
            taskEntity.setChargeName(chargeName);
        }
        return this.updateById(taskEntity);
    }


    /**
     * 根据任务di 获取 编辑的任务详情
     *
     * @param taskId
     * @return com.erp.model.plm.dto.ProjectTaskDTO
     * @author yl
     * @date 2022-10-15 10:04
     */
    @Override
    public ProjectTaskDTO taskDetails(String taskId) {
        ProjectTaskEntity taskEntity = this.getById(taskId);
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        ProjectTaskDTO resultDTO = new ProjectTaskDTO();
        BeanMapper.copy(taskEntity, resultDTO);
        String chargeId = taskEntity.getChargeId();
        if (StringUtils.isNotBlank(chargeId)) {
            resultDTO.setChargeIds(Arrays.asList(chargeId.split(",")));
        }
        String approvalUserId = taskEntity.getApprovalUserId();
        List<String> approvalUserIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(approvalUserId)) {
            approvalUserIdList = Arrays.asList(approvalUserId.split(","));
        }
        List<UserInfoDTO> approvalUserList = new ArrayList<>();
        List<FindUserDTO> userList = commonService.getAllUser();
        for (String userId : approvalUserIdList) {
            UserInfoDTO u = new UserInfoDTO();
            u.setUserId(userId);
            FindUserDTO user = userList.stream().filter(s -> s.getUserId().
                    equals(userId)).findFirst().orElse(null);
            if (!Objects.isNull(user)) {
                u.setUserName(user.getUserName());
            } else {
                u.setUserName("");
            }
            approvalUserList.add(u);
        }
        resultDTO.setApprovalUserIds(approvalUserList);
        resultDTO.setDeliveryDocsList(taskDeliveryService.getDocsByTaskId(taskId));
        String businessProcessId = resultDTO.getBusinessProcessId();
        if (StringUtils.isNotBlank(businessProcessId)) {
            BusinessProcessEntity processEntity = businessProcessService.getById(businessProcessId);
            if (processEntity != null) {
                resultDTO.setBusinessName(processEntity.getBusinessName());
            }

        }
        resultDTO.setPreTaskIdList(preTaskService.getPreTaskIdList(taskId));

        return resultDTO;
    }

    /**
     * 根据任务id 获取任务信息
     *
     * @param taskIds
     * @return
     */
    @Override
    public List<ProjectTaskEntity> getByTaskIds(List<String> taskIds) {
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProjectTaskEntity::getId, taskIds);
        return this.list(queryWrapper);
    }


    /**
     * 修改 任务状态
     *
     * @param taskIds
     * @param state
     * @return
     */
    @Override
    public boolean updateTaskState(List<String> taskIds, Integer state, Date realityStart, Date realityEnd) {
        if (CollectionUtils.isNotEmpty(taskIds)) {
            LambdaUpdateWrapper<ProjectTaskEntity> updateWrapper = new LambdaUpdateWrapper<ProjectTaskEntity>();
            updateWrapper.set(ProjectTaskEntity::getStatus, state);
            if (realityStart != null) {
                updateWrapper.set(ProjectTaskEntity::getRealityStartTime, realityStart);
            }
            if (realityEnd != null) {
                updateWrapper.set(ProjectTaskEntity::getRealityEndTime, realityEnd);
            }
            updateWrapper.in(ProjectTaskEntity::getId, taskIds);
            return this.update(updateWrapper);
        }
        return true;
    }


    /**
     * 统计未完成的任务数
     *
     * @param finishState
     * @param taskIds
     * @return int
     * @author yl
     * @date 2022-10-18 19:47
     */
    @Override
    public int countUndoneByTaskIds(Integer finishState, Integer approvalPassState, List<String> taskIds) {
        if (CollectionUtils.isNotEmpty(taskIds)) {
            return this.baseMapper.findUndone(finishState, approvalPassState, taskIds);
        }
        return 0;
    }


    /**
     * 检查任务列表 下有子任务 是否有未完成的任务
     *
     * @param taskIds
     * @return void
     * @author yl
     * @date 2022-10-18 19:53
     */
    @Override
    public void checkSonTaskFinish(List<String> taskIds, String productId) {
        List<ProjectTaskEntity> list = this.getByProductId(productId);
        Integer finishCode = TaskStateEnum.FINISH.getCode();
        Integer approvalPassCode = TaskStateEnum.APPROVAL_PASS.getCode();
        for (String taskId : taskIds) {
            List<String> resultList = new ArrayList<>();
            //递归获取他的子任务id
            getChilds(taskId, list, resultList);
            int count = countUndoneByTaskIds(finishCode, approvalPassCode, resultList);
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_95036);
            }
        }
    }


    @Override
    public void checkTaskFinish(List<ProjectTaskEntity> list) {
        Integer finishCode = TaskStateEnum.FINISH.getCode();
        Integer approvalPassCode = TaskStateEnum.APPROVAL_PASS.getCode();
        List<String> parentTaskIds = list.stream().filter(t -> t.getPid().equals("0")).map(ProjectTaskEntity::getId).collect(Collectors.toList());
        int parentCount = countUndoneByTaskIds(finishCode, approvalPassCode, parentTaskIds);
        if (parentCount > 0) {
            throw new ServiceException(ApiError.ERROR_95050);
        }
    }

    /**
     * 开始任务
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public Boolean startTask(OperateBaseTaskDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        List<String> taskIds = dto.getTaskIdList();
        //获取所有的任务列表
        List<ProjectTaskEntity> list = getByTaskIds(taskIds);
        //检查任务状态是否一样
        Integer state = checkTaskState(list);
        //只有待开始 和待审核 才能开始任务
        if (!TaskStateEnum.NOT_START.getCode().equals(state)
                && !TaskStateEnum.CLOSE.getCode().equals(state)) {
            throw new ServiceException(ApiError.ERROR_95032);
        }
        Integer ingCode = TaskStateEnum.ING.getCode();

        //一般任务
        Integer generalTaskCode = TaskTypeEnum.GENERAL_TASK.getCode();
        //一般任务 列表  都是将任务状态改为进行中
        List<ProjectTaskEntity> generalTasks = list.stream().filter(t -> generalTaskCode.equals(t.getType())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(generalTasks)) {
            List<String> taskIdList = generalTasks.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            this.updateTaskState(taskIdList, ingCode, new Date(), null);
            taskOperatorRecordService.batchSaveRecord(taskIds, TaskStateEnum.NOT_START.getCode(), ingCode, loginUser.getUid(), loginUser.getUserName(), "");
        }

        return true;
    }

    /**
     * 发布任务
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public Boolean publishTask(OperateBaseTaskDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        List<String> taskIds = dto.getTaskIdList();
        //待发布
        Integer releasedCode = TaskStateEnum.TO_BE_RELEASED.getCode();
        List<ProjectTaskEntity> list = this.getByTaskIds(taskIds);
        //检查任务状态
        checkTaskState(list);
        //统计项目状态为  不是待发布的任务
        long releasedCount = list.stream().filter(t -> !releasedCode.equals(t.getStatus())).count();
        if (releasedCount > 0) {
            throw new ServiceException(ApiError.ERROR_95030);
        }

        //一般任务
        Integer generalTaskCode = TaskTypeEnum.GENERAL_TASK.getCode();
        //一般任务 列表  都是将任务状态改为进行中
        List<ProjectTaskEntity> generalTasks = list.stream().filter(t -> generalTaskCode.equals(t.getType())).collect(Collectors.toList());
        List<String> generalTaskIds = generalTasks.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        boolean flag = updateTaskState(generalTaskIds, TaskStateEnum.NOT_START.getCode(), null, null);
        if (flag) {
            taskOperatorRecordService.batchSaveRecord(generalTaskIds, releasedCode, TaskStateEnum.NOT_START.getCode(), loginUser.getUid(), loginUser.getUserName(), "");
        }

        //审核任务
        Integer reviewTaskCode = TaskTypeEnum.REVIEW_TASK.getCode();
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
                        StartProcessDTO startProcess = new StartProcessDTO();
                        startProcess.setBusinessKey(processEntity.getBusinessKey());
                        startProcess.setProcessDefinitionKey(processEntity.getProcessDefinitionKey());
                        startProcess.setUserId(loginUser.getUid());
                        Map<String, Object> parameterMap = new HashMap<>();
                        //taskChargeIds
                        parameterMap.put("taskChargeIdList", Arrays.asList(chargeId.split(",")));
                        startProcess.setParameterMap(parameterMap);
                        //启动一个流程
                        ProcessNodeDTO process = workflowFeign.startProcess(startProcess);
                        //这个是流程Id
                        String processId = process.getProcessId();
                        //流程id 不为空 表示成功
                        if (StringUtils.isNotBlank(processId)) {
                            review.setProcessId(processId);
                        }
                        review.setRealityStartTime(nowDate);
                        review.setStatus(waitConfirmCode);
                        //更改 时间 很流程id
                        this.updateById(review);

                        //保存操作记录
                        TaskOperatorRecordEntity recordEntity = new TaskOperatorRecordEntity();
                        recordEntity.setTaskId(review.getId());
                        recordEntity.setBeforeState(review.getStatus());
                        recordEntity.setAfterState(waitConfirmCode);
                        recordEntity.setOperatorId(loginUser.getUid());
                        recordEntity.setOperatorName(loginUser.getUserName());
                        taskOperatorRecordService.save(recordEntity);

                    }
                }
            }
        }
        return flag;
    }

    /**
     * 任务操作 的时候 检查
     * 所选的的任务状态是否是一致
     * 如果不一致 就抛异常
     */
    public Integer checkTaskState(List<ProjectTaskEntity> list) {
        //当不为空
        if (CollectionUtils.isNotEmpty(list)) {
            if (list.size() > 1) {
                List<Integer> stateList = list.stream().map(ProjectTaskEntity::getStatus).distinct().collect(Collectors.toList());
                if (list.size() == stateList.size()) {
                    throw new ServiceException(ApiError.ERROR_95029);
                }
            }
            return list.get(0).getStatus();
        }
        return -1;
    }

    /**
     * 取消发布
     * 取消发布任务
     * 在未开始 未审核 可以取消发布
     * 在未开始的时候 可以取消发布 否则 不行，取消发布后变为待发布  任务状态为待发布
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean cancelPublishTask(OperateBaseTaskDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        List<String> taskIds = dto.getTaskIdList();
        //待开始
        Integer notStartCode = TaskStateEnum.NOT_START.getCode();
        //未审核
        Integer waitConfirmCode = TaskStateEnum.WAIT_CONFIRM.getCode();
        List<ProjectTaskEntity> list = this.getByTaskIds(taskIds);
        checkTaskState(list);
        //统计项目状态为  不是待发布的任务 和待审核的任务
        long releasedCount = list.stream().filter(t -> !notStartCode.equals(t.getStatus()) && !waitConfirmCode.equals(t.getStatus())).count();
        if (releasedCount > 0) {
            throw new ServiceException(ApiError.ERROR_95031);
        }
        boolean flag = this.updateTaskState(taskIds, TaskStateEnum.TO_BE_RELEASED.getCode(), null, null);
        if (flag) {
            taskOperatorRecordService.batchSaveTaskRecord(list, TaskStateEnum.TO_BE_RELEASED.getCode(), loginUser.getUid(), loginUser.getUserName(), "");
        }
        return flag;

    }


    /**
     * 关闭任务
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public Boolean closeTask(OperateBaseTaskDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        List<String> taskIds = dto.getTaskIdList();

        //进行中
        Integer ingCode = TaskStateEnum.ING.getCode();
        List<ProjectTaskEntity> list = this.getByTaskIds(taskIds);
        //检查任务状态是否一样
        Integer state = checkTaskState(list);
        //统计项目状态为  不是进行中的任务
        long releasedCount = list.stream().filter(t -> !ingCode.equals(t.getStatus())).count();
        if (releasedCount > 0) {
            throw new ServiceException(ApiError.ERROR_95033);
        }
        boolean flag = this.updateTaskState(taskIds, TaskStateEnum.CLOSE.getCode(), null, null);
        if (flag) {
            taskOperatorRecordService.batchSaveTaskRecord(list, TaskStateEnum.CLOSE.getCode(), loginUser.getUid(), loginUser.getUserName(), "");
        }
        return flag;

    }

    /**
     * 完成任务
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public Boolean finishTask(OperateBaseTaskDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        List<String> taskIds = dto.getTaskIdList();
        //获取所有的任务列表
        List<ProjectTaskEntity> list = this.getByTaskIds(taskIds);
        //检查任务状态是否一样
        Integer state = checkTaskState(list);

        //评审任务code
        Integer reviewTaskCode = TaskTypeEnum.REVIEW_TASK.getCode();
        /**
         * 评审任务
         */
        List<ProjectTaskEntity> reviewList = list.stream().filter(t -> reviewTaskCode.equals(t.getType())).collect(Collectors.toList());
        //表示有 评审任务 则要 踢出去
        if (CollectionUtils.isNotEmpty(reviewList) && reviewList.size() > 0) {
            throw new ServiceException(ApiError.ERROR_95034);
        }

        List<String> allTaskIds = list.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        //检查前置任务是否完成
        preTaskService.checkPreTaskFinish(allTaskIds);
        //检查子任务是否有完成
        this.checkSonTaskFinish(allTaskIds, dto.getProductId());
        //检查文档是否有上传
        finishService.checkTaskDocsUpload(allTaskIds);
        //一般任务code
        Integer generalTaskCode = TaskTypeEnum.GENERAL_TASK.getCode();


        //一般任务 列表
        List<ProjectTaskEntity> generalTasks = list.stream().filter(t -> generalTaskCode.equals(t.getType())).collect(Collectors.toList());

        //一般任务 没有流程
        List<ProjectTaskEntity> noProcessList = generalTasks.stream().filter(p -> StringUtils.isBlank(p.getBusinessProcessId())).collect(Collectors.toList());

        //一般任务 有流程
        List<ProjectTaskEntity> processList = generalTasks.stream().filter(p -> StringUtils.isNotBlank(p.getBusinessProcessId())).collect(Collectors.toList());

        //进行中
        Integer ingCode = TaskStateEnum.ING.getCode();
        //
        Integer approvalNoPass = TaskStateEnum.APPROVAL_NO_PASS.getCode();

        //找出 没有流程中 不是进行中的任务 如果有表示 不能完成任务
        long noProcess = list.stream().filter(t -> !ingCode.equals(t.getStatus()) && !approvalNoPass.equals(t.getStatus())).count();
        if (noProcess > 0) {
            throw new ServiceException(ApiError.ERROR_95044);
        }

        //完成待审核
        Integer finishWaitConfirmCode = TaskStateEnum.FINISH_WAIT_CONFIRM.getCode();
        /**
         *
         *   到了这一步 那么可以完成任务了
         *   没有流程审核的任务  更改状态为已完成
         *
         */
        List<String> noProcessTaskIds = noProcessList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        Date nowDate = new Date();
        this.updateTaskState(noProcessTaskIds, TaskStateEnum.FINISH.getCode(), null, nowDate);
        taskOperatorRecordService.batchSaveRecord(noProcessTaskIds, ingCode, TaskStateEnum.FINISH.getCode(), loginUser.getUid(), loginUser.getUserName(), "");

        //当有流程的不为空
        if (CollectionUtils.isNotEmpty(processList)) {
            //获取到所有流程的信息
            List<BusinessProcessEntity> businessProcessList = businessProcessService.list();

            //有审核流程的 要启动流程了
            for (ProjectTaskEntity processTask : processList) {
                //获取到自定义的审核人
                String approvalUserId = processTask.getApprovalUserId();
                if (StringUtils.isBlank(approvalUserId)) {
                    throw new ServiceException(ApiError.ERROR_95045);
                }
                List<String> membersIds = Arrays.asList(approvalUserId.split(","));
                String businessProcessId = processTask.getBusinessProcessId();
                //当不是审核不通过 就启动一个流程
                if (!state.equals(TaskStateEnum.APPROVAL_NO_PASS.getCode())) {
                    if (StringUtils.isNotBlank(businessProcessId)) {
                        BusinessProcessEntity processEntity = businessProcessList.stream().filter(b -> businessProcessId.equals(b.getId())).findFirst().orElse(null);
                        if (!Objects.isNull(processEntity)) {
                            if (CollectionUtils.isNotEmpty(membersIds)) {
                                StartProcessDTO startProcess = new StartProcessDTO();
                                startProcess.setBusinessKey(processEntity.getBusinessKey());
                                startProcess.setProcessDefinitionKey(processEntity.getProcessDefinitionKey());
                                startProcess.setUserId(loginUser.getUid());
                                Map<String, Object> parameterMap = new HashMap<>();
                                parameterMap.put("memberChargeList", membersIds);
                                startProcess.setParameterMap(parameterMap);
                                //启动流程
                                ProcessNodeDTO processResult = workflowFeign.startProcess(startProcess);
                                String processId = processResult.getProcessId();
                                //当流程id不为空的时候
                                if (StringUtils.isNotBlank(processId)) {
                                    processTask.setProcessId(processId);
                                    processTask.setStatus(finishWaitConfirmCode);
                                    processTask.setRealityStartTime(nowDate);
                                    processTask.setBusinessProcessId(processEntity.getId());
                                    this.updateById(processTask);

                                    TaskOperatorRecordEntity recordEntity = new TaskOperatorRecordEntity();
                                    recordEntity.setOperatorName(loginUser.getUserName());
                                    recordEntity.setOperatorId(loginUser.getUid());
                                    recordEntity.setBeforeState(ingCode);
                                    recordEntity.setAfterState(finishWaitConfirmCode);
                                    recordEntity.setTaskId(processTask.getId());
                                    taskOperatorRecordService.save(recordEntity);
                                }
                            }
                        }
                    }
                } else {
                    List<String> processTaskIds = processList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
                    //当流程Id 不为空就表示 有流程 是审核不通过的
                    this.updateTaskState(processTaskIds, finishWaitConfirmCode, null, null);
                    taskOperatorRecordService.batchSaveRecord(processTaskIds, TaskStateEnum.APPROVAL_NO_PASS.getCode(), finishWaitConfirmCode, loginUser.getUid(), loginUser.getUserName(), "");

                }
            }

        }
        return true;
    }


    /**
     * 审核任务
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-20 14:06
     */
    @Override
    @Transactional
    public Boolean approvalPass(TaskOperateDTO dto) {
        List<TaskHandleDataDTO> taskDataList = dto.getTaskDataList();
        List<String> taskIds = taskDataList.stream().map(TaskHandleDataDTO::getTaskId).collect(Collectors.toList());
        //根据任务id 获取所有的任务列表
        List<ProjectTaskEntity> list = this.getByTaskIds(taskIds);
        /**
         * 评审任务
         */
        //评审任务state
        Integer reviewTaskCode = TaskTypeEnum.REVIEW_TASK.getCode();
        List<ProjectTaskEntity> reviewList = list.stream().filter(t -> reviewTaskCode.equals(t.getType())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(reviewList)) {
            List<String> allTaskIds = reviewList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            //检查文档是否有上传
            finishService.checkTaskDocsUpload(allTaskIds);
        }
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        List<TaskShowDTO> myToDoList = workflowFeign.queryMyToDo(userId);
        //这是用户的流程id
        List<String> processInstanceIds = myToDoList.stream().map(TaskShowDTO::getProcessInstanceId).collect(Collectors.toList());
        //传过来的流程id
        List<String> processIds = taskDataList.stream().map(TaskHandleDataDTO::getProcessId).collect(Collectors.toList());
        //传过来的流程id 和 当前用户的流程id 如果当前用户的流程id 不包含 就是不能审核
        if (!processInstanceIds.containsAll(processIds)) {
            throw new ServiceException(ApiError.ERROR_95049);
        }

        //检查任务状态是否一样
        Integer state = checkTaskState(list);
        Integer waitConfirmCode = TaskStateEnum.WAIT_CONFIRM.getCode();
        Integer finishWaitConfirmCode = TaskStateEnum.FINISH_WAIT_CONFIRM.getCode();
        //审核中
        Integer approvalIngCode = TaskStateEnum.APPROVAL_ING.getCode();
        // 只有待审核 和 完成待审核 的状态 才可以审核通过
        if (!waitConfirmCode.equals(state) &&
                !finishWaitConfirmCode.equals(state) &&
                !approvalIngCode.equals(state)) {
            throw new ServiceException(ApiError.ERROR_95038);
        }


        List<String> taskIdList = list.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        this.updateTaskState(taskIdList, approvalIngCode, null, null);
        taskOperatorRecordService.batchSaveRecord(taskIdList, waitConfirmCode, approvalIngCode, loginUser.getUid(), loginUser.getUserName(), "");
        String comment = dto.getComment();
        if (StringUtils.isBlank(comment)) {
            comment = "";
        }

        //这里需要去 调用审核通过的工作流
        for (ProjectTaskEntity item : list) {
            TaskHandleDataDTO handleData = taskDataList.stream().filter(d -> d.getProcessId().equals(item.getProcessId())).findFirst().orElse(null);
            if (handleData != null) {
                ApproveProcessDTO approveProcess = new ApproveProcessDTO();
                approveProcess.setTaskId(handleData.getProcessTaskId());
                approveProcess.setProcessInstanceId(item.getProcessId());
                approveProcess.setUserId(userId);
                approveProcess.setComment(comment);
                workflowFeign.taskPass(approveProcess);
            }
        }
        return true;
    }

    /**
     * 审批驳回
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-20 14:13
     */
    @Override
    @Transactional
    public Boolean approvalReject(TaskOperateDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();

        List<TaskShowDTO> myToDoList = workflowFeign.queryMyToDo(loginUser.getUid());
        //这是用户的流程id
        List<String> processInstanceIds = myToDoList.stream().map(TaskShowDTO::getProcessInstanceId).collect(Collectors.toList());


        //审核不通过表示要博回的
        //根据任务id 获取所有的任务列表
        List<TaskHandleDataDTO> taskDataList = dto.getTaskDataList();
        List<String> taskIds = taskDataList.stream().map(TaskHandleDataDTO::getTaskId).collect(Collectors.toList());
        //传过来的流程id
        List<String> processIds = taskDataList.stream().map(TaskHandleDataDTO::getProcessId).collect(Collectors.toList());
        //传过来的流程id 和 当前用户的流程id 如果当前用户的流程id 不包含 就是不能审核
        if (!processInstanceIds.containsAll(processIds)) {
            throw new ServiceException(ApiError.ERROR_95049);
        }

        List<ProjectTaskEntity> list = this.getByTaskIds(taskIds);
        Integer state = checkTaskState(list);
        //审核中
        Integer approvalIngCode = TaskStateEnum.APPROVAL_ING.getCode();
        //审核中
        Integer finishWaitConfirmCode = TaskStateEnum.FINISH_WAIT_CONFIRM.getCode();
        //待审核
        Integer waitConfirmCode = TaskStateEnum.WAIT_CONFIRM.getCode();
        if (!approvalIngCode.equals(state) && !finishWaitConfirmCode.equals(state)
                && !waitConfirmCode.equals(state)
        ) {
            throw new ServiceException(ApiError.ERROR_95046);
        }

        List<String> taskIdList = list.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        boolean flag = this.updateTaskState(taskIdList, TaskStateEnum.APPROVAL_NO_PASS.getCode(), null, null);
        if (flag) {
            taskOperatorRecordService.batchSaveRecord(taskIdList, approvalIngCode, TaskStateEnum.APPROVAL_NO_PASS.getCode(), loginUser.getUid(), loginUser.getUserName(), "");
        }

        List<TaskCommentEntity> taskCommentList = new ArrayList<>(taskIds.size());
        for (String taskId : taskIds) {
            //添加评论
            TaskCommentEntity comment = new TaskCommentEntity();
            comment.setComment(dto.getComment());
            comment.setTaskId(taskId);
            comment.setCreateUserName(loginUser.getUserName());
            comment.setCreateUserId(loginUser.getUid());
            taskCommentList.add(comment);
        }

        taskCommentService.batchSaveTaskComment(taskCommentList);
        return flag;

    }


    /**
     * 查看任务流程情况
     *
     * @param taskId
     * @return com.erp.model.plm.dto.TaskProcessDTO
     * @author yl
     * @date 2022-10-20 14:44
     */
    @Override
    public List<TaskProcessNodeDTO> findTaskProcess(String taskId) {
        ProjectTaskEntity taskEntity = this.getById(taskId);
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        Integer taskType = taskEntity.getType();
        //一般任务code
        Integer generalTask = TaskTypeEnum.GENERAL_TASK.getCode();
        //如果是一般任务
        if (generalTask.equals(taskType)) {
            String processId = taskEntity.getProcessId();
            String businessProcessId = taskEntity.getBusinessProcessId();
            //表示一般任务带有审核审核
            if (StringUtils.isNotBlank(processId) && StringUtils.isNotBlank(businessProcessId)) {
                return getByProcessTye(TaskProcessTypeEnum.GENERAL_APPROVAL_TASK.getCode(), taskEntity);
            } else {
                return getByProcessTye(TaskProcessTypeEnum.GENERAL_TASK.getCode(), taskEntity);
            }
        }
        return getByProcessTye(TaskProcessTypeEnum.REVIEW_TASK.getCode(), taskEntity);
    }


    /**
     * 方法说明
     *
     * @param processId
     * @return void
     * @author yl
     * @date 2022-10-21 10:01
     */
    @Override
    @Transactional
    public void approvalTaskPass(String processId) {
        LoginUser loginUser = commonService.getUserInfo();
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskEntity::getProcessId, processId);
        queryWrapper.last("LIMIT 1");
        ProjectTaskEntity taskEntity = this.getOne(queryWrapper);
        if (!Objects.isNull(taskEntity)) {
            //保存记录
            TaskOperatorRecordEntity recordEntity = new TaskOperatorRecordEntity();
            recordEntity.setTaskId(taskEntity.getId());
            recordEntity.setBeforeState(taskEntity.getStatus());
            recordEntity.setAfterState(TaskStateEnum.APPROVAL_PASS.getCode());
            recordEntity.setOperatorId(loginUser.getUid());
            recordEntity.setOperatorName(loginUser.getUserName());
            taskEntity.setRealityEndTime(new Date());
            taskEntity.setStatus(TaskStateEnum.APPROVAL_PASS.getCode());

            this.updateById(taskEntity);
            taskOperatorRecordService.save(recordEntity);
        }

    }


    /**
     * 根据流程类型 获取到流程信息
     *
     * @param processType
     * @return
     * @author yl
     * @date 2022-10-20 15:00
     */
    public List<TaskProcessNodeDTO> getByProcessTye(Integer processType, ProjectTaskEntity taskEntity) {
        List<TaskOperatorRecordEntity> recordList = taskOperatorRecordService.getByTaskId(taskEntity.getId());
        List<TaskProcessNodeDTO> resultList = new ArrayList<>();
        Integer taskState = taskEntity.getStatus();
        //待发布
        Integer waitReleasedState = TaskStateEnum.TO_BE_RELEASED.getCode();
        //待开始
        Integer notStart = TaskStateEnum.NOT_START.getCode();
        //进行中
        Integer ingState = TaskStateEnum.ING.getCode();
        //待审核
        Integer waitConfirmState = TaskStateEnum.WAIT_CONFIRM.getCode();

        //已完成
        Integer finishState = TaskStateEnum.FINISH.getCode();

        //完成待审核
        Integer finishWaitConfirmState = TaskStateEnum.FINISH_WAIT_CONFIRM.getCode();

        //审核中
        Integer approvalIngState = TaskStateEnum.APPROVAL_ING.getCode();

        //审核通过
        Integer approvalPassState = TaskStateEnum.APPROVAL_PASS.getCode();

        //审核不通过
        Integer approvalNoPassState = TaskStateEnum.APPROVAL_NO_PASS.getCode();

        TaskOperatorRecordEntity notStartEntity = recordList.stream().filter(r -> r.getAfterState().equals(notStart)).findFirst().orElse(null);
        TaskOperatorRecordEntity ingStateEntity = recordList.stream().filter(r -> r.getAfterState().equals(ingState)).findFirst().orElse(null);
        TaskOperatorRecordEntity finishStateEntity = recordList.stream().filter(r -> r.getAfterState().equals(finishState)).findFirst().orElse(null);
        TaskOperatorRecordEntity finishWaitConfirmEntity = recordList.stream().filter(r -> r.getAfterState().equals(finishWaitConfirmState)).findFirst().orElse(null);
        TaskOperatorRecordEntity approvalIngEntity = recordList.stream().filter(r -> r.getAfterState().equals(approvalIngState)).findFirst().orElse(null);
        TaskOperatorRecordEntity approvalPassEntity = recordList.stream().filter(r -> r.getAfterState().equals(approvalPassState)).findFirst().orElse(null);
        TaskOperatorRecordEntity waitConfirmEntity = recordList.stream().filter(r -> r.getAfterState().equals(waitConfirmState)).findFirst().orElse(null);
        TaskOperatorRecordEntity approvalNoPassEntity = recordList.stream().filter(r -> r.getAfterState().equals(approvalNoPassState)).findFirst().orElse(null);

        Boolean approvalNoPassFlag = taskState.equals(approvalNoPassState);

        //先添加待发布的
        TaskProcessNodeDTO processNode = new TaskProcessNodeDTO();
        processNode.setIfFinishNode(true);
        processNode.setOperateTime(taskEntity.getCreateTime());
        processNode.setOperateUserName(taskEntity.getCreateUserName());
        processNode.setNodeName(TaskStateEnum.TO_BE_RELEASED.getName());
        processNode.setNodeState(waitReleasedState);
        resultList.add(processNode);

        //一般任务
        Integer general = TaskProcessTypeEnum.GENERAL_TASK.getCode();

        //一般带审核任务
        Integer generalApproval = TaskProcessTypeEnum.GENERAL_APPROVAL_TASK.getCode();
        //评审任务
        Integer reviewTask = TaskProcessTypeEnum.REVIEW_TASK.getCode();

        if (general.equals(processType)) {
            //添加待开始
            resultList.add(getProcessNode(notStartEntity, notStart));
            //添加进行中
            resultList.add(getProcessNode(ingStateEntity, ingState));
            //添加已完成
            resultList.add(getProcessNode(finishStateEntity, finishState));
        }

        if (generalApproval.equals(processType)) {
            //添加待开始
            resultList.add(getProcessNode(notStartEntity, notStart));
            //添加进行中
            resultList.add(getProcessNode(ingStateEntity, ingState));
            //添加完成待审核
            resultList.add(getProcessNode(finishWaitConfirmEntity, finishWaitConfirmState));
            //添加审核中
            resultList.add(getProcessNode(approvalIngEntity, approvalIngState));

            if (approvalNoPassFlag) {//添加审核不通过
                resultList.add(getProcessNode(approvalNoPassEntity, approvalNoPassState));
            } else {
                //添加审核通过
                resultList.add(getProcessNode(approvalPassEntity, approvalPassState));
            }

        }

        if (reviewTask.equals(processType)) {
            //添加待审核
            resultList.add(getProcessNode(waitConfirmEntity, waitConfirmState));
            resultList.add(getProcessNode(approvalIngEntity, approvalIngState));
            if (approvalNoPassFlag) {//添加审核不通过
                resultList.add(getProcessNode(approvalNoPassEntity, approvalNoPassState));
            } else {
                //添加审核通过
                resultList.add(getProcessNode(approvalPassEntity, approvalPassState));
            }
        }

        return resultList;
    }


    //
    public TaskProcessNodeDTO getProcessNode(TaskOperatorRecordEntity entity, Integer state) {
        boolean flag = !Objects.isNull(entity);
        String operatorName = "";
        Date operatorTime = null;
        if (flag) {
            operatorName = entity.getOperatorName();
            operatorTime = entity.getCreateTime();
        }
        TaskProcessNodeDTO waitReleasedDTO = new TaskProcessNodeDTO();
        waitReleasedDTO.setNodeName(TaskStateEnum.getName(state));
        waitReleasedDTO.setNodeState(state);
        waitReleasedDTO.setOperateUserName(operatorName);
        waitReleasedDTO.setOperateTime(operatorTime);
        waitReleasedDTO.setIfFinishNode(flag);
        return waitReleasedDTO;
    }

    //检查子任务
    private void checkTaskIfExistPid(String taskId) {
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskEntity::getPid, taskId);
        Integer count = baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95024);
        }
    }


    /**
     * 根据任务id 获取到 他子的任务id
     *
     * @param taskId
     * @return
     */
    private List<String> getChildTaskIds(String taskId, String productId) {
        List<String> resultList = new LinkedList<>();
        //根据产品id 获取到产品任务
        List<ProjectTaskEntity> taskList = getByProductId(productId);
        List<ProjectTaskEntity> pidTaskList = taskList.stream().filter(t -> taskId.equals(t.getPid())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(pidTaskList)) {
            resultList.addAll(pidTaskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList()));
        }
        return resultList;

    }


    /**
     * 递归获取该任务的 子任务id
     *
     * @param taskId
     * @param taskList
     * @param resultList
     * @return void
     * @author yl
     * @date 2022-10-11 14:46
     */

    private void getChilds(String taskId, List<ProjectTaskEntity> taskList, List<String> resultList) {
        ProjectTaskEntity item = taskList.stream().filter(t -> taskId.equals(t.getPid())).findFirst().orElse(null);
        if (item != null) {
            resultList.add(item.getId());
            getChilds(item.getId(), taskList, resultList);
        }
    }

    /**
     * 获取到关联的任务信息
     *
     * @param taskIds
     * @return
     */
    public List<RefTaskInfoDTO> getRefTask(List<String> taskIds) {
        List<RefTaskInfoDTO> refTaskList = baseMapper.getRefTask(taskIds);
        return refTaskList;
    }


    /**
     * /**
     * 根据项目id 获取列表
     *
     * @param projectId
     * @return java.util.List<com.erp.model.plm.entity.ProjectTaskEntity>
     * @author yl
     * @date 2022-09-27 11:12
     */
    private List<ProjectTaskEntity> getByProjectId(String projectId) {
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskEntity::getProjectId, projectId);
        return this.list(queryWrapper);
    }


    /**
     * 检查任务名是否重复
     *
     * @param productId
     * @param name
     * @return void
     * @author yl
     * @date 2022-09-22 16:36
     */
    private void checkTaskName(String taskId, String productId, String name) {
        //根据产品很任务id 获取任务名
        List<ProjectTaskEntity> taskList = getByProductId(productId);
        if (StringUtils.isNotBlank(taskId)) {
            taskList = taskList.stream().filter(t -> !taskId.equals(t.getId())).collect(Collectors.toList());
        }
        List<String> taskNames = taskList.stream().map(ProjectTaskEntity::getName).collect(Collectors.toList());
        //获取系统的任务名
        List<String> sysTaskNames = projectTaskSysService.getSysTaskNames();
        //  taskNames.addAll(sysTaskNames);
        if (taskNames.contains(name)) {
            throw new ServiceException(ApiError.ERROR_95013);
        }
    }

    //获取预警信息
    public String getWarning(Integer state, Integer finishState, Date planEndTime) {
        Date nowDay = new Date();
        Integer approvalPass = TaskStateEnum.APPROVAL_PASS.getCode();
        String warning = "-";
        if (planEndTime != null) {
            //状态
            if (!finishState.equals(state) && !approvalPass.equals(state)) {
                int difference = DateUtil.getDiffDay(planEndTime, nowDay);
                if (difference > 0) {
                    warning = "过期" + difference + "天";
                }
                if (difference == 0) {
                    warning = "今天后过期";
                }
                if (difference < 0) {
                    if (difference >= -2) {
                        warning = Math.abs(difference) + 1 + "天后过期";
                    }
                }
            }
        }
        return warning;
    }


}
