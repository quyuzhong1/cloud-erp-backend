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
import com.common.web.service.RedisService;
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
import com.erp.server.plm.enums.*;
import com.erp.server.plm.mapper.ProjectTaskMapper;
import com.erp.server.plm.mapper.ProjectTaskRefSkuMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    private TaskDocsNameService taskDocsNameService;


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
    private ProductArchiveService productArchiveService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TaskRefSkuConfigService taskRefSkuConfigService;

    @Autowired
    private ProjectTaskRefSkuService projectTaskRefSkuService;


    @Autowired
    private ProductDetailService productDetailService;


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
    public List<ProjectTaskEntity> addSysTask(String productId, List<TaskDocsNameEntity> taskDocsNameList) {
        // 这是立项任务任务
        List<ProjectTaskSysEntity> sysTaskList = projectTaskSysService.getListByProperty(TaskConstant.APPROVAL_TASK);
        //添加前置任务
        //添加立项阶段
        String taskPhaseId = projectPhaseService.saveTaskPhase(productId, TaskConstant.APPROVAL_TASK_NAME, IsConstant.YES);
        List<ProjectTaskEntity> addTaskList = new ArrayList<>();
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
                    addTaskList.add(entity);
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
            //处理sku 关系
            List<TaskRefSkuConfigEntity> sysTaskRefSkuConfigList = taskRefSkuConfigService.getByTaskIds(sysTaskIds);
            List<TaskRefSkuConfigEntity> copyRefConfigList = new ArrayList<>(sysTaskRefSkuConfigList.size());
            for (TaskRefSkuConfigEntity item : sysTaskRefSkuConfigList) {
                CopySourceDTO source = sourceList.stream().filter(s -> s.getDataId().equals(item.getTaskId()))
                        .findFirst().orElse(null);
                if (source != null) {
                    TaskRefSkuConfigEntity addRefConfig = new TaskRefSkuConfigEntity();
                    addRefConfig.setTaskId(source.getNewCreateId());
                    addRefConfig.setFieldConfigType(item.getFieldConfigType());
                    addRefConfig.setFieldJson(item.getFieldJson());
                    copyRefConfigList.add(addRefConfig);
                }

            }
            taskRefSkuConfigService.saveBatch(copyRefConfigList);

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

        return addTaskList;

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
    public List<ProjectTaskEntity> copyTaskBySys(String saveProductId, String saveProjectId) {
        //从系统拿到 项目任务
        List<ProjectTaskSysEntity> sysTaskList = projectTaskSysService.getListByProperty(TaskConstant.PROJECT_TASK);
        List<ProjectTaskEntity> addTaskList = new ArrayList<>(sysTaskList.size());

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
                    addTaskList.add(entity);
                    taskDeliveryService.saveTaskDeliveryDocs(saveProductId, entity.getId(), item.getId(), docsNameList);
                }
            }

            //处理前置任务
            List<String> sysTaskIds = sysTaskList.stream().map(ProjectTaskSysEntity::getId).collect(Collectors.toList());

            List<TaskRefSkuConfigEntity> sysTaskRefSkuConfigList = taskRefSkuConfigService.getByTaskIds(sysTaskIds);
            List<TaskRefSkuConfigEntity> addTaskRefSkuList = new ArrayList<>(sysTaskRefSkuConfigList.size());
            for (TaskRefSkuConfigEntity item : sysTaskRefSkuConfigList) {
                CopySourceDTO source = sourceList.stream().filter(s -> s.getDataId().equals(item.getTaskId()))
                        .findFirst().orElse(null);
                if (source != null) {
                    TaskRefSkuConfigEntity addEntity = new TaskRefSkuConfigEntity();
                    addEntity.setFieldJson(item.getFieldJson());
                    addEntity.setFieldConfigType(item.getFieldConfigType());
                    addEntity.setTaskId(source.getNewCreateId());
                    addEntity.setProductId(saveProductId);
                    addTaskRefSkuList.add(addEntity);
                }
            }
            taskRefSkuConfigService.saveBatch(addTaskRefSkuList);

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

        return addTaskList;
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
        dto.getParams().setParam(dto.getParam());
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        TaskPagingDTO params = dto.getParams();
        Integer taskFlag = params.getTaskFlag();
        String phaseId = params.getPhaseId();
        String productId = params.getProductId();
        String searchKeyword = params.getSearchKeyword();
        List<Integer> statusList = params.getStatusList();
        List<TaskSearchDTO> searchList = params.getSearchList();
        String param = params.getParam();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = new Page();

        //这个是我完成的任务
        if (TaskConstant.MY_FINISH_TASK.equals(taskFlag)) {
            pageData = baseMapper.paging(query, productId, phaseId, searchList, userId, searchKeyword, statusList, null);
        }
        //这个待我审核的任务
        if (TaskConstant.MY_APPROVAL_TASK.equals(taskFlag)) {
            List<TaskShowDTO> myToDoList = workflowFeign.queryMyToDo(userId);
            statusList.add(TaskStateEnum.APPROVAL_ING.getCode());
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
            pageData = baseMapper.paging(query, productId, phaseId, searchList, null, searchKeyword, statusList, param);
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
            //   List<TaskPagingShowDTO> allList = getAllChildrenList(productId);

            //产品id
            List<String> productIds = list.stream().map(TaskPagingShowDTO::getProductId).collect(Collectors.toList());
            List<ProductInfoEntity> productList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(productIds)) {
                productList = productInfoService.listByIds(productIds);
            }

            for (TaskPagingShowDTO item : list) {
                String taskId = item.getId();
                Integer state = item.getStatus();
                item.setStatusName(TaskStateEnum.getName(state));
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
        LoginUser loginUser = commonService.getUserInfo();
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

            taskRefSkuConfigService.deleteByTaskId(taskId);

            //发送删除任务通知
            noticeMessageService.deleteTaskNotice(loginUser.getUserName(), entity, entity.getProductId());
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
                    BusinessProcessEnum.DOCS_CHANGE.getBusinessKey().equals(processEntity.getBusinessType())) {
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
        TaskRefSkuConfigEntity refSku = taskRefSkuConfigService.getByTaskId(taskId);
        List<ProjectTaskRefSkuEntity> taskRefSkuList = projectTaskRefSkuService.getByTaskId(taskId);
        List<String> skuIdList = taskRefSkuList.stream().map(ProjectTaskRefSkuEntity::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = productDetailService.getByIdList(skuIdList);
        if (refSku != null) {
            detailsDTO.setFieldJson(refSku.getFieldJson());
            detailsDTO.setFieldConfigType(refSku.getFieldConfigType());
        }
        detailsDTO.setRefSkuIdList(skuIdList);
        List<String> skuNoList = productDetailList.stream().filter(d -> skuIdList.contains(d.getId())).map(ProductDetailEntity::getSkuNo).collect(Collectors.toList());
        detailsDTO.setRefSkuNoList(skuNoList);
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
        LoginUser loginUser = commonService.getUserInfo();
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

        List<String> updateField = getUpdateField(dto);
        if (updateField.size() > 0) {
            //新增产品操作日志
            ProductOperateRecordDTO productOperateRecordDTO = new ProductOperateRecordDTO();
            productOperateRecordDTO.setProductId(dto.getProductId());
            productOperateRecordDTO.setRemark(JSONObject.toJSONString(updateField));
            productOperateRecordService.saveOrUpdate(productOperateRecordDTO);
        }

        boolean flag = this.updateById(taskEntity);
        if (flag) {
            //保存交付文档
            taskDeliveryService.saveDeliveryDocs(taskEntity.getId(), dto.getProductId(), deliveryDocsList);
            //保存前置任务
            preTaskService.savePreTask(taskEntity.getId(), dto.getPreTaskIdList(), dto.getProductId());

            //保存SKU配置 字段 关系表
            taskRefSkuConfigService.addSkuField(taskEntity.getId(), taskEntity.getProductId(), dto.getFieldConfigType(), dto.getFieldJson());
            //保存任务与SKU 关系表
            projectTaskRefSkuService.addTaskSkuRef(taskEntity.getId(), taskEntity.getProductId(), dto.getRefSkuIdList());

            noticeMessageService.editTaskNotice(loginUser.getUserName(), taskEntity, taskEntity.getProductId());
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
        String updateJson = JSONObject.toJSONString(dto);
        Map<String, Object> updateMap = JSONObject.parseObject(updateJson, Map.class);
        ProjectTaskEntity taskEntity = this.getById(dto.getTaskId());
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        LoginUser loginUser = commonService.getUserInfo();
        //任务名
        String name = dto.getName();
        if (updateMap.containsKey("planStartTime")) {
            Date planStartTime = dto.getPlanStartTime();
            taskEntity.setPlanStartTime(planStartTime);
        }
        if (updateMap.containsKey("planEndTime")) {
            //结束时间
            Date planEndTime = dto.getPlanEndTime();
            taskEntity.setPlanEndTime(planEndTime);
        }


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
        noticeMessageService.editTaskNotice(loginUser.getUserName(), taskEntity, taskEntity.getProductId());
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

        TaskRefSkuConfigEntity refSku = taskRefSkuConfigService.getByTaskId(taskId);
        List<ProjectTaskRefSkuEntity> taskRefSkuList = projectTaskRefSkuService.getByTaskId(taskId);
        List<String> skuIdList = taskRefSkuList.stream().map(ProjectTaskRefSkuEntity::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = productDetailService.getByIdList(skuIdList);
        if (refSku != null) {
            resultDTO.setFieldJson(refSku.getFieldJson());
            resultDTO.setFieldConfigType(refSku.getFieldConfigType());
        }
        resultDTO.setRefSkuIdList(skuIdList);
        List<String> skuNoList = productDetailList.stream().filter(d -> skuIdList.contains(d.getId())).map(ProductDetailEntity::getSkuNo).collect(Collectors.toList());
        resultDTO.setRefSkuNoList(skuNoList);


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
     * 方法说明
     *
     * @param dto
     * @return java.util.List<com.erp.model.plm.dto.TaskGroupResultDTO>
     * @author yl
     * @date 2022-11-08 11:24
     */
    @Override
    public List<TaskGroupResultDTO> getGroupCondition(TaskGroupParamDTO dto) {
        String taskProperty = dto.getTaskProperty();
        String userId = commonService.getUserInfo().getUid();
        //任务条件 1 待完成  2 全部
        Integer taskCondition = dto.getTaskCondition();
        //product 产品  planEndTime 计划结束时间
        String groupName = dto.getGroupName();
        switch (taskProperty) {
            //分配给我  任务负责人=当前账号人的待完成/审核任务
            case TaskConstant.ASSIGN_TO_ME:
                return toMeTaskGroupResult(userId, taskCondition, groupName);
            //我创建的  创建人=当前账号人的待完成/审核任务
            case TaskConstant.MY_CREATE:
                return myCreateGroupResult(userId, taskCondition, groupName);
            //全部任务
            case TaskConstant.ALL:
                return allGroupResult(taskCondition, groupName);
            default:
                return new ArrayList<>();
        }


    }

    @Override
    public PagingVO<List<TaskPagingShowDTO>> expertPaging(PagingDTO<TaskSearchParamDTO> searchParamDTO) {
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        TaskSearchParamDTO params = searchParamDTO.getParams();
        Page query = new Page(searchParamDTO.getCurrPage(), searchParamDTO.getPageSize());
        //"assignToMe", "myCreate", "all"
        String taskProperty = TaskConstant.ASSIGN_TO_ME;
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
            pageData = baseMapper.allProductTaskList(query, notStateList, params);
        } else {
            //根据产品分组
            if (ifProductGroup) {
                //这个就是产品的id
                pageData = baseMapper.allProductTaskList(query, notStateList, params);
            } else {
                //标示是是计划时间
                String groupFlag = params.getGroupFlag();
                //获取到时间
                Map<String, Date> planTimeMap = getPlanEndTime(groupFlag);
                //计划时间
                pageData = baseMapper.allPlanTimeTaskList(query, notStateList, params, planTimeMap.get("startTime"), planTimeMap.get("endTime"));
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
                TaskShowDTO workflowTask = workflowList.stream().filter(w -> w.getProcessInstanceId().equals(item.getProcessId())).findFirst().orElse(null);
                if (workflowTask != null) {
                    item.setProcessTaskId(workflowTask.getTaskId());
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
        searchParamDTO.getParams().setParam(searchParamDTO.getParam());
        LoginUser loginUser = commonService.getUserInfo();
        String userId = loginUser.getUid();
        TaskSearchParamDTO params = searchParamDTO.getParams();
        Page query = new Page(searchParamDTO.getCurrPage(), searchParamDTO.getPageSize());
        //"assignToMe", "myCreate", "all"
        String taskProperty = TaskConstant.ASSIGN_TO_ME;
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
            pageData = baseMapper.toMeProductTaskList(query, userId, notStateList, params);
        } else {
            //根据产品分组
            if (ifProductGroup) {
                //这个就是产品的id
                pageData = baseMapper.toMeProductTaskList(query, userId, notStateList, params);
            } else {
                //标示是是计划时间
                String groupFlag = params.getGroupFlag();
                //获取到时间
                Map<String, Date> planTimeMap = getPlanEndTime(groupFlag);
                //计划时间
                pageData = baseMapper.toMePlanEndTimeTaskList(query, userId, notStateList, params, planTimeMap.get("startTime"), planTimeMap.get("endTime"));
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
                TaskShowDTO workflowTask = workflowList.stream().filter(w -> w.getProcessInstanceId().equals(item.getProcessId())).findFirst().orElse(null);
                if (workflowTask != null) {
                    item.setProcessTaskId(workflowTask.getTaskId());
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

            }
        }

        return new PagingVO(pageData);
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
        searchParamDTO.getParams().setParam(searchParamDTO.getParam());
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
            pageData = baseMapper.myCreateProductTaskList(query, userId, notStateList, params);
        } else {
            //根据产品分组
            if (ifProductGroup) {
                //这个就是产品的id
                pageData = baseMapper.myCreateProductTaskList(query, userId, notStateList, params);
            } else {
                //标示是是计划时间
                String groupFlag = params.getGroupFlag();
                //获取到时间
                Map<String, Date> planTimeMap = getPlanEndTime(groupFlag);
                //计划时间
                pageData = baseMapper.myCreatePlanEndTimeTaskList(query, userId, notStateList, params, planTimeMap.get("startTime"), planTimeMap.get("endTime"));
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
                TaskShowDTO workflowTask = workflowList.stream().filter(w -> w.getProcessInstanceId().equals(item.getProcessId())).findFirst().orElse(null);
                if (workflowTask != null) {
                    item.setProcessTaskId(workflowTask.getTaskId());
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

            }
        }

        return new PagingVO(pageData);
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
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        Integer taskState = taskEntity.getStatus();
        //编辑任务
        Map<String, Object> editTaskMap = new HashMap<>();
        editTaskMap.put("name", "编辑任务");
        editTaskMap.put("flag", "editTask");
        editTaskMap.put("isShow", true);
        resultList.add(editTaskMap);
        //创建子任务
        Map<String, Object> createChildTaskMap = new HashMap<>();
        createChildTaskMap.put("name", "创建子任务");
        createChildTaskMap.put("flag", "createChildTask");
        createChildTaskMap.put("isShow", true);
        resultList.add(createChildTaskMap);
        List<String> taskIds = Arrays.asList(taskId);
        //获取总的任务文档数
        List<CountDTO> taskDocsCounts = taskDeliveryService.getTaskDocsCount(taskIds);
        List<TaskDocsFinishEntity> taskDocsList = finishService.getByTaskIds(taskIds);
        Map<String, Object> uploadMap = new HashMap<>();
        uploadMap.put("name", "上传文件");
        uploadMap.put("flag", "uploadFile");
        Boolean uploadFileFlag = true;
        int totalCount = 0;
        if (CollectionUtils.isNotEmpty(taskDocsCounts)) {
            totalCount = taskDocsCounts.get(0).getCount();
        }
        if (totalCount == taskDocsList.size()) {
            uploadFileFlag = false;
        }
        uploadMap.put("isShow", uploadFileFlag);
        resultList.add(uploadMap);

        boolean deleteTaskShow = true;
        Integer IsFixed = taskEntity.getIsFixed();
        //如果是固定任务
        if (IsConstant.YES.equals(IsFixed)) {
            deleteTaskShow = false;
        }
        //删除任务
        Map<String, Object> deleteTaskMap = new HashMap<>();
        deleteTaskMap.put("name", "删除任务");
        deleteTaskMap.put("flag", "deleteTask");
        deleteTaskMap.put("isShow", deleteTaskShow);
        resultList.add(deleteTaskMap);


        //变更文档
        //只有任务完成了或者审核不通过才能变更流程
        Integer finishCode = TaskStateEnum.FINISH.getCode();
        Integer approvalNoPassCode = TaskStateEnum.APPROVAL_NO_PASS.getCode();
        Boolean changeDocsShow = true;
        if (!finishCode.equals(taskState)
                && !approvalNoPassCode.equals(taskState)) {
            changeDocsShow = false;
        }

        //如果没有上传文档也不显示

        if (CollectionUtils.isEmpty(taskDocsList)) {
            changeDocsShow = false;
        }
        String processId = taskEntity.getProcessId();
        if (StringUtils.isBlank(processId)) {
            changeDocsShow = false;
        }

        Map<String, Object> changeDocsMap = new HashMap<>();
        changeDocsMap.put("name", "变更文档");
        changeDocsMap.put("flag", "changeDocs");
        changeDocsMap.put("isShow", changeDocsShow);
        resultList.add(changeDocsMap);


        return resultList;
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
     * 所有任务 任务列表 分组数据
     *
     * @param taskCondition
     * @param groupName
     * @return java.util.List<com.erp.model.plm.dto.TaskGroupResultDTO>
     * @author yl
     * @date 2022-11-08 17:12
     */
    private List<TaskGroupResultDTO> allGroupResult(Integer taskCondition, String groupName) {
        //不在的 任务状态
        List<Integer> notStateList = new ArrayList<>();
        //这个是待处理 状态为-未开始，进行中，待审核，审核中，完成待审核，审核不通过
        if (TaskConstant.WAIT_HANDLE.equals(taskCondition)) {
            notStateList.add(TaskStateEnum.CLOSE.getCode());
            notStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            notStateList.add(TaskStateEnum.FINISH.getCode());
            notStateList.add(TaskStateEnum.APPROVAL_PASS.getCode());
        }
        List<TaskGroupResultDTO> resultList = new ArrayList<>();

        //当是产品的时候
        if (TaskConstant.PRODUCT.equals(groupName)) {
            List<TaskGroupResultDTO> list = baseMapper.allTaskGroup(notStateList);
            resultList = getProductGroup(list);
        }
        //以计划结束时间
        if (TaskConstant.PLAN_END_TIME.equals(groupName)) {
            List<TaskGroupResultDTO> list = baseMapper.taskPlanEndTimeGroup(notStateList);
            resultList = getPlanEndTimeGroup(list);
        }
        return resultList;
    }


    /**
     * 我创建的分组
     *
     * @param userId
     * @param taskCondition
     * @param groupName
     * @return java.util.List<com.erp.model.plm.dto.TaskGroupResultDTO>
     * @author yl
     * @date 2022-11-08 17:03
     */
    private List<TaskGroupResultDTO> myCreateGroupResult(String userId, Integer taskCondition, String groupName) {
        //不在的 任务状态
        List<Integer> notStateList = new ArrayList<>();
        //这个是待处理 状态为-未开始，进行中，待审核，审核中，完成待审核，审核不通过
        if (TaskConstant.WAIT_HANDLE.equals(taskCondition)) {
            notStateList.add(TaskStateEnum.CLOSE.getCode());
            notStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            notStateList.add(TaskStateEnum.FINISH.getCode());
            notStateList.add(TaskStateEnum.APPROVAL_PASS.getCode());
        }
        List<TaskGroupResultDTO> resultList = new ArrayList<>();
        //当是产品的时候
        if (TaskConstant.PRODUCT.equals(groupName)) {
            List<TaskGroupResultDTO> list = baseMapper.myCreateTaskGroup(userId, notStateList);
            resultList = getProductGroup(list);
        }
        //以计划结束时间
        if (TaskConstant.PLAN_END_TIME.equals(groupName)) {
            List<TaskGroupResultDTO> list = baseMapper.myCreateTaskPlanEndTimeGroup(userId, notStateList);
            resultList = getPlanEndTimeGroup(list);
        }

        return resultList;
    }

    /**
     * 任务列表 分配给我 获取分组列表数据
     *
     * @param taskCondition 任务条件 1 待完成  2 全部
     * @param groupName
     * @return java.util.List<com.erp.model.plm.dto.TaskGroupResultDTO>
     * @author yl
     * @date 2022-11-08 12:26
     */
    public List<TaskGroupResultDTO> toMeTaskGroupResult(String userId, Integer taskCondition, String groupName) {
        //不在的 任务状态
        List<Integer> notStateList = new ArrayList<>();
        //这个是待处理 状态为-未开始，进行中，待审核，审核中，完成待审核，审核不通过
        if (TaskConstant.WAIT_HANDLE.equals(taskCondition)) {
            notStateList.add(TaskStateEnum.CLOSE.getCode());
            notStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
            notStateList.add(TaskStateEnum.FINISH.getCode());
            notStateList.add(TaskStateEnum.APPROVAL_PASS.getCode());
        }
        //状态包含所有状态-除了待发布
        if (TaskConstant.ALL_TASK.equals(taskCondition)) {
            notStateList.add(TaskStateEnum.TO_BE_RELEASED.getCode());
        }
        List<TaskGroupResultDTO> resultList = new ArrayList<>();
        //当是产品的时候
        if (TaskConstant.PRODUCT.equals(groupName)) {
            List<TaskGroupResultDTO> list = baseMapper.toMeTaskGroup(userId, notStateList);
            resultList = getProductGroup(list);
        }
        //以计划结束时间
        if (TaskConstant.PLAN_END_TIME.equals(groupName)) {
            List<TaskGroupResultDTO> list = baseMapper.toMeTaskPlanEndTimeGroup(userId, notStateList);
            resultList = getPlanEndTimeGroup(list);
        }
        return resultList;
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
                Date nowDate = new Date();
                Integer waitConfirmCode = TaskStateEnum.WAIT_CONFIRM.getCode();
                List<ProjectTaskEntity> noticeList = new ArrayList<>();
                for (ProjectTaskEntity review : reviewList) {
                    String chargeId = review.getChargeId();
                    if (StringUtils.isNotBlank(chargeId)) {
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

        //待审核
        Integer WaitConfirmCode = TaskStateEnum.WAIT_CONFIRM.getCode();
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
        //发送完成任务通知
        noticeMessageService.finishTaskNotice(loginUser.getUserName(), noProcessList, dto.getProductId());

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
    private Map<String, Object> getProcessParameter(BusinessProcessEntity processEntity, List<String> approvalUserIds) {
        String param = processEntity.getParam();
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(param)) {
            String[] paramList = param.split(",");
            String businessKey = processEntity.getBusinessKey();
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
                map.put(paramList[0], approvalUserIds);
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
        //审核中
        Integer approvalIngCode = TaskStateEnum.APPROVAL_ING.getCode();
        // 只有待审核 和 完成待审核 的状态 才可以审核通过
        if (!waitConfirmCode.equals(state) &&
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
                CompletableFuture completableFuture = CompletableFuture.supplyAsync(() -> {
                    return workflowFeign.taskPass(approveProcess);
                });

            }
        }
        noticeMessageService.approvalTaskNotice(loginUser.getUserName(), list, dto.getProductId());

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

        List<String> taskIdList = list.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        boolean flag = this.updateTaskState(taskIdList, TaskStateEnum.APPROVAL_NO_PASS.getCode(), null, null);
        if (flag) {
            taskOperatorRecordService.batchSaveRecord(taskIdList, approvalIngCode, TaskStateEnum.APPROVAL_NO_PASS.getCode(), loginUser.getUid(), loginUser.getUserName(), "");
        }

        List<TaskCommentEntity> taskCommentList = new ArrayList<>(taskIds.size());
        for (String taskId : taskIds) {
            //添加评论
            TaskCommentEntity comment = new TaskCommentEntity();
            comment.setComment("[审核结果-审核不通过]" + dto.getComment());
            comment.setTaskId(taskId);
            comment.setCreateUserName(loginUser.getUserName());
            comment.setCreateUserId(loginUser.getUid());
            taskCommentList.add(comment);
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
            recordEntity.setAfterState(TaskStateEnum.FINISH.getCode());
            recordEntity.setOperatorId(loginUser.getUid());
            recordEntity.setOperatorName(loginUser.getUserName());
            taskEntity.setRealityEndTime(new Date());
            taskEntity.setStatus(TaskStateEnum.FINISH.getCode());

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
            //添加待审核
            resultList.add(getProcessNode(waitConfirmEntity, waitConfirmState));
            //添加审核中
            resultList.add(getProcessNode(approvalIngEntity, approvalIngState));

            if (approvalNoPassFlag) {//添加审核不通过
                resultList.add(getProcessNode(approvalNoPassEntity, approvalNoPassState));
            } else {
                //添加审核通过
                resultList.add(getProcessNode(finishStateEntity, finishState));
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
                resultList.add(getProcessNode(finishStateEntity, finishState));
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

    /**
     * @param productId
     * @param dateDTO
     * @return ProductMilepostDateDTO
     * @description: 创建里程碑结束时间
     * @author Will
     * @date: 2022/11/18 18:37
     */
    private ProductMilepostDateDTO getStartMilepostDate(String productId, ProductMilepostDateDTO dateDTO) {
        ProductInfoEntity productInfoEntity = productInfoService.getById(productId);
        dateDTO.setRealityEndTime(productInfoEntity.getCreateTime());
        return dateDTO;

    }

    ;

    /**
     * @param productId
     * @param dateDTO
     * @return ProductMilepostDateDTO
     * @description: 立项里程碑结束时间
     * @author Will
     * @date: 2022/11/18 18:37
     */
    private ProductMilepostDateDTO getApprovalMilepostDate(String productId, ProductMilepostDateDTO dateDTO) {
        ProductInfoEntity productInfoEntity = productInfoService.getById(productId);
        dateDTO.setRealityEndTime(productInfoEntity.getApprovalTime());
        return dateDTO;
    }

    ;

    /**
     * @param taskId
     * @param dateDTO
     * @return ProductMilepostDateDTO
     * @description: 任务里程碑结束时间
     * @author Will
     * @date: 2022/11/18 18:37
     */
    private ProductMilepostDateDTO getTaskMilepostDate(String taskId, ProductMilepostDateDTO dateDTO) {
        ProjectTaskEntity projectTaskEntity = this.getById(taskId);
        dateDTO.setPlanEndTime(projectTaskEntity.getPlanEndTime());
        dateDTO.setRealityEndTime(projectTaskEntity.getRealityEndTime());
        return dateDTO;
    }

    ;

    /**
     * @param productId
     * @param dateDTO
     * @return ProductMilepostDateDTO
     * @description: 归档里程碑结束时间
     * @author Will
     * @date: 2022/11/18 18:37
     */
    private ProductMilepostDateDTO getArchiveMilepostDate(String productId, ProductMilepostDateDTO dateDTO) {
        ProductArchiveEntity productArchiveEntity = productArchiveService.getArchiveByProductId(productId);
        dateDTO.setRealityEndTime(productArchiveEntity.getCreateTime());
        return dateDTO;
    }

    ;

}
