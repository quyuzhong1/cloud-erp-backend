package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.common.business.interceptor.CommonInterceptor;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.workflow.dto.ProcessNodeDTO;
import com.erp.model.workflow.dto.StartProcessDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.enums.BusinessProcessEnum;
import com.erp.server.plm.enums.DistributionTypeEnum;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.enums.TaskTypeEnum;
import com.erp.server.plm.mapper.TemplateTaskMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname TemplateTaskServiceImpl
 * @Description TODO
 * @Date 2022-09-20 15:35
 * @Created by yl
 */
@Service
public class TemplateTaskServiceImpl extends ServiceImpl<TemplateTaskMapper, TemplateTaskEntity> implements TemplateTaskService {

    @Autowired
    private ProjectTaskService taskService;

    @Autowired
    private TemplateDeliveryDocsService templateDeliveryDocsService;

    @Autowired
    private TemplatePreTaskService templatePreTaskService;

    @Autowired
    @Lazy
    private NoticeMessageService noticeMessageService;

    @Autowired
    private TemplatePhaseService templatePhaseService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private BusinessProcessService businessProcessService;

    @Autowired
    private TemplateTaskRefSkuConfigService templateTaskRefSkuConfigService;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private SysUserFeign sysUserFeign;

    /**
     * 保存模板任务
     *
     * @param templateId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-09-20 15:40
     */
    @Override
    @Transactional
    public void saveTemplateTask(String templateId, String productId) {
        List<ProjectTaskEntity> projectTaskList = taskService.getByProductId(productId);
        if (CollectionUtils.isNotEmpty(projectTaskList)) {
            List<TemplateTaskEntity> saveList = new ArrayList<>();
            for (ProjectTaskEntity item : projectTaskList) {
                TemplateTaskEntity entity = new TemplateTaskEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }


    /**
     * 根据模板 获取项目任务
     *
     * @param flagTemplateId
     * @return java.util.List<com.erp.model.plm.entity.TemplateTaskEntity>
     * @author yl
     * @date 2022-09-21 9:57
     */
    @Override
    public List<TemplateTaskEntity> getTaskByTemplateId(String flagTemplateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, flagTemplateId);
        queryWrapper.isNull(TemplateTaskEntity::getQuoteSysTaskId);
        return this.list(queryWrapper);
    }

    /**
     * @param templateId
     * @return List<TemplateTaskEntity>
     * @description: 获取模板下面所有的任务
     * @author Will
     * @date: 2022/11/14 14:35
     */
    @Override
    public List<TemplateTaskEntity> getAllTaskByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        return this.list(queryWrapper);
    }

