package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.IsConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.DistributionTypeEnum;
import com.erp.model.plm.enums.RelatedSkuTypeEnum;
import com.erp.model.plm.enums.TaskTypeEnum;
import com.erp.model.plm.vo.PreTaskVO;
import com.erp.model.plm.vo.SysTaskVO;
import com.erp.model.sys.dto.UserSuperiorDTO;
import com.erp.model.sys.enums.ChargeSuperiorEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.mapper.ProjectTaskSysMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.isNotEmpty;

/**
 * <p>
 * 系统任务 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
public class ProjectTaskSysServiceImpl extends ServiceImpl<ProjectTaskSysMapper, ProjectTaskSysEntity> implements ProjectTaskSysService {


    @Autowired
    private TaskDeliveryService taskDeliveryService;

    @Autowired
    private SysTaskPhaseService sysTaskPhaseService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private BusinessProcessService businessProcessService;

    @Autowired
    private PreTaskService preTaskService;

    @Autowired
    private TaskRefSkuConfigService taskRefSkuConfigService;

    @Autowired
    private TemplateRoleService templateRoleService;

    @Autowired
    private TaskChargeDistributionService taskChargeDistributionService;

    @Autowired
    private SysUserFeign sysUserFeign;


    @Override
    @Transactional
    public Boolean saveOrUpdateSysTask(SysTaskDTO dto) {
        checkTaskName(dto.getId(), dto.getName(), dto.getTemplateId());
        //当前登录人
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        ProjectTaskSysEntity entity = new ProjectTaskSysEntity();
        //获取到任务阶段
        SysTaskPhaseEntity phaseEntity = sysTaskPhaseService.getById(dto.getPhaseId());
        BeanMapper.copy(dto, entity);
        //负责人
        List<String> chargeIds = dto.getChargeIds();
        //角色
        List<String> roleIds = dto.getRoleIds();

        //任务分配类型处理
        if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(dto.getDistributionType())) {//分配类型为角色
            List<TemplateRoleEntity> templateRoleList = templateRoleService.listByIds(roleIds);
            entity.setRoleId(String.join(",", roleIds));
            if (CollectionUtils.isNotEmpty(templateRoleList)) {
                List<String> roleNames = templateRoleList.stream().map(TemplateRoleEntity::getName).collect(Collectors.toList());
                entity.setRoleName(String.join(",", roleNames));
                entity.setChargeId("");
                entity.setChargeName("");
            }
        } else if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(dto.getDistributionType())) {//分配类型为负责人
            String chargeNames = commonService.getNameByIds(chargeIds);
            entity.setChargeId(String.join(",", chargeIds));
            entity.setChargeName(chargeNames);
            entity.setRoleId("");
            entity.setRoleName("");
        }

        //自定义审核人
        List<TaskChargeDistributionDTO> approvalList = dto.getApprovalList();

        if (!Objects.isNull(loginUser)) {
            entity.setCreateUserId(loginUser.getUid());
            entity.setCreateUserName(loginUser.getUserName());
        }
        Integer priority = dto.getPriority();
        if (priority == null) {
            entity.setPriority(0);
        }
        if (!Objects.isNull(phaseEntity)) {
            entity.setPhaseName(phaseEntity.getName());
            //如果是立项任务
            if (IsConstant.YES.equals(phaseEntity.getIsProjectApproval())) {
                entity.setProperty(TaskConstant.APPROVAL_TASK);
            } else {
                entity.setProperty(TaskConstant.PROJECT_TASK);
            }

        }
        if (null != dto.getWorkPeriod() && 0 < dto.getWorkPeriod()) {
            entity.setWorkPeriod(dto.getWorkPeriod());
        }
        List<DocsDTO> docsList = dto.getDeliveryDocsList();
        boolean flag = this.saveOrUpdate(entity);
        //表示保存成功
        if (flag) {
            List<TaskChargeDistributionEntity> taskChargeDistributionList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(approvalList)) {
                approvalList.forEach(obj -> obj.setCharges(String.join(",", obj.getChargeList())));
                taskChargeDistributionList = BeanMapperUtils.copyList(TaskChargeDistributionEntity.class, approvalList);
                //根据分配类型查询模板中的数据
                for (TaskChargeDistributionEntity taskChargeDistributionEntity : taskChargeDistributionList) {
                    if (StringUtils.isBlank(taskChargeDistributionEntity.getCharges())) {
                        throw new ServiceException(ApiError.ERROR_95097);
                    }
                    if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(taskChargeDistributionEntity.getDistributionType())) {
                        taskChargeDistributionEntity.setChargeIds(taskChargeDistributionEntity.getCharges());
                    }
                    if (DistributionTypeEnum.DISTRIBUTION_SUPERIOR.getCode().equals(taskChargeDistributionEntity.getDistributionType()) && CollectionUtils.isNotEmpty(dto.getChargeIds())) {
                        //查询对应负责人的上级
                        List<String> ids = dto.getChargeIds();
                        List<UserSuperiorDTO> userSuperiorDTOS = sysUserFeign.listSuperiorByUserIds(ids);
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
            //保存交付文档的审核人
            taskChargeDistributionService.removeAndSave(entity.getId(), taskChargeDistributionList, MathUtil.ONE);

            taskDeliveryService.saveSysDeliveryDocs(entity.getId(), docsList);
            //保存前置任务
            preTaskService.savePreTask(entity.getId(), dto.getPreTaskIdList(), "");

            //保存SKU配置 字段 关系表
            taskRefSkuConfigService.addSkuField(entity.getId(), "", dto.getFieldConfigType(), dto.getFieldJson());

        }
        return flag;
    }

    /**
     * 检查任务名是否存在
     *
     * @param id
     * @param name
     * @param templateId 模板id
     * @return void
     * @author yl
     * @date 2022-10-20 19:54
     */
    private void checkTaskName(String id, String name, String templateId) {
        LambdaQueryWrapper<ProjectTaskSysEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskSysEntity::getName, name);
        if (StringUtils.isNotBlank(templateId)) {
            queryWrapper.eq(ProjectTaskSysEntity::getTemplateId, templateId);
        }
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(ProjectTaskSysEntity::getId, id);
        }
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95013);
        }
    }

    @Override
    public PagingVO<SysTaskPagingDTO> paging(PagingDTO<SysTaskPagingSearchDTO> dto) {
        Page<SysTaskPagingSearchDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        SysTaskPagingSearchDTO params = dto.getParams();
        IPage<SysTaskPagingDTO> pageData = baseMapper.paging(query, params);
        List<SysTaskPagingDTO> list = pageData.getRecords();
        //获取所有的系统任务的 文档名
        List<SysTaskPagingDTO> docsNames = getSysTaskDocsNames();

        //获取到任务id 集合
        List<String> taskIds = list.stream().map(SysTaskPagingDTO::getId).collect(Collectors.toList());
        List<PreTaskEntity> preTaskList = preTaskService.getPreTaskListBytaskIds(taskIds);
        //前置任务
        List<ProjectTaskSysEntity> preTaskEntityList = this.getByTaskIds(preTaskList.stream().map(PreTaskEntity::getPreTaskId).collect(Collectors.toList()));

        for (SysTaskPagingDTO item : list) {
            List<String> docsNameList = docsNames.stream().filter(d -> item.getId().equals(d.getId())).map(SysTaskPagingDTO::getName).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(docsNameList)) {
                item.setDocsNames(String.join(",", docsNameList));
            } else {
                item.setDocsNames("");
            }
            List<String> preTaskIds = preTaskList.stream().filter(p -> p.getTaskId().equals(item.getId())).map(PreTaskEntity::getPreTaskId).collect(Collectors.toList());

            List<String> preTaskNameList = preTaskEntityList.stream().filter(t -> preTaskIds.contains(t.getId())).map(ProjectTaskSysEntity::getName).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(preTaskNameList)) {
                item.setPreTaskName(String.join(",", preTaskNameList));
            }
            if (CollectionUtils.isNotEmpty(preTaskIds)) {
                item.setPreTaskId(String.join(",", preTaskIds));
            }
        }


        return new PagingVO<>(pageData);
    }

    private List<ProjectTaskSysEntity> getByTaskIds(List<String> taskIds) {
        if (CollectionUtils.isNotEmpty(taskIds)) {
            LambdaQueryWrapper<ProjectTaskSysEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ProjectTaskSysEntity::getId, taskIds);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }


    /**
     * 获取到所有 系统任务锁设置的文件名
     *
     * @param
     * @return
     * @author yl
     * @date 2022-09-16 9:05
     */
    private List<SysTaskPagingDTO> getSysTaskDocsNames() {
        return baseMapper.getSysTaskDocsNames();
    }

    /**
     * 刪除任務
     *
     * @param taskId
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-15 19:19
     */
    @Override
    @Transactional
    public Boolean removeTask(String taskId) {
        Boolean flag = this.removeById(taskId);
        if (flag) {
            //删除任务审核人
            taskChargeDistributionService.removeBySourceAndTaskId(MathUtil.ONE, taskId);

            taskDeliveryService.removeByTaskId(taskId);
        }
        return flag;
    }


    /**
     * 根据任务属性 查询对应的项目任务
     *
     * @param property
     * @return java.util.List<com.erp.model.plm.entity.ProjectTaskSysEntity>
     * @author yl
     * @date 2022-09-21 9:12
     */
    @Override
    public List<ProjectTaskSysEntity> getListByProperty(Integer property, String templateId) {
        LambdaQueryWrapper<ProjectTaskSysEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskSysEntity::getProperty, property);
        queryWrapper.eq(ProjectTaskSysEntity::getTemplateId, templateId);
        queryWrapper.orderByAsc(ProjectTaskSysEntity::getCreateTime);
        return this.list(queryWrapper);
    }


    /**
     * /**
     * 系统任务获取前置任务
     *
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.String>>
     * @author yl
     * @date 2022-10-08 10:57
     */
    @Override
    public List<Map<String, Object>> taskList(String templateId) {
        LambdaQueryWrapper<ProjectTaskSysEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ProjectTaskSysEntity::getId, ProjectTaskSysEntity::getName);
        if (StringUtils.isNotBlank(templateId)) {
            queryWrapper.eq(ProjectTaskSysEntity::getTemplateId, templateId);
        }
        return this.listMaps(queryWrapper);
    }

    @Override
    public SysTaskVO taskDetails(String taskId) {
        ProjectTaskSysEntity sysEntity = this.getById(taskId);
        if (Objects.isNull(sysEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        SysTaskVO sysTaskVO = new SysTaskVO();
        BeanMapper.copy(sysEntity, sysTaskVO);
        //判断任务分配类型
        if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(sysEntity.getDistributionType())) {
            String roleId = sysEntity.getRoleId();
            if (StringUtils.isNotBlank(roleId)) {
                sysTaskVO.setRoleIds(Arrays.asList(roleId.split(",")));
            }
        } else if (DistributionTypeEnum.DISTRIBUTION_USER.getCode().equals(sysEntity.getDistributionType())) {
            String chargeId = sysEntity.getChargeId();
            if (StringUtils.isNotBlank(chargeId)) {
                sysTaskVO.setChargeIds(Arrays.asList(chargeId.split(",")));
            }
        }
        //查询任务下审核人
        List<TaskChargeDistributionEntity> taskChargeDistributionList = taskChargeDistributionService.listBySourceAndTaskId(MathUtil.ONE, sysEntity.getId());
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
            sysTaskVO.setApprovalList(list);
        }
        String businessProcessId = sysEntity.getBusinessProcessId();
        if (StringUtils.isNotBlank(businessProcessId)) {
            BusinessProcessEntity processEntity = businessProcessService.getById(businessProcessId);
            if (processEntity != null) {
                sysTaskVO.setBusinessName(processEntity.getBusinessName());
            }
        }
        //前置任务id集合
        List<PreTaskVO> preTaskList = preTaskService.getPreTaskIdList(taskId);
        sysTaskVO.setDeliveryDocsList(taskDeliveryService.getSysTaskFinishDocs(taskId));
        List<String> pretaskIdList = Collections.emptyList();
        if (isNotEmpty(preTaskList)) {
            pretaskIdList = preTaskList.stream().map(PreTaskVO::getPreTaskId).collect(Collectors.toList());
        }
        sysTaskVO.setPreTaskIdList(pretaskIdList);
        TaskRefSkuConfigEntity refSku = taskRefSkuConfigService.getByTaskId(taskId);
        if (refSku != null) {
            sysTaskVO.setFieldJson(refSku.getFieldJson());
            sysTaskVO.setFieldConfigType(refSku.getFieldConfigType());
        }

        return sysTaskVO;
    }

    /**
     * 检查阶段是否引用
     *
     * @param phaseId
     * @return void
     * @author yl
     * @date 2022-10-27 14:55
     */
    @Override
    public void checkQuotePhase(String phaseId) {
        LambdaQueryWrapper<ProjectTaskSysEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskSysEntity::getPhaseId, phaseId);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95048);
        }
    }
}
