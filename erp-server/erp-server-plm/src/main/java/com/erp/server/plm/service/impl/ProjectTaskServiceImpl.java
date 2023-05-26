package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.excel.util.DateUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.IsConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BaseStatusEnum;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.service.RedisService;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.*;
import com.erp.model.plm.vo.PreTaskVO;
import com.erp.model.plm.vo.ScheduleTaskExportExcelVO;
import com.erp.model.plm.vo.ScheduleTaskVO;
import com.erp.model.sys.dto.SysCalendarDTO;
import com.erp.model.sys.dto.UserSuperiorDTO;
import com.erp.model.sys.enums.ChargeSuperiorEnum;
import com.erp.model.sys.vo.SysCalendarListVO;
import com.erp.model.workflow.dto.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.ProjectPlanConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.mapper.ProjectTaskMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    private TaskOperatorRecordService taskOperatorRecordService;
    @Autowired
    private TaskCommentService taskCommentService;

    @Autowired
    private TaskDocsFinishService taskDocsFinishService;


    @Autowired
    private ProjectMembersService projectMembersService;

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    @Lazy
    private NoticeMessageService noticeMessageService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TaskRefSkuConfigService taskRefSkuConfigService;

    @Autowired
    private ProjectTaskRefSkuService projectTaskRefSkuService;


    @Autowired
    private ProductDetailService productDetailService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private TaskChargeDistributionService taskChargeDistributionService;


    @Autowired
    private ProjectTaskTimeRecordService projectTaskTimeRecordService;

    /**
     * 添加系统的产品任务
     * 只添加立项模板的任务
     *
     * @param productId
     * @param productPropertyId 产品属性id
     * @return void
     * @author yl
     * @date 2022-09-17 10:32
     */
    @Transactional
    @Override
    public List<ProjectTaskEntity> addSysTask(String productId, List<TaskDocsNameEntity> taskDocsNameList, LoginUser loginUser, String productPropertyId) {
        return new ArrayList<>();
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
                entity.setPrevId(entity.getId());
                entity.setId(IdWorker.getIdStr());
                entity.setStatus(TaskStateEnum.TO_BE_RELEASED.getCode());
            }
            this.saveBatch(list);
            //复制审核人信息
            for (ProjectTaskEntity entity : list) {
                //查询模板任务下审核人
                List<TaskChargeDistributionEntity> taskChargeDistributionList = taskChargeDistributionService.listBySourceAndTaskId(MathUtil.THREE, entity.getPrevId());
                //保存交付文档的审核人
                taskChargeDistributionService.removeAndSave(entity.getId(), taskChargeDistributionList, MathUtil.THREE);
            }
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
    public Pair<List<String>, List<ProjectTaskEntity>> copyTaskBySys(String saveProductId, String saveProjectId) {
        return null;
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
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        TaskPagingDTO params = dto.getParams();
        Integer taskFlag = params.getTaskFlag();
        List<Integer> statusList = params.getStatusList();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = new Page();

        //这个是我完成的任务
        if (TaskConstant.MY_FINISH_TASK.equals(taskFlag)) {
            statusList.add(TaskStateEnum.PORTION_FINISH.getCode());
            pageData = baseMapper.paging(query, params, userId);
        }
        //这个待我审核的任务
        if (TaskConstant.MY_APPROVAL_TASK.equals(taskFlag)) {
            List<TaskShowDTO> myToDoList = workflowFeign.queryMyToDo(userId);
            statusList.add(TaskStateEnum.APPROVAL_ING.getCode());
            //获取流程集合
            List<String> processIds = myToDoList.stream().map(TaskShowDTO::getProcessInstanceId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(processIds)) {
                pageData = baseMapper.myApprovalPaging(query, params, processIds);
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
            pageData = baseMapper.allPaging(query, params);
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

            //任务关联字段配置
            List<TaskRefSkuConfigEntity> refSkuConfigList = taskRefSkuConfigService.getByTaskIds(taskIds);

            Integer finish = TaskStateEnum.FINISH.getCode();
            //根据产品id 获取到所有的 任务信息
            //   List<TaskPagingShowDTO> allList = getAllChildrenList(productId);

            //产品id
            List<String> productIds = list.stream().map(TaskPagingShowDTO::getProductId).collect(Collectors.toList());
            List<ProductInfoEntity> productList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(productIds)) {
                productList = productInfoService.listByIds(productIds);
            }
            List<PreTaskEntity> preTaskList = preTaskService.getPreTaskListBytaskIds(taskIds);
            //前置任务
            List<ProjectTaskEntity> preTaskEntityList = this.getByTaskIds(preTaskList.stream().map(PreTaskEntity::getPreTaskId).collect(Collectors.toList()));

            for (TaskPagingShowDTO item : list) {
                String taskId = item.getId();
                Integer state = item.getStatus();
                item.setStatusName(TaskStateEnum.getName(state));

                String chargeId = item.getChargeId();
                String quoteSysTaskId = item.getQuoteSysTaskId();
                if (StringUtils.isNotBlank(quoteSysTaskId)) {
                    item.setIsSysTask(true);
                }
                if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(item.getDistributionType())) {
                    //负责人为角色
                    if (StringUtils.isNotBlank(chargeId)) {
                        item.setChargeIdList(Arrays.asList(chargeId.split(",")));
                    } else {
                        item.setChargeIdList(Arrays.asList(item.getRoleName().split(",")));
                    }
                } else {
                    if (StringUtils.isNotBlank(chargeId)) {
                        item.setChargeIdList(Arrays.asList(chargeId.split(",")));
                    } else {
                        item.setChargeIdList(new ArrayList<>());
                    }
                }

                String warning = getWarning(item.getStatus(), finish, item.getPlanEndTime());
                item.setWarning(warning);
                Integer totalDocsCount = 0;
                CountDTO countDTO = taskDocsCounts.stream().filter(d -> d.getFlagId().equals(taskId)).findFirst().orElse(null);
                if (countDTO != null) {
                    totalDocsCount = countDTO.getCount();
                }
                TaskRefSkuConfigEntity refSku = refSkuConfigList.stream().filter(r -> r.getTaskId().equals(taskId)).findFirst().orElse(null);
                if (refSku != null) {
                    item.setTaskFieldConfigType(refSku.getFieldConfigType());
                }
                item.setTotalDocsCount(totalDocsCount);
                Integer finishDocsCount = finishTasks.stream().filter(f -> taskId.equals(f.getTaskId())).map(TaskDocsFinishEntity::getTaskDocsId).distinct().collect(Collectors.toList()).size();
                item.setFinishDocsCount(finishDocsCount);
                Boolean ifEditTask = getIfEditTask(item.getType(), item.getStatus());
                item.setIfEditTask(ifEditTask);
                List<Map<String, Object>> operateList = getOperateList(state, totalDocsCount, finishDocsCount);
                item.setOperateList(operateList);
                ProductInfoEntity product = productList.stream().filter(p -> p.getId().equals(item.getProductId())).findFirst().orElse(null);
                if (product != null) {
                    item.setProductName(product.getName());
                }

                List<String> preTaskIds = preTaskList.stream().filter(p -> p.getTaskId().equals(item.getId())).map(PreTaskEntity::getPreTaskId).collect(Collectors.toList());
                int totalPreTaskCount = preTaskIds.size();
                item.setTotalPreTaskCount(totalPreTaskCount);
                List<String> preTaskNameList = preTaskEntityList.stream().filter(t -> preTaskIds.contains(t.getId())).map(ProjectTaskEntity::getName).collect(Collectors.toList());
                int finishPreTaskCount = (int) preTaskEntityList.stream().filter(t -> finish.equals(t.getStatus()) && preTaskIds.contains(t.getId())).count();
                item.setPreTaskNameList(preTaskNameList);
                item.setFinishPreTaskCount(finishPreTaskCount);

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
                Integer finishDocsCount = finishTasks.stream().filter(f -> taskId.equals(f.getTaskId())).map(TaskDocsFinishEntity::getTaskDocsId).distinct().collect(Collectors.toList()).size();
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean save(ProjectTaskDTO dto) {
        //验证名称是否重复
        checkTaskName(dto.getId(), dto.getProductId(), dto.getName());
        productInfoService.checkProduct(dto.getProductId());
        //验证表单数据
        checkFieldConfig(dto);
        List<TaskChargeDistributionDTO> approvalList = dto.getApprovalList();
        LoginUser loginUser = commonService.getUserInfo();
        ProjectTaskEntity taskEntity = new ProjectTaskEntity();
        BeanMapper.copy(dto, taskEntity);
        ProjectPhaseEntity phaseEntity = projectPhaseService.getById(dto.getPhaseId());
        String phaseName = "";
        if (phaseEntity != null) {
            phaseName = phaseEntity.getName();
        }

        if (TaskConstant.APPROVAL_TASK_PHASE.equals(phaseName)) {
            taskEntity.setProperty(TaskConstant.APPROVAL_TASK);
        } else {
            taskEntity.setProperty(TaskConstant.PROJECT_TASK);
        }
        List<String> chargeId = dto.getChargeIds();
        String chargeNames = commonService.getNameByIds(chargeId);
        taskEntity.setChargeId(String.join(",", chargeId));
        taskEntity.setChargeName(chargeNames);
        taskEntity.setPhaseName(phaseName);
        taskEntity.setCreateUserId(loginUser.getUid());
        taskEntity.setCreateUserName(loginUser.getUserName());
        if (null != dto.getWorkPeriod() && 0 < dto.getWorkPeriod()) {
            taskEntity.setWorkPeriod(dto.getWorkPeriod());
        }
        //交付文档
        List<DocsDTO> deliveryDocsList = dto.getDeliveryDocsList();
        boolean flag = this.save(taskEntity);
        if (flag) {
            //新增操作日志
            sysLogService.addSysLogBySave("新增了一个：[" + taskEntity.getName() + "]", SysLogClassPathEnum.PROJECTTASKENTITY.getDesc(), taskEntity.getId(), null);
            if (StringUtils.isNotBlank(taskEntity.getPid())) {
                //创建子任务时父级任务新增操作日志
                SysLogEntity sysLogEntity = new SysLogEntity().setContent(String.format("创建子任务[%s]", taskEntity.getName()))
                        .setBusinessId(taskEntity.getPid())
                        .setClassPath(SysLogClassPathEnum.PROJECTTASKENTITY.getDesc());
                sysLogService.addSysLogByOther(sysLogEntity);
            }
            List<TaskChargeDistributionEntity> taskChargeDistributionList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(approvalList)) {
                approvalList.forEach(obj -> {
                    obj.setCharges(String.join(",", obj.getChargeList()));
                    obj.setDistributionType(ObjectUtils.isEmpty(obj.getDistributionType()) ? DistributionTypeEnum.DISTRIBUTION_USER.getCode() : obj.getDistributionType());
                });
                taskChargeDistributionList = BeanMapperUtils.copyList(TaskChargeDistributionEntity.class, approvalList);
                //根据分配类型查询模板中的数据
                for (TaskChargeDistributionEntity taskChargeDistributionEntity : taskChargeDistributionList) {
                    String charges = taskChargeDistributionEntity.getCharges();
                    if (StringUtils.isBlank(taskChargeDistributionEntity.getCharges())) {
                        throw new ServiceException(ApiError.ERROR_95097);
                    }
                    if (ObjectUtils.isEmpty(taskChargeDistributionEntity.getDistributionType()) || DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(taskChargeDistributionEntity.getDistributionType())) {
                        taskChargeDistributionEntity.setChargeIds(charges);
                    }
                }
            }
            //保存交付文档的审核人
            taskChargeDistributionService.removeAndSave(taskEntity.getId(), taskChargeDistributionList, MathUtil.THREE);
            //保存交付文档
            taskDeliveryService.saveDeliveryDocs(taskEntity.getId(), dto.getProductId(), deliveryDocsList);
            //保存前置任务
            preTaskService.savePreTask(taskEntity.getId(), dto.getPreTaskIdList(), dto.getProductId());

            //保存SKU配置 字段 关系表
            taskRefSkuConfigService.addSkuField(taskEntity.getId(), taskEntity.getProductId(), dto.getFieldConfigType(), dto.getFieldJson());
            //保存任务与SKU 关系表
            projectTaskRefSkuService.addTaskSkuRef(taskEntity.getId(), taskEntity.getProductId(), dto.getRefSkuIdList());
            List<ProjectTaskEntity> taskList = new ArrayList<>();
            taskList.add(taskEntity);
            noticeMessageService.newTaskNotice(loginUser.getUserName(), taskList, dto.getProductId());
        }

        return flag;
    }


    /**
     * 自动操作一步
     * taskType 任务属性 是一般任务 还是评审任务
     *
     * @param taskEntity   任务实体
     * @param taskType     任务类型
     * @param chargeIdList 负责人id
     * @param userId       用户id
     * @return com.erp.model.plm.entity.ProjectTaskEntity
     * @author yl
     * @date 2022-11-30 10:56
     */
    public ProjectTaskEntity automationTask(ProjectTaskEntity taskEntity, Integer taskType, List<String> chargeIdList, String userId) {
        //审核任务
        Integer reviewTask = TaskTypeEnum.REVIEW_TASK.getCode();
        //一般任务
        Integer generalTask = TaskTypeEnum.GENERAL_TASK.getCode();

        taskEntity.setProperty(TaskConstant.APPROVAL_TASK);
        //如果是一般任务就变成待开始
        if (generalTask.equals(taskType)) {
            taskEntity.setStatus(TaskStateEnum.NOT_START.getCode());
        }
        //如果是审核任务就变成开启流程并变成待审核
        if (reviewTask.equals(taskType)) {
            String businessKey = BusinessProcessEnum.REVIEW_TASK.getBusinessKey();
            BusinessProcessEntity processEntity = businessProcessService.getProcessByBusinessKey(businessKey);
            if (processEntity != null) {
                //评审任务 由任务负责人审核
                if (CollectionUtils.isNotEmpty(chargeIdList)) {
                    LocalDateTime nowDate = LocalDateTime.now();
                    //待审核
                    Integer waitConfirmCode = TaskStateEnum.WAIT_CONFIRM.getCode();
                    StartProcessDTO startProcess = new StartProcessDTO();
                    startProcess.setBusinessKey(processEntity.getBusinessKey());
                    startProcess.setProcessDefinitionKey(processEntity.getProcessDefinitionKey());
                    startProcess.setUserId(userId);
                    Map<String, Object> parameterMap = new HashMap<>();
                    String params = processEntity.getParam();
                    if (StringUtils.isNotBlank(params)) {
                        String[] paramList = params.split(",");
                        if (paramList.length == 1) {
                            parameterMap.put(paramList[0], chargeIdList);
                        }
                    }
                    startProcess.setParameterMap(parameterMap);
                    //启动一个流程
                    ProcessNodeDTO process = workflowFeign.startProcess(startProcess);
                    //这个是流程Id
                    String processId = process.getProcessId();
                    //流程id 不为空 表示成功
                    if (StringUtils.isNotBlank(processId)) {
                        taskEntity.setProcessId(processId);
                        taskEntity.setRealityStartTime(nowDate);
                        taskEntity.setStatus(waitConfirmCode);
                    }
                }
            }
        }
        return taskEntity;
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
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        LoginUser loginUser = commonService.getUserInfo();

        String scheduleStatus = entity.getScheduleStatus();
        if (BaseStatusEnum.AUDIT_PASS.getStatus().equals(scheduleStatus)) {
            if (!"admin".equals(loginUser.getUserAccount())) {
                throw new ServiceException(ApiError.ERROR_95137);
            }
        }
        Integer IsFixed = entity.getIsFixed();

        //如果是固定任务
        if (IsConstant.YES.equals(IsFixed) && !"admin".equals(loginUser.getUserAccount())) {
            throw new ServiceException(ApiError.ERROR_95014);
        }
        //检查是否是子任务
        checkTaskIfExistPid(taskId);
        Boolean flag = this.removeById(entity);
        if (flag) {
            taskChargeDistributionService.removeBySourceAndTaskId(MathUtil.THREE, taskId);
            taskDeliveryService.removeByTaskId(taskId);
            taskDocsFinishService.removeByTaskId(taskId);
            taskRefSkuConfigService.deleteByTaskId(taskId);
            preTaskService.deleteByTaskId(taskId);
            //发送删除任务通知
            noticeMessageService.deleteTaskNotice(loginUser.getUserName(), entity, entity.getProductId());
            //新增操作日志
            SysLogEntity sysLogEntity = new SysLogEntity().setContent(String.format("删除任务[%s]", entity.getName()))
                    .setBusinessId(entity.getProductId())
                    .setClassPath(SysLogClassPathEnum.PRODUCTINFOENTITY.getDesc());
            //添加日志
            sysLogService.addSysLogByOther(sysLogEntity);
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
        List<TaskConductDTO> resultList = new LinkedList<>();
        List<ProjectTaskEntity> list = this.getByProductId(productId);
        List<ProjectMembersEntity> membersList = projectMembersService.getListByProductId(productId);
        Integer finishTask = TaskStateEnum.FINISH.getCode();
        for (ProjectMembersEntity item : membersList) {
            TaskConductDTO dto = new TaskConductDTO();
            dto.setMembersId(item.getMemberId());
            List<ProjectTaskEntity> taskList = list.stream().filter(t -> t.getChargeId().contains(item.getMemberId())).collect(Collectors.toList());
            //完成任务数
            int finishTaskCount = taskList.stream().filter(t -> finishTask.equals(t.getStatus())).collect(Collectors.toList()).size();
            //进行中
            int ingTaskCount = taskList.stream().filter(t -> TaskStateEnum.ING.getCode().equals(t.getStatus())).collect(Collectors.toList()).size();
            //总任务数
            int totalTaskCount = taskList.size();
            //延期的任务数
            int postponeTaskCount = 0;
            postponeTaskCount = taskList.stream().filter(t -> t.getPlanEndTime() != null && t.getRealityEndTime() != null && t.getRealityEndTime().compareTo(LocalDateTimeUtil.of(t.getPlanEndTime())) == 1).collect(Collectors.toList()).size();
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
        int postponeTaskCount = list.stream().filter(t -> t.getPlanEndTime() != null && t.getRealityEndTime() != null && t.getRealityEndTime().compareTo(LocalDateTimeUtil.of(t.getPlanEndTime())) == 1).collect(Collectors.toList()).size();
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
                    BusinessProcessEnum.DOCS_CHANGE.getBusinessKey().equals(processEntity.getBusinessType())) {
                Integer taskState = detailsDTO.getTaskState();
                detailsDTO.setChangeDocsProcessState(TaskStateEnum.getName(taskState));
            }
        }
        StringBuffer planTime = new StringBuffer();
        if (detailsDTO.getPlanStartTime() != null) {
            planTime.append(LocalDateTimeUtil.format(detailsDTO.getPlanStartTime(), DateUtil.fmt_day));
        }
        planTime.append(" - ");
        if (detailsDTO.getPlanEndTime() != null) {
            planTime.append(LocalDateTimeUtil.format(detailsDTO.getPlanEndTime(), DateUtil.fmt_day));
        }
        detailsDTO.setPlanTime(planTime.toString());

        StringBuffer realityTime = new StringBuffer();
        if (detailsDTO.getRealityStartTime() != null) {
            realityTime.append(LocalDateTimeUtil.format(detailsDTO.getRealityStartTime(), DateUtil.fmt_day));
        }
        realityTime.append(" - ");
        if (detailsDTO.getRealityEndTime() != null) {
            realityTime.append(LocalDateTimeUtil.format(detailsDTO.getRealityEndTime(), DateUtil.fmt_day));
        }
        detailsDTO.setRealityTime(realityTime.toString());
        //前置任务id集合
        List<PreTaskVO> preTaskList = preTaskService.getPreTaskIdList(taskId);
        //前置任务
        List<RefTaskInfoDTO> preTasks = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(preTaskList)) {
            preTasks = getRefTask(preTaskList.stream().map(PreTaskVO::getPreTaskId).collect(Collectors.toList()));
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
        detailsDTO.setCreateUserName(commonService.getNameById(detailsDTO.getCreateUserId()));
        //获取交付文档
        List<DeliveryDocsDTO> docsList = taskDeliveryService.getByTaskId(taskId);
        //获取到任务的属性
        Integer taskProperty = getTaskProperty(taskEntity);
        Integer taskState = taskEntity.getStatus();
        Integer generalApproval = TaskProcessTypeEnum.GENERAL_APPROVAL_TASK.getCode();
        Integer reviewTask = TaskProcessTypeEnum.REVIEW_TASK.getCode();
        Boolean isApprovalPass = TaskStateEnum.FINISH.getCode().equals(taskEntity.getStatus());


        for (DeliveryDocsDTO docs : docsList) {
            //当审核通过就是完成
            if (isApprovalPass) {
                docs.setOldFileUrl(docs.getFileUrl());
                docs.setOldFileName(docs.getFileName());
                docs.setOldUploadType(docs.getUploadType());
            }

            // 如果任务类型是审核的
            if (taskProperty.equals(generalApproval) || taskProperty.equals(reviewTask)) {
                //如果审核通过可以变更
                if (taskState.equals(TaskStateEnum.FINISH.getCode())) {
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
        //查询项目文档（同产品输出物）
        if (StringUtils.isNotBlank(detailsDTO.getProductId())) {
            List<DeliveryDocsDTO> productDocsList = taskDeliveryService.listProductDocs(detailsDTO.getProductId());
            if (CollectionUtils.isNotEmpty(productDocsList)) {
                detailsDTO.setProductDocsList(productDocsList);
            }
        }

        TaskRefSkuConfigEntity refSku = taskRefSkuConfigService.getByTaskId(taskId);
        List<ProjectTaskRefSkuEntity> taskRefSkuList = projectTaskRefSkuService.getByTaskId(taskId);
        List<Map<String, Object>> refSkuFinishList = new ArrayList<>(taskRefSkuList.size());
        List<String> skuIdList = taskRefSkuList.stream().map(ProjectTaskRefSkuEntity::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = productDetailService.getByIdList(skuIdList);
        List<ProductDetailEntity> details = productDetailService.getSkuListByProductId(taskEntity.getProductId());
        //如果任务设置的自动关联，则查询产品下未关联的sku
        if (RelatedSkuTypeEnum.ALL_RELATED.getCode().equals(taskEntity.getRelatedSkuType())) {
            if (CollectionUtils.isNotEmpty(details)) {
                List<ProductDetailEntity> noRelatedList = details.stream().filter(obj -> !skuIdList.contains(obj.getId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(noRelatedList)) {
                    //未关联上的sku
                    for (ProductDetailEntity entity : noRelatedList) {
                        Map<String, Object> refSkuMap = new HashMap<>();
                        refSkuMap.put("skuId", entity.getId());
                        refSkuMap.put("skuNo", entity.getSkuNo());
                        refSkuMap.put("isFinishTask", IsConstant.YES);
                        refSkuMap.put("isGenerated", IsConstant.NO);
                        skuIdList.add(entity.getId());
                        refSkuFinishList.add(refSkuMap);
                    }
                }

            }
        }

        for (ProjectTaskRefSkuEntity refSkuItem : taskRefSkuList) {
            Map<String, Object> refSkuMap = new HashMap<>();
            String skuId = refSkuItem.getSkuId();
            refSkuMap.put("skuId", skuId);
            ProductDetailEntity detail = productDetailList.stream().filter(d -> skuId.equals(d.getId())).findFirst().orElse(null);
            if (!Objects.isNull(detail)) {
                refSkuMap.put("skuNo", detail.getSkuNo());
            } else {
                refSkuMap.put("skuNo", "");
            }
            refSkuMap.put("isFinishTask", refSkuItem.getIsFinishTask());
            refSkuFinishList.add(refSkuMap);
        }
        detailsDTO.setRefSkuFinishList(refSkuFinishList);
        if (refSku != null) {
            detailsDTO.setFieldJson(refSku.getFieldJson());
            detailsDTO.setFieldConfigType(refSku.getFieldConfigType());
        }
        detailsDTO.setRefSkuIdList(skuIdList);
        List<String> skuNoList = details.stream().filter(d -> skuIdList.contains(d.getId())).map(ProductDetailEntity::getSkuNo).collect(Collectors.toList());
        detailsDTO.setRefSkuNoList(skuNoList);

        SysCalendarDTO.ListDTO listDTO = new SysCalendarDTO.ListDTO();
        listDTO.setIsWorkDay(Boolean.FALSE);
        List<SysCalendarListVO> holidayList = sysUserFeign.listCalendar(listDTO);
        List<LocalDate> holidays = holidayList.stream().map(SysCalendarListVO::getCalendarDate).collect(Collectors.toList());
        // 计算计划工时
        // 赋值
        detailsDTO.setPlanWorkTime(initWorkTime(detailsDTO.getPlanStartTime(), detailsDTO.getPlanEndTime(), holidays));
        detailsDTO.setRealWorkTime(initWorkTime(detailsDTO.getRealityStartTime(), detailsDTO.getRealityEndTime(), holidays));
        return detailsDTO;
    }

    /**
     * 计算工期
     *
     * @param planStartTime
     * @param planEndTime
     * @param holidays
     * @return
     */
    private Integer initWorkTime(LocalDateTime planStartTime, LocalDateTime planEndTime, List<LocalDate> holidays) {
        Integer planWorkTime = 0;
        if (null != planStartTime && null != planEndTime) {
            planWorkTime = LocalDateUtil.countDaysForLocalDate(planStartTime.toLocalDate(), planEndTime.toLocalDate(), holidays);
        }
        return planWorkTime;
    }


    /**
     * 获取任务属性
     * 是一般任务 还是一般带审核  还是待审核
     *
     * @return
     */
    @Override
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


    /**
     * 修改 任务信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-13 9:25
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTask(ProjectTaskDTO dto) {
        checkTaskName(dto.getId(), dto.getProductId(), dto.getName());
        //sku不关联
        String notRelated = RelatedSkuTypeEnum.NOT_RELATED.getCode();
        //是否关联
        boolean isNotRelated = notRelated.equalsIgnoreCase(dto.getRelatedSkuType());
        //验证表单数据
        checkFieldConfig(dto);
        ProjectTaskEntity taskEntity = this.getById(dto.getId());
        String dbBusinessProcessId = taskEntity.getBusinessProcessId();
        String parameterBusinessProcessId = dto.getBusinessProcessId();

        //是否修改流程 true 是
        boolean ifUpdateProcess = !dbBusinessProcessId.equals(parameterBusinessProcessId);

        ProjectTaskEntity oldEntity = new ProjectTaskEntity();
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        BeanMapper.copy(taskEntity, oldEntity);
        //交付文档
        List<DocsDTO> deliveryDocsList = dto.getDeliveryDocsList();
        //固定任务不能修改任务名称、目标交付物、审核流程
        if (MathUtil.ONE.equals(taskEntity.getIsFixed())) {
            //任务名称
            if (!taskEntity.getName().equals(dto.getName())) {
                throw new ServiceException(ApiError.ERROR_95110);
            }
            //目标交付文档
            List<DeliveryDocsDTO> docsList = taskDeliveryService.getByTaskId(dto.getId());
            List<String> newDocs = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(deliveryDocsList)) {
                newDocs = deliveryDocsList.stream().map(DocsDTO::getName).sorted().collect(Collectors.toList());
            }
            List<String> oldDocs = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(docsList)) {
                oldDocs = docsList.stream().map(DeliveryDocsDTO::getDeliveryDocsName).sorted().collect(Collectors.toList());
            }
            //比较交付文档
            boolean equalList = ListUtils.isEqualList(newDocs, oldDocs);
            if (!equalList) {
                throw new ServiceException(ApiError.ERROR_95111);
            }
            //审核流程
            if (!StringUtils.equals(dto.getBusinessProcessId(), taskEntity.getBusinessProcessId())) {
                throw new ServiceException(ApiError.ERROR_95112);
            }
        }

        String processId = taskEntity.getProcessId();
        BeanMapper.copy(dto, taskEntity);
        Integer priority = dto.getPriority();
        if (priority == null) {
            taskEntity.setPriority(0);
        }

        taskEntity.setBusinessProcessId(parameterBusinessProcessId);
        taskEntity.setProcessId(processId);
        LoginUser loginUser = commonService.getUserInfo();
        ProjectPhaseEntity phaseEntity = projectPhaseService.getById(dto.getPhaseId());
        String phaseName = "";
        if (phaseEntity != null) {
            phaseName = phaseEntity.getName();
        }
        if (TaskConstant.APPROVAL_TASK_PHASE.equals(phaseName)) {
            taskEntity.setProperty(TaskConstant.APPROVAL_TASK);
        } else {
            taskEntity.setProperty(TaskConstant.PROJECT_TASK);
        }

        List<String> chargeIds = dto.getChargeIds();

        taskEntity.setPhaseName(phaseName);
        if (null != dto.getWorkPeriod() && 0 < dto.getWorkPeriod()) {
            taskEntity.setWorkPeriod(dto.getWorkPeriod());
        }
        dto.setPhaseName(phaseName);
        //如果分配类型为角色，则需要更新底层角色名称字段
        if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(taskEntity.getDistributionType())) {
            if (CollectionUtils.isNotEmpty(chargeIds)) {
                //如果传入负责人和角色一致则无需插入
                String[] splits = taskEntity.getRoleName().split(",");
                for (String roleName : splits) {
                    if (chargeIds.contains(roleName)) {
                        chargeIds.remove(roleName);
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(chargeIds)) {
            String chargeName = commonService.getNameByIds(chargeIds);
            taskEntity.setChargeId(String.join(",", chargeIds));
            taskEntity.setChargeName(chargeName);
        }


        //交付文档名称
        if (CollectionUtils.isNotEmpty(deliveryDocsList)) {
            String deliveryDocsNames = deliveryDocsList.stream().map(DocsDTO::getName).collect(Collectors.joining(","));
            dto.setDeliveryDocsNames(deliveryDocsNames);
        }
        //前置任务名称
        if (CollectionUtils.isNotEmpty(dto.getPreTaskIdList())) {
            List<ProjectTaskEntity> projectTaskList = this.listByIds(dto.getPreTaskIdList());
            if (CollectionUtils.isNotEmpty(projectTaskList)) {
                String preTaskNames = projectTaskList.stream().map(ProjectTaskEntity::getName).collect(Collectors.joining(","));
                dto.setPreTaskNames(preTaskNames);
            }
        }
        //关联sku名称查询
        if (CollectionUtils.isNotEmpty(dto.getRefSkuIdList())) {
            List<ProductDetailEntity> productDetailList = productDetailService.listByIds(dto.getRefSkuIdList());
            if (CollectionUtils.isNotEmpty(productDetailList)) {
                List<String> refSkuNoList = productDetailList.stream().map(ProductDetailEntity::getSkuNo).collect(Collectors.toList());
                dto.setRefSkuNoList(refSkuNoList);
            }
        }

        //新增任务操作日志
        addProjectTaskDTOLog(dto, oldEntity, taskEntity.getId());
        //更新审核人
        setTaskChargeDistributionEntity(dto, taskEntity, ifUpdateProcess);


        boolean flag = this.updateById(taskEntity);
        if (flag) {

            //保存交付文档
            taskDeliveryService.saveDeliveryDocs(taskEntity.getId(), dto.getProductId(), deliveryDocsList);
            //保存前置任务
            preTaskService.savePreTask(taskEntity.getId(), dto.getPreTaskIdList(), dto.getProductId());

            //保存SKU配置 字段 关系表 当不关联的时候删除
            if (!isNotRelated) {
                taskRefSkuConfigService.addSkuField(taskEntity.getId(), taskEntity.getProductId(), dto.getFieldConfigType(), dto.getFieldJson());
                //保存任务与SKU 关系表
                projectTaskRefSkuService.addTaskSkuRef(taskEntity.getId(), taskEntity.getProductId(), dto.getRefSkuIdList());
            } else {
                projectTaskRefSkuService.removeTaskSkuRefByTaskId(taskEntity.getId());
                taskRefSkuConfigService.removeTaskRefSkuByTaskId(taskEntity.getId());
            }


            noticeMessageService.editTaskNotice(loginUser.getUserName(), taskEntity, taskEntity.getProductId());
        }
        return flag;
    }


    /**
     * 根据产品id 获取任务数量信息
     *
     * @param showDTO
     * @return com.erp.model.plm.dto.ProductTaskCountDTO
     * @author yl
     * @date 2022-10-13 16:51
     */
    @Override
    public ProductTaskCountDTO getProductTaskCount(ProductTaskCountShowDTO showDTO, Date date) {
        List<ProjectTaskEntity> taskList = baseMapper.getProjectTaskByProductId(showDTO);
        //List<ProjectTaskEntity> taskList = this.getByProductId(showDTO.getProductId());
        Integer finishState = TaskStateEnum.FINISH.getCode();
        Integer approvalPassState = TaskStateEnum.APPROVAL_PASS.getCode();
        //完成任务数
        int finishTaskCount = taskList.stream().filter(t -> finishState.equals(t.getStatus()) || approvalPassState.equals(t.getStatus())).collect(Collectors.toList()).size();
        //未完成任务数
        int unfinishedTaskCount = taskList.stream().filter(t -> !finishState.equals(t.getStatus()) && !approvalPassState.equals(t.getStatus())).collect(Collectors.toList()).size();
        //总任务数
        int totalTaskCount = taskList.size();
        //延期的任务数
        int postponeTaskCount = taskList.stream().filter(t -> t.getPlanEndTime() != null && t.getRealityEndTime() != null && t.getRealityEndTime().compareTo(LocalDateTimeUtil.of(t.getPlanEndTime())) == 1).collect(Collectors.toList()).size();
        ProductTaskCountDTO taskCountDTO = new ProductTaskCountDTO();
        taskCountDTO.setFinishTaskCount(finishTaskCount);
        taskCountDTO.setUnfinishedTaskCount(unfinishedTaskCount);
        taskCountDTO.setTotalTaskCount(totalTaskCount);
        taskCountDTO.setPostponeTaskCount(postponeTaskCount);

        ProductInfoEntity productInfoEntity = productInfoService.getById(showDTO.getProductId());
        taskCountDTO.setPropertyId(productInfoEntity.getPropertyId());
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
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DateUtil.fmt_day);
        String updateJson = JSONObject.toJSONString(dto);
        Map<String, Object> updateMap = JSONObject.parseObject(updateJson, Map.class);
        ProjectTaskEntity taskEntity = this.getById(dto.getTaskId());
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        ProjectTaskEntity oldEntity = new ProjectTaskEntity();
        BeanMapperUtils.copy(taskEntity, oldEntity);

        LoginUser loginUser = commonService.getUserInfo();
        //任务名
        String name = dto.getName();
        //固定任务不能修改名称
        if (MathUtil.ONE.equals(taskEntity.getIsFixed()) && StringUtils.isNotBlank(name)) {
            if (!taskEntity.getName().equals(name)) {
                throw new ServiceException(ApiError.ERROR_95110);
            }
        }
        if (updateMap.containsKey("planStartTime")) {
            String planStartTime = dto.getPlanStartTime();
            if (StringUtils.isNotBlank(planStartTime)) {
                taskEntity.setPlanStartTime(LocalDate.from(DateTimeFormatter.ofPattern(DateUtil.fmt_day).parse(planStartTime)));
            } else {
                taskEntity.setPlanStartTime(null);
            }

        }
        if (updateMap.containsKey("planEndTime")) {
            //结束时间
            String planEndTime = dto.getPlanEndTime();
            if (StringUtils.isNotBlank(planEndTime)) {
                taskEntity.setPlanEndTime(LocalDate.from(DateTimeFormatter.ofPattern(DateUtil.fmt_day).parse(planEndTime)));
            } else {
                taskEntity.setPlanEndTime(null);
            }
        }
        List<String> chargeIdList = dto.getChargeIdList();
        if (StringUtils.isNotBlank(name)) {
            checkTaskName(taskEntity.getId(), taskEntity.getProductId(), name);
            taskEntity.setName(name);
        }
        //如果分配类型为角色
        if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(taskEntity.getDistributionType())) {
            if (CollectionUtils.isNotEmpty(chargeIdList)) {
                //如果传入负责人和角色一致则无需插入
                String[] splits = taskEntity.getRoleName().split(",");
                for (String roleName : splits) {
                    if (chargeIdList.contains(roleName)) {
                        chargeIdList.remove(roleName);
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(chargeIdList)) {
            //如果审核分配类型是上级负责人则需要更新审核人
            List<TaskChargeDistributionEntity> taskChargeDistributionList = taskChargeDistributionService.listBySourceAndTaskId(MathUtil.THREE, dto.getTaskId());
            if (CollectionUtils.isNotEmpty(taskChargeDistributionList)) {
                for (TaskChargeDistributionEntity taskChargeDistributionEntity : taskChargeDistributionList) {
                    //查询对应负责人的上级
                    List<UserSuperiorDTO> userSuperiorDTOS = sysUserFeign.listSuperiorByUserIds(chargeIdList);
                    if (CollectionUtils.isNotEmpty(userSuperiorDTOS)) {
                        List<String> superiorTypeList = Arrays.stream(taskChargeDistributionEntity.getCharges().split(",")).collect(Collectors.toList());
                        for (String superiorType : superiorTypeList) {
                            String userIds = userSuperiorDTOS.stream().filter(obj -> obj.getSuperiorType().equals(superiorType)).map(UserSuperiorDTO::getUserId).collect(Collectors.joining(","));
                            if (StringUtils.isNotBlank(userIds)) {
                                taskChargeDistributionEntity.setChargeIds(userIds);
                            }
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(chargeIdList)) {
            taskEntity.setChargeId(String.join(",", chargeIdList));
            String chargeName = commonService.getNameByIds(chargeIdList);
            taskEntity.setChargeName(chargeName);
        }
        noticeMessageService.editTaskNotice(loginUser.getUserName(), taskEntity, taskEntity.getProductId());
        //添加操作日志
        addUpdateTaskDTOLog(taskEntity, oldEntity, oldEntity.getId());
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
    public ProjectTaskVO taskDetails(String taskId) {
        ProjectTaskEntity taskEntity = this.getById(taskId);
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        ProjectTaskVO resultVO = new ProjectTaskVO();
        BeanMapper.copy(taskEntity, resultVO);
        String chargeId = taskEntity.getChargeId();
        if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(taskEntity.getDistributionType())) {
            //负责人为角色
            if (StringUtils.isNotBlank(chargeId)) {
                resultVO.setChargeIds(Arrays.asList(chargeId.split(",")));
            } else {
                resultVO.setChargeIds(Arrays.asList(taskEntity.getRoleName().split(",")));
            }
        } else {
            if (StringUtils.isNotBlank(chargeId)) {
                resultVO.setChargeIds(Arrays.asList(chargeId.split(",")));
            }
        }
        //查询模板任务下审核人
        List<TaskChargeDistributionEntity> taskChargeDistributionList = taskChargeDistributionService.listBySourceAndTaskId(MathUtil.THREE, taskEntity.getId());
        if (CollectionUtils.isNotEmpty(taskChargeDistributionList)) {
            List<TaskChargeDistributionDTO> list = BeanMapperUtils.copyList(TaskChargeDistributionDTO.class, taskChargeDistributionList);
            list.forEach(obj -> {
                if (StringUtils.isBlank(obj.getCharges())) {
                    return;
                }
                List<String> collect = Arrays.stream(obj.getCharges().split(",")).collect(Collectors.toList());
                //回显名称
                if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(obj.getDistributionType())) {
                    //用户分配查询名称
                    List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(collect);
                    if (CollectionUtils.isNotEmpty(userList)) {
                        List<String> usrNameList = userList.stream().map(FindUserDTO::getUserName).collect(Collectors.toList());
                        obj.setChargeNames(String.join(",", usrNameList));
                    }
                    obj.setChargeList(collect);
                }
                if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(obj.getDistributionType())) {
                    if (StringUtils.isBlank(obj.getChargeIds())) {
                        //角色分配直接取名称
                        obj.setChargeNames(obj.getCharges());
                        obj.setChargeList(collect);
                    } else {
                        List<String> collect1 = Arrays.stream(obj.getChargeIds().split(",")).collect(Collectors.toList());
                        //用户分配查询名称
                        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(collect1);
                        if (CollectionUtils.isNotEmpty(userList)) {
                            List<String> usrNameList = userList.stream().map(FindUserDTO::getUserName).collect(Collectors.toList());
                            obj.setChargeNames(String.join(",", usrNameList));
                        }
                        obj.setChargeList(collect1);
                    }
                }
                if (DistributionTypeEnum.DISTRIBUTION_SUPERIOR.getCode().equals(obj.getDistributionType())) {
                    if (StringUtils.isBlank(obj.getChargeIds())) {
                        //上级分配取枚举
                        List<String> superiors = collect.stream().map(e -> ChargeSuperiorEnum.getDesc(e)).collect(Collectors.toList());
                        obj.setChargeNames(String.join(",", superiors));
                        obj.setChargeList(superiors);
                    } else {
                        List<String> collect1 = Arrays.stream(obj.getChargeIds().split(",")).collect(Collectors.toList());
                        //用户分配查询名称
                        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(collect1);
                        if (CollectionUtils.isNotEmpty(userList)) {
                            List<String> usrNameList = userList.stream().map(FindUserDTO::getUserName).collect(Collectors.toList());
                            obj.setChargeNames(String.join(",", usrNameList));
                        }
                        obj.setChargeList(collect1);
                    }
                }

            });
            resultVO.setApprovalList(list);
        }
        TaskRefSkuConfigEntity refSku = taskRefSkuConfigService.getByTaskId(taskId);
        List<ProjectTaskRefSkuEntity> taskRefSkuList = projectTaskRefSkuService.getByTaskId(taskId);
        List<String> skuIdList = taskRefSkuList.stream().map(ProjectTaskRefSkuEntity::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = productDetailService.getByIdList(skuIdList);
        List<Map<String, Object>> refSkuFinishList = new ArrayList<>(taskRefSkuList.size());
        for (ProjectTaskRefSkuEntity refSkuItem : taskRefSkuList) {
            Map<String, Object> refSkuMap = new HashMap<>();
            String skuId = refSkuItem.getSkuId();
            refSkuMap.put("skuId", skuId);
            ProductDetailEntity detail = productDetailList.stream().filter(d -> skuId.equals(d.getId())).findFirst().orElse(null);
            if (!Objects.isNull(detail)) {
                refSkuMap.put("skuNo", detail.getSkuNo());
            } else {
                refSkuMap.put("skuNo", "");
            }
            refSkuMap.put("isFinishTask", refSkuItem.getIsFinishTask());
            refSkuFinishList.add(refSkuMap);
        }
        resultVO.setRefSkuFinishList(refSkuFinishList);

        if (refSku != null) {
            resultVO.setFieldJson(refSku.getFieldJson());
            resultVO.setFieldConfigType(refSku.getFieldConfigType());
        }
        resultVO.setRefSkuIdList(skuIdList);
        List<String> skuNoList = productDetailList.stream().filter(d -> skuIdList.contains(d.getId())).map(ProductDetailEntity::getSkuNo).collect(Collectors.toList());
        resultVO.setRefSkuNoList(skuNoList);

        resultVO.setDeliveryDocsList(taskDeliveryService.getDocsByTaskId(taskId));
        String businessProcessId = resultVO.getBusinessProcessId();
        if (StringUtils.isNotBlank(businessProcessId)) {
            BusinessProcessEntity processEntity = businessProcessService.getById(businessProcessId);
            if (processEntity != null) {
                resultVO.setBusinessName(processEntity.getBusinessName());
            }

        }
        List<PreTaskVO> preTaskList = preTaskService.getPreTaskIdList(taskId);
        List<String> pretaskIdList = Collections.emptyList();
        if (CollectionUtil.isNotEmpty(preTaskList)) {
            pretaskIdList = preTaskList.stream().map(PreTaskVO::getPreTaskId).collect(Collectors.toList());
        }
        resultVO.setPreTaskIdList(pretaskIdList);

        return resultVO;
    }

    /**
     * 根据任务id 获取任务信息
     *
     * @param taskIds
     * @return
     */
    @Override
    public List<ProjectTaskEntity> getByTaskIds(List<String> taskIds) {
        if (CollectionUtils.isNotEmpty(taskIds)) {
            LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ProjectTaskEntity::getId, taskIds);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }


    /**
     * 修改 任务状态
     *
     * @param taskIds
     * @param state
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTaskState(List<String> taskIds, Integer state, LocalDateTime realityStart, LocalDateTime realityEnd) {
        if (CollectionUtils.isNotEmpty(taskIds)) {
            LambdaUpdateWrapper<ProjectTaskEntity> updateWrapper = new LambdaUpdateWrapper<ProjectTaskEntity>();
            updateWrapper.set(ProjectTaskEntity::getStatus, state);
            if (null != realityStart) {
                updateWrapper.set(ProjectTaskEntity::getRealityStartTime, realityStart);
            }
            if (null != realityEnd) {
                updateWrapper.set(ProjectTaskEntity::getRealityEndTime, realityEnd);
            }
            if (null != realityStart || null != realityEnd) {
                List<ProjectTaskEntity> updateOrSavEntitiyList = taskIds.stream()
                        .map(taskId -> new ProjectTaskEntity(taskId, realityStart, realityEnd))
                        .collect(Collectors.toList());
                if (!projectTaskTimeRecordService.saveOrUpdateByProjectTaskList(updateOrSavEntitiyList)) {
                    log.error("ProjectTaskServiceImpl>>>updateTaskState>>更新/保存工时记录失败请重试！");
                    throw new RuntimeException("更新/保存工时记录失败请重试");
                }
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
     * 方法说明
     *
     * @param dto
     * @return java.util.List<com.erp.model.plm.dto.TaskGroupResultDTO>
     * @author yl
     * @date 2022-11-08 11:24
     */
    @Override
    public List<TaskGroupResultDTO> getGroupCondition(TaskGroupParamDTO dto) {
        //任务条件 1 待完成  2 全部
        Integer taskCondition = dto.getTaskCondition();
        //product 产品  planEndTime 计划结束时间
        String groupName = dto.getGroupName();
        //不在的 任务状态
        List<Integer> notStateList = new ArrayList<>();
        //这个是待处理 状态为-未开始，进行中，待审核，审核中，完成待审核，审核不通过
        if (TaskConstant.WAIT_HANDLE.equals(taskCondition)) {
            notStateList.add(TaskStateEnum.CLOSE.getCode());
            notStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            notStateList.add(TaskStateEnum.FINISH.getCode());
            notStateList.add(TaskStateEnum.APPROVAL_PASS.getCode());
            notStateList.add(TaskStateEnum.WAIT_CONFIRM.getCode());
            notStateList.add(TaskStateEnum.APPROVAL_ING.getCode());
            notStateList.add(TaskStateEnum.PORTION_FINISH.getCode());
        }
        List<TaskGroupResultDTO> resultList = new ArrayList<>();
        //当是产品的时候
        if (TaskConstant.PRODUCT.equals(groupName)) {
            List<TaskGroupResultDTO> list = baseMapper.allTaskGroup(dto, notStateList);
            resultList = getProductGroup(list);
        }
        //以计划结束时间
        if (TaskConstant.PLAN_END_TIME.equals(groupName)) {
            List<TaskGroupResultDTO> list = baseMapper.taskPlanEndTimeGroup(dto, notStateList);
            resultList = getPlanEndTimeGroup(list);
        }
        return resultList;


    }

    /**
     * 分配给我 分组条件
     *
     * @param dto
     * @return
     * @author yl
     * @date 2022-11-08 11:24
     */
    @Override
    public List<TaskGroupResultDTO> getGroupAssignToMeCondition(TaskGroupParamDTO dto) {
        //任务条件 1 待完成  2 全部
        Integer taskCondition = dto.getTaskCondition();
        //product 产品  planEndTime 计划结束时间
        String groupName = dto.getGroupName();

        //分配给我  任务负责人=当前账号人的待完成/审核任务
        //不在的 任务状态
        List<Integer> notStateList = new ArrayList<>();
        //这个是待处理 状态为-未开始，进行中，待审核，审核中，完成待审核，审核不通过
        if (TaskConstant.WAIT_HANDLE.equals(taskCondition)) {
            notStateList.add(TaskStateEnum.CLOSE.getCode());
            notStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            notStateList.add(TaskStateEnum.FINISH.getCode());
            notStateList.add(TaskStateEnum.APPROVAL_PASS.getCode());
            notStateList.add(TaskStateEnum.WAIT_CONFIRM.getCode());
            notStateList.add(TaskStateEnum.APPROVAL_ING.getCode());
            notStateList.add(TaskStateEnum.PORTION_FINISH.getCode());
        } else if (TaskConstant.WAIT_AUDIT.equals(taskCondition)) {
            notStateList.add(TaskStateEnum.CLOSE.getCode());
            notStateList.add(TaskStateEnum.ING.getCode());
            notStateList.add(TaskStateEnum.NOT_START.getCode());
            notStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            notStateList.add(TaskStateEnum.FINISH.getCode());
            notStateList.add(TaskStateEnum.APPROVAL_PASS.getCode());
            notStateList.add(TaskStateEnum.APPROVAL_NO_PASS.getCode());
            notStateList.add(TaskStateEnum.PORTION_FINISH.getCode());

        }
        //状态包含所有状态-除了待发布
        if (TaskConstant.ALL_TASK.equals(taskCondition)) {
            notStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
        }
        List<TaskGroupResultDTO> resultList = new ArrayList<>();
        //当是产品的时候
        if (TaskConstant.PRODUCT.equals(groupName)) {
            List<TaskGroupResultDTO> list = baseMapper.toMeTaskGroup(dto, notStateList);
            resultList = getProductGroup(list);
        }
        //以计划结束时间
        if (TaskConstant.PLAN_END_TIME.equals(groupName)) {
            List<TaskGroupResultDTO> list = baseMapper.toMeTaskPlanEndTimeGroup(dto, notStateList);
            resultList = getPlanEndTimeGroup(list);
        }
        return resultList;

    }

    /**
     * 我创造的 分组条件
     *
     * @param dto
     * @return
     * @author yl
     * @date 2022-11-08 11:24
     */
    @Override
    public List<TaskGroupResultDTO> groupMyCreateConditionList(TaskGroupParamDTO dto) {
        //任务条件 1 待完成  2 全部
        Integer taskCondition = dto.getTaskCondition();
        //product 产品  planEndTime 计划结束时间
        String groupName = dto.getGroupName();
        //不在的 任务状态
        List<Integer> notStateList = new ArrayList<>();
        //这个是待处理 状态为-未开始，进行中，待审核，审核中，完成待审核，审核不通过
        if (TaskConstant.WAIT_HANDLE.equals(taskCondition)) {
            notStateList.add(TaskStateEnum.CLOSE.getCode());
            notStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            notStateList.add(TaskStateEnum.FINISH.getCode());
            notStateList.add(TaskStateEnum.APPROVAL_PASS.getCode());
            notStateList.add(TaskStateEnum.WAIT_CONFIRM.getCode());
            notStateList.add(TaskStateEnum.APPROVAL_ING.getCode());
            notStateList.add(TaskStateEnum.PORTION_FINISH.getCode());
        }
        List<TaskGroupResultDTO> resultList = new ArrayList<>();
        //当是产品的时候
        if (TaskConstant.PRODUCT.equals(groupName)) {
            List<TaskGroupResultDTO> list = baseMapper.myCreateTaskGroup(dto, notStateList);
            resultList = getProductGroup(list);
        }
        //以计划结束时间
        if (TaskConstant.PLAN_END_TIME.equals(groupName)) {
            List<TaskGroupResultDTO> list = baseMapper.myCreateTaskPlanEndTimeGroup(dto, notStateList);
            resultList = getPlanEndTimeGroup(list);
        }

        return resultList;

    }

    /**
     * 保存或者修改sku 是否有被修改
     * 在完成任务的时候用到
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-11-30 14:01
     */
    @Override
    public void skuChangeResult(StateDTO dto) {
        ProjectTaskEntity taskEntity = this.getById(dto.getId());
        if (taskEntity != null) {
            taskEntity.setIsSkuChange(dto.getState());
            this.updateById(taskEntity);
        }
    }

    @Override
    public List<ProjectTaskEntity> listByProductId(String productId) {
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskEntity::getProductId, productId);
        List<ProjectTaskEntity> list = this.list(queryWrapper);
        return list;
    }

    @Override
    public PagingVO<List<TaskPagingShowDTO>> expertPaging(PagingDTO<TaskSearchParamDTO> searchParamDTO) {
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        TaskSearchParamDTO params = searchParamDTO.getParams();
        params.setPermissionSql(searchParamDTO.getPermissionSql());
        Page query = new Page(searchParamDTO.getCurrPage(), searchParamDTO.getPageSize());
        //"assignToMe", "myCreate", "all"
        String taskProperty = TaskConstant.ALL;
        //任务条件 1 待完成  2 全部
        Integer taskCondition = params.getTaskCondition();
        IPage pageData = new Page();
        //分组的标示
        String groupNameFlag = params.getGroupNameFlag();
        //是否分组
        Boolean ifGroup = false;
        if (StringUtils.isNotBlank(groupNameFlag) && !groupNameFlag.equals("no")) {
            ifGroup = true;
        }

        //是否是产品分组
        Boolean ifProductGroup = ifGroup && groupNameFlag.equals(TaskConstant.PRODUCT) ? true : false;
        //不在的 任务状态
        List<Integer> notStateList = getNoExistState(taskProperty, taskCondition);
        List<TaskShowDTO> workflowList = workflowFeign.queryMyToDo(userId);
        //当不分组
        if (!ifGroup) {
            params.setGroupFlag("");
            params.setSearchCategory(TaskSearchCategoryEnum.ALLPRODUCTTASKLIST.getCode());
            pageData = baseMapper.listProductTaskBySearchCategory(query, notStateList, params);
        } else {
            //根据产品分组
            if (ifProductGroup) {
                //这个就是产品的id
                params.setSearchCategory(TaskSearchCategoryEnum.ALLPRODUCTTASKLIST.getCode());
                pageData = baseMapper.listProductTaskBySearchCategory(query, notStateList, params);
            } else {
                //标示是是计划时间
                String groupFlag = params.getGroupFlag();
                //获取到时间
                Map<String, Date> planTimeMap = getPlanEndTime(groupFlag);
                params.setStartTime(planTimeMap.get("startTime").toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());
                params.setEndTime(planTimeMap.get("endTime").toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());
                params.setSearchCategory(TaskSearchCategoryEnum.ALLPLANTIMETASKLIST.getCode());
                //计划时间
                pageData = baseMapper.listProductTaskBySearchCategory(query, notStateList, params);
            }

        }

        List<TaskPagingShowDTO> records = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            //完成的状态
            Integer finishState = TaskStateEnum.FINISH.getCode();
            //获取到任务id 集合
            List<String> taskIds = records.stream().map(TaskPagingShowDTO::getId).collect(Collectors.toList());
            //任务关联字段配置
            List<TaskRefSkuConfigEntity> refSkuConfigList = taskRefSkuConfigService.getByTaskIds(taskIds);
            //获取总的任务文档数
            List<CountDTO> taskDocsCounts = taskDeliveryService.getTaskDocsCount(taskIds);
            List<TaskDocsFinishEntity> finishTasks = finishService.getByTaskIds(taskIds);
            List<PreTaskEntity> preTaskList = preTaskService.getPreTaskListBytaskIds(taskIds);
            //前置任务
            List<ProjectTaskEntity> preTaskEntityList = this.getByTaskIds(preTaskList.stream().map(PreTaskEntity::getPreTaskId).collect(Collectors.toList()));
            //产品id
            List<String> productIds = records.stream().map(TaskPagingShowDTO::getProductId).collect(Collectors.toList());
            List<ProductInfoEntity> productList = productInfoService.listByIds(productIds);
            Integer finish = TaskStateEnum.FINISH.getCode();
            //根据产品id 获取到所有的 任务信息
            for (TaskPagingShowDTO item : records) {
                String taskId = item.getId();
                Integer state = item.getStatus();
                item.setStatusName(TaskStateEnum.getName(state));
                String quoteSysTaskId = item.getQuoteSysTaskId();
                if (StringUtils.isNotBlank(quoteSysTaskId)) {
                    item.setIsSysTask(true);
                }
                String chargeId = item.getChargeId();
                if (StringUtils.isNotBlank(chargeId)) {
                    item.setChargeIdList(Arrays.asList(chargeId.split(",")));
                } else {
                    item.setChargeIdList(new ArrayList<>());
                }
                TaskShowDTO workflowTask = workflowList.stream().filter(w -> w.getProcessInstanceId().equals(item.getProcessId())).findFirst().orElse(null);
                if (workflowTask != null) {
                    item.setProcessTaskId(workflowTask.getTaskId());
                }
                String warning = getWarning(item.getStatus(), finish, item.getPlanEndTime());
                item.setWarning(warning);
                if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(item.getDistributionType())) {
                    //负责人为角色
                    if (StringUtils.isNotBlank(chargeId)) {
                        item.setChargeIdList(Arrays.asList(chargeId.split(",")));
                    } else {
                        item.setChargeIdList(Arrays.asList(item.getRoleName().split(",")));
                    }
                } else {
                    if (StringUtils.isNotBlank(chargeId)) {
                        item.setChargeIdList(Arrays.asList(chargeId.split(",")));
                    } else {
                        item.setChargeIdList(new ArrayList<>());
                    }
                }
                Integer totalDocsCount = 0;
                CountDTO countDTO = taskDocsCounts.stream().filter(d -> d.getFlagId().equals(taskId)).findFirst().orElse(null);
                if (countDTO != null) {
                    totalDocsCount = countDTO.getCount();
                }
                item.setTotalDocsCount(totalDocsCount);
                Integer finishDocsCount = finishTasks.stream().filter(f -> taskId.equals(f.getTaskId())).map(TaskDocsFinishEntity::getTaskDocsId).distinct().collect(Collectors.toList()).size();
                item.setFinishDocsCount(finishDocsCount);
                TaskRefSkuConfigEntity refSku = refSkuConfigList.stream().filter(r -> r.getTaskId().equals(taskId)).findFirst().orElse(null);
                if (refSku != null) {
                    item.setTaskFieldConfigType(refSku.getFieldConfigType());
                }
                List<String> preTaskIds = preTaskList.stream().filter(p -> p.getTaskId().equals(item.getId())).map(PreTaskEntity::getPreTaskId).collect(Collectors.toList());
                int totalPreTaskCount = preTaskIds.size();
                //前置任务
                item.setTotalPreTaskCount(totalPreTaskCount);
                List<String> preTaskNameList = preTaskEntityList.stream().filter(t -> preTaskIds.contains(t.getId())).map(ProjectTaskEntity::getName).collect(Collectors.toList());
                int finishPreTaskCount = (int) preTaskEntityList.stream().filter(t -> finishState.equals(t.getStatus()) && preTaskIds.contains(t.getId())).count();
                item.setPreTaskNameList(preTaskNameList);
                item.setFinishPreTaskCount(finishPreTaskCount);

                Boolean ifEditTask = getIfEditTask(item.getType(), item.getStatus());
                item.setIfEditTask(ifEditTask);
                //获取任务操作项
                List<Map<String, Object>> operateList = getOperateList(state, totalDocsCount, finishDocsCount);
                item.setOperateList(operateList);
                ProductInfoEntity product = productList.stream().filter(p -> p.getId().equals(item.getProductId())).findFirst().orElse(null);
                if (product != null) {
                    item.setProductName(product.getName());
                }

            }
        }

        return new PagingVO(pageData);
    }


    /**
     * 分配给我    任务负责人=当前账号人的待完成/审核任务
     *
     * @param searchParamDTO
     * @return com.erp.common.vo.PagingVO<java.util.List < com.erp.model.plm.dto.TaskPagingShowDTO>>
     * @author yl
     * @date 2022-11-24 14:27
     */
    @Override
    public PagingVO<List<TaskPagingShowDTO>> assignToMePaging(PagingDTO<TaskSearchParamDTO> searchParamDTO) {
        searchParamDTO.getParams().setPermissionSql(searchParamDTO.getPermissionSql());
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        TaskSearchParamDTO params = searchParamDTO.getParams();
        params.setPermissionSql(searchParamDTO.getPermissionSql());
        Page query = new Page(searchParamDTO.getCurrPage(), searchParamDTO.getPageSize());
        //"assignToMe", "myCreate", "all"
        String taskProperty = TaskConstant.ASSIGN_TO_ME;
        //任务条件 1 待完成  2 全部  3 待审核
        Integer taskCondition = params.getTaskCondition();
        IPage pageData = new Page();
        //分组的标示
        String groupNameFlag = params.getGroupNameFlag();
        //是否分组
        Boolean ifGroup = false;
        if (StringUtils.isNotBlank(groupNameFlag) && !groupNameFlag.equals("no")) {
            ifGroup = true;
        }
        //是否是产品分组
        Boolean ifProductGroup = ifGroup && groupNameFlag.equals(TaskConstant.PRODUCT) ? true : false;
        //不在的 任务状态
        List<Integer> notStateList = getAssignToMeNoExistState(taskProperty, taskCondition);
        List<TaskShowDTO> workflowList = workflowFeign.queryMyToDo(userId);
        //当不分组
        if (!ifGroup) {
            params.setGroupFlag("");
            params.setSearchCategory(TaskSearchCategoryEnum.TOMEPRODUCTTASKLIST.getCode());
            pageData = baseMapper.listProductTaskBySearchCategory(query, notStateList, params);
        } else {
            //根据产品分组
            if (ifProductGroup) {
                params.setSearchCategory(TaskSearchCategoryEnum.TOMEPRODUCTTASKLIST.getCode());
                //这个就是产品的id
                pageData = baseMapper.listProductTaskBySearchCategory(query, notStateList, params);
            } else {
                //标示是是计划时间
                String groupFlag = params.getGroupFlag();
                //获取到时间
                Map<String, Date> planTimeMap = getPlanEndTime(groupFlag);
                params.setSearchCategory(TaskSearchCategoryEnum.TOMEPLANENDTIMETASKLIST.getCode());
                params.setStartTime(planTimeMap.get("startTime").toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());
                params.setEndTime(planTimeMap.get("endTime").toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());
                //计划时间
                pageData = baseMapper.listProductTaskBySearchCategory(query, notStateList, params);
            }
        }


        List<TaskPagingShowDTO> records = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            //完成的状态
            Integer finishState = TaskStateEnum.FINISH.getCode();
            //获取到任务id 集合
            List<String> taskIds = records.stream().map(TaskPagingShowDTO::getId).collect(Collectors.toList());
            //任务关联字段配置
            List<TaskRefSkuConfigEntity> refSkuConfigList = taskRefSkuConfigService.getByTaskIds(taskIds);
            //获取总的任务文档数
            List<CountDTO> taskDocsCounts = taskDeliveryService.getTaskDocsCount(taskIds);
            List<TaskDocsFinishEntity> finishTasks = finishService.getByTaskIds(taskIds);
            List<PreTaskEntity> preTaskList = preTaskService.getPreTaskListBytaskIds(taskIds);
            //前置任务
            List<ProjectTaskEntity> preTaskEntityList = this.getByTaskIds(preTaskList.stream().map(PreTaskEntity::getPreTaskId).collect(Collectors.toList()));
            //产品id
            List<String> productIds = records.stream().map(TaskPagingShowDTO::getProductId).collect(Collectors.toList());
            List<ProductInfoEntity> productList = productInfoService.listByIds(productIds);
            Integer finish = TaskStateEnum.FINISH.getCode();
            //根据产品id 获取到所有的 任务信息
            for (TaskPagingShowDTO item : records) {
                String taskId = item.getId();
                Integer state = item.getStatus();
                item.setStatusName(TaskStateEnum.getName(state));
                String quoteSysTaskId = item.getQuoteSysTaskId();
                if (StringUtils.isNotBlank(quoteSysTaskId)) {
                    item.setIsSysTask(true);
                }

                String chargeId = item.getChargeId();
                if (StringUtils.isNotBlank(chargeId)) {
                    item.setChargeIdList(Arrays.asList(chargeId.split(",")));
                } else {
                    item.setChargeIdList(new ArrayList<>());
                }
                TaskShowDTO workflowTask = workflowList.stream().filter(w -> w.getProcessInstanceId().equals(item.getProcessId())).findFirst().orElse(null);
                if (workflowTask != null) {
                    item.setProcessTaskId(workflowTask.getTaskId());
                }
                String warning = getWarning(item.getStatus(), finish, item.getPlanEndTime());
                item.setWarning(warning);

                if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(item.getDistributionType())) {
                    //负责人为角色
                    if (StringUtils.isNotBlank(chargeId)) {
                        item.setChargeIdList(Arrays.asList(chargeId.split(",")));
                    } else {
                        item.setChargeIdList(Arrays.asList(item.getRoleName().split(",")));
                    }
                } else {
                    if (StringUtils.isNotBlank(chargeId)) {
                        item.setChargeIdList(Arrays.asList(chargeId.split(",")));
                    } else {
                        item.setChargeIdList(new ArrayList<>());
                    }
                }

                Integer totalDocsCount = 0;
                CountDTO countDTO = taskDocsCounts.stream().filter(d -> d.getFlagId().equals(taskId)).findFirst().orElse(null);
                if (countDTO != null) {
                    totalDocsCount = countDTO.getCount();
                }
                TaskRefSkuConfigEntity refSku = refSkuConfigList.stream().filter(r -> r.getTaskId().equals(taskId)).findFirst().orElse(null);
                if (refSku != null) {
                    item.setTaskFieldConfigType(refSku.getFieldConfigType());
                }
                item.setTotalDocsCount(totalDocsCount);
                Integer finishDocsCount = finishTasks.stream().filter(f -> taskId.equals(f.getTaskId())).map(TaskDocsFinishEntity::getTaskDocsId).distinct().collect(Collectors.toList()).size();
                item.setFinishDocsCount(finishDocsCount);
                List<String> preTaskIds = preTaskList.stream().filter(p -> p.getTaskId().equals(item.getId())).map(PreTaskEntity::getPreTaskId).collect(Collectors.toList());
                int totalPreTaskCount = preTaskIds.size();
                //前置任务
                item.setTotalPreTaskCount(totalPreTaskCount);
                List<String> preTaskNameList = preTaskEntityList.stream().filter(t -> preTaskIds.contains(t.getId())).map(ProjectTaskEntity::getName).collect(Collectors.toList());
                int finishPreTaskCount = (int) preTaskEntityList.stream().filter(t -> finishState.equals(t.getStatus()) && preTaskIds.contains(t.getId())).count();
                item.setPreTaskNameList(preTaskNameList);
                item.setFinishPreTaskCount(finishPreTaskCount);
                //获取任务操作项
                List<Map<String, Object>> operateList = getOperateList(state, totalDocsCount, finishDocsCount);
                item.setOperateList(operateList);
                ProductInfoEntity product = productList.stream().filter(p -> p.getId().equals(item.getProductId())).findFirst().orElse(null);
                if (product != null) {
                    item.setProductName(product.getName());
                }
                Boolean ifEditTask = getIfEditTask(item.getType(), item.getStatus());
                item.setIfEditTask(ifEditTask);

            }
        }

        return new PagingVO(pageData);
    }


    /**
     * 分配给我待审核
     *
     * @param searchParamDTO
     * @return com.erp.common.vo.PagingVO<java.util.List < com.erp.model.plm.dto.TaskPagingShowDTO>>
     * @author yl
     * @date 2023-01-10 15:34
     */
    @Override
    public PagingVO<List<TaskPagingShowDTO>> assignToMeWaitAuditPaging(PagingDTO<TaskSearchParamDTO> searchParamDTO) {
        searchParamDTO.getParams().setPermissionSql("");
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        TaskSearchParamDTO params = searchParamDTO.getParams();
        params.setPermissionSql(searchParamDTO.getPermissionSql());
        Page query = new Page(searchParamDTO.getCurrPage(), searchParamDTO.getPageSize());
        //"assignToMe", "myCreate", "all"
        String taskProperty = TaskConstant.ASSIGN_TO_ME;
        //任务条件 1 待完成  2 全部  3 待审核
        Integer taskCondition = params.getTaskCondition();
        IPage pageData = new Page();
        //分组的标示
        String groupNameFlag = params.getGroupNameFlag();
        //是否分组
        Boolean ifGroup = false;
        if (StringUtils.isNotBlank(groupNameFlag) && !groupNameFlag.equals("no")) {
            ifGroup = true;
        }
        //是否是产品分组
        Boolean ifProductGroup = ifGroup && groupNameFlag.equals(TaskConstant.PRODUCT) ? true : false;
        //不在的 任务状态
        List<Integer> notStateList = getAssignToMeNoExistState(taskProperty, taskCondition);
        List<TaskShowDTO> workflowList = workflowFeign.queryMyToDo(userId);
        List<String> processInstanceIds = workflowList.stream().map(TaskShowDTO::getProcessInstanceId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(processInstanceIds)) {
            return new PagingVO(pageData);
        }
        //当不分组
        if (!ifGroup) {
            params.setGroupFlag("");
            params.setSearchCategory(TaskSearchCategoryEnum.TOMEWAITAUDITPRODUCTTASKLIST.getCode());
            params.setProcessInstanceIds(processInstanceIds);
            pageData = baseMapper.listProductTaskBySearchCategory(query, notStateList, params);
        } else {
            //根据产品分组
            if (ifProductGroup) {
                params.setSearchCategory(TaskSearchCategoryEnum.TOMEWAITAUDITPRODUCTTASKLIST.getCode());
                params.setProcessInstanceIds(processInstanceIds);
                //这个就是产品的id
                pageData = baseMapper.listProductTaskBySearchCategory(query, notStateList, params);
            } else {
                //标示是是计划时间
                String groupFlag = params.getGroupFlag();
                //获取到时间
                Map<String, Date> planTimeMap = getPlanEndTime(groupFlag);
                params.setSearchCategory(TaskSearchCategoryEnum.TOMEWAITAUDITPLANENDTIMETASKLIST.getCode());
                params.setStartTime(planTimeMap.get("startTime").toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());
                params.setEndTime(planTimeMap.get("endTime").toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());
                params.setProcessInstanceIds(processInstanceIds);
                //计划时间
                pageData = baseMapper.listProductTaskBySearchCategory(query, notStateList, params);
            }
        }


        List<TaskPagingShowDTO> records = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            //完成的状态
            Integer finishState = TaskStateEnum.FINISH.getCode();
            //获取到任务id 集合
            List<String> taskIds = records.stream().map(TaskPagingShowDTO::getId).collect(Collectors.toList());
            //任务关联字段配置
            List<TaskRefSkuConfigEntity> refSkuConfigList = taskRefSkuConfigService.getByTaskIds(taskIds);
            //获取总的任务文档数
            List<CountDTO> taskDocsCounts = taskDeliveryService.getTaskDocsCount(taskIds);
            List<TaskDocsFinishEntity> finishTasks = finishService.getByTaskIds(taskIds);
            List<PreTaskEntity> preTaskList = preTaskService.getPreTaskListBytaskIds(taskIds);
            //前置任务
            List<ProjectTaskEntity> preTaskEntityList = this.getByTaskIds(preTaskList.stream().map(PreTaskEntity::getPreTaskId).collect(Collectors.toList()));
            //产品id
            List<String> productIds = records.stream().map(TaskPagingShowDTO::getProductId).collect(Collectors.toList());
            List<ProductInfoEntity> productList = productInfoService.listByIds(productIds);
            Integer finish = TaskStateEnum.FINISH.getCode();
            //根据产品id 获取到所有的 任务信息
            for (TaskPagingShowDTO item : records) {
                String taskId = item.getId();
                Integer state = item.getStatus();
                item.setStatusName(TaskStateEnum.getName(state));
                String quoteSysTaskId = item.getQuoteSysTaskId();
                if (StringUtils.isNotBlank(quoteSysTaskId)) {
                    item.setIsSysTask(true);
                }

                String chargeId = item.getChargeId();
                if (StringUtils.isNotBlank(chargeId)) {
                    item.setChargeIdList(Arrays.asList(chargeId.split(",")));
                } else {
                    item.setChargeIdList(new ArrayList<>());
                }
                TaskShowDTO workflowTask = workflowList.stream().filter(w -> w.getProcessInstanceId().equals(item.getProcessId())).findFirst().orElse(null);
                if (workflowTask != null) {
                    item.setProcessTaskId(workflowTask.getTaskId());
                }
                String warning = getWarning(item.getStatus(), finish, item.getPlanEndTime());
                item.setWarning(warning);

                if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(item.getDistributionType())) {
                    //负责人为角色
                    if (StringUtils.isNotBlank(chargeId)) {
                        item.setChargeIdList(Arrays.asList(chargeId.split(",")));
                    } else {
                        item.setChargeIdList(Arrays.asList(item.getRoleName().split(",")));
                    }
                } else {
                    if (StringUtils.isNotBlank(chargeId)) {
                        item.setChargeIdList(Arrays.asList(chargeId.split(",")));
                    } else {
                        item.setChargeIdList(new ArrayList<>());
                    }
                }
                Integer totalDocsCount = 0;
                CountDTO countDTO = taskDocsCounts.stream().filter(d -> d.getFlagId().equals(taskId)).findFirst().orElse(null);
                if (countDTO != null) {
                    totalDocsCount = countDTO.getCount();
                }
                TaskRefSkuConfigEntity refSku = refSkuConfigList.stream().filter(r -> r.getTaskId().equals(taskId)).findFirst().orElse(null);
                if (refSku != null) {
                    item.setTaskFieldConfigType(refSku.getFieldConfigType());
                }
                item.setTotalDocsCount(totalDocsCount);
                Integer finishDocsCount = finishTasks.stream().filter(f -> taskId.equals(f.getTaskId())).map(TaskDocsFinishEntity::getTaskDocsId).distinct().collect(Collectors.toList()).size();
                item.setFinishDocsCount(finishDocsCount);
                List<String> preTaskIds = preTaskList.stream().filter(p -> p.getTaskId().equals(item.getId())).map(PreTaskEntity::getPreTaskId).collect(Collectors.toList());
                int totalPreTaskCount = preTaskIds.size();
                //前置任务
                item.setTotalPreTaskCount(totalPreTaskCount);
                List<String> preTaskNameList = preTaskEntityList.stream().filter(t -> preTaskIds.contains(t.getId())).map(ProjectTaskEntity::getName).collect(Collectors.toList());
                int finishPreTaskCount = (int) preTaskEntityList.stream().filter(t -> finishState.equals(t.getStatus()) && preTaskIds.contains(t.getId())).count();
                item.setPreTaskNameList(preTaskNameList);
                item.setFinishPreTaskCount(finishPreTaskCount);
                //获取任务操作项
                List<Map<String, Object>> operateList = getOperateList(state, totalDocsCount, finishDocsCount);
                item.setOperateList(operateList);
                ProductInfoEntity product = productList.stream().filter(p -> p.getId().equals(item.getProductId())).findFirst().orElse(null);
                if (product != null) {
                    item.setProductName(product.getName());
                }
                Boolean ifEditTask = getIfEditTask(item.getType(), item.getStatus());
                item.setIfEditTask(ifEditTask);

            }
        }

        return new PagingVO(pageData);
    }

    @Override
    public List<ProductTaskCategoryCountDTO> listProductTaskCategoryCount(TaskPagingDTO params) {
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        String productId = params.getProductId();
        String param = params.getPermissionSql();

        List<ProductTaskCategoryCountDTO> list = new ArrayList<>();
        //这个是我完成的任务
        ProductTaskCategoryCountDTO taskDTO1 = new ProductTaskCategoryCountDTO();
        List<Integer> statusList1 = Arrays.asList(TaskStateEnum.NOT_START.getCode(), TaskStateEnum.ING.getCode(), TaskStateEnum.PORTION_FINISH.getCode());
        Integer count1 = baseMapper.pagingCount(productId, userId, statusList1, null);
        taskDTO1.setCount(count1);
        taskDTO1.setType(TaskConstant.MY_FINISH_TASK);
        list.add(taskDTO1);
        //这个待我审核的任务
        ProductTaskCategoryCountDTO taskDTO2 = new ProductTaskCategoryCountDTO();
        List<Integer> statusList2 = Arrays.asList(TaskStateEnum.WAIT_CONFIRM.getCode(), TaskStateEnum.APPROVAL_ING.getCode());
        List<TaskShowDTO> myToDoList = workflowFeign.queryMyToDo(userId);
        taskDTO2.setType(TaskConstant.MY_APPROVAL_TASK);
        taskDTO2.setCount(MathUtil.ZERO);
        //获取流程集合
        List<String> processIds = myToDoList.stream().map(TaskShowDTO::getProcessInstanceId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(processIds)) {
            Integer count2 = baseMapper.myApprovalPagingCount(productId, userId, statusList2, processIds);
            taskDTO2.setCount(count2);
        }
        list.add(taskDTO2);
        //这个是全部
        ProductTaskCategoryCountDTO taskDTO3 = new ProductTaskCategoryCountDTO();
        Integer count3 = baseMapper.allPagingCount(productId, param);
        taskDTO3.setCount(count3);
        taskDTO3.setType(TaskConstant.ALL_FINISH_TASK);
        list.add(taskDTO3);
        return list;
    }

    @Override
    @Transactional
    public void flyingBookReminder(FlyingBookReminderDTO dto) {
        //飞书提醒
        noticeMessageService.flyingBookReminder(dto);
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        //操作日志
        List<String> taskIds = dto.getTaskIds();
        List<SysLogEntity> logList = new ArrayList<>();
        taskIds.forEach(obj -> {
            SysLogEntity sysLogEntity = new SysLogEntity();
            sysLogEntity.setOperation("飞书提醒");
            sysLogEntity.setBusinessId(obj);
            sysLogEntity.setClassPath(SysLogClassPathEnum.PROJECTTASKENTITY.getDesc());
            sysLogEntity.setContent("发送飞书提醒【".concat(dto.getContent()).concat("】"));
            if (ObjectUtils.isNotEmpty(loginUser)) {
                sysLogEntity.setCreateUserId(loginUser.getUid());
                sysLogEntity.setCreateUserName(loginUser.getUserName());
            }
            logList.add(sysLogEntity);
        });
        sysLogService.saveBatch(logList);
    }


    /**
     * 更改任务状态
     *
     * @param productId
     * @param taskIdList
     * @return void
     * @author yl
     * @date 2023-02-09 14:16
     */
    @Override
    public void updateScheduleStatus(String productId, List<String> taskIdList, String status, String scheduleType) {
        if (CollectionUtils.isNotEmpty(taskIdList)) {
            if (StringUtils.isNotBlank(scheduleType)) {
                baseMapper.updateScheduleTask(productId, taskIdList, status, scheduleType);
            } else {
                baseMapper.updateScheduleStatus(productId, taskIdList, status);

            }

        }

    }

    /**
     * 获取产品名称及任务数
     *
     * @param productId
     * @return java.util.Map<java.lang.String, java.lang.Integer>
     * @author yl
     * @date 2023-02-09 17:14
     */
    @Override
    public Map<String, Object> getProductMapByProductId(String productId) {
        Map<String, Object> resultMap = baseMapper.getProductMapByProductId(productId);
        return resultMap;
    }

    @Override
    public List<ScheduleTaskVO> getScheduleTaskByTaskIds(String productId, List<String> taskIds) {
        if (CollectionUtils.isNotEmpty(taskIds)) {
            return baseMapper.getScheduleTaskByTaskIds(productId, taskIds);
        }
        return new ArrayList<>();

    }

    @Override
    public ScheduleTaskExportExcelVO getExport(String productId, String taskId) {
        return baseMapper.getExport(productId, taskId);
    }


    /**
     * 批量更新任务字段
     *
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-10 16:38
     */
    @Override
    @Transactional
    public Boolean batchUpdate(BatchScheduleTaskDTO dto) {
        List<String> taskIds = dto.getTaskIdList();
        if (CollectionUtils.isEmpty(taskIds)) {
            return false;
        }
        List<ProjectTaskEntity> taskEntityList = this.getByTaskIds(taskIds);
        // 只有审核不通过和待提交 才能修改开始和结束时间
        if (dto.getPlanStartTime() != null && dto.getPlanEndTime() != null) {
            List<String> statusList = new ArrayList<>(2);
            String auditNoPassStatus = BaseStatusEnum.AUDIT_NO_PASS.getStatus();
            String waitSubmitStatus = BaseStatusEnum.WAIT_SUBMIT.getStatus();
            statusList.add(auditNoPassStatus);
            statusList.add(waitSubmitStatus);
            long count = taskEntityList.stream().filter(p -> !statusList.contains(p.getScheduleStatus())).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_95136);
            }
        }

        //是否修改
        boolean ifModify = false;
        LambdaUpdateWrapper<ProjectTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(ProjectTaskEntity::getId, dto.getTaskIdList());
        if (dto.getPlanStartTime() != null) {
            updateWrapper.set(ProjectTaskEntity::getPlanStartTime, dto.getPlanStartTime());
            ifModify = true;
        }
        if (dto.getPlanEndTime() != null) {
            updateWrapper.set(ProjectTaskEntity::getPlanEndTime, dto.getPlanEndTime());
            ifModify = true;
        }
        if (StringUtils.isNotBlank(dto.getPhaseId())) {
            updateWrapper.set(ProjectTaskEntity::getPhaseId, dto.getPhaseId());
            ProjectPhaseEntity phaseEntity = projectPhaseService.getById(dto.getPhaseId());
            String phaseName = "";
            if (phaseEntity != null) {
                phaseName = phaseEntity.getName();
            }
            updateWrapper.set(ProjectTaskEntity::getPhaseName, phaseName);
            ifModify = true;
        }
        List<String> chargeId = dto.getChargeIds();
        if (CollectionUtils.isNotEmpty(chargeId)) {
            updateWrapper.set(ProjectTaskEntity::getChargeId, String.join(",", chargeId));
            String chargeNames = commonService.getNameByIds(chargeId);
            updateWrapper.set(ProjectTaskEntity::getChargeName, chargeNames);
            ifModify = true;
        }
        //描述
        if (StringUtils.isNotBlank(dto.getDescription())) {
            updateWrapper.set(ProjectTaskEntity::getDescription, dto.getDescription());
            ifModify = true;
        }
        if (dto.getPriority() != null) {
            updateWrapper.set(ProjectTaskEntity::getPriority, dto.getPriority());
            ifModify = true;

        }
        if (dto.getIsMilepost() != null) {
            updateWrapper.set(ProjectTaskEntity::getIsMilepost, dto.getIsMilepost());
            ifModify = true;
        }
        if (StringUtils.isNotBlank(dto.getRelatedSkuType())) {
            updateWrapper.set(ProjectTaskEntity::getRelatedSkuType, dto.getRelatedSkuType());
            ifModify = true;
        }
        Boolean result = true;
        if (ifModify) {
            result = this.update(updateWrapper);
        }

        //前置任务
        if (CollectionUtils.isNotEmpty(dto.getPreTaskIdList())) {
            //批量更新前置任务
            preTaskService.batchUpdate(dto.getProductId(), dto.getTaskIdList(), dto.getPreTaskIdList());
        }
        if (CollectionUtils.isNotEmpty(dto.getRefSkuIdList())) {
            //批量更新关联的sku
            projectTaskRefSkuService.batchUpdate(dto.getProductId(), dto.getTaskIdList(), dto.getRefSkuIdList());
        }

        return result;
    }


    /**
     * 项目计划
     * 当变更通过后 更改负责人 和时间
     *
     * @param taskList
     * @param status
     * @return void
     * @author yl
     * @date 2023-02-11 16:42
     */
    @Override
    public void updateScheduleTask(List<ProjectPlanTaskEntity> taskList, String status, LoginUser loginUser, String productId) {
        if (CollectionUtils.isNotEmpty(taskList)) {
            List<String> taskIdList = taskList.stream().map(ProjectPlanTaskEntity::getTaskId).collect(Collectors.toList());
            List<ProjectTaskEntity> projectTaskList = this.getByTaskIds(taskIdList);
            List<ProjectTaskEntity> updateList = new ArrayList<>(projectTaskList.size());
            List<FindUserDTO> userList = sysUserFeign.getUserList();
            for (ProjectTaskEntity item : projectTaskList) {
                String taskId = item.getId();
                ProjectPlanTaskEntity planTask = taskList.stream().filter(t -> taskId.
                        equals(t.getTaskId())).findFirst().orElse(null);
                if (planTask != null) {
                    String changeChargeId = planTask.getChangeChargeId();
                    item.setChargeId(changeChargeId);
                    if (StringUtils.isNotBlank(changeChargeId)) {
                        List<String> names = new ArrayList<>();
                        for (String userId : changeChargeId.split(",")) {
                            FindUserDTO findUser = userList.stream().filter(u -> userId.equals(u.getUserId())).findFirst().orElse(null);
                            if (findUser != null) {
                                names.add(findUser.getUserName());
                            } else {
                                names.add("");
                            }
                        }
                        item.setChargeName(String.join(",", names));
                    }

                    item.setPlanStartTime(planTask.getChangeStartTime());
                    item.setPlanEndTime(planTask.getChangeEndTime());
                    item.setScheduleStatus(status);
                    //如果是重启
                    if (planTask.getIsRestart()) {
                        //任务状态变成未开始
                        item.setStatus(TaskStateEnum.NOT_START.getCode());
                        Integer taskType = item.getType();
                        List<String> chargeIdList = new ArrayList<>();
                        String chargeId = item.getChargeId();
                        if (StringUtils.isNotBlank(chargeId)) {
                            chargeIdList = Arrays.asList(chargeId.split(","));
                        }
                        item = automationTask(item, taskType, chargeIdList, loginUser.getUid());
                        Integer afterState = TaskStateEnum.NOT_START.getCode();
                        //一般任务
                        Integer generalTask = TaskTypeEnum.GENERAL_TASK.getCode();
                        //是否是一般任务 true 是
                        Boolean isGeneralTask = generalTask.equals(taskType);
                        //如果不是是一般任务
                        if (!isGeneralTask) {
                            afterState = TaskStateEnum.WAIT_CONFIRM.getCode();
                        }
                        item.setStatus(afterState);

                        //保存任务记录
                        taskOperatorRecordService.addTaskOperator(item.getId(), TaskStateEnum.TO_BE_RELEASED.getCode(), afterState, loginUser.getUid(), loginUser.getUserName());
                        //发布任务通知
                        List<ProjectTaskEntity> noticeTaskList = new ArrayList<>(1);
                        noticeTaskList.add(item);
                        if (TaskStateEnum.WAIT_CONFIRM.getCode().equals(item.getStatus())) {
                            noticeMessageService.finishWaitConfirmNotice(loginUser.getUserName(), noticeTaskList, productId);
                        } else {
                            noticeMessageService.releaseTaskNotice(loginUser.getUserName(), noticeTaskList, productId);
                        }


                    }
                    updateList.add(item);
                }
            }
            if (CollectionUtils.isNotEmpty(updateList)) {
                this.updateBatchById(updateList);
            }
        }

    }


    /**
     * 在排期任务审核通过后就要 改变
     * 任务排期状态 如果满足自动发布就要自动 发布
     * 要是待发布的任务
     *
     * @param productId
     * @param taskIdList
     * @param scheduleStatus 排期状态
     * @return void
     * @author yl
     * @date 2023-02-17 10:59
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initialScheduleTaskPass(LoginUser loginUser, String productId, List<String> taskIdList, String scheduleStatus) {
        List<ProjectTaskEntity> taskList = this.getByTaskIds(taskIdList);
        //待发布
        Integer releasedCode = TaskStateEnum.TO_BE_RELEASED.getCode();
        List<ProjectTaskEntity> releasedTaskList =taskList.stream().filter(f -> f.getStatus().equals(releasedCode)).collect(Collectors.toList());
        String userId = commonService.getUserInfo().getUid();

        for (ProjectTaskEntity task : releasedTaskList) {
            Integer taskType = task.getType();
            task.setScheduleStatus(scheduleStatus);
            List<String> chargeIdList = new ArrayList<>();
            String chargeId = task.getChargeId();
            if (StringUtils.isNotBlank(chargeId)) {
                chargeIdList = Arrays.asList(chargeId.split(","));
            }

            task = automationTask(task, taskType, chargeIdList, userId);
            Integer afterState = TaskStateEnum.NOT_START.getCode();
            //一般任务
            Integer generalTask = TaskTypeEnum.GENERAL_TASK.getCode();
            //是否是一般任务 true 是
            Boolean isGeneralTask = generalTask.equals(taskType);
            //如果不是是一般任务
            if (!isGeneralTask) {
                afterState = TaskStateEnum.WAIT_CONFIRM.getCode();
            }
            task.setStatus(afterState);


            //保存任务记录
            taskOperatorRecordService.addTaskOperator(task.getId(), TaskStateEnum.TO_BE_RELEASED.getCode(), afterState, loginUser.getUid(), loginUser.getUserName());
            //发布任务通知
            List<ProjectTaskEntity> noticeTaskList = new ArrayList<>(1);
            noticeTaskList.add(task);
            if (TaskStateEnum.WAIT_CONFIRM.getCode().equals(task.getStatus())) {
                noticeMessageService.finishWaitConfirmNotice(loginUser.getUserName(), noticeTaskList, productId);
            } else {
                noticeMessageService.releaseTaskNotice(loginUser.getUserName(), noticeTaskList, productId);
            }

        }
        if (CollectionUtils.isNotEmpty(taskList)) {
            this.updateBatchById(taskList);
            if (!projectTaskTimeRecordService.saveOrUpdateByProjectTaskList(taskList)) {
                log.error("ProjectTaskServiceImpl>>>initialScheduleTaskPass>>更新/保存工时记录失败请重试！");
                throw new RuntimeException("更新/保存工时记录失败请重试");
            }
        }
    }


    /**
     * 根据任务名 和产品id 获取到对应的人
     *
     * @param productId
     * @param taskName
     * @return com.erp.model.plm.entity.ProjectTaskEntity
     * @author yl
     * @date 2023-02-17 15:14
     */
    @Override
    public ProjectTaskEntity getbyName(String productId, String taskName) {
        if (StringUtils.isBlank(productId) || StringUtils.isBlank(taskName)) {
            return null;
        }
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskEntity::getProductId, productId);
        queryWrapper.eq(ProjectTaskEntity::getName, taskName.trim());
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public void updatePhase(ProjectTaskEntity taskEntity) {
        LambdaUpdateWrapper<ProjectTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProjectTaskEntity::getProductId, taskEntity.getProductId());
        updateWrapper.eq(ProjectTaskEntity::getPhaseId, taskEntity.getPhaseId());
        updateWrapper.set(ProjectTaskEntity::getPhaseName, taskEntity.getPhaseName());
        this.update(updateWrapper);
    }

    @Override
    public List<ProjectTaskEntity> listByTaskIds(List<String> preTaskIds) {
        return lambdaQuery().in(ProjectTaskEntity::getId, preTaskIds)
                .list();
    }

    /**
     * 我创造的    任务创建人=当前账号人
     *
     * @param searchParamDTO
     * @return com.erp.common.vo.PagingVO<java.util.List < com.erp.model.plm.dto.TaskPagingShowDTO>>
     * @author yl
     * @date 2022-11-24 14:27
     */
    @Override
    public PagingVO<List<TaskPagingShowDTO>> myCreatePaging(PagingDTO<TaskSearchParamDTO> searchParamDTO) {
        searchParamDTO.getParams().setPermissionSql(searchParamDTO.getPermissionSql());
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        TaskSearchParamDTO params = searchParamDTO.getParams();
        Page query = new Page(searchParamDTO.getCurrPage(), searchParamDTO.getPageSize());
        //"assignToMe", "myCreate", "all"
        String taskProperty = TaskConstant.MY_CREATE;
        //任务条件 1 待完成  2 全部
        Integer taskCondition = params.getTaskCondition();
        IPage pageData = new Page();
        //分组的标示
        String groupNameFlag = params.getGroupNameFlag();
        //是否分组
        Boolean ifGroup = false;
        if (StringUtils.isNotBlank(groupNameFlag) && !groupNameFlag.equals("no")) {
            ifGroup = true;
        }
        //是否是产品分组
        Boolean ifProductGroup = ifGroup && groupNameFlag.equals(TaskConstant.PRODUCT) ? true : false;
        //不在的 任务状态
        List<Integer> notStateList = getNoExistState(taskProperty, taskCondition);
        List<TaskShowDTO> workflowList = workflowFeign.queryMyToDo(userId);
        //当不分组
        if (!ifGroup) {
            params.setGroupFlag("");
            params.setSearchCategory(TaskSearchCategoryEnum.MYCREATEPRODUCTTASKLIST.getCode());
            pageData = baseMapper.listProductTaskBySearchCategory(query, notStateList, params);
        } else {
            //根据产品分组
            if (ifProductGroup) {
                params.setSearchCategory(TaskSearchCategoryEnum.MYCREATEPRODUCTTASKLIST.getCode());
                //这个就是产品的id
                pageData = baseMapper.listProductTaskBySearchCategory(query, notStateList, params);
            } else {
                //标示是是计划时间
                String groupFlag = params.getGroupFlag();
                //获取到时间
                Map<String, Date> planTimeMap = getPlanEndTime(groupFlag);
                params.setSearchCategory(TaskSearchCategoryEnum.MYCREATEPLANENDTIMETASKLIST.getCode());
                params.setStartTime(planTimeMap.get("startTime").toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());
                params.setEndTime(planTimeMap.get("endTime").toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());
                //计划时间
                pageData = baseMapper.listProductTaskBySearchCategory(query, notStateList, params);
            }
        }


        List<TaskPagingShowDTO> records = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            //完成的状态
            Integer finishState = TaskStateEnum.FINISH.getCode();
            //获取到任务id 集合
            List<String> taskIds = records.stream().map(TaskPagingShowDTO::getId).collect(Collectors.toList());
            //获取总的任务文档数
            List<CountDTO> taskDocsCounts = taskDeliveryService.getTaskDocsCount(taskIds);
            //任务关联字段配置
            List<TaskRefSkuConfigEntity> refSkuConfigList = taskRefSkuConfigService.getByTaskIds(taskIds);
            List<TaskDocsFinishEntity> finishTasks = finishService.getByTaskIds(taskIds);
            List<PreTaskEntity> preTaskList = preTaskService.getPreTaskListBytaskIds(taskIds);
            //前置任务
            List<ProjectTaskEntity> preTaskEntityList = this.getByTaskIds(preTaskList.stream().map(PreTaskEntity::getPreTaskId).collect(Collectors.toList()));
            //产品id
            List<String> productIds = records.stream().map(TaskPagingShowDTO::getProductId).collect(Collectors.toList());
            List<ProductInfoEntity> productList = productInfoService.listByIds(productIds);
            Integer finish = TaskStateEnum.FINISH.getCode();
            //根据产品id 获取到所有的 任务信息
            for (TaskPagingShowDTO item : records) {
                String taskId = item.getId();
                Integer state = item.getStatus();
                String chargeId = item.getChargeId();
                if (StringUtils.isNotBlank(chargeId)) {
                    item.setChargeIdList(Arrays.asList(chargeId.split(",")));
                } else {
                    item.setChargeIdList(new ArrayList<>());
                }
                item.setStatusName(TaskStateEnum.getName(state));
                String quoteSysTaskId = item.getQuoteSysTaskId();
                if (StringUtils.isNotBlank(quoteSysTaskId)) {
                    item.setIsSysTask(true);
                }
                TaskShowDTO workflowTask = workflowList.stream().filter(w -> w.getProcessInstanceId().equals(item.getProcessId())).findFirst().orElse(null);
                if (workflowTask != null) {
                    item.setProcessTaskId(workflowTask.getTaskId());
                }

                TaskRefSkuConfigEntity refSku = refSkuConfigList.stream().filter(r -> r.getTaskId().equals(taskId)).findFirst().orElse(null);
                if (refSku != null) {
                    item.setTaskFieldConfigType(refSku.getFieldConfigType());
                }
                String warning = getWarning(item.getStatus(), finish, item.getPlanEndTime());
                item.setWarning(warning);

                if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(item.getDistributionType())) {
                    //负责人为角色
                    if (StringUtils.isNotBlank(chargeId)) {
                        item.setChargeIdList(Arrays.asList(chargeId.split(",")));
                    } else {
                        item.setChargeIdList(Arrays.asList(item.getRoleName().split(",")));
                    }
                } else {
                    if (StringUtils.isNotBlank(chargeId)) {
                        item.setChargeIdList(Arrays.asList(chargeId.split(",")));
                    } else {
                        item.setChargeIdList(new ArrayList<>());
                    }
                }
                Integer totalDocsCount = 0;
                CountDTO countDTO = taskDocsCounts.stream().filter(d -> d.getFlagId().equals(taskId)).findFirst().orElse(null);
                if (countDTO != null) {
                    totalDocsCount = countDTO.getCount();
                }
                item.setTotalDocsCount(totalDocsCount);
                Integer finishDocsCount = finishTasks.stream().filter(f -> taskId.equals(f.getTaskId())).map(TaskDocsFinishEntity::getTaskDocsId).distinct().collect(Collectors.toList()).size();
                item.setFinishDocsCount(finishDocsCount);
                List<String> preTaskIds = preTaskList.stream().filter(p -> p.getTaskId().equals(item.getId())).map(PreTaskEntity::getPreTaskId).collect(Collectors.toList());
                int totalPreTaskCount = preTaskIds.size();
                //前置任务
                item.setTotalPreTaskCount(totalPreTaskCount);
                List<String> preTaskNameList = preTaskEntityList.stream().filter(t -> preTaskIds.contains(t.getId())).map(ProjectTaskEntity::getName).collect(Collectors.toList());
                int finishPreTaskCount = (int) preTaskEntityList.stream().filter(t -> finishState.equals(t.getStatus()) && preTaskIds.contains(t.getId())).count();
                item.setPreTaskNameList(preTaskNameList);
                item.setFinishPreTaskCount(finishPreTaskCount);
                //获取任务操作项
                List<Map<String, Object>> operateList = getOperateList(state, totalDocsCount, finishDocsCount);
                item.setOperateList(operateList);
                ProductInfoEntity product = productList.stream().filter(p -> p.getId().equals(item.getProductId())).findFirst().orElse(null);
                if (product != null) {
                    item.setProductName(product.getName());
                }
                Boolean ifEditTask = getIfEditTask(item.getType(), item.getStatus());
                item.setIfEditTask(ifEditTask);
            }
        }

        return new PagingVO(pageData);
    }

    /**
     * 完成sku
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-11-29 14:59
     */
    @Override
    public void taskFinishSku(TaskFinishSkuDTO dto) {
        projectTaskRefSkuService.taskFinishRefSku(dto);
    }

    /**
     * 获取任务更多操作的列表
     *
     * @param taskId 任务id
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @author yl
     * @date 2022-11-10 11:23
     */
    @Override
    public List<Map<String, Object>> operateMoreList(String taskId) {
        ProjectTaskEntity taskEntity = this.getById(taskId);
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        TaskRefSkuConfigEntity skuConfigEntity = taskRefSkuConfigService.getByTaskId(taskId);
        List<ProjectTaskRefSkuEntity> taskRefSkuList = projectTaskRefSkuService.getByTaskId(taskId);
        //产品下sku
        List<ProductDetailEntity> list = productDetailService.getSkuListByProductId(taskEntity.getProductId());
        Boolean taskRefSkuFlag = (CollectionUtils.isNotEmpty(taskRefSkuList) && taskRefSkuList.size() > 0) || (RelatedSkuTypeEnum.ALL_RELATED.getCode().equals(taskEntity.getRelatedSkuType()) && CollectionUtils.isNotEmpty(list));
        List<Map<String, Object>> resultList = new ArrayList<>();

        Integer taskState = taskEntity.getStatus();
        Integer finishCode = TaskStateEnum.FINISH.getCode();

        List<String> taskIds = Arrays.asList(taskId);
        //编辑任务
        Map<String, Object> editTaskMap = new HashMap<>();
        editTaskMap.put("name", "编辑任务");
        editTaskMap.put("flag", "editTask");
        Boolean ifEditTask = getIfEditTask(taskEntity.getType(), taskEntity.getStatus());
        editTaskMap.put("isShow", ifEditTask);
        //创建子任务
        Map<String, Object> createChildTaskMap = new HashMap<>();
        createChildTaskMap.put("name", "创建子任务");
        createChildTaskMap.put("flag", "createChildTask");
        createChildTaskMap.put("isShow", true);
        resultList.add(createChildTaskMap);

        /**
         * 获取任务属性
         * 是一般任务 还是一般带审核  还是待审核
         * 0 一般任务
         * 1 有文档审核任务
         * 2 评审任务
         */
        resultList.add(editTaskMap);
        Map<String, Object> uploadMap = new HashMap<>();
        uploadMap.put("name", "上传交付物");
        uploadMap.put("flag", "uploadFile");
        List<CountDTO> taskDocsCounts = taskDeliveryService.getTaskDocsCount(taskIds);
        int totalCount = 0;
        if (CollectionUtils.isNotEmpty(taskDocsCounts)) {
            totalCount = taskDocsCounts.get(0).getCount();
        }

        Boolean uploadFlag = getIfUpload(taskEntity.getType(), taskState);
        //表示有交付物 有交付物[文档+信息填写]时，显示上传文档
        if (totalCount == 0 && Objects.isNull(skuConfigEntity) && !taskRefSkuFlag) {
            uploadFlag = false;
        }
        uploadMap.put("isShow", uploadFlag);
        resultList.add(uploadMap);

        boolean deleteTaskShow = true;
        Integer IsFixed = taskEntity.getIsFixed();
        LoginUser userInfo = CommonInterceptor.threadLocal.get();

        //如果是固定任务
        if (IsConstant.YES.equals(IsFixed) && !"admin".equals(userInfo.getUserAccount())) {
            deleteTaskShow = false;
        }
        //删除任务
        Map<String, Object> deleteTaskMap = new HashMap<>();
        deleteTaskMap.put("name", "删除任务");
        deleteTaskMap.put("flag", "deleteTask");
        deleteTaskMap.put("isShow", deleteTaskShow);
        resultList.add(deleteTaskMap);


        //变更文档
        //只有任务完成了或者审核不通过才能变更流程 表示有交付物 有交付物[文档+信息填写]时，显示上传文档
        Boolean changeDocsShow = false;
        if (finishCode.equals(taskState)) {
            changeDocsShow = true;
        }
        //当没有上传文档 并且没有填写的时候 不用显示
        if (totalCount == 0 && Objects.isNull(skuConfigEntity) && !taskRefSkuFlag) {
            changeDocsShow = false;
        }
        Map<String, Object> changeDocsMap = new HashMap<>();
        changeDocsMap.put("name", "变更交付物");
        changeDocsMap.put("flag", "changeDocs");
        changeDocsMap.put("isShow", changeDocsShow);
        resultList.add(changeDocsMap);
        return resultList;
    }


    /**
     * 获取是否可以显示上传交付物
     *
     * @param taskState
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-30 15:52
     */
    public Boolean getIfUpload(Integer taskType, Integer taskState) {
        Integer generalTask = TaskTypeEnum.GENERAL_TASK.getCode();
        //是否是一般任务 true 就是
        Boolean generalTaskFlag = generalTask.equals(taskType);
        List<Integer> stateList = new ArrayList<>();
        //待发布
        Integer releasedState = TaskStateEnum.TO_BE_RELEASED.getCode();
        //未开始
        Integer notStartState = TaskStateEnum.NOT_START.getCode();
        //进行中
        Integer ingState = TaskStateEnum.ING.getCode();

        //审核不通过
        Integer approvalNoPassState = TaskStateEnum.APPROVAL_NO_PASS.getCode();
        //待审核
        Integer waitConfirmState = TaskStateEnum.WAIT_CONFIRM.getCode();
        //部分完成
        Integer portionFinishState = TaskStateEnum.PORTION_FINISH.getCode();

        stateList.add(releasedState);
        stateList.add(notStartState);
        stateList.add(ingState);
        stateList.add(approvalNoPassState);
        stateList.add(portionFinishState);
        if (!generalTaskFlag) {
            stateList.add(waitConfirmState);
        }
        return stateList.contains(taskState);
    }

    /**
     * 待发布，未开始，待审核，已取消可编辑
     * 一般任务 待审核也不能编辑
     * 获取能否编辑任务
     *
     * @param taskType
     * @param taskState
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-30 14:58
     */
    public Boolean getIfEditTask(Integer taskType, Integer taskState) {
        Integer generalTask = TaskTypeEnum.GENERAL_TASK.getCode();
        //是否是一般任务 true 就是
        Boolean generalTaskFlag = generalTask.equals(taskType);
        //待发布
        Integer releasedState = TaskStateEnum.TO_BE_RELEASED.getCode();
        //未开始
        Integer notStartState = TaskStateEnum.NOT_START.getCode();
        //已取消
        Integer closeState = TaskStateEnum.CLOSE.getCode();

        //进行中
        Integer ingState = TaskStateEnum.ING.getCode();

        List<Integer> stateList = new ArrayList<>(5);
        stateList.add(releasedState);
        stateList.add(notStartState);
        stateList.add(closeState);
        stateList.add(ingState);

        return stateList.contains(taskState);
    }

    /**
     * 获取已过期 或者即将过期的任务
     *
     * @param nowDay
     * @param days
     * @return java.util.List<com.erp.model.plm.entity.ProjectTaskEntity>
     * @author yl
     * @date 2022-11-16 11:01
     */
    @Override
    public List<ProjectTaskEntity> getExpireTaskList(Date nowDay, int days) {
        Date flagDate = DateUtil.addDateDays(nowDay, days);
        Date flagDateEnd = DateUtil.getStartTime(flagDate);
        //今天开始时间
        Date startNowDate = DateUtil.getStartTime(nowDay);
        return baseMapper.getExpireWarnTaskList(startNowDate, flagDateEnd, TaskStateEnum.FINISH.getCode());
    }


    /**
     * 获取操作列表
     *
     * @param state
     * @param totalDocsCount
     * @param finishDocsCount
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @author yl
     * @date 2022-11-09 15:06
     */
    private List<Map<String, Object>> getOperateList(Integer state, Integer totalDocsCount, Integer finishDocsCount) {
        List<Map<String, Object>> operateList = new LinkedList<>();
        Map<String, Object> taskMap = new HashMap<>();
        String taskName = "";
        String taskFlag = "";
        Boolean taskShow = false;
        if (TaskStateEnum.TO_BE_RELEASED.getCode().equals(state)) {
            taskName = "发布任务";
            taskFlag = "releasedTask";
            taskShow = true;
        }
        if (TaskStateEnum.NOT_START.getCode().equals(state)) {
            taskName = "开始任务";
            taskFlag = "startTask";
            taskShow = true;
        }
        if (TaskStateEnum.WAIT_CONFIRM.getCode().equals(state)) {
            taskName = "审核任务";
            taskFlag = "approvalTask";
            taskShow = true;
        }
        if (TaskStateEnum.ING.getCode().equals(state)) {
            taskName = "完成任务";
            taskFlag = "finishTask";
            taskShow = true;
        }
        //审核不通过 就要重新开始
        if (TaskStateEnum.APPROVAL_NO_PASS.getCode().equals(state)) {
            taskName = "重新开始";
            taskFlag = "restartTask";
            taskShow = true;
        }
        taskMap.put("name", taskName);
        taskMap.put("flag", taskFlag);
        taskMap.put("isShow", taskShow);
        operateList.add(taskMap);
        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put("name", "查看详情");
        detailsMap.put("flag", "taskDetails");
        detailsMap.put("isShow", TaskStateEnum.FINISH.getCode().equals(state) ? true : false);
        operateList.add(detailsMap);


        Map<String, Object> moreMap = new HashMap<>();
        moreMap.put("name", "更多");
        moreMap.put("flag", "more");
        moreMap.put("isShow", true);
        operateList.add(moreMap);
        return operateList;

    }


    /**
     * 根据以计划结束时间分组 的标示
     * 获取到计划的开始时间 和结束时间
     *
     * @return
     */
    public Map<String, Date> getPlanEndTime(String groupFlag) {
        Map<String, Date> resultMap = new HashMap<>();
        Date startTime = null;
        Date endTime = null;
        Date nowDay = new Date();
        Date nowDayStartTime = DateUtil.getStartTime(nowDay);
        //今天
        if (TaskPlanEndTimeEnum.TODAY.getFlag().equals(groupFlag)) {
            startTime = nowDayStartTime;
            endTime = DateUtil.getEndTime(nowDay);
        }
        //明天
        if (TaskPlanEndTimeEnum.TOMORROW.getFlag().equals(groupFlag)) {
            Date tomorrowDay = DateUtil.addDateDays(nowDay, 1);
            startTime = DateUtil.getStartTime(tomorrowDay);
            endTime = DateUtil.getEndTime(tomorrowDay);
        }
        //近三天
        if (TaskPlanEndTimeEnum.LAST_THREE_DAYS.getFlag().equals(groupFlag)) {
            startTime = nowDayStartTime;
            endTime = DateUtil.addDateDays(nowDay, 2);
        }
        //近七天
        if (TaskPlanEndTimeEnum.LAST_SEVEN_DAYS.getFlag().equals(groupFlag)) {
            startTime = nowDayStartTime;
            endTime = DateUtil.addDateDays(nowDay, 6);

        }
        //近十五天
        if (TaskPlanEndTimeEnum.LAST_FIFTEEN_DAYS.getFlag().equals(groupFlag)) {
            startTime = nowDayStartTime;
            endTime = DateUtil.addDateDays(nowDay, 14);
        }
        //近三十天
        if (TaskPlanEndTimeEnum.LAST_THIRTY_DAYS.getFlag().equals(groupFlag)) {
            startTime = nowDayStartTime;
            endTime = DateUtil.addDateDays(nowDay, 29);
        }
        //三十天后
        if (TaskPlanEndTimeEnum.AFTER_THIRTY_DAYS.getFlag().equals(groupFlag)) {
            startTime = DateUtil.addDateDays(nowDay, 29);
        }
        resultMap.put("startTime", startTime);
        resultMap.put("endTime", endTime);
        return resultMap;
    }

    /**
     * 获取不在的状态列表
     *
     * @param taskCondition
     * @param taskProperty  "assignToMe", "myCreate", "all"
     * @return java.util.List<java.lang.Integer>
     * @author yl  1 待完成  2 全部
     * @date 2022-11-09 9:47
     */
    public List<Integer> getNoExistState(String taskProperty, Integer taskCondition) {
        List<Integer> noExistStateList = new ArrayList<>();
        //待完成 待审核
        if (TaskConstant.WAIT_HANDLE.equals(taskCondition)) {
            noExistStateList.add(TaskStateEnum.CLOSE.getCode());
            noExistStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            noExistStateList.add(TaskStateEnum.FINISH.getCode());
            noExistStateList.add(TaskStateEnum.APPROVAL_PASS.getCode());
        }
        //全部
        if (TaskConstant.ALL_TASK.equals(taskCondition)) {
            //分配给我   状态包含所有状态-除了待发布
            if (TaskConstant.ASSIGN_TO_ME.equals(taskProperty)) {
                noExistStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            }
        }
        return noExistStateList;
    }

    /**
     * 获取分配给我的 不在状态
     *
     * @param taskCondition
     * @param taskProperty  "assignToMe", "myCreate", "all"
     * @return java.util.List<java.lang.Integer>
     * @author yl  1 待完成  2 全部
     * @date 2022-11-09 9:47
     */
    public List<Integer> getAssignToMeNoExistState(String taskProperty, Integer taskCondition) {
        List<Integer> noExistStateList = new ArrayList<>();
        //待完成  未开始，进行中，审核不通过
        if (TaskConstant.WAIT_HANDLE.equals(taskCondition)) {
            noExistStateList.add(TaskStateEnum.CLOSE.getCode());
            noExistStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            noExistStateList.add(TaskStateEnum.FINISH.getCode());
            noExistStateList.add(TaskStateEnum.APPROVAL_PASS.getCode());
            noExistStateList.add(TaskStateEnum.WAIT_CONFIRM.getCode());
            noExistStateList.add(TaskStateEnum.APPROVAL_ING.getCode());
            noExistStateList.add(TaskStateEnum.PORTION_FINISH.getCode());
        }
        // 待审核 待审核，审核中
        if (TaskConstant.WAIT_AUDIT.equals(taskCondition)) {
            noExistStateList.add(TaskStateEnum.CLOSE.getCode());
            noExistStateList.add(TaskStateEnum.ING.getCode());
            noExistStateList.add(TaskStateEnum.NOT_START.getCode());
            noExistStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            noExistStateList.add(TaskStateEnum.FINISH.getCode());
            noExistStateList.add(TaskStateEnum.APPROVAL_PASS.getCode());
            noExistStateList.add(TaskStateEnum.APPROVAL_NO_PASS.getCode());
            noExistStateList.add(TaskStateEnum.PORTION_FINISH.getCode());


        }
        //全部
        if (TaskConstant.ALL_TASK.equals(taskCondition)) {
            //分配给我   状态包含所有状态-除了待发布
            if (TaskConstant.ASSIGN_TO_ME.equals(taskProperty)) {
                noExistStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            }
        }
        return noExistStateList;
    }

    /**
     * 获取产品分组数据
     *
     * @param list
     * @return
     */
    public List<TaskGroupResultDTO> getProductGroup(List<TaskGroupResultDTO> list) {
        List<TaskGroupResultDTO> resultList = new ArrayList<>();
        int totalTaskCount = 0;
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> productIds = list.stream().map(TaskGroupResultDTO::getGroupFlag).collect(Collectors.toList());
            List<ProductInfoEntity> productList = productInfoService.listByIds(productIds);
            for (TaskGroupResultDTO result : list) {
                ProductInfoEntity product = productList.stream().
                        filter(p -> p.getId().equals(result.getGroupFlag())).findFirst().orElse(null);
                if (product != null) {
                    result.setName(product.getName());
                }
                totalTaskCount += result.getTaskCount();
            }
        }
        TaskGroupResultDTO total = new TaskGroupResultDTO();
        total.setName(TaskConstant.ALL_CN);
        total.setGroupFlag("");
        total.setTaskCount(totalTaskCount);
        resultList.add(total);
        resultList.addAll(list);
        return resultList;
    }

    /**
     * 获取计划结束时间  维度数据
     *
     * @return java.util.List<com.erp.model.plm.dto.TaskGroupResultDTO>
     * @author yl
     * @date 2022-11-08 15:23
     */
    public List<TaskGroupResultDTO> getPlanEndTimeGroup(List<TaskGroupResultDTO> list) {
        List<TaskGroupResultDTO> resultList = new ArrayList<>();
        TaskGroupResultDTO total = new TaskGroupResultDTO();
        int totalTaskCount = 0;
        for (TaskGroupResultDTO result : list) {
            totalTaskCount += result.getTaskCount();
        }
        total.setName(TaskConstant.ALL_CN);
        total.setGroupFlag("");
        total.setTaskCount(totalTaskCount);
        resultList.add(total);
        Date nowDay = DateUtil.getStartTime(new Date());
        String fmt = DateUtil.fmt_day;
        for (TaskPlanEndTimeEnum timeEnum : TaskPlanEndTimeEnum.values()) {
            TaskGroupResultDTO result = new TaskGroupResultDTO();
            result.setName(timeEnum.getName());
            result.setGroupFlag(timeEnum.getFlag());
            String flag = timeEnum.getFlag();
            int taskCount = 0;
            //今天
            if (flag.equals(TaskPlanEndTimeEnum.TODAY.getFlag())) {
                String todayStr = DateUtil.conversionDate(nowDay, fmt);
                taskCount = list.stream().filter(t -> t.getGroupFlag().equals(todayStr)).mapToInt(TaskGroupResultDTO::getTaskCount).sum();
            }
            //明天
            if (flag.equals(TaskPlanEndTimeEnum.TOMORROW.getFlag())) {
                Date tomorrowDay = DateUtil.addDateDays(nowDay, 1);
                String tomorrow = DateUtil.conversionDate(tomorrowDay, fmt);
                taskCount = list.stream().filter(t -> t.getGroupFlag().equals(tomorrow)).mapToInt(TaskGroupResultDTO::getTaskCount).sum();
            }
            //近三天
            if (flag.equals(TaskPlanEndTimeEnum.LAST_THREE_DAYS.getFlag())) {
                Date lastThree = DateUtil.addDateDays(nowDay, 2);
                taskCount = list.stream().filter(t ->
                        DateUtil.stringToDate(t.getGroupFlag()).compareTo(lastThree) <= 0
                                && DateUtil.stringToDate(t.getGroupFlag()).compareTo(nowDay) >= 0
                ).mapToInt(TaskGroupResultDTO::getTaskCount).sum();
            }
            //近七天
            if (flag.equals(TaskPlanEndTimeEnum.LAST_SEVEN_DAYS.getFlag())) {
                Date lastSeven = DateUtil.addDateDays(nowDay, 6);
                taskCount = list.stream().filter(t ->
                        DateUtil.stringToDate(t.getGroupFlag()).compareTo(lastSeven) <= 0
                                && DateUtil.stringToDate(t.getGroupFlag()).compareTo(nowDay) >= 0
                ).mapToInt(TaskGroupResultDTO::getTaskCount).sum();

            }
            //近十五天
            if (flag.equals(TaskPlanEndTimeEnum.LAST_FIFTEEN_DAYS.getFlag())) {
                Date lastFifteen = DateUtil.addDateDays(nowDay, 14);
                taskCount = list.stream().filter(t ->
                        DateUtil.stringToDate(t.getGroupFlag()).compareTo(lastFifteen) <= 0
                                && DateUtil.stringToDate(t.getGroupFlag()).compareTo(nowDay) >= 0
                ).mapToInt(TaskGroupResultDTO::getTaskCount).sum();

            }
            //近三十天
            if (flag.equals(TaskPlanEndTimeEnum.LAST_THIRTY_DAYS.getFlag())) {
                Date lastThirty = DateUtil.addDateDays(nowDay, 29);
                taskCount = list.stream().filter(t ->
                        DateUtil.stringToDate(t.getGroupFlag()).compareTo(lastThirty) <= 0
                                && DateUtil.stringToDate(t.getGroupFlag()).compareTo(nowDay) >= 0
                ).mapToInt(TaskGroupResultDTO::getTaskCount).sum();
            }
            //三十天后
            if (flag.equals(TaskPlanEndTimeEnum.AFTER_THIRTY_DAYS.getFlag())) {
                Date afterThirty = DateUtil.addDateDays(nowDay, 30);
                taskCount = list.stream().filter(t ->
                        DateUtil.stringToDate(t.getGroupFlag()).compareTo(afterThirty) >= 0
                ).mapToInt(TaskGroupResultDTO::getTaskCount).sum();
            }
            result.setTaskCount(taskCount);
            resultList.add(result);
        }
        return resultList;
    }


    /**
     * 开始任务
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        //检查是否在变更中
        checkScheduleChangeStatus(list);
        Integer ingCode = TaskStateEnum.ING.getCode();

        //一般任务
        Integer generalTaskCode = TaskTypeEnum.GENERAL_TASK.getCode();
        //一般任务 列表  都是将任务状态改为进行中
        List<ProjectTaskEntity> generalTasks = list.stream().filter(t -> generalTaskCode.equals(t.getType())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(generalTasks)) {
            List<String> taskIdList = generalTasks.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            this.updateTaskState(taskIdList, ingCode, LocalDateTime.now(), null);
            taskOperatorRecordService.batchSaveRecord(taskIds, TaskStateEnum.NOT_START.getCode(), ingCode, loginUser.getUid(), loginUser.getUserName(), "");

            //操作日志
            List<SysLogEntity> sysLogEntityList = new LinkedList<>();
            taskIdList.forEach(taskId -> {
                sysLogEntityList.add(
                        new SysLogEntity().setContent(String.format("编辑了一个[任务状态]由[%s]为[%s]", TaskStateEnum.NOT_START.getName(), TaskStateEnum.ING.getName()))
                                .setClassPath(SysLogClassPathEnum.PROJECTTASKENTITY.getDesc())
                                .setBusinessId(taskId));
            });
            sysLogService.addSysLogByBatchSave(sysLogEntityList);
            //发送开始任务通知
            noticeMessageService.startTaskNotice(loginUser.getUserName(), generalTasks, dto.getProductId());
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishTask(OperateBaseTaskDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        List<String> taskIds = dto.getTaskIdList();
        //待发布
        Integer releasedCode = TaskStateEnum.TO_BE_RELEASED.getCode();
        List<ProjectTaskEntity> list = this.getByTaskIds(taskIds);
        long blankChargeIdCount = list.stream().filter(t -> StringUtils.isBlank(t.getChargeId())).count();
        if (blankChargeIdCount > 0) {
            throw new ServiceException(ApiError.ERROR_95097);
        }

        //检查任务状态
        checkTaskState(list);
        //检查任务审核人不能为空
        checkTaskAuditor(taskIds);


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
            //操作日志
            List<SysLogEntity> sysLogEntityList = new LinkedList<>();
            generalTaskIds.forEach(taskId -> {
                sysLogEntityList.add(new SysLogEntity().setContent(String.format("编辑了一个[任务状态]由[%s]为[%s]", TaskStateEnum.TO_BE_RELEASED.getName(), TaskStateEnum.NOT_START.getName())).setClassPath(SysLogClassPathEnum.PROJECTTASKENTITY.getDesc()).setBusinessId(taskId));
            });
            sysLogService.addSysLogByBatchSave(sysLogEntityList);
            //发布任务消息
            noticeMessageService.releaseTaskNotice(loginUser.getUserName(), generalTasks, dto.getProductId());
        }
        //审核任务
        Integer reviewTaskCode = TaskTypeEnum.REVIEW_TASK.getCode();
        /**
         * 审核任务要 启动流程 任务评审流程
         */
        List<ProjectTaskEntity> reviewList = list.stream().filter(t -> reviewTaskCode.equals(t.getType())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(reviewList)) {
            String businessKey = BusinessProcessEnum.REVIEW_TASK.getBusinessKey();
            BusinessProcessEntity processEntity = businessProcessService.getProcessByBusinessKey(businessKey);
            //该流程是 任务负责人会签审核的
            if (!Objects.isNull(processEntity)) {
                List<TaskOperatorRecordEntity> recordEntityList = new ArrayList<>();
                LocalDateTime nowDate = LocalDateTime.now();
                Integer waitConfirmCode = TaskStateEnum.WAIT_CONFIRM.getCode();
                List<ProjectTaskEntity> noticeList = new ArrayList<>();
                for (ProjectTaskEntity review : reviewList) {
                    String chargeId = review.getChargeId();
                    if (StringUtils.isBlank(chargeId)) {
                        throw new ServiceException(ApiError.ERROR_95027);
                    }
                    StartProcessDTO startProcess = new StartProcessDTO();
                    startProcess.setBusinessKey(processEntity.getBusinessKey());
                    startProcess.setProcessDefinitionKey(processEntity.getProcessDefinitionKey());
                    startProcess.setUserId(loginUser.getUid());
                    Map<String, Object> parameterMap = new HashMap<>();
                    String params = processEntity.getParam();
                    if (StringUtils.isNotBlank(params)) {
                        String[] paramList = params.split(",");
                        if (paramList.length == 1) {
                            parameterMap.put(paramList[0], Arrays.asList(chargeId.split(",")));
                        }
                    }
                    //taskChargeIds
                    startProcess.setParameterMap(parameterMap);
                    //启动一个流程
                    ProcessNodeDTO process = workflowFeign.startProcess(startProcess);
                    //这个是流程Id
                    String processId = process.getProcessId();
                    //流程id 不为空 表示成功
                    if (StringUtils.isNotBlank(processId)) {
                        review.setProcessId(processId);
                        review.setRealityStartTime(nowDate);
                        review.setStatus(waitConfirmCode);
                        //保存操作记录
                        TaskOperatorRecordEntity recordEntity = new TaskOperatorRecordEntity();
                        recordEntity.setTaskId(review.getId());
                        recordEntity.setBeforeState(review.getStatus());
                        recordEntity.setAfterState(waitConfirmCode);
                        recordEntity.setOperatorId(loginUser.getUid());
                        recordEntity.setOperatorName(loginUser.getUserName());
                        recordEntityList.add(recordEntity);

                        //更改 时间 很流程id
                        this.updateById(review);
                        noticeList.add(review);
                    }
                }
                if (!projectTaskTimeRecordService.saveOrUpdateByProjectTaskList(noticeList)) {
                    log.error("ProjectTaskServiceImpl>>>publishTask>>>更新/保存工时记录失败请重试！");
                    throw new RuntimeException("更新/保存工时记录失败请重试");
                }
                //发送通知
                noticeMessageService.releaseTaskNotice(loginUser.getUserName(), noticeList, dto.getProductId());
                taskOperatorRecordService.saveBatch(recordEntityList);
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
     * 检查任务审核人
     *
     * @param taskIdList
     * @return void
     * @author yl
     * @date 2023-03-01 15:10
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
            //发送取消发布的 通知
            noticeMessageService.cancelReleaseTaskNotice(loginUser.getUserName(), list, dto.getProductId());
            taskOperatorRecordService.batchSaveTaskRecord(list, TaskStateEnum.TO_BE_RELEASED.getCode(), loginUser.getUid(), loginUser.getUserName(), "");
            //操作日志
            List<SysLogEntity> sysLogEntityList = new LinkedList<>();
            list.forEach(task -> {
                sysLogEntityList.add(new SysLogEntity().setContent(String.format("编辑了一个[任务状态]由[%s]为[%s]", TaskStateEnum.getName(task.getStatus()), TaskStateEnum.TO_BE_RELEASED.getName())).setClassPath(SysLogClassPathEnum.PROJECTTASKENTITY.getDesc()).setBusinessId(task.getId()));
            });
            sysLogService.addSysLogByBatchSave(sysLogEntityList);
        }
        return flag;

    }

    /**
     * 如果 排期变更任务状态 要是不是审核通过 和 审核不通过 就不能 操作任务
     */
    public void checkScheduleChangeStatus(List<ProjectTaskEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        String change = ProjectPlanConstant.PROJECT_PLAN_CHANGE;
        String auditPass = BaseStatusEnum.AUDIT_PASS.getStatus();
        String auditNoPass = BaseStatusEnum.AUDIT_NO_PASS.getStatus();
        List<String> status = new ArrayList<>(2);
        status.add(auditPass);
        status.add(auditNoPass);
        List<String> scheduleStatusList = list.stream().filter(s -> change.equals(s.getScheduleType()) && !status.contains(s.getScheduleStatus())).
                map(ProjectTaskEntity::getScheduleStatus).
                collect(Collectors.toList());
        //当不包含就要去除
        if (CollectionUtils.isNotEmpty(scheduleStatusList)) {
            throw new ServiceException(ApiError.ERROR_95147);
        }
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

        //检查是否在变更中
        checkScheduleChangeStatus(list);
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
            //操作日志
            List<SysLogEntity> sysLogEntityList = new LinkedList<>();
            list.forEach(task -> {
                sysLogEntityList.add(new SysLogEntity().setContent(String.format("编辑了一个[任务状态]由[%s]为[%s]", TaskStateEnum.getName(task.getStatus()), TaskStateEnum.CLOSE.getName())).setClassPath(SysLogClassPathEnum.PROJECTTASKENTITY.getDesc()).setBusinessId(task.getId()));
            });
            sysLogService.addSysLogByBatchSave(sysLogEntityList);
            //发送关闭任务通知
            noticeMessageService.closeTaskNotice(loginUser.getUserName(), list, dto.getProductId());
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean finishTask(OperateBaseTaskDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        List<String> taskIds = dto.getTaskIdList();
        //获取所有的任务列表
        List<ProjectTaskEntity> list = this.getByTaskIds(taskIds);
        //检查任务状态是否一样
        Integer state = checkTaskState(list);

        //检查是否在变更中
        checkScheduleChangeStatus(list);

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
        //检查sku 信息是否更改过
        List<TaskRefSkuConfigEntity> refSkuConfigList = taskRefSkuConfigService.getByTaskIds(allTaskIds);
        //这个是有配置表单的任务id
        List<String> configTaskIds = refSkuConfigList.stream().filter(r -> StringUtils.isNotBlank(r.getFieldConfigType())).map(TaskRefSkuConfigEntity::getTaskId).collect(Collectors.toList());
        Integer noSkuChange = IsConstant.NO;
        //这个是没有改变sku 的任务id 集合
        List<String> noSkuChangeTaskIds = list.stream().filter(l -> l.getIsSkuChange().equals(noSkuChange)).map(ProjectTaskEntity::getId).collect(Collectors.toList());
        //如果 包含没有改变的sku  就要提醒
        if (CollectionUtils.isNotEmpty(noSkuChangeTaskIds) && configTaskIds.containsAll(noSkuChangeTaskIds)) {
            throw new ServiceException(ApiError.ERROR_95077);
        }
        //一般任务code
        Integer generalTaskCode = TaskTypeEnum.GENERAL_TASK.getCode();
        //是否确认完成
        Boolean isConfirmFinish = dto.getIsConfirmFinish() == null ? false : dto.getIsConfirmFinish();
        //当不是的时候
        if (!isConfirmFinish) {
            //查询是否有未完成的sku
            List<String> notFinishSkuList = projectTaskRefSkuService.checkTaskRefSkuFinish(taskIds);
            if (CollectionUtils.isNotEmpty(notFinishSkuList)) {
                String skuNo = notFinishSkuList.stream().collect(Collectors.joining(","));
                String warning = ApiError.ERROR_800.msg;
                String warningMsg = String.format(warning, skuNo);
                throw new ServiceException(ApiError.ERROR_800.code, warningMsg);
            }

        }

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
        long noProcess = list.stream().filter(t -> !ingCode.equals(t.getStatus()) && !approvalNoPass.equals(t.getStatus()) && !TaskStateEnum.PORTION_FINISH.getCode().equals(t.getStatus())).count();
        if (noProcess > 0) {
            throw new ServiceException(ApiError.ERROR_95044);
        }

        //待审核
        Integer WaitConfirmCode = TaskStateEnum.WAIT_CONFIRM.getCode();
        /**
         *
         *   到了这一步 那么可以完成任务了
         *   没有流程审核的任务  更改状态为已完成
         *
         */
        List<String> noProcessTaskIds = noProcessList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        LocalDateTime nowDate = LocalDateTime.now();
        Integer noFinish = IsConstant.NO;
        //任务与sku 的关联
        List<ProjectTaskRefSkuEntity> taskRefSkuList = projectTaskRefSkuService.getByTaskIdList(noProcessTaskIds);

        //没有完成的sku的 任务id
        List<String> noFinishSkuTaskIdList = taskRefSkuList.stream().filter(r -> noFinish.equals(r.getIsFinishTask())).map(ProjectTaskRefSkuEntity::getTaskId).distinct().collect(Collectors.toList());
        this.updateTaskState(noFinishSkuTaskIdList, TaskStateEnum.PORTION_FINISH.getCode(), null, nowDate);
        //发送部分完成任务通知
        List<ProjectTaskEntity> portionFinishList = list.stream().filter(p -> noFinishSkuTaskIdList.contains(p.getId())).collect(Collectors.toList());
        noticeMessageService.portionFinishTaskNotice(loginUser.getUserName(), portionFinishList, dto.getProductId());

        List<String> finishSkuTaskIdList = noProcessTaskIds.stream().filter(t -> !noFinishSkuTaskIdList.contains(t)).collect(Collectors.toList());
        this.updateTaskState(finishSkuTaskIdList, TaskStateEnum.FINISH.getCode(), null, nowDate);

        taskOperatorRecordService.batchSaveRecord(noProcessTaskIds, ingCode, TaskStateEnum.FINISH.getCode(), loginUser.getUid(), loginUser.getUserName(), "");
        //操作日志
        List<SysLogEntity> sysLogEntityList = new LinkedList<>();
        list.forEach(task -> {
            sysLogEntityList.add(new SysLogEntity().setContent(String.format("编辑了一个[任务状态]由[%s]为[%s]", TaskStateEnum.getName(task.getStatus()), TaskStateEnum.FINISH.getName())).setClassPath(SysLogClassPathEnum.PROJECTTASKENTITY.getDesc()).setBusinessId(task.getId()));
        });
        sysLogService.addSysLogByBatchSave(sysLogEntityList);
        //发送完成任务通知
        List<ProjectTaskEntity> finishSkuTaskList = list.stream().filter(p -> finishSkuTaskIdList.contains(p.getId())).collect(Collectors.toList());
        noticeMessageService.finishTaskNotice(loginUser.getUserName(), finishSkuTaskList, dto.getProductId());

        //当有流程的不为空
        if (CollectionUtils.isNotEmpty(processList)) {
            //获取到所有流程的信息
            List<BusinessProcessEntity> businessProcessList = businessProcessService.list();
            List<ProjectTaskEntity> waitConfirmNoticeList = new ArrayList<>(processList.size());
            //有审核流程的 要启动流程了
            for (ProjectTaskEntity processTask : processList) {
                //查询任务下审核人
                List<TaskChargeDistributionEntity> taskChargeDistributionList = taskChargeDistributionService.listBySourceAndTaskId(MathUtil.THREE, processTask.getId());
                if (CollectionUtils.isEmpty(taskChargeDistributionList)) {
                    throw new ServiceException(ApiError.ERROR_95045);
                }
                List<List<String>> membersIds = new ArrayList<>();
                for (TaskChargeDistributionEntity taskChargeDistributionEntity : taskChargeDistributionList) {
                    if (StringUtils.isBlank(taskChargeDistributionEntity.getChargeIds())) {
                        throw new ServiceException(ApiError.ERROR_95045);
                    }
                    List<String> userIdList = Arrays.stream(taskChargeDistributionEntity.getChargeIds().split(",")).collect(Collectors.toList());
                    membersIds.add(userIdList);
                }
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
                                Map<String, Object> parameterMap = getProcessParameter(processEntity, membersIds);
                                startProcess.setParameterMap(parameterMap);
                                //启动流程
                                ProcessNodeDTO processResult = workflowFeign.startProcess(startProcess);
                                String processId = processResult.getProcessId();
                                //当流程id不为空的时候
                                if (StringUtils.isNotBlank(processId)) {
                                    processTask.setProcessId(processId);
                                    processTask.setStatus(WaitConfirmCode);
                                    processTask.setRealityStartTime(nowDate);
                                    processTask.setBusinessProcessId(processEntity.getId());
                                    waitConfirmNoticeList.add(processTask);
                                    this.updateById(processTask);
                                    TaskOperatorRecordEntity recordEntity = new TaskOperatorRecordEntity();
                                    recordEntity.setOperatorName(loginUser.getUserName());
                                    recordEntity.setOperatorId(loginUser.getUid());
                                    recordEntity.setBeforeState(ingCode);
                                    recordEntity.setAfterState(WaitConfirmCode);
                                    recordEntity.setTaskId(processTask.getId());
                                    taskOperatorRecordService.save(recordEntity);
                                }
                            }
                        }
                    }
                } else {
                    List<String> processTaskIds = processList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
                    //当流程Id 不为空就表示 有流程 是审核不通过的
                    this.updateTaskState(processTaskIds, WaitConfirmCode, null, null);
                    taskOperatorRecordService.batchSaveRecord(processTaskIds, TaskStateEnum.APPROVAL_NO_PASS.getCode(), WaitConfirmCode, loginUser.getUid(), loginUser.getUserName(), "");
                }
            }
            if (!projectTaskTimeRecordService.saveOrUpdateByProjectTaskList(waitConfirmNoticeList)) {
                log.error("ProjectTaskServiceImpl>>>publishTask>>>更新/保存工时记录失败请重试！");
                throw new RuntimeException("更新/保存工时记录失败请重试");
            }

            //发送完成待审核的消息
            noticeMessageService.finishWaitConfirmNotice(loginUser.getUserName(), waitConfirmNoticeList, dto.getProductId());
        }

        return true;
    }

    /**
     * 获取到流程所需要的参数
     *
     * @param processEntity
     * @param approvalUserIds 审核人
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author yl
     * @date 2022-11-22 14:36
     */
    private Map<String, Object> getProcessParameter(BusinessProcessEntity processEntity, List<List<String>> approvalUserIds) {
        String param = processEntity.getParam();
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(param)) {
            String[] paramList = param.split(",");
 /*           String businessKey = processEntity.getBusinessKey();
            String generalTask = BusinessProcessEnum.GENERAL_TASK.getBusinessKey();
            String docsChange = BusinessProcessEnum.DOCS_CHANGE.getBusinessKey();
            String reviewTask = BusinessProcessEnum.REVIEW_TASK.getBusinessKey();
            List<String> list = new ArrayList<>(3);
            list.add(generalTask);
            list.add(docsChange);
            list.add(reviewTask);
            if (!list.contains(businessKey)) {
                for (int i = 0; i < approvalUserIds.size(); i++) {
                    map.put(paramList[i], approvalUserIds.get(i));
                }
            } else {
                map.put(paramList[0], approvalUserIds.get(0));
            }*/
            for (int i = 0; i < approvalUserIds.size(); i++) {
                map.put(paramList[i], approvalUserIds.get(i));
            }

        }
        return map;
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
        //检查是否在变更中
        checkScheduleChangeStatus(list);
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
            //检查前置任务是否完成
            preTaskService.checkPreTaskFinish(allTaskIds);
            //检查子任务是否有完成
            this.checkSonTaskFinish(allTaskIds, dto.getProductId());


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
        //审核中
        Integer approvalIngCode = TaskStateEnum.APPROVAL_ING.getCode();

        //部分完成
        Integer portionFinishCode = TaskStateEnum.PORTION_FINISH.getCode();
        // 只有待审核 和 完成待审核 的状态 才可以审核通过
        if (!waitConfirmCode.equals(state) &&
                !approvalIngCode.equals(state) &&
                !portionFinishCode.equals(state)) {
            throw new ServiceException(ApiError.ERROR_95038);
        }

        List<String> taskIdList = list.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        this.updateTaskState(taskIdList, approvalIngCode, null, null);
        taskOperatorRecordService.batchSaveRecord(taskIdList, waitConfirmCode, approvalIngCode, loginUser.getUid(), loginUser.getUserName(), "");
        //操作日志
        List<SysLogEntity> sysLogEntityList = new LinkedList<>();
        taskIdList.forEach(taskId -> {
            sysLogEntityList.add(new SysLogEntity().setContent(String.format("编辑了一个[任务状态]由[%s]为[%s]", TaskStateEnum.WAIT_CONFIRM.getName(), TaskStateEnum.APPROVAL_ING.getName())).setClassPath(SysLogClassPathEnum.PROJECTTASKENTITY.getDesc()).setBusinessId(taskId));
        });
        sysLogService.addSysLogByBatchSave(sysLogEntityList);

        List<TaskCommentEntity> taskCommentList = new ArrayList<>(taskIds.size());
        for (String taskId : taskIds) {
            //添加评论
            TaskCommentEntity comment = new TaskCommentEntity();
            comment.setComment("[审核结果-审核通过]" + dto.getComment());
            comment.setTaskId(taskId);
            comment.setCreateUserName(loginUser.getUserName());
            comment.setCreateUserId(loginUser.getUid());
            taskCommentList.add(comment);
        }
        taskCommentService.batchSaveTaskComment(taskCommentList);

        String comment = dto.getComment();
        if (StringUtils.isBlank(comment)) {
            comment = "";
        }

        String keyFlag = userId + JSONObject.toJSONString(dto.getTaskDataList());
        boolean result = redisService.setNx(keyFlag, 1, 1, TimeUnit.MINUTES);
        if (!result) {
            throw new ServiceException(ApiError.ERROR_1014);
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
        noticeMessageService.approvalTaskNotice(loginUser.getUserName(), list, dto.getProductId());

        //发送完成待审核的消息
        noticeMessageService.finishWaitConfirmNotice(loginUser.getUserName(), list, dto.getProductId());
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

        //待审核
        Integer waitConfirmCode = TaskStateEnum.WAIT_CONFIRM.getCode();
        if (!approvalIngCode.equals(state) && !waitConfirmCode.equals(state)
        ) {
            throw new ServiceException(ApiError.ERROR_95046);
        }
        List<TaskCommentEntity> taskCommentList = new ArrayList<>(taskIds.size());

        for (TaskHandleDataDTO entity : taskDataList) {
            String taskId = myToDoList.stream().filter(obj -> entity.getProcessId().equals(obj.getProcessInstanceId())).map(TaskShowDTO::getTaskId).findFirst().orElse("");
            //审核不通过
            if (StringUtils.isNotBlank(taskId)) {
                ApproveProcessDTO approveProcess = new ApproveProcessDTO();
                approveProcess.setTaskId(taskId);
                approveProcess.setProcessInstanceId(entity.getProcessId());
                approveProcess.setUserId(loginUser.getUid());
                approveProcess.setComment(dto.getComment());
                workflowFeign.taskNoPass(approveProcess);
            }

            //添加评论
            TaskCommentEntity comment = new TaskCommentEntity();
            comment.setComment("[审核结果-审核不通过]" + dto.getComment());
            comment.setTaskId(entity.getTaskId());
            comment.setCreateUserName(loginUser.getUserName());
            comment.setCreateUserId(loginUser.getUid());
            taskCommentList.add(comment);
        }

        List<String> taskIdList = list.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        boolean flag = this.updateTaskState(taskIdList, TaskStateEnum.APPROVAL_NO_PASS.getCode(), null, null);
        if (flag) {
            taskOperatorRecordService.batchSaveRecord(taskIdList, approvalIngCode, TaskStateEnum.APPROVAL_NO_PASS.getCode(), loginUser.getUid(), loginUser.getUserName(), "");
            //操作日志
            List<SysLogEntity> sysLogEntityList = new LinkedList<>();
            taskIdList.forEach(taskId -> {
                sysLogEntityList.add(new SysLogEntity().setContent(String.format("编辑了一个[任务状态]由[%s]为[%s],原因[%s]", TaskStateEnum.APPROVAL_ING.getName(), TaskStateEnum.APPROVAL_NO_PASS.getName(), dto.getComment())).setClassPath(SysLogClassPathEnum.PROJECTTASKENTITY.getDesc()).setBusinessId(taskId));
            });
            sysLogService.addSysLogByBatchSave(sysLogEntityList);
        }

        taskCommentService.batchSaveTaskComment(taskCommentList);
        //发送通知
        noticeMessageService.approvalTaskNotice(loginUser.getUserName(), list, dto.getProductId());
        return flag;
    }


    /**
     * 从新开始
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-17 15:52
     */
    @Override
    public Boolean restartTask(OperateBaseTaskDTO dto) {
        List<String> taskIds = dto.getTaskIdList();
        //获取所有的任务列表
        List<ProjectTaskEntity> list = this.getByTaskIds(taskIds);
        //检查任务状态是否一样
        Integer state = checkTaskState(list);

        checkScheduleChangeStatus(list);
        //审核不通过
        Integer approvalNoPass = TaskStateEnum.APPROVAL_NO_PASS.getCode();
        if (!approvalNoPass.equals(state)) {
            throw new ServiceException(ApiError.ERROR_95066);
        }
        //一般任务有审核
        Integer generalApproval = TaskProcessTypeEnum.GENERAL_APPROVAL_TASK.getCode();
        Integer reviewTask = TaskProcessTypeEnum.REVIEW_TASK.getCode();
        for (ProjectTaskEntity task : list) {
            Integer taskProperty = getTaskProperty(task);
            //一般任务有审核 状态改成进行中
            if (taskProperty.equals(generalApproval)) {
                task.setStatus(TaskStateEnum.ING.getCode());
            }
            //评审任务 从新开始 改为待审核
            if (taskProperty.equals(reviewTask)) {
                task.setStatus(TaskStateEnum.WAIT_CONFIRM.getCode());
            }
        }
        return this.saveOrUpdateBatch(list);

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
    @Transactional(rollbackFor = Exception.class)
    public void approvalTaskPass(String processId) {
        LoginUser loginUser = commonService.getUserInfo();
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskEntity::getProcessId, processId);
        queryWrapper.last("LIMIT 1");
        ProjectTaskEntity taskEntity = this.getOne(queryWrapper);
        Integer notFinish = IsConstant.NO;
        if (!Objects.isNull(taskEntity)) {
            List<ProjectTaskRefSkuEntity> list = projectTaskRefSkuService.getByTaskId(taskEntity.getId());
            List<String> skuIdList = list.stream().filter(ref -> notFinish.equals(ref.getIsFinishTask())).map(ProjectTaskRefSkuEntity::getSkuId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(skuIdList)) {
                taskEntity.setStatus(TaskStateEnum.PORTION_FINISH.getCode());
                //任务属性
                Integer taskType = taskEntity.getType();
                //评审任务
                Integer reviewTask = TaskTypeEnum.REVIEW_TASK.getCode();
                //当是评审任务的时候就 并且是部分完成的时候就要启动评审流程
                if (reviewTask.equals(taskType)) {
                    taskEntity = startReviewTaskProcess(taskEntity, loginUser.getUid(), BusinessProcessEnum.REVIEW_TASK.getBusinessKey());
                }
            } else {
                taskEntity.setStatus(TaskStateEnum.FINISH.getCode());
            }
            taskEntity.setRealityEndTime(LocalDateTime.now());
            this.updateById(taskEntity);
            if (!projectTaskTimeRecordService.saveOrUpdateByProjectTaskList(new ArrayList<>(Arrays.asList(taskEntity)))) {
                log.error("ProjectTaskServiceImpl>>>approvalTaskPass>>更新/保存工时记录失败请重试！");
                throw new RuntimeException("更新/保存工时记录失败请重试");
            }
            if (MathUtil.ONE.equals(taskEntity.getProperty())) {
                //审核完成后查询产品下立项任务是否全部完成，完成则自动将产品变更为已立项
                Boolean approvalTaskFlag = this.projectApprovalTaskFinish(taskEntity.getProductId(), MathUtil.ONE);
                if (approvalTaskFlag) {
                    UpdateProductDTO dto = new UpdateProductDTO();
                    dto.setProductId(taskEntity.getProductId());
                    dto.setApprovalStatus(ApprovalStatusEnum.APPROVAL.getCode());
                    productInfoService.updateProduct(dto);
                }
            }

            //审核完成后查询产品下所有任务是否全部完成，完成则自动将SKU列表的产品开发状态变更为已完成
            Boolean allTaskFlag = this.projectApprovalTaskFinish(taskEntity.getProductId(), MathUtil.TWO);
            if (allTaskFlag) {
                productDetailService.updateProductStateByProductId(taskEntity.getProductId(), ProductDetailStateEnum.DEVELOP_FINISH.getCode());
            }
            //保存记录
            TaskOperatorRecordEntity recordEntity = new TaskOperatorRecordEntity();
            recordEntity.setTaskId(taskEntity.getId());
            recordEntity.setBeforeState(taskEntity.getStatus());
            recordEntity.setAfterState(TaskStateEnum.FINISH.getCode());
            recordEntity.setOperatorId(loginUser.getUid());
            recordEntity.setOperatorName(loginUser.getUserName());
            taskOperatorRecordService.save(recordEntity);
            //操作日志
            SysLogEntity sysLogEntity = new SysLogEntity()
                    .setContent(String.format("编辑了一个[任务状态]由[%s]为[%s]", TaskStateEnum.getName(taskEntity.getStatus()), TaskStateEnum.FINISH.getName()))
                    .setClassPath(SysLogClassPathEnum.PROJECTTASKENTITY.getDesc())
                    .setBusinessId(taskEntity.getId());
            sysLogService.save(sysLogEntity);
        }

    }

    /**
     * @param productId
     * @param type
     * @return Boolean
     * @description: 判断任务是否完成
     * @author Will
     * @date: 2023/2/2 11:00
     */
    private Boolean projectApprovalTaskFinish(String productId, Integer type) {
        List<ProjectTaskEntity> projectTaskList = this.listByProductId(productId);
        if (CollectionUtils.isEmpty(projectTaskList)) {
            return Boolean.FALSE;
        }
        //已立项的任务
        if (MathUtil.ONE.equals(type)) {
            List<ProjectTaskEntity> taskList = projectTaskList.stream().filter(obj -> TaskConstant.APPROVAL_TASK.equals(obj.getProperty())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(taskList)) {
                return Boolean.FALSE;
            }
            //立项任务及其子任务全部完成则返回true
            if (CollectionUtils.isNotEmpty(taskList)) {
                //已完成任务数量
                long count = taskList.stream().filter(obj -> !TaskStateEnum.FINISH.getCode().equals(obj.getStatus())).count();
                if (count > 0) {
                    return Boolean.FALSE;
                }
                List<String> taskIds = taskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
                //查询子任务是否全部完成
                List<ProjectTaskEntity> childrenTaskList = new ArrayList<>();
                //添加子任务
                preTaskService.listChildrenTask(taskIds, childrenTaskList);
                if (CollectionUtils.isNotEmpty(childrenTaskList)) {
                    //判断子任务是否全部完成
                    long count1 = childrenTaskList.stream().filter(obj -> !TaskStateEnum.FINISH.getCode().equals(obj.getStatus())).count();
                    if (count1 > 0) {
                        return Boolean.FALSE;
                    }
                }
            }
        }
        //所有任务
        if (MathUtil.TWO.equals(type)) {
            //已完成任务数量
            long count = projectTaskList.stream().filter(obj -> !TaskStateEnum.FINISH.getCode().equals(obj.getStatus())).count();
            if (count > 0) {
                return Boolean.FALSE;
            }
            List<String> taskIds = projectTaskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            //查询子任务是否全部完成
            List<ProjectTaskEntity> childrenTaskList = new ArrayList<>();
            //添加子任务
            preTaskService.listChildrenTask(taskIds, childrenTaskList);
            if (CollectionUtils.isNotEmpty(childrenTaskList)) {
                //判断子任务是否全部完成
                long count1 = childrenTaskList.stream().filter(obj -> !TaskStateEnum.FINISH.getCode().equals(obj.getStatus())).count();
                if (count1 > 0) {
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.TRUE;
    }


    /**
     * 启动一个评审任务流程
     *
     * @param taskEntity
     * @return com.erp.model.plm.entity.ProjectTaskEntity
     * @author yl
     * @date 2022-12-06 15:24
     */
    private ProjectTaskEntity startReviewTaskProcess(ProjectTaskEntity taskEntity, String userId, String businessKey) {
        //这里需要启动一个变更流程
        BusinessProcessEntity businessProcess = businessProcessService.getProcessByBusinessKey(businessKey);
        StartProcessDTO startProcess = new StartProcessDTO();
        startProcess.setUserId(userId);
        startProcess.setProcessDefinitionKey(businessProcess.getProcessDefinitionKey());
        startProcess.setBusinessKey(businessProcess.getBusinessType());
        Map<String, Object> parameterMap = new HashMap<>();
        //如果是 评审任务 就是任务负责人
        String chargeId = taskEntity.getChargeId();
        if (StringUtils.isEmpty(chargeId)) {
            throw new ServiceException(ApiError.ERROR_95045);
        }
        List<String> membersIds = Arrays.asList(chargeId.split(","));
        parameterMap.put("taskChargeIdList", membersIds);
        startProcess.setParameterMap(parameterMap);
        ProcessNodeDTO processResult = workflowFeign.startProcess(startProcess);
        String processId = processResult.getProcessId();
        if (StringUtils.isNotBlank(processId)) {
            //更改任务的状态为未待审核 以及流程id
            taskEntity.setProcessId(processId);
            taskEntity.setBusinessProcessId(businessProcess.getId());
        }
        return taskEntity;
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

        //部分完成
        Integer portionFinishState = TaskStateEnum.PORTION_FINISH.getCode();
        //审核中
        Integer approvalIngState = TaskStateEnum.APPROVAL_ING.getCode();

        //审核通过
        Integer approvalPassState = TaskStateEnum.APPROVAL_PASS.getCode();

        //审核不通过
        Integer approvalNoPassState = TaskStateEnum.APPROVAL_NO_PASS.getCode();

        TaskOperatorRecordEntity notStartEntity = recordList.stream().filter(r -> r.getAfterState().equals(notStart)).findFirst().orElse(null);
        TaskOperatorRecordEntity ingStateEntity = recordList.stream().filter(r -> r.getAfterState().equals(ingState)).findFirst().orElse(null);
        TaskOperatorRecordEntity finishStateEntity = recordList.stream().filter(r -> r.getAfterState().equals(finishState)).findFirst().orElse(null);
        TaskOperatorRecordEntity approvalIngEntity = recordList.stream().filter(r -> r.getAfterState().equals(approvalIngState)).findFirst().orElse(null);
        TaskOperatorRecordEntity waitConfirmEntity = recordList.stream().filter(r -> r.getAfterState().equals(waitConfirmState)).findFirst().orElse(null);
        TaskOperatorRecordEntity approvalNoPassEntity = recordList.stream().filter(r -> r.getAfterState().equals(approvalNoPassState)).findFirst().orElse(null);


        //先添加待发布的
        TaskProcessNodeDTO processNode = new TaskProcessNodeDTO();
        processNode.setIfFinishNode(true);
        processNode.setOperateTime(taskEntity.getCreateTime());
        processNode.setOperateUserName(taskEntity.getCreateUserName());
        processNode.setNodeName(TaskStateEnum.TO_BE_RELEASED.getName());
        processNode.setNodeState(waitReleasedState);
        resultList.add(processNode);
        //所有人员
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        // 根据流程id查询所有审核信息
        List<AuditorHandleDTO> approveRecordShowList = null;
        if (StringUtils.isNotBlank(taskEntity.getProcessId())) {
            approveRecordShowList = workflowFeign.getHistoryTaskByProcessId(taskEntity.getProcessId());
        }
        //查询一般任务审核人
        List<TaskChargeDistributionEntity> taskChargeDistributionList = taskChargeDistributionService.listBySourceAndTaskId(MathUtil.THREE, taskEntity.getId());

        //一般任务
        Integer general = TaskProcessTypeEnum.GENERAL_TASK.getCode();

        //一般带审核任务
        Integer generalApproval = TaskProcessTypeEnum.GENERAL_APPROVAL_TASK.getCode();
        //评审任务
        Integer reviewTask = TaskProcessTypeEnum.REVIEW_TASK.getCode();

        if (general.equals(processType)) {
            //添加待开始
            resultList.add(getProcessNode(notStartEntity, notStart, userList, approveRecordShowList, null));
            //添加进行中
            resultList.add(getProcessNode(ingStateEntity, ingState, userList, approveRecordShowList, null));
            //添加已完成
            resultList.add(getProcessNode(finishStateEntity, finishState, userList, approveRecordShowList, null));
        }

        if (generalApproval.equals(processType)) {
            //添加待开始
            resultList.add(getProcessNode(notStartEntity, notStart, userList, approveRecordShowList, null));
            //添加进行中
            resultList.add(getProcessNode(ingStateEntity, ingState, userList, approveRecordShowList, null));

            if (waitConfirmState.equals(taskState)) {
                //添加待审核
                resultList.add(getProcessNode(waitConfirmEntity, waitConfirmState, userList, approveRecordShowList, taskChargeDistributionList));
            }
            if (approvalIngState.equals(taskState)) {
                //添加审核中
                resultList.add(getProcessNode(approvalIngEntity, approvalIngState, userList, approveRecordShowList, taskChargeDistributionList));
            }

            if (approvalNoPassState.equals(taskState)) {//添加审核不通过
                resultList.add(getProcessNode(approvalNoPassEntity, approvalNoPassState, userList, approveRecordShowList, taskChargeDistributionList));
            }

            if (finishState.equals(taskState) || portionFinishState.equals(taskState)) {
                //添加审核通过
                resultList.add(getProcessNode(finishStateEntity, finishState, userList, approveRecordShowList, taskChargeDistributionList));
            }

        }

        if (reviewTask.equals(processType)) {
            if (waitConfirmState.equals(taskState)) {
                //添加待审核
                resultList.add(getProcessNode(waitConfirmEntity, waitConfirmState, userList, approveRecordShowList, null));
            }
            if (approvalIngState.equals(taskState)) {
                //添加审核中
                resultList.add(getProcessNode(approvalIngEntity, approvalIngState, userList, approveRecordShowList, null));
            }

            if (approvalNoPassState.equals(taskState)) {//添加审核不通过
                resultList.add(getProcessNode(approvalNoPassEntity, approvalNoPassState, userList, approveRecordShowList, null));
            }

            if (finishState.equals(taskState) || portionFinishState.equals(taskState)) {
                //添加审核通过
                resultList.add(getProcessNode(finishStateEntity, finishState, userList, approveRecordShowList, null));
            }
        }

        return resultList;
    }


    //
    public TaskProcessNodeDTO getProcessNode(TaskOperatorRecordEntity entity, Integer state, List<FindUserDTO> userList, List<AuditorHandleDTO> approveRecordShowList, List<TaskChargeDistributionEntity> taskChargeDistributionList) {
        boolean flag = !Objects.isNull(entity);
        String operatorName = "";
        LocalDateTime operatorTime = null;
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
        List<Pair<String, LocalDateTime>> dateList = new ArrayList<>();

        if (CollectionUtils.isEmpty(approveRecordShowList)) {
            return waitReleasedDTO;
        }
        Boolean isShwoDate = Boolean.TRUE;
        List<TaskProcessNodeDetailDTO> detailList = new ArrayList<>();
        //当状态为待审核、审核中、审核通过、审核不通过时添加详情
        if (TaskStateEnum.WAIT_CONFIRM.getCode().equals(state) || TaskStateEnum.APPROVAL_ING.getCode().equals(state)
                || TaskStateEnum.FINISH.getCode().equals(state) || TaskStateEnum.APPROVAL_NO_PASS.getCode().equals(state)) {
            //根据节点名称分组，将不同节点审核人分隔
            Map<String, List<AuditorHandleDTO>> map = approveRecordShowList.stream().collect(Collectors.groupingBy(AuditorHandleDTO::getActivityName));
            for (Map.Entry<String, List<AuditorHandleDTO>> entry : map.entrySet()) {
                List<AuditorHandleDTO> value = entry.getValue();
                List<TaskProcessNodeDTO> taskProcessNodeList = new ArrayList<>();
                TaskProcessNodeDetailDTO taskProcessNodeDetailDTO = new TaskProcessNodeDetailDTO();
                taskProcessNodeDetailDTO.setStartDate(StringUtils.isBlank(value.get(0).getStartTime()) ? null : value.get(0).getStartTime());
                //查询流程
                for (AuditorHandleDTO auditorHandleDTO : value) {
                    TaskProcessNodeDTO taskProcessNodeDTO = new TaskProcessNodeDTO();
                    String userName = userList.stream().filter(e -> e.getUserId().equals(auditorHandleDTO.getHandleUserId())).map(FindUserDTO::getUserName).findFirst().orElse("");
                    taskProcessNodeDTO.setOperateUserName(userName);
                    taskProcessNodeDTO.setIfFinishNode(Boolean.TRUE);
                    //已经审核通过的数据格式化时间
                    if (ObjectUtils.isNotEmpty(auditorHandleDTO.getEndTime())) {
                        try {
                            LocalDateTime date = LocalDateTime.parse(auditorHandleDTO.getEndTime(), DateTimeFormatter.ofPattern(DateUtils.DATE_FORMAT_19));
                            taskProcessNodeDTO.setOperateTime(date);
                        } catch (Exception e) {
                            throw new ServiceException(ApiError.Default);
                        }
                    }
                    if (TaskStateEnum.APPROVAL_NO_PASS.getCode().equals(state) && operatorName.equals(userName) && !TaskStateEnum.APPROVAL_PASS.getName().equals(auditorHandleDTO.getHandContent())) {
                        //审核不通过时将对应数据状态变更为审核不通过
                        taskProcessNodeDTO.setNodeName(TaskStateEnum.APPROVAL_NO_PASS.getName());
                        taskProcessNodeDTO.setOperateTime(operatorTime);
                    } else {
                        taskProcessNodeDTO.setNodeName(auditorHandleDTO.getHandContent());
                    }
                    if (BaseStatusEnum.WAIT_AUDIT.getName().equals(auditorHandleDTO.getHandContent())) {
                        isShwoDate = Boolean.FALSE;
                    }
                    dateList.add(new Pair(userName, taskProcessNodeDTO.getOperateTime()));
                    taskProcessNodeList.add(taskProcessNodeDTO);
                }
                taskProcessNodeDetailDTO.setIfFinishNode(Boolean.TRUE);
                taskProcessNodeDetailDTO.setList(taskProcessNodeList);
                detailList.add(taskProcessNodeDetailDTO);
            }
            //按生成时间排序（逐级排序）
            if (CollectionUtils.isNotEmpty(detailList)) {
                List<TaskProcessNodeDetailDTO> collect = detailList.stream().sorted(Comparator.comparing(e -> e.getStartDate(), Comparator.nullsLast(String::compareTo))).collect(Collectors.toList());
                waitReleasedDTO.setDetailList(collect);
            }
            if (CollectionUtils.isNotEmpty(dateList)) {
                Pair<String, LocalDateTime> pair = dateList.stream().max(Comparator.comparing(e -> e.getValue(), Comparator.nullsLast(LocalDateTime::compareTo))).get();
                waitReleasedDTO.setOperateUserName(pair.getKey());
                if (CollectionUtils.isNotEmpty(dateList) && isShwoDate) {
                    LocalDateTime date = pair.getValue();
                    waitReleasedDTO.setOperateTime(date);
                } else {
                    waitReleasedDTO.setOperateTime(null);
                }
            }
        }
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
        int nameLength = name.length();
        if (nameLength > 50) {
            throw new ServiceException(ApiError.ERROR_95065);
        }
        //根据产品很任务id 获取任务名
        List<ProjectTaskEntity> taskList = getByProductId(productId);
        if (StringUtils.isNotBlank(taskId)) {
            taskList = taskList.stream().filter(t -> !taskId.equals(t.getId())).collect(Collectors.toList());
        }
        List<String> taskNames = taskList.stream().map(ProjectTaskEntity::getName).collect(Collectors.toList());
        if (taskNames.contains(name)) {
            throw new ServiceException(ApiError.ERROR_95013);
        }
    }

    private void checkFieldConfig(ProjectTaskDTO dto) {
        //配置表单属性
        String fieldConfigType = dto.getFieldConfigType();
        List<String> refSkuIdList = dto.getRefSkuIdList();
        //生成sku
        String createSku = TaskConstant.CREATE_SKU;
        //填写sku
        String fillProductInfo = TaskConstant.FILL_PRODUCT_INFO;
        //filedjson
        String fieldJson = dto.getFieldJson();
        //第一种 sku不等于空并且大于0  并且  表单属性不为空且为填写
        Boolean needCheckFirst = CollectionUtils.isNotEmpty(refSkuIdList) && (StringUtils.isNotBlank(fieldConfigType) && fillProductInfo.equals(fieldConfigType) && StringUtils.isNotBlank(fieldJson));

        //第二种 sku 没有  并且 表单属性不为空 且为生成
        Boolean needCheckSecond = CollectionUtils.isEmpty(refSkuIdList)
                && (StringUtils.isNotBlank(fieldConfigType) && (createSku.equals(fieldConfigType) || (StringUtils.isNotBlank(dto.getFieldJson()) && RelatedSkuTypeEnum.ALL_RELATED.getCode().equals(dto.getRelatedSkuType()))));


        //自定义审核人
        Integer type = dto.getType();
        //一般任务
        Integer generalTask = TaskTypeEnum.GENERAL_TASK.getCode();
        List<TaskChargeDistributionDTO> approvalList = dto.getApprovalList();
        //TODO 2023-03-30 暂时取消审核流程
        //如果是一般任务 必须要有审核流程
     /*   if (needCheckFirst || needCheckSecond) {
            if (generalTask.equals(type)) {
                if (CollectionUtils.isEmpty(approvalList)) {
                    throw new ServiceException(ApiError.ERROR_95078);
                }
            }
        }*/
    }

    //获取预警信息
    public String getWarning(Integer state, Integer finishState, LocalDateTime planEndTime) {
        Date nowDay = new Date();
        Integer approvalPass = TaskStateEnum.APPROVAL_PASS.getCode();
        String warning = "";
        if (planEndTime != null) {
            //状态
            if (!finishState.equals(state) && !approvalPass.equals(state)) {
                Long difference = DateUtil.getDiffDay(LocalDateTimeUtil.format(planEndTime, DateUtils.DATE_FORMAT_10), DateUtils.format(nowDay, DateUtils.DATE_FORMAT_10));
                if (difference > 0) {
                    warning = "过期" + difference + "天";
                }
                if (difference == 0) {
                    warning = "今天后过期";
                }
                if (difference < 0) {
                    if (difference >= -2) {
                        warning = Math.abs(difference) + "天后过期";
                    }
                }
            }
        }
        return warning;
    }

    /**
     * 编辑任务操作日志
     */
    private void addProjectTaskDTOLog(ProjectTaskDTO projectTaskDTO, ProjectTaskEntity oldEntity, String businessId) {
        ProjectTaskDTO oldDto = new ProjectTaskDTO();
        //查询修改之前的任务数据
        if (ObjectUtils.isNotEmpty(oldEntity)) {
            BeanMapperUtils.copy(oldEntity, oldDto);
        }
        //处理变更前交付文档
        List<DeliveryDocsDTO> deliveryDocsList = taskDeliveryService.getByTaskId(businessId);
        if (CollectionUtils.isNotEmpty(deliveryDocsList)) {
            String deliveryDocsNames = deliveryDocsList.stream().map(DeliveryDocsDTO::getDeliveryDocsName).collect(Collectors.joining(","));
            oldDto.setDeliveryDocsNames(deliveryDocsNames);
        }
        //处理变更前前置任务
        List<PreTaskVO> preTaskList = preTaskService.getPreTaskIdList(businessId);
        List<String> preTaskIdList = preTaskList.stream().map(PreTaskVO::getPreTaskId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(preTaskIdList)) {
            List<ProjectTaskEntity> projectTaskList = this.listByIds(preTaskIdList);
            if (CollectionUtils.isNotEmpty(projectTaskList)) {
                String preTaskNames = projectTaskList.stream().map(ProjectTaskEntity::getName).collect(Collectors.joining(","));
                oldDto.setPreTaskNames(preTaskNames);
            }
        }
        //处理人员字段格式
        List<String> chargeIds = Arrays.stream(oldEntity.getChargeId().split(",")).collect(Collectors.toList());
        oldDto.setChargeIds(chargeIds);
        //处理关联sku名称
        List<ProjectTaskRefSkuEntity> refList = projectTaskRefSkuService.getByTaskId(businessId);
        if (CollectionUtils.isNotEmpty(refList)) {
            List<String> skuIds = refList.stream().map(ProjectTaskRefSkuEntity::getSkuId).collect(Collectors.toList());
            List<ProductDetailEntity> skuList = productDetailService.listByIds(skuIds);
            if (CollectionUtils.isNotEmpty(skuList)) {
                List<String> refSkuNoList = skuList.stream().map(ProductDetailEntity::getSkuNo).collect(Collectors.toList());
                oldDto.setRefSkuNoList(refSkuNoList);
            }
        }
        sysLogService.addSysLogByUpdate(oldDto, projectTaskDTO, SysLogClassPathEnum.PROJECTTASKENTITY.getDesc(), businessId, null, String.format("任务[%s]", oldEntity.getName()));
    }

    /**
     * 编辑任务操作日志
     */
    private void addUpdateTaskDTOLog(ProjectTaskEntity newEntity, ProjectTaskEntity oldEntity, String businessId) {
        sysLogService.addSysLogByUpdate(oldEntity, newEntity, SysLogClassPathEnum.PROJECTTASKENTITY.getDesc(), businessId, null, String.format("任务[%s]", oldEntity.getName()));
    }

    /**
     * @param dto
     * @param taskEntity
     * @description: 更新审核人
     * @author Will
     * @date: 2023/2/28 15:34
     */
    private void setTaskChargeDistributionEntity(ProjectTaskDTO dto, ProjectTaskEntity taskEntity, boolean ifUpdateProcess) {
        List<TaskChargeDistributionEntity> taskChargeDistributionList = new ArrayList<>();
        List<TaskChargeDistributionDTO> approvalList = dto.getApprovalList();
        if (CollectionUtils.isNotEmpty(approvalList)) {
            for (TaskChargeDistributionDTO taskChargeDistributionDTO : approvalList) {
                //保存集合
                List<String> chargeList = taskChargeDistributionDTO.getChargeList();
                //分配值
                String charges = taskChargeDistributionDTO.getCharges();
                //按人员分配
                if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(taskChargeDistributionDTO.getDistributionType())) {
                    taskChargeDistributionDTO.setChargeIds(String.join(",", chargeList));
                    taskChargeDistributionDTO.setCharges(String.join(",", chargeList));
                }
                //按角色分配
                if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(taskChargeDistributionDTO.getDistributionType())) {
                    List<String> chargesList = Arrays.stream(charges.split(",")).collect(Collectors.toList());
                    //删除保存到后台的角色
                    for (String chargeName : chargesList) {
                        if (chargeList.contains(chargeName)) {
                            chargeList.remove(chargeName);
                        }
                    }

                    if (CollectionUtils.isNotEmpty(chargeList)) {
                        taskChargeDistributionDTO.setChargeIds(String.join(",", chargeList));
                    }
                }
                //按上级
                if (DistributionTypeEnum.DISTRIBUTION_SUPERIOR.getCode().equals(taskChargeDistributionDTO.getDistributionType()) && CollectionUtils.isNotEmpty(dto.getChargeIds())) {
                    //查询对应负责人的上级
                    List<String> ids = dto.getChargeIds();
                    //查询上级
                    List<UserSuperiorDTO> userSuperiorDTOS = sysUserFeign.listSuperiorByUserIds(ids);
                    List<String> superiorTypeList = Arrays.stream(taskChargeDistributionDTO.getCharges().split(",")).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(userSuperiorDTOS)) {
                        for (String superiorType : superiorTypeList) {
                            String userIds = userSuperiorDTOS.stream().filter(obj -> obj.getSuperiorType().equals(superiorType)).map(UserSuperiorDTO::getUserId).collect(Collectors.joining(","));
                            if (StringUtils.isNotBlank(userIds)) {
                                taskChargeDistributionDTO.setChargeIds(userIds);
                            }
                        }
                    }
                    //如果有存值则优先取选择值
                    if (CollectionUtils.isNotEmpty(chargeList)) {
                        List<String> superiorNameList = superiorTypeList.stream().map(obj -> ChargeSuperiorEnum.getDesc(obj)).collect(Collectors.toList());
                        //删除保存到后台的上级编码
                        for (String superiorName : superiorNameList) {
                            if (chargeList.contains(superiorName)) {
                                chargeList.remove(superiorName);
                            }
                        }
                        if (CollectionUtils.isNotEmpty(chargeList)) {
                            taskChargeDistributionDTO.setChargeIds(String.join(",", chargeList));
                        }
                    }
                }

                //如果改了 流程就按人员
                if (ifUpdateProcess) {
                    if (CollectionUtils.isEmpty(chargeList)) {
                        throw new ServiceException(ApiError.ERROR_95045);
                    }
                    taskChargeDistributionDTO.setChargeIds(String.join(",", chargeList));
                    taskChargeDistributionDTO.setCharges(String.join(",", chargeList));
                    taskChargeDistributionDTO.setDistributionType(DistributionTypeEnum.DISTRIBUTION_USER.getCode());
                }
            }
            taskChargeDistributionList = BeanMapperUtils.copyList(TaskChargeDistributionEntity.class, dto.getApprovalList());
        }
        //保存交付文档的审核人
        taskChargeDistributionService.removeAndSave(taskEntity.getId(), taskChargeDistributionList, MathUtil.THREE);
    }

    /**
     * 根据任务名称查询任务
     *
     * @param productId productId
     * @param name      name
     * @return com.erp.model.plm.entity.ProjectTaskEntity
     * @Author Luo_WG
     * @Date 2023/3/29 14:08
     **/
    public ProjectTaskEntity getTaskByName(String productId, String name) {
        LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskEntity::getName, name);
        queryWrapper.eq(ProjectTaskEntity::getProductId, productId);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * 批量删除任务
     *
     * @param ids ids
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/3/29 18:00
     **/
    @Transactional
    public Boolean removeBatch(List<String> ids) {
        List<ProjectTaskEntity> entity = this.getByTaskIds(ids);
        if (CollectionUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        LoginUser loginUser = commonService.getUserInfo();

        for (ProjectTaskEntity req : entity) {

            if (BaseStatusEnum.AUDIT_PASS.getStatus().equals(req.getScheduleStatus())) {
                if (!"admin".equals(loginUser.getUserAccount())) {
                    throw new ServiceException(ApiError.ERROR_95137);
                }
            }
            //如果是固定任务
            if (IsConstant.YES.equals(req.getIsFixed()) && !"admin".equals(loginUser.getUserAccount())) {
                throw new ServiceException(ApiError.ERROR_95014);
            }

            //检查是否是子任务
            checkTaskIfExistPid(req.getId());

            Boolean flag = this.removeById(req);
            if (flag) {
                taskChargeDistributionService.removeBySourceAndTaskId(MathUtil.THREE, req.getId());
                taskDeliveryService.removeByTaskId(req.getId());
                taskDocsFinishService.removeByTaskId(req.getId());
                taskRefSkuConfigService.deleteByTaskId(req.getId());
                preTaskService.deleteByTaskId(req.getId());
                //发送删除任务通知
                noticeMessageService.deleteTaskNotice(loginUser.getUserName(), req, req.getProductId());
                //新增操作日志
                SysLogEntity sysLogEntity = new SysLogEntity().setContent(String.format("删除任务[%s]", req.getName()))
                        .setBusinessId(req.getProductId())
                        .setClassPath(SysLogClassPathEnum.PRODUCTINFOENTITY.getDesc());
                //添加日志
                sysLogService.addSysLogByOther(sysLogEntity);
            } else {
                return false;
            }
        }
        return true;
    }
}
