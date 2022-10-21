package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.SysTaskDTO;
import com.erp.model.plm.dto.SysTaskPagingDTO;
import com.erp.model.plm.dto.FinishDocsDTO;
import com.erp.model.plm.entity.BusinessProcessEntity;
import com.erp.model.plm.entity.ProjectTaskSysEntity;
import com.erp.model.plm.entity.SysTaskPhaseEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProjectTaskSysMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private SysDocsService sysDocsService;

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
        String chargeNames = commonService.getNameByIds(chargeIds);
        entity.setChargeName(chargeNames);
        entity.setChargeId(String.join(",", chargeIds));
        if (!Objects.isNull(loginUser)) {
            entity.setCreateUserId(loginUser.getUid());
            entity.setCreateUserName(loginUser.getUserName());
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
        List<FinishDocsDTO> docsList = dto.getFinishDocsList();
        boolean flag = this.saveOrUpdate(entity);
        //表示保存成功
        if (flag) {
            taskDeliveryService.saveSysDeliveryDocs(entity.getId(), docsList);
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
    public PagingVO<SysTaskPagingDTO> paging(PagingDTO<BaseSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params);
        List<SysTaskPagingDTO> list = pageData.getRecords();
        //获取所有的系统任务的 文档名
        List<SysTaskPagingDTO> docsNames = getSysTaskDocsNames();
        for (SysTaskPagingDTO item : list) {
            List<String> docsNameList = docsNames.stream().filter(d -> item.getId().equals(d.getId())).map(SysTaskPagingDTO::getName).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(docsNameList)) {
                item.setDocsNames(String.join(",", docsNameList));
            } else {
                item.setDocsNames("");
            }
        }
        return new PagingVO(pageData);
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
        String businessProcessId = sysEntity.getBusinessProcessId();
        if (StringUtils.isNotBlank(businessProcessId)) {
            BusinessProcessEntity processEntity = businessProcessService.getById(businessProcessId);
            if (processEntity != null) {
                sysTaskDTO.setBusinessName(processEntity.getBusinessName());
            }
        }
        sysTaskDTO.setFinishDocsList(taskDeliveryService.getSysTaskFinishDocs(taskId));
        return sysTaskDTO;
    }
}
