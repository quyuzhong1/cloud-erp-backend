package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.ProjectTaskSysEntity;
import com.erp.model.plm.entity.TaskDocsFinishEntity;
import com.erp.model.plm.entity.TemplateTaskEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProjectTaskMapper;
import com.erp.server.plm.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
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
    private TemplateTaskService templateTaskService;

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


    /**
     * 添加系统的产品任务
     *
     * @param productId
     * @return void
     * @author yl
     * @date 2022-09-17 10:32
     */
    @Transactional
    @Override
    public void addSysTask(String productId) {
        // 这是立项任务任务
        List<ProjectTaskSysEntity> sysTaskList = projectTaskSysService.getListByProperty(TaskConstant.APPROVAL_TASK);
        //添加立项阶段
        String taskPhaseId = projectPhaseService.saveTaskPhase(productId, TaskConstant.APPROVAL_TASK_NAME, IsConstant.YES);
        if (CollectionUtils.isNotEmpty(sysTaskList)) {
            for (ProjectTaskSysEntity item : sysTaskList) {
                ProjectTaskEntity entity = new ProjectTaskEntity();
                BeanMapper.copy(item, entity);
                entity.setQuoteSysTaskId(item.getId());
                entity.setProductId(productId);
                entity.setPhaseId(taskPhaseId);
                entity.setPhaseName(TaskConstant.APPROVAL_TASK_NAME);
                entity.setId(IdWorker.getIdStr());
                boolean flag = this.save(entity);
                if (flag) {
                    taskDeliveryService.saveTaskDeliveryDocs(productId, entity.getId(), item.getId());
                }
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
     * 从模板 复制数据
     *
     * @param saveProductId
     * @param saveProjectId
     * @param flagTemplateId
     * @return void
     * @author yl
     * @date 2022-09-21 9:49
     */
    @Override
    public void copyTaskByTemplate(String saveProductId, String saveProjectId, String flagTemplateId) {
        List<TemplateTaskEntity> templateTasks = templateTaskService.getTaskByTemplateId(flagTemplateId);
        List<ProjectTaskEntity> saveList = new LinkedList<>();
        for (TemplateTaskEntity item : templateTasks) {
            ProjectTaskEntity entity = new ProjectTaskEntity();
            BeanMapper.copy(item, entity);
            entity.setProductId(saveProductId);
            entity.setProjectId(saveProjectId);
            entity.setId(IdWorker.getIdStr());
            saveList.add(entity);
        }
        this.saveBatch(saveList);
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
    public void copyTaskBySys(String saveProductId, String saveProjectId) {
        //从系统拿到 项目任务
        List<ProjectTaskSysEntity> sysTaskList = projectTaskSysService.getListByProperty(TaskConstant.PROJECT_TASK);
        if (CollectionUtils.isNotEmpty(sysTaskList)) {
            List<ProjectTaskEntity> saveList = new LinkedList<>();
            for (ProjectTaskSysEntity item : sysTaskList) {
                ProjectTaskEntity entity = new ProjectTaskEntity();
                BeanMapper.copy(item, entity);
                entity.setQuoteSysTaskId(item.getId());
                entity.setProductId(saveProductId);
                entity.setProjectId(saveProjectId);
                entity.setId(IdWorker.getIdStr());
                saveList.add(entity);
            }
            this.saveBatch(saveList);
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
        String userId = loginUser.getUid();
        TaskPagingDTO params = dto.getParams();
        Integer taskFlag = params.getTaskFlag();
        String phaseId = params.getPhaseId();
        String productId = params.getProductId();
        String searchKeyword = params.getSearchKeyword();
        List<TaskSearchDTO> searchList = params.getSearchList();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = null;
        //这个是我完成的任务
        if (TaskConstant.MY_FINISH_TASK.equals(taskFlag)) {
            pageData = baseMapper.paging(query, productId, phaseId, searchList, userId, searchKeyword);
        }
        if (TaskConstant.ALL_FINISH_TASK.equals(taskFlag)) {
            pageData = baseMapper.paging(query, productId, phaseId, searchList, null, searchKeyword);
        }
        if (pageData != null) {
            List<TaskPagingShowDTO> list = pageData.getRecords();
            //获取到任务id 集合
            List<String> taskIds = list.stream().map(TaskPagingShowDTO::getId).collect(Collectors.toList());
            //获取总的任务文档数
            List<CountDTO> taskDocsCounts = taskDeliveryService.getTaskDocsCount(taskIds);
            List<TaskDocsFinishEntity> finishTasks = finishService.getByTaskIds(taskIds);
            Integer finish = TaskStateEnum.FINISH.getCode();
            for (TaskPagingShowDTO item : list) {
                String taskId = item.getId();
                String quoteSysTaskId = item.getQuoteSysTaskId();
                if(StringUtils.isNotBlank(quoteSysTaskId)){
                    item.setIfSysTask(true);
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
        }

        return new PagingVO(pageData);
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
        String chargeId = dto.getChargeId();
        String chargeName = commonService.getNameById(chargeId);
        taskEntity.setChargeId(chargeId);
        taskEntity.setChargeName(chargeName);
        //交付文档
        List<DocsDTO> deliveryDocsList = dto.getDeliveryDocsList();
        boolean flag = this.save(taskEntity);
        if (flag) {
            //保存交付文档
            taskDeliveryService.saveDeliveryDocs(loginUser.getUid(), taskEntity.getId(), dto.getProductId(), deliveryDocsList);
            //保存前置任务
            preTaskService.savePreTask(taskEntity.getId(), dto.getPreTaskIdList());
        }
        return flag;
    }


    /**
     * 根据产品id 获取任务id 和名字
     *
     * @param dto
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @author yl
     * @date 2022-09-22 17:40
     */
    @Override
    public List<Map<String, Object>> getTaskListByProductId(BasicProductIdDTO dto) {
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ProjectTaskEntity::getId, ProjectTaskEntity::getName);
        queryWrapper.eq(ProjectTaskEntity::getProductId, dto.getProductId());
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
        return this.removeById(entity);
    }


    /**
     * 根据产品id 获取到成员任务处理情况
     *
     * @param projectId
     * @return java.util.List<com.erp.model.plm.dto.TaskConductDTO>
     * @author yl
     * @date 2022-09-27 9:47
     */
    @Override
    public List<TaskConductDTO> getTaskConductList(String projectId) {
        Date date = new Date();
        List<TaskConductDTO> resultList = new LinkedList<>();
        List<ProjectTaskEntity> list = this.getByProjectId(projectId);
        //以成员分组
        Map<String, List<ProjectTaskEntity>> map = list.stream().
                collect(Collectors.groupingBy(ProjectTaskEntity::getChargeId));
        for (Map.Entry<String, List<ProjectTaskEntity>> item : map.entrySet()) {
            TaskConductDTO dto = new TaskConductDTO();
            dto.setMembersId(item.getKey());
            List<ProjectTaskEntity> taskList = list.stream().filter(t -> t.getChargeId().equals(item.getKey())).collect(Collectors.toList());

            //完成任务数
            int finishTaskCount = taskList.stream().filter(t -> TaskStateEnum.FINISH.getCode().equals(t.getStatus())).collect(Collectors.toList()).size();
            //进行中
            int ingTaskCount = taskList.stream().filter(t -> TaskStateEnum.ING.getCode().equals(t.getStatus())).collect(Collectors.toList()).size();
            //总任务数
            int totalTaskCount = taskList.size();
            //延期的任务数
            int postponeTaskCount = taskList.stream().filter(t -> date.compareTo(t.getPlanEndTime()) == 1).collect(Collectors.toList()).size();

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
        //完成任务数
        int finishTaskCount = list.stream().filter(t -> TaskStateEnum.FINISH.getCode().equals(t.getStatus())).collect(Collectors.toList()).size();
        //进行中
        int ingTaskCount = list.stream().filter(t -> TaskStateEnum.ING.getCode().equals(t.getStatus())).collect(Collectors.toList()).size();
        //总任务数
        int totalTaskCount = list.size();
        //延期的任务数
        int postponeTaskCount = list.stream().filter(t -> date.compareTo(t.getPlanEndTime()) == 1).collect(Collectors.toList()).size();
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
        //前置任务id集合
        List<String> preTaskIdList = preTaskService.getPreTaskIdList(taskId);
        //前置任务
        List<RefTaskInfoDTO> preTasks = new ArrayList<>();
        //子任务
        List<RefTaskInfoDTO> childTasks = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(preTaskIdList)) {
            preTasks = getRefTask(preTaskIdList);
        }
        detailsDTO.setPreTasks(preTasks);
        //获取到当前任务id 的子任务
        List<String> childTaskIds = getChildTaskIds(taskId, detailsDTO.getProductId());
        if (CollectionUtils.isNotEmpty(childTaskIds)) {
            childTasks = getRefTask(childTaskIds);
        }
        detailsDTO.setChildTasks(childTasks);
        detailsDTO.setOutputDocsList(taskDeliveryService.getByTaskId(taskId));
        return detailsDTO;
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
    public Boolean updateTask(ProjectTaskDTO dto) {
        checkTaskName(dto.getId(), dto.getProductId(), dto.getName());
        LoginUser loginUser = commonService.getUserInfo();
        ProjectTaskEntity taskEntity = new ProjectTaskEntity();
        BeanMapper.copy(dto, taskEntity);
        String chargeId = dto.getChargeId();
        String chargeName = commonService.getNameById(chargeId);
        taskEntity.setChargeId(chargeId);
        taskEntity.setChargeName(chargeName);
        //交付文档
        List<DocsDTO> deliveryDocsList = dto.getDeliveryDocsList();
        boolean flag = this.save(taskEntity);
        if (flag) {
            //保存交付文档
            taskDeliveryService.saveDeliveryDocs(loginUser.getUid(), taskEntity.getId(), dto.getProductId(), deliveryDocsList);
            //保存前置任务
            preTaskService.savePreTask(taskEntity.getId(), dto.getPreTaskIdList());
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
        //完成任务数
        int finishTaskCount = taskList.stream().filter(t -> TaskStateEnum.FINISH.getCode().equals(t.getStatus())).collect(Collectors.toList()).size();
        //未完成任务数
        int unfinishedTaskCount = taskList.stream().filter(t -> !TaskStateEnum.FINISH.getCode().equals(t.getStatus())).collect(Collectors.toList()).size();
        //总任务数
        int totalTaskCount = taskList.size();
        //延期的任务数
        int postponeTaskCount = taskList.stream().filter(t -> date.compareTo(t.getPlanEndTime()) == 1).collect(Collectors.toList()).size();
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
        LambdaUpdateWrapper<ProjectTaskEntity> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.eq(ProjectTaskEntity::getId, dto.getTaskId());
        //任务名
        String name = dto.getName();
        //开始时间
        Date planStartTime = dto.getPlanStartTime();
        //结束时间
        Date planEndTime = dto.getPlanStartTime();
        String chargeId = dto.getChargeId();
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.set(ProjectTaskEntity::getName, name);
        }
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.set(ProjectTaskEntity::getName, name);
        }
        if (planStartTime != null) {
            queryWrapper.set(ProjectTaskEntity::getPlanStartTime, planStartTime);
        }
        if (planEndTime != null) {
            queryWrapper.set(ProjectTaskEntity::getPlanEndTime, planStartTime);
        }
        if (StringUtils.isNotBlank(chargeId)) {
            queryWrapper.set(ProjectTaskEntity::getChargeId, chargeId);
            String chargeName = commonService.getNameById(chargeId);
            queryWrapper.set(ProjectTaskEntity::getChargeName, chargeName);
        }
        return this.update(queryWrapper);
    }


    private void checkTaskIfExistPid(String taskId) {
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskEntity::getPid, taskId);
        ProjectTaskEntity entity = baseMapper.selectOne(queryWrapper);
        if (entity != null) {
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
        ProjectTaskEntity pidTask = taskList.stream().filter(t -> taskId.equals(t.getPid())).findFirst().orElse(null);
        if (pidTask != null) {
            resultList.add(pidTask.getId());
            //递归获取他的子任务id
            getChilds(pidTask.getId(), taskList, resultList);
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
            taskList = taskList.stream().filter(t -> !taskId.equals(t.getPid())).collect(Collectors.toList());
        }
        List<String> taskNames = taskList.stream().map(ProjectTaskEntity::getName).collect(Collectors.toList());
        //获取系统的任务名
        List<String> sysTaskNames = projectTaskSysService.getSysTaskNames();
        taskNames.addAll(sysTaskNames);
        if (taskNames.contains(name)) {
            throw new ServiceException(ApiError.ERROR_95013);
        }
    }

    //获取预警信息
    public String getWarning(Integer state, Integer finishState, Date planEndTime) {
        Date nowDay = new Date();
        String warning = "-";
        //状态
        if (!finishState.equals(state)) {
            int difference = DateUtil.getDiffDay(planEndTime, nowDay);
            if (difference > 0) {
                warning = "过期" + difference + "天";
            } else {
                if (difference >= -2) {
                    warning = Math.abs(difference) + 1 + "天后过期";
                }
            }
        }
        return warning;
    }


}
