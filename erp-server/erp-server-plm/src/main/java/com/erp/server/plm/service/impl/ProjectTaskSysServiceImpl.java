package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.SysTaskDTO;
import com.erp.model.plm.dto.SysTaskPagingDTO;
import com.erp.model.plm.dto.finishDocsDTO;
import com.erp.model.plm.entity.ProjectTaskSysEntity;
import com.erp.model.plm.entity.SysTaskPhaseEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.mapper.ProjectTaskSysMapper;
import com.erp.server.plm.service.ProjectTaskSysService;
import com.erp.server.plm.service.SysTaskPhaseService;
import com.erp.server.plm.service.TaskDeliveryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public Boolean saveOrUpdateSysTask(SysTaskDTO dto) {
        ProjectTaskSysEntity entity = new ProjectTaskSysEntity();
        //获取到任务阶段
        SysTaskPhaseEntity phaseEntity = sysTaskPhaseService.getById(dto.getPhaseId());
        BeanMapper.copy(dto, entity);
        if (!Objects.isNull(phaseEntity)) {
            entity.setPhaseName(phaseEntity.getName());
            //如果是立项任务
            if (IsConstant.YES.equals(phaseEntity.getIsProjectApproval())) {
                entity.setProperty(TaskConstant.APPROVAL_TASK);
            } else {
                entity.setProperty(TaskConstant.PROJECT_TASK);
            }

        }
        List<finishDocsDTO> docsList = dto.getFinishDocsList();
        boolean flag = this.saveOrUpdate(entity);
        //表示保存成功
        if(flag){
            taskDeliveryService.saveSysDeliveryDocs(entity.getId(),docsList);
        }
        return flag;
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
        Boolean flag=this.removeById(taskId);
        if(flag){
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
}
