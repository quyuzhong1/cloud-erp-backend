package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.ChargeSuperiorEnum;
import com.erp.model.plm.enums.DistributionTypeEnum;
import com.erp.model.plm.enums.RelatedSkuTypeEnum;
import com.erp.model.plm.enums.TaskTypeEnum;
import com.erp.model.plm.vo.PreTaskVO;
import com.erp.model.plm.vo.TemplateTaskVO;
import com.erp.model.sys.dto.UserSuperiorDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.mapper.TemplateTaskMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private TemplateRoleService templateRoleService;

    @Autowired
    private TaskChargeDistributionService taskChargeDistributionService;

    @Autowired
    private TemplateMembersService templateMembersService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private ProjectTemplateService projectTemplateService;


    /**
     * 产品保存模板 保存任务
     *
     * @param templateId
     * @param productId
     * @param phaseSourceList 阶段的源数据
     * @return
     */
    @Override
    @Transactional
    public List<CopySourceDTO> saveTemplateTask(String templateId, String productId,List<CopySourceDTO>  phaseSourceList) {
        List<ProjectTaskEntity> projectTaskList = taskService.getByProductId(productId);
        List<CopySourceDTO> copySourceList = new ArrayList<>(20);
        if (CollectionUtils.isNotEmpty(projectTaskList)) {
            List<TemplateTaskEntity> saveList = new ArrayList<>();
            for (ProjectTaskEntity item : projectTaskList) {
                CopySourceDTO source = new CopySourceDTO();
                TemplateTaskEntity entity = new TemplateTaskEntity();
                String newCreateId = IdWorker.getIdStr();
                source.setNewCreateId(newCreateId);
                source.setDataId(item.getId());
                BeanMapper.copy(item, entity);
                entity.setId(newCreateId);
                entity.setTemplateId(templateId);
                entity.setSourceTaskId(item.getId());
                //新的阶段id
                String newPhaseId=phaseSourceList.stream().filter(p->p.getDataId().
                        equals(item.getPhaseId())).findFirst().flatMap(obj->Optional.ofNullable(obj.getNewCreateId())).orElse("");
                entity.setPhaseId(newPhaseId);
                copySourceList.add(source);
                if (ObjectUtils.isEmpty(item.getDistributionType())) {
                    entity.setDistributionType(DistributionTypeEnum.DISTRIBUTION_USER.getCode());
                }
                //如果分配方式为角色时角色为空则自动转为人员分配
                if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(item.getDistributionType()) && StringUtils.isBlank(item.getRoleName())) {
                    entity.setDistributionType(DistributionTypeEnum.DISTRIBUTION_USER.getCode());
                }
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
        return copySourceList;
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
        queryWrapper.isNull(TemplateTaskEntity::getSourceTaskId);
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
        //删除任务审核人
        taskChargeDistributionService.removeBySourceAndTaskId(MathUtil.TWO, id);
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
    public TemplateTaskVO taskDetails(TemplateTaskParamDTO dto) {

        TemplateTaskEntity taskEntity = this.getByIdAndTemplateId(dto.getId(), dto.getTemplateId());
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        TemplateTaskVO resultVO = new TemplateTaskVO();
        BeanMapper.copy(taskEntity, resultVO);
        //判断任务分配类型
        if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(taskEntity.getDistributionType())) {
            String roleId = taskEntity.getRoleId();
            if (StringUtils.isNotBlank(roleId)) {
                resultVO.setRoleIds(Arrays.asList(roleId.split(",")));
            }
        } else if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(taskEntity.getDistributionType())) {
            String chargeId = taskEntity.getChargeId();
            if (StringUtils.isNotBlank(chargeId)) {
                resultVO.setChargeIds(Arrays.asList(chargeId.split(",")));
            }
        }
        //查询模板任务下审核人
        List<TaskChargeDistributionEntity> taskChargeDistributionList = taskChargeDistributionService.listBySourceAndTaskId(MathUtil.TWO, dto.getId());
        if (CollectionUtils.isNotEmpty(taskChargeDistributionList)) {
            List<TaskChargeDistributionDTO> list = BeanMapperUtils.copyList(TaskChargeDistributionDTO.class, taskChargeDistributionList);
            list.forEach(obj -> {
                List<String> collect = Arrays.stream(obj.getCharges().split(",")).collect(Collectors.toList());
                obj.setChargeList(collect);
                //回显名称
                if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(obj.getDistributionType())) {
                    //用户分配查询名称
                    List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(collect);
                    if (CollectionUtils.isNotEmpty(userList)) {
                        List<String> usrNameList = userList.stream().map(FindUserDTO::getUserName).collect(Collectors.toList());
                        obj.setChargeNames(String.join(",", usrNameList));
                    }
                }
                if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(obj.getDistributionType())) {
                    //角色分配直接取名称
                    obj.setChargeNames(obj.getCharges());
                }
                if (DistributionTypeEnum.DISTRIBUTION_SUPERIOR.getCode().equals(obj.getDistributionType())) {
                    //上级分配取枚举
                    List<String> superiors = collect.stream().map(e -> ChargeSuperiorEnum.getDesc(e)).collect(Collectors.toList());
                    obj.setChargeNames(String.join(",", superiors));
                }

            });
            resultVO.setApprovalList(list);
        }
        resultVO.setDeliveryDocsList(templateDeliveryDocsService.getDocsByTaskIdAndTemplateId(dto.getId(), dto.getTemplateId()));
        String businessProcessId = resultVO.getBusinessProcessId();
        if (StringUtils.isNotBlank(businessProcessId)) {
            BusinessProcessEntity processEntity = businessProcessService.getById(businessProcessId);
            if (processEntity != null) {
                resultVO.setBusinessName(processEntity.getBusinessName());
            }
        }
        List<PreTaskVO> preTaskList = templatePreTaskService.getTemplatePreTaskIdList(dto.getId(), dto.getTemplateId());
        List<String> pretaskIdList = Collections.emptyList();
        if (CollectionUtil.isNotEmpty(preTaskList)) {
            pretaskIdList = preTaskList.stream().map(PreTaskVO::getPreTaskId).collect(Collectors.toList());
        }
        resultVO.setPreTaskIdList(pretaskIdList);
        TemplateTaskRefSkuConfigEntity skuConfigEntity = templateTaskRefSkuConfigService.getByTaskId(taskEntity.getId());
        if (skuConfigEntity != null) {
            resultVO.setFieldJson(skuConfigEntity.getFieldJson());
            resultVO.setFieldConfigType(skuConfigEntity.getFieldConfigType());
        }

        return resultVO;
    }

    @Override
    public List<TemplateTaskEntity> listByRoleId(String roleId) {
        return this.baseMapper.listByRoleId(roleId);
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
        // 查询模板
        ProjectTemplateEntity projectTemplateEntity = projectTemplateService.getById(templateId);
        if (ObjectUtils.isEmpty(projectTemplateEntity) || !MathUtil.ONE.equals(projectTemplateEntity.getStatus())) {
            return new ArrayList<>();
        }
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

                source.setNewCreateId(taskId);
                source.setDataId(item.getId());
                CopySourceDTO phase = phaseSourceList.stream().filter(p -> p.getDataId()
                        .equals(item.getPhaseId())).findFirst().orElse(null);
                if (phase != null) {
                    taskEntity.setPhaseId(phase.getNewCreateId());
                } else {
                    taskEntity.setPhaseId("");
                }
                if (ObjectUtils.isNotEmpty(projectTemplateEntity)) {
                    //判断负责人分配方式是否是角色
                    if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(taskEntity.getDistributionType())) {
                        List<String> roleIds = Arrays.stream(item.getRoleId().split(",")).collect(Collectors.toList());
                        List<TemplateMembersEntity> templateMembersList = templateMembersService.listByRoleIds(roleIds, projectTemplateEntity.getId());
                        if (CollectionUtils.isNotEmpty(templateMembersList)) {
                            List<String> memberIds = templateMembersList.stream().map(TemplateMembersEntity::getMemberId).distinct().collect(Collectors.toList());
                            List<String> memberNames = templateMembersList.stream().map(TemplateMembersEntity::getMemberName).distinct().collect(Collectors.toList());
                            taskEntity.setChargeId(StringUtils.join(memberIds, ","));
                            taskEntity.setChargeName(StringUtils.join(memberNames, ","));
                        }
                    }
                }
                copyList.add(taskEntity);
                sourceList.add(source);
                List<String> chargeIds = new ArrayList<>();
                if (StringUtils.isNotBlank(taskEntity.getChargeId())) {
                    chargeIds = Arrays.stream(taskEntity.getChargeId().split(",")).collect(Collectors.toList());
                }
                //查询模板任务下审核人
                List<TaskChargeDistributionEntity> taskChargeDistributionList = taskChargeDistributionService.listBySourceAndTaskId(MathUtil.TWO, item.getId());
                setTaskChargeDistribution(taskChargeDistributionList, chargeIds, projectTemplateEntity.getId(), taskEntity.getId(), MathUtil.THREE);
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

        }
        return sourceList;

    }

    @Override
    public PagingVO<TemplateTaskShowDTO> paging(PagingDTO<TemplateSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        TemplateSearchDTO params = dto.getParams();
        IPage<TemplateTaskShowDTO> paging = baseMapper.paging(query, params);
        return new PagingVO(paging);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdate(TemplateTaskDTO dto) {
        //验证任务名称是否已存在
        checkTemplateTaskName(dto);
        TemplateTaskEntity entity = new TemplateTaskEntity();
        BeanMapperUtils.copy(dto, entity);
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }

        //配置表单属性
        String fieldConfigType = dto.getFieldConfigType();
        //生成sku
        String createSku = TaskConstant.CREATE_SKU;
        //填写sku
        String fillProductInfo = TaskConstant.FILL_PRODUCT_INFO;
        //配置表单属性
        String fieldJson = dto.getFieldJson();
        //第一种 sku不等于空并且大于0  并且  表单属性不为空且为填写
        Boolean needCheckFirst = (StringUtils.isNotBlank(fieldConfigType) && fillProductInfo.equals(fieldConfigType) && StringUtils.isNotBlank(fieldJson));

        //第二种 sku 没有  并且 表单属性不为空 且为生成
        Boolean needCheckSecond = (StringUtils.isNotBlank(fieldConfigType) && (createSku.equals(fieldConfigType) || (StringUtils.isNotBlank(dto.getFieldJson()) && RelatedSkuTypeEnum.ALL_RELATED.getCode().equals(dto.getRelatedSkuType()))));
        Integer type = dto.getType();
        Integer generalTask = TaskTypeEnum.GENERAL_TASK.getCode();
        List<TaskChargeDistributionDTO> approvalList = dto.getApprovalList();
        //如果配置表单 一般任务 一定要走流程,自定义审核人，存在多级审核及会签，暂时用两层list接收，之后公共审核模块可添加审核人表储存
        if (needCheckFirst || needCheckSecond) {
            if (generalTask.equals(type)) {
                if (CollectionUtils.isEmpty(approvalList)) {
                    throw new ServiceException(ApiError.ERROR_95078);
                }
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
        List<String> chargeIds = dto.getChargeIds();
        List<String> roleIds = dto.getRoleIds();
        //任务分配类型处理
        if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(dto.getDistributionType())) {
            //分配类型为角色
            List<TemplateRoleEntity> templateRoleList = templateRoleService.listByIds(roleIds);
            entity.setRoleId(String.join(",", roleIds));
            if (CollectionUtils.isNotEmpty(templateRoleList)) {
                List<String> roleNames = templateRoleList.stream().map(TemplateRoleEntity::getName).collect(Collectors.toList());
                entity.setRoleName(String.join(",", roleNames));
                entity.setChargeId("");
                entity.setChargeName("");
            }
        } else if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(dto.getDistributionType())) {
            //分配类型为负责人
            String chargeNames = commonService.getNameByIds(chargeIds);
            entity.setChargeId(String.join(",", chargeIds));
            entity.setChargeName(chargeNames);
            entity.setRoleName("");
            entity.setRoleId("");
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
        if (null != dto.getWorkPeriod() && 0 < dto.getWorkPeriod()) {
            entity.setWorkPeriod(dto.getWorkPeriod());
        }
        if (StringUtils.isBlank(entity.getId())) {
            this.save(entity);
        } else {
            this.updateByIdAndTemplateId(entity);
        }
        //保存审核人信息
        if (CollectionUtils.isNotEmpty(approvalList)) {
            approvalList.forEach(obj -> obj.setCharges(String.join(",", obj.getChargeList())));
            List<TaskChargeDistributionEntity> taskChargeDistributionList = BeanMapperUtils.copyList(TaskChargeDistributionEntity.class, approvalList);
            setTaskChargeDistribution(taskChargeDistributionList, dto.getChargeIds(), entity.getTemplateId(), entity.getId(), MathUtil.TWO);
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
        updateWrapper.set(TemplateTaskEntity::getBusinessProcessId, entity.getBusinessProcessId());
        updateWrapper.set(TemplateTaskEntity::getChargeId, entity.getChargeId());
        updateWrapper.set(TemplateTaskEntity::getChargeName, entity.getChargeName());
        updateWrapper.set(TemplateTaskEntity::getDescription, entity.getDescription());
        updateWrapper.set(TemplateTaskEntity::getIsFixed, entity.getIsFixed());
        updateWrapper.set(TemplateTaskEntity::getPlanEndTime, entity.getPlanEndTime());
        updateWrapper.set(TemplateTaskEntity::getPlanStartTime, entity.getPlanStartTime());
        updateWrapper.set(TemplateTaskEntity::getPriority, entity.getPriority());
        updateWrapper.set(TemplateTaskEntity::getType, entity.getType());
        updateWrapper.set(TemplateTaskEntity::getRoleId, entity.getRoleId());
        updateWrapper.set(TemplateTaskEntity::getRoleName, entity.getRoleName());
        updateWrapper.set(TemplateTaskEntity::getDistributionType, entity.getDistributionType());
        updateWrapper.set(TemplateTaskEntity::getRelatedSkuType, entity.getRelatedSkuType());
        updateWrapper.set(TemplateTaskEntity::getWorkPeriod, entity.getWorkPeriod());
        this.update(updateWrapper);
    }

    /**
     * 添加任务审核人信息
     */
    @Override
    public void setTaskChargeDistribution(List<TaskChargeDistributionEntity> taskChargeDistributionList, List<String> ids, String templateId, String taskId, Integer source) {

        if (CollectionUtils.isNotEmpty(taskChargeDistributionList)) {
            //根据分配类型查询模板中的数据
            for (TaskChargeDistributionEntity taskChargeDistributionEntity : taskChargeDistributionList) {
                if (StringUtils.isBlank(taskChargeDistributionEntity.getCharges())) {
                    throw new ServiceException(ApiError.ERROR_95097);
                }
                if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(taskChargeDistributionEntity.getDistributionType())) {
                    taskChargeDistributionEntity.setChargeIds(taskChargeDistributionEntity.getCharges());
                }
                if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(taskChargeDistributionEntity.getDistributionType())) {
                    List<String> roleIdList = Arrays.stream(taskChargeDistributionEntity.getCharges().split(",")).collect(Collectors.toList());
                    //查询对应模板角色下的人员
                    List<TemplateMembersEntity> templateMembersList = templateMembersService.listByRoleIds(roleIdList, templateId);
                    if (CollectionUtils.isNotEmpty(templateMembersList)) {
                        String approverIds = templateMembersList.stream().map(TemplateMembersEntity::getMemberId).distinct().collect(Collectors.joining(","));
                        taskChargeDistributionEntity.setChargeIds(approverIds);
                    }
                }
                if (DistributionTypeEnum.DISTRIBUTION_SUPERIOR.getCode().equals(taskChargeDistributionEntity.getDistributionType()) && CollectionUtils.isNotEmpty(ids)) {
                    //查询对应负责人的上级
                    List<UserSuperiorDTO> userSuperiorDTOS = sysUserFeign.listSuperiorByUserIds(ids);
                    if (CollectionUtils.isEmpty(userSuperiorDTOS)) {
                        continue;
                    }
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
        //保存交付文档的审核人
        taskChargeDistributionService.removeAndSave(taskId, taskChargeDistributionList, source);
    }
}
