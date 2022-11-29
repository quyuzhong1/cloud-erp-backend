package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.SetPreTaskDTO;
import com.erp.model.plm.entity.PreTaskEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.mapper.PreTaskMapper;
import com.erp.server.plm.service.PreTaskService;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.TaskDeliveryService;
import com.erp.server.plm.service.TaskDocsFinishService;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 *
 */
@Service
public class PreTaskServiceImpl extends ServiceImpl<PreTaskMapper, PreTaskEntity>
        implements PreTaskService {


    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private TaskDocsFinishService taskDocsFinishService;

    @Autowired
    private TaskDeliveryService taskDeliveryService;

    /**
     * 保存前置任务
     *
     * @param taskId
     * @param preTaskIdList
     * @return void
     * @author yl
     * @date 2022-10-12 14:28
     */

    @Override
    public void savePreTask(String taskId, List<String> preTaskIdList, String productId) {
        //先删除前置任务
        removePreTaskByTaskId(taskId, preTaskIdList);
        if (CollectionUtils.isNotEmpty(preTaskIdList)) {
            List<PreTaskEntity> addList = new ArrayList<>();
            for (String preTaskId : preTaskIdList) {
                PreTaskEntity entity = new PreTaskEntity();
                entity.setPreTaskId(preTaskId);
                entity.setTaskId(taskId);
                entity.setProductId(productId);
                addList.add(entity);
            }
            this.saveBatch(addList);
        }
    }


    /**
     * 删除前置任务
     *
     * @param taskId
     * @param preTaskIdList
     * @return void
     * @author yl
     * @date 2022-10-13 9:39
     */
    private void removePreTaskByTaskId(String taskId, List<String> preTaskIdList) {
        LambdaQueryWrapper<PreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PreTaskEntity::getTaskId, taskId);
        if (CollectionUtils.isNotEmpty(preTaskIdList)) {
            queryWrapper.in(PreTaskEntity::getPreTaskId, preTaskIdList);
        }
        this.remove(queryWrapper);
    }

    /**
     * 添加前置任务
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-12 14:32
     */

    @Override
    public Boolean addPreTask(SetPreTaskDTO dto) {
        PreTaskEntity entity = new PreTaskEntity();
        entity.setTaskId(dto.getTaskId());
        entity.setPreTaskId(dto.getPreTaskId());
        return this.save(entity);
    }

    /**
     * 移除前置任务
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean removePreTask(SetPreTaskDTO dto) {
        LambdaQueryWrapper<PreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PreTaskEntity::getTaskId, dto.getTaskId());
        queryWrapper.eq(PreTaskEntity::getPreTaskId, dto.getPreTaskId());
        Boolean flag = remove(queryWrapper);
        if (flag) {
            taskDeliveryService.removeByTaskId(dto.getTaskId());
            taskDocsFinishService.removeByTaskId(dto.getTaskId());
        }
        return flag;
    }

    /**
     * 根据任务id 获取任务的前置任务id
     *
     * @param taskId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-10-12 14:45
     */
    @Override
    public List<String> getPreTaskIdList(String taskId) {
        LambdaQueryWrapper<PreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PreTaskEntity::getTaskId, taskId);
        queryWrapper.select(PreTaskEntity::getPreTaskId);
        return this.listObjs(queryWrapper, Object::toString);
    }

    /**
     * 根任务id 获取前置任务id
     *
     * @param noProcessTaskIds
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-10-18 19:28
     */

    public List<String> getPreTaskIdListByTaskIds(List<String> noProcessTaskIds) {
        List<String> taskIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(noProcessTaskIds)) {
            LambdaQueryWrapper<PreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(PreTaskEntity::getPreTaskId);
            queryWrapper.in(PreTaskEntity::getTaskId, noProcessTaskIds);
            taskIdList = this.listObjs(queryWrapper, Object::toString);
        }
        return taskIdList;
    }

    /**
     * 完成任务 检查 前置任务是否已完成
     *
     * @param taskIds
     */
    @Override
    public void checkPreTaskFinish(List<String> taskIds) {
        //获取到前置任务id
        List<String> preTaskIds = getPreTaskIdListByTaskIds(taskIds);
        if (CollectionUtils.isNotEmpty(preTaskIds)) {
            int count = projectTaskService.countUndoneByTaskIds(TaskStateEnum.FINISH.getCode(), TaskStateEnum.APPROVAL_PASS.getCode(), preTaskIds);
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_95035);
            }
        }

    }


    /**
     * 获取该任务的前置任务
     *
     * @param taskId
     * @return java.util.List<com.erp.model.plm.entity.ProjectTaskEntity>
     * @author yl
     * @date 2022-10-27 9:58
     */
    @Override
    public List<ProjectTaskEntity> getPreTaskList(String taskId) {
        List<String> preTaskIdList = getPreTaskIdList(taskId);
        if (CollectionUtils.isNotEmpty(preTaskIdList)) {
            LambdaQueryWrapper<ProjectTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ProjectTaskEntity::getId, preTaskIdList);
            return projectTaskService.list(queryWrapper);
        }
        return new ArrayList<>();
    }

    @Override
    public List<PreTaskEntity> getPreTaskByProductId(String productId) {
        LambdaQueryWrapper<PreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PreTaskEntity::getProductId, productId);
        return list(queryWrapper);
    }

    /**
     * 获取系统的前置任务
     *
     * @param sysTaskIds
     * @return java.util.List<com.erp.model.plm.entity.PreTaskEntity>
     * @author yl
     * @date 2022-11-01 16:18
     */
    @Override
    public List<PreTaskEntity> getSysPreTask(List<String> sysTaskIds) {
        LambdaQueryWrapper<PreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PreTaskEntity::getProductId, "");
        queryWrapper.in(PreTaskEntity::getTaskId, sysTaskIds);
        return this.list(queryWrapper);
    }

    @Override
    public List<PreTaskEntity> getPreTaskListBytaskIds(List<String> taskIds) {
        if (CollectionUtils.isNotEmpty(taskIds)) {
            LambdaQueryWrapper<PreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(PreTaskEntity::getTaskId, taskIds);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();

    }

    /**
     * 根据前置任务id 集合 获取对应 任务id
     * @author yl
     * @date 2022-11-15 16:14
     * @param preTaskIds
     * @return java.util.List<com.erp.model.plm.entity.PreTaskEntity>
     */
    @Override
    public List<PreTaskEntity> getPreTaskListByPreTaskIds(List<String> preTaskIds) {
        if (CollectionUtils.isNotEmpty(preTaskIds)) {
            LambdaQueryWrapper<PreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(PreTaskEntity::getPreTaskId, preTaskIds);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }
}




