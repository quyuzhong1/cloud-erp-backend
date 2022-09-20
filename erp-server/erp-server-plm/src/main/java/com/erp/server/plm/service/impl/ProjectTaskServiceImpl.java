package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.SysTaskDTO;
import com.erp.model.plm.dto.finishDocsDTO;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.ProjectTaskSysEntity;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.mapper.ProjectTaskMapper;
import com.erp.server.plm.service.ProjectTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.server.plm.service.ProjectTaskSysService;
import com.erp.server.plm.service.TaskRefDocsService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

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
        List<ProjectTaskSysEntity> sysTaskList = projectTaskSysService.list();
        if (CollectionUtils.isNotEmpty(sysTaskList)) {
            List<ProjectTaskEntity> saveList = new LinkedList<>();
            for (ProjectTaskSysEntity item : sysTaskList) {
                ProjectTaskEntity entity = new ProjectTaskEntity();
                BeanMapper.copy(item, entity);
                entity.setQuoteSysTaskId(item.getId());
                entity.setProductId(productId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
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
                entity.setStatus(TaskStateEnum.TO_BE_RELEASED.getCode());
                entity.setTaskApprovalStatus(TaskStateEnum.TO_BE_RELEASED.getCode());
            }
            this.saveBatch(list);

        }


    }
}