    /**
     * @param id
     * @param templateId
     * @return Boolean
     * @description: 删除模板任务
     * @author Will
     * @date: 2022/11/14 16:16
     */
    @Override
    @Transactional
    public Boolean removeTask(String id, String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getId, id);
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        List<TemplateTaskEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_95058);
        }
        TemplateTaskEntity entity = list.stream().findFirst().orElse(null);
        Integer IsFixed = entity.getIsFixed();
        //判断是否是子任务
        checkTaskIfExistPid(id, templateId);
        //删除任务交付文档数据
        templateDeliveryDocsService.removeByTaskIdAndTemplateId(id, templateId);
        //删除模板任务
        return this.remove(queryWrapper);
    }

    /**
     * @param templateId
     * @description: 根据模板id删除
     * @author Will
     * @date: 2022/11/14 16:54
     */
    @Override
    public void removeByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        this.remove(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getTaskListByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(TemplateTaskEntity::getId, TemplateTaskEntity::getName);
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        return this.listMaps(queryWrapper);
    }

    @Override
    public TemplateTaskDTO taskDetails(TemplateTaskParamDTO dto) {

        TemplateTaskEntity taskEntity = this.getByIdAndTemplateId(dto.getId(), dto.getTemplateId());
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        TemplateTaskDTO resultDTO = new TemplateTaskDTO();
        BeanMapper.copy(taskEntity, resultDTO);
        //判断任务分配类型
        if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(taskEntity.getDistributionType())) {
            String roleId = taskEntity.getRoleId();
            if (StringUtils.isNotBlank(roleId)) {
                resultDTO.setRoleIds(Arrays.asList(roleId.split(",")));
            }
        } else if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(taskEntity.getDistributionType())){
            String chargeId = taskEntity.getChargeId();
            if (StringUtils.isNotBlank(chargeId)) {
                resultDTO.setChargeIds(Arrays.asList(chargeId.split(",")));
            }
        }
        //判断输出物审核分配类型
        if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(taskEntity.getApprovalDistributionType())) {
            String approvalRoleId = taskEntity.getApprovalRoleId();
            List<List<String>> approvalRoleIdList = new ArrayList<>();
            if (StringUtils.isNotBlank(approvalRoleId)) {
                List<String> roleIdsList = Arrays.asList(approvalRoleId.split("|"));
                for (String roleId: roleIdsList) {
                    List<String> roleIdList = Arrays.asList(roleId.split(","));
                    approvalRoleIdList.add(roleIdList);
                }
            }
            resultDTO.setApprovalRoleIds(approvalRoleIdList);
        } else if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(taskEntity.getApprovalDistributionType())){
            String approvalUserId = taskEntity.getApprovalUserId();
            List<List<String>> approvalUserIdList = new ArrayList<>();
            if (StringUtils.isNotBlank(approvalUserId)) {
                List<String> userIdsList = Arrays.asList(approvalUserId.split("|"));
                for (String userId: userIdsList) {
                    List<String> userIdList = Arrays.asList(userId.split(","));
                    approvalUserIdList.add(userIdList);
                }
            }
            resultDTO.setApprovalUserIds(approvalUserIdList);
        }
        resultDTO.setDeliveryDocsList(templateDeliveryDocsService.getDocsByTaskIdAndTemplateId(dto.getId(), dto.getTemplateId()));
        String businessProcessId = resultDTO.getBusinessProcessId();
        if (StringUtils.isNotBlank(businessProcessId)) {
            BusinessProcessEntity processEntity = businessProcessService.getById(businessProcessId);
            if (processEntity != null) {
                resultDTO.setBusinessName(processEntity.getBusinessName());
            }
        }
        resultDTO.setPreTaskIdList(templatePreTaskService.getTemplatePreTaskIdList(dto.getId(), dto.getTemplateId()));
        TemplateTaskRefSkuConfigEntity skuConfigEntity = templateTaskRefSkuConfigService.getByTaskId(taskEntity.getId());
        if (skuConfigEntity != null) {
            resultDTO.setFieldJson(skuConfigEntity.getFieldJson());
            resultDTO.setFieldConfigType(skuConfigEntity.getFieldConfigType());
        }

        return resultDTO;
    }

    /**
     * 复制模板任务
     *
     * @param templateId
     * @param productId
     * @param projectId
     * @return void
     * @author yl
     * @date 2022-10-28 14:22
     */
    @Override
    public List<CopySourceDTO> copyTemplateTask(String templateId, String productId, String projectId, List<CopySourceDTO> phaseSourceList) {
        List<TemplateTaskEntity> list = this.getByTemplateId(templateId);
        LoginUser loginUser = commonService.getUserInfo();
        //来源信息
        List<CopySourceDTO> sourceList = new ArrayList<>();
        List<ProjectTaskEntity> copyList = new ArrayList<>(list.size());
        if (CollectionUtils.isNotEmpty(list)) {
            for (TemplateTaskEntity item : list) {
                CopySourceDTO source = new CopySourceDTO();
                String taskId = IdWorker.getIdStr();
                ProjectTaskEntity taskEntity = new ProjectTaskEntity();
                BeanMapper.copy(item, taskEntity);
                taskEntity.setProductId(productId);
                taskEntity.setProjectId(projectId);
                taskEntity.setId(taskId);
                String chargeId = item.getChargeId();
                List<String> chargeIdList = new ArrayList<>();
                if (StringUtils.isNotBlank(chargeId)) {
                    chargeIdList = Arrays.asList(chargeId.split(","));
                }
                /**
                 *  如果是立项阶段
                 *  自动完成一步
                 */
                if (TaskConstant.APPROVAL_TASK_NAME.equals(item.getPhaseName())) {
                    taskEntity = automationTask(taskEntity, taskEntity.getType(), chargeIdList, loginUser.getUid());
                }

                source.setNewCreateId(taskId);
                source.setDataId(item.getId());
                CopySourceDTO phase = phaseSourceList.stream().filter(p -> p.getDataId()
                        .equals(item.getPhaseId())).findFirst().orElse(null);
                if (phase != null) {
                    taskEntity.setPhaseId(phase.getNewCreateId());
                } else {
                    taskEntity.setPhaseId("");
                }
                copyList.add(taskEntity);
                sourceList.add(source);
            }
        }

        //更改父id
        for (ProjectTaskEntity task : copyList) {
            //这个pid 还是 模板数据的pid
            String pid = task.getPid();
            if (!pid.equals("0")) {
                CopySourceDTO source = sourceList.stream().
                        filter(s -> s.getDataId().equals(pid)).findFirst().orElse(null);
                if (source != null) {
                    task.setPid(source.getNewCreateId());
                } else {
                    task.setPid("0");
                }
            }
        }

        Boolean flag = taskService.saveBatch(copyList);
        if (flag) {
            //发送新建任务通知
            noticeMessageService.newTaskNotice(loginUser.getUserName(), copyList, productId);

            //找出立项任务 的一般任务集合
            Integer approvalTask = TaskConstant.APPROVAL_TASK;
            //一般任务
            Integer generalTask = TaskTypeEnum.GENERAL_TASK.getCode();
            List<ProjectTaskEntity> projectApprovalTaskList = copyList.stream().filter(p -> p.getProperty().equals(approvalTask)).collect(Collectors.toList());
            //立项任务 发送发布任务通知
            noticeMessageService.releaseTaskNotice(loginUser.getUserName(), projectApprovalTaskList, productId);

        }
        return sourceList;

    }

    /**
     * 立项任务 自动发布
     *
     * @param taskEntity
     * @param taskType
     * @param chargeIdList
     * @param userId
     * @return com.erp.model.plm.entity.ProjectTaskEntity
     * @author yl
     * @date 2022-12-01 10:28
     */
    private ProjectTaskEntity automationTask(ProjectTaskEntity taskEntity, Integer taskType, List<String> chargeIdList, String userId) {
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
                    Date nowDate = new Date();
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

    @Override
    public PagingVO<TemplateTaskShowDTO> paging(PagingDTO<TemplateSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        TemplateSearchDTO params = dto.getParams();
        IPage<TemplateTaskShowDTO> paging = baseMapper.paging(query, params);
        return new PagingVO(paging);
    }

    @Override
    @Transactional
    public Boolean saveOrUpdate(TemplateTaskDTO dto) {
        //验证任务名称是否已存在
        checkTemplateTaskName(dto);
        TemplateTaskEntity entity = new TemplateTaskEntity();
        BeanMapperUtils.copy(dto, entity);
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        Integer type = dto.getType();
        //配置表单属性
        String fieldConfigType = dto.getFieldConfigType();
        Integer generalTask = TaskTypeEnum.GENERAL_TASK.getCode();
        
        //如果配置表单 一般任务 一定要走流程,自定义审核人，存在多级审核及会签，暂时用两层list接收，之后公共审核模块可添加审核人表储存
        if (StringUtils.isNotBlank(fieldConfigType)) {
            //如果是一般任务 必须要有审核流程
            if (generalTask.equals(type)) {
                if (CollectionUtils.isEmpty(dto.getApprovalUserIds()) && CollectionUtils.isEmpty(dto.getApprovalRoleIds())) {
                    throw new ServiceException(ApiError.ERROR_95078);
                }
            }
        }
        //目标交付文档分配类型
        if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(dto.getApprovalDistributionType())) {
            //审核角色逐级审核及会签,逐级任何人用'|'分割，会签审核人用','分割
            List<List<String>> approvalRoleIdList = dto.getApprovalRoleIds();
            if (CollectionUtils.isNotEmpty(approvalRoleIdList)) {
                List<String> roleIdsList = new ArrayList<>();
                for (List<String> list: approvalRoleIdList) {
                    String approvalRoleIds = StringUtils.join(list, ",");
                    roleIdsList.add(approvalRoleIds);
                }
                String join = StringUtils.join(roleIdsList, "|");
                entity.setApprovalRoleId(join);
            }
        } else if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(dto.getApprovalDistributionType())){
            //审核人员逐级审核及会签
            List<List<String>> approvalUserIdList = dto.getApprovalUserIds();
            if (CollectionUtils.isNotEmpty(approvalUserIdList)) {
                List<String> userIdsList = new ArrayList<>();
                for (List<String> userIdList: approvalUserIdList) {
                    String approvalUserIds = StringUtils.join(userIdList, ",");
                    userIdsList.add(approvalUserIds);
                }
                String join = StringUtils.join(userIdsList, "|");
                entity.setApprovalUserId(join);
            }
        }

        String uid = loginUser.getUid();
        String userName = loginUser.getUserName();
        if (StringUtils.isBlank(dto.getId())) {
            entity.setCreateUserId(uid);
            entity.setCreateUserName(userName);
        } else {
            entity.setUpdateUserId(uid);
            entity.setUpdateUserName(userName);
        }

        //任务分配类型处理
        if (MathUtil.ZERO.equals(dto.getDistributionType())) {//分配类型为角色
            List<String> roleIds = dto.getRoleIds();
            List<String> roleNames = sysUserFeign.listRoleByIds(roleIds);
            entity.setRoleId(String.join(",", roleIds));
            if (CollectionUtils.isNotEmpty(roleNames)) {
                entity.setRoleName(String.join(",", roleNames));
            }
        } else if (MathUtil.ONE.equals(dto.getDistributionType())) {//分配类型为负责人
            List<String> chargeIds = dto.getChargeIds();
            String chargeNames = commonService.getNameByIds(chargeIds);
            entity.setChargeId(String.join(",", chargeIds));
            entity.setChargeName(chargeNames);
        }

        //如果是评审任务的话就 清空
        if(!generalTask.equals(type)){
            entity.setApprovalUserId("");
        }
        //阶段名称
        if (StringUtils.isNotBlank(dto.getPhaseId())) {
            TemplatePhaseEntity phaseEntity = templatePhaseService.getByIdAndTemplateId(dto.getPhaseId(), dto.getTemplateId());
            if (ObjectUtils.isEmpty(phaseEntity)) {
                throw new ServiceException(ApiError.ERROR_95041);
            }
            entity.setPhaseName(phaseEntity.getName());
        }
        //交付文档
        List<DocsDTO> deliveryDocsList = dto.getDeliveryDocsList();
        //因为模板任务无主键，则无法用saveOrUpdate进行操作
        if (StringUtils.isBlank(entity.getId())) {
            this.save(entity);
        } else {
            this.updateByIdAndTemplateId(entity);
        }
        //保存交付文档
        templateDeliveryDocsService.saveTemplateDeliveryDocsList(entity.getId(), dto.getTemplateId(), deliveryDocsList);

        //保存模板配置信息
        templateTaskRefSkuConfigService.addTemplateTaskRefSkuConfig(entity.getId(), dto.getTemplateId(), dto.getFieldConfigType(), dto.getFieldJson());
        //保存前置任务
        templatePreTaskService.saveTemplatePreTaskList(entity.getId(), dto.getPreTaskIdList(), dto.getTemplateId());
        return true;
    }


    /**
     * 获取项目任务
     *
     * @param templateId
     * @return java.util.List<com.erp.model.plm.entity.TemplateTaskEntity>
     * @author yl
     * @date 2022-10-29 16:29
     */
    public List<TemplateTaskEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        queryWrapper.eq(TemplateTaskEntity::getProperty, TaskConstant.PROJECT_TASK);
        queryWrapper.orderByAsc(TemplateTaskEntity::getPid);
        return this.list(queryWrapper);
    }

    /**
     * @param id
     * @param templateId
     * @return TemplateTaskEntity
     * @description: 根据id和模板id查询
     * @author Will
     * @date: 2022/11/18 11:42
     */
    public TemplateTaskEntity getByIdAndTemplateId(String id, String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        queryWrapper.eq(TemplateTaskEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    /**
     * @param dto
     * @description: 验证模板任务名称是否已存在
     * @author Will
     * @date: 2022/11/14 14:05
     */
    private void checkTemplateTaskName(TemplateTaskDTO dto) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getName, dto.getName());
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, dto.getTemplateId());
        TemplateTaskEntity entity = this.getOne(queryWrapper);
        if (Objects.nonNull(entity) && !entity.getId().equals(dto.getId())) {
            throw new ServiceException(ApiError.ERROR_95057);
        }
    }

    //检查子任务
    private void checkTaskIfExistPid(String taskId, String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getPid, taskId);
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        Integer count = baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95024);
        }
    }

    /**
     * @param entity
     * @description: 任务编辑时需要更新字段
     * @author Will
     * @date: 2022/11/18 10:38
     */
    private void updateByIdAndTemplateId(TemplateTaskEntity entity) {
        LambdaUpdateWrapper<TemplateTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TemplateTaskEntity::getId, entity.getId());
        updateWrapper.eq(TemplateTaskEntity::getTemplateId, entity.getTemplateId());
        updateWrapper.set(TemplateTaskEntity::getPhaseId, entity.getPhaseId());
        updateWrapper.set(TemplateTaskEntity::getPhaseName, entity.getPhaseName());
        updateWrapper.set(TemplateTaskEntity::getUpdateUserId, entity.getUpdateUserId());
        updateWrapper.set(TemplateTaskEntity::getUpdateUserName, entity.getUpdateUserName());
        updateWrapper.set(TemplateTaskEntity::getName, entity.getName());
        updateWrapper.set(TemplateTaskEntity::getApprovalUserId, entity.getApprovalUserId());
        updateWrapper.set(TemplateTaskEntity::getBusinessProcessId, entity.getBusinessProcessId());
        updateWrapper.set(TemplateTaskEntity::getChargeId, entity.getChargeId());
        updateWrapper.set(TemplateTaskEntity::getChargeName, entity.getChargeName());
        updateWrapper.set(TemplateTaskEntity::getDescription, entity.getDescription());
        updateWrapper.set(TemplateTaskEntity::getIsFixed, entity.getIsFixed());
        updateWrapper.set(TemplateTaskEntity::getPlanEndTime, entity.getPlanEndTime());
        updateWrapper.set(TemplateTaskEntity::getPlanStartTime, entity.getPlanStartTime());
        updateWrapper.set(TemplateTaskEntity::getPriority, entity.getPriority());
        updateWrapper.set(TemplateTaskEntity::getType, entity.getType());
        this.update(updateWrapper);
    }
}
