package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.enums.TaskTypeEnum;
import com.erp.server.plm.mapper.ProjectTaskSysMapper;
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


    @Override
    @Transactional
    public Boolean saveOrUpdateSysTask(SysTaskDTO dto) {
        checkTaskName(dto.getId(), dto.getName());
        //当前登录人
        LoginUser loginUser = commonService.getUserInfo();
        ProjectTaskSysEntity entity = new ProjectTaskSysEntity();
        //获取到任务阶段
        SysTaskPhaseEntity phaseEntity = sysTaskPhaseService.getById(dto.getPhaseId());
        BeanMapper.copy(dto, entity);
        List<String> chargeIds = dto.getChargeIds();


        //自定义审核人
        List<UserInfoDTO> approvalUserIds = dto.getApprovalUserIds();
        //配置表单属性
        String fieldConfigType = dto.getFieldConfigType();
        Integer type = dto.getType();
        //如果配置表单 一般任务 一定要走流程
        if (StringUtils.isNotBlank(fieldConfigType)) {
            Integer generalTask = TaskTypeEnum.GENERAL_TASK.getCode();
            //如果是一般任务 必须要有审核流程
            if (generalTask.equals(type)) {
                if (CollectionUtils.isEmpty(approvalUserIds)) {
                    throw new ServiceException(ApiError.ERROR_95078);
                }
            }
        }
        Boolean isReview=TaskTypeEnum.REVIEW_TASK.getCode().equals(type);
        if (CollectionUtils.isNotEmpty(approvalUserIds)) {
            List<String> approvalUserIdList = approvalUserIds.stream().map(UserInfoDTO::getUserId).collect(Collectors.toList());
            entity.setApprovalUserId(String.join(",", approvalUserIdList));
        }else{
            entity.setApprovalUserId("");
        }
        if(isReview){
            entity.setApprovalUserId("");
        }
        String chargeNames = commonService.getNameByIds(chargeIds);
        entity.setChargeName(chargeNames);
        entity.setChargeId(String.join(",", chargeIds));
        if (!Objects.isNull(loginUser)) {
            entity.setCreateUserId(loginUser.getUid());
            entity.setCreateUserName(loginUser.getUserName());
        }
        Integer priority=dto.getPriority();
        if(priority==null){
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
        List<DocsDTO> docsList = dto.getDeliveryDocsList();
        boolean flag = this.saveOrUpdate(entity);
        //表示保存成功
        if (flag) {
            taskDeliveryService.saveSysDeliveryDocs(entity.getId(), docsList);
            //保存前置任务
            preTaskService.savePreTask(entity.getId(), dto.getPreTaskIdList(), "");

            //保存SKU配置 字段 关系表
            taskRefSkuConfigService.addSkuField(entity.getId(),"", dto.getFieldConfigType(), dto.getFieldJson());

        }
        return flag;
    }

    /**
     * 检查任务名是否存在
     *
     * @param id
     * @param name
     * @return void
     * @author yl
     * @date 2022-10-20 19:54
     */
    private void checkTaskName(String id, String name) {
        LambdaQueryWrapper<ProjectTaskSysEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskSysEntity::getName, name);
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
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        SysTaskPagingSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params);
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
            if(CollectionUtils.isNotEmpty(preTaskNameList)){
                item.setPreTaskName(String.join(",",preTaskNameList));
            }
            if(CollectionUtils.isNotEmpty(preTaskIds)){
                item.setPreTaskId(String.join(",",preTaskIds));
            }
        }


        return new PagingVO(pageData);
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
    public List<ProjectTaskSysEntity> getListByProperty(Integer property) {
        LambdaQueryWrapper<ProjectTaskSysEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskSysEntity::getProperty, property);
        queryWrapper.orderByAsc(ProjectTaskSysEntity::getCreateTime);
        return this.list(queryWrapper);
    }


    /**
     * 获取系统任务名
     *
     * @param
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-09-22 16:43
     */
    @Override
    public List<String> getSysTaskNames() {
        LambdaQueryWrapper<ProjectTaskSysEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ProjectTaskSysEntity::getName);
        return this.listObjs(queryWrapper, Object::toString);
    }


    /**
     * 系统任务获取前置任务
     *
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.String>>
     * @author yl
     * @date 2022-10-08 10:57
     */
    @Override
    public List<Map<String, Object>> taskList() {
        LambdaQueryWrapper<ProjectTaskSysEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.select(ProjectTaskSysEntity::getId, ProjectTaskSysEntity::getName);
        return this.listMaps(queryWrapper);
    }

    @Override
    public SysTaskDTO taskDetails(String taskId) {
        ProjectTaskSysEntity sysEntity = this.getById(taskId);
        if (Objects.isNull(sysEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        SysTaskDTO sysTaskDTO = new SysTaskDTO();
        BeanMapper.copy(sysEntity, sysTaskDTO);
        String chargeId = sysEntity.getChargeId();
        if (StringUtils.isNotBlank(chargeId)) {
            sysTaskDTO.setChargeIds(Arrays.asList(chargeId.split(",")));
        }
        String approvalUserId = sysEntity.getApprovalUserId();
        List<String> approvalUserIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(approvalUserId) && !approvalUserId.toLowerCase().equals("null")) {
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
        sysTaskDTO.setApprovalUserIds(approvalUserList);
        String businessProcessId = sysEntity.getBusinessProcessId();
        if (StringUtils.isNotBlank(businessProcessId)) {
            BusinessProcessEntity processEntity = businessProcessService.getById(businessProcessId);
            if (processEntity != null) {
                sysTaskDTO.setBusinessName(processEntity.getBusinessName());
            }
        }
        //前置任务id集合
        List<String> preTaskIdList = preTaskService.getPreTaskIdList(taskId);
        sysTaskDTO.setDeliveryDocsList(taskDeliveryService.getSysTaskFinishDocs(taskId));
        sysTaskDTO.setPreTaskIdList(preTaskIdList);
        TaskRefSkuConfigEntity refSku = taskRefSkuConfigService.getByTaskId(taskId);
        if (refSku != null) {
            sysTaskDTO.setFieldJson(refSku.getFieldJson());
            sysTaskDTO.setFieldConfigType(refSku.getFieldConfigType());
        }

        return sysTaskDTO;
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
