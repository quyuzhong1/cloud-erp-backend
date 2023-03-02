package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.dto.PreTaskDTO;
import com.erp.model.plm.dto.PreTaskUpdateDTO;
import com.erp.model.plm.dto.ProjectChildTaskDTO;
import com.erp.model.plm.dto.SetPreTaskDTO;
import com.erp.model.plm.entity.PreTaskEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.ProjectTaskSysEntity;
import com.erp.model.plm.enums.TaskRelationshipEnum;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.model.plm.vo.PreTaskListVO;
import com.erp.model.plm.vo.PreTaskVO;
import com.erp.server.plm.mapper.PreTaskMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
    @Autowired
    private ProjectTaskSysService projectTaskSysService;

    /**
     * 保存前置任务
     *
     * @param taskId
     * @param preTaskList
     * @return void
     * @author yl
     * @date 2022-10-12 14:28
     */

    @Override
    public void savePreTask(String taskId, List<String> preTaskList, String productId) {
        //先删除前置任务
        removePreTaskByTaskId(taskId, preTaskList);
        if (CollectionUtils.isNotEmpty(preTaskList)) {
            List<PreTaskEntity> addList = preTaskList.stream()
                    .map(preTask -> new PreTaskEntity(preTask, taskId, productId))
                    .collect(Collectors.toList());
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
        entity.setIntervalWorkPeriod(dto.getIntervalWorkPeriod());
        entity.setRelationship(TaskRelationshipEnum.getByCode(dto.getRelationshipCode()));
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
    public List<PreTaskVO> getPreTaskIdList(String taskId) {
        LambdaQueryWrapper<PreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PreTaskEntity::getTaskId, taskId);
        queryWrapper.select(PreTaskEntity::getPreTaskId, PreTaskEntity::getRelationship,
                PreTaskEntity::getIntervalWorkPeriod, PreTaskEntity::getId, PreTaskEntity::getTaskId);
        List<PreTaskEntity> entityList = this.list(queryWrapper);
        if (CollectionUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }
        return entityList.stream().map(PreTaskVO::new).collect(Collectors.toList());
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
     *
     * @param preTaskIds
     * @return java.util.List<com.erp.model.plm.entity.PreTaskEntity>
     * @author yl
     * @date 2022-11-15 16:14
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

    /**
     * 删除任务后 需要删除对应的前置任务
     *
     * @param taskId
     * @return void
     * @author yl
     * @date 2022-12-02 11:04
     */
    @Override
    public void deleteByTaskId(String taskId) {
        if (StringUtils.isNotBlank(taskId)) {
            LambdaQueryWrapper<PreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PreTaskEntity::getPreTaskId, taskId);
            this.remove(queryWrapper);

            LambdaQueryWrapper<PreTaskEntity> query = new LambdaQueryWrapper<>();
            query.eq(PreTaskEntity::getTaskId, taskId);
            this.remove(query);

        }

    }

    @Override
    public void listChildrenTask(List<String> taskIds, List<ProjectTaskEntity> list) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return;
        }
        List<PreTaskEntity> preTaskList = this.getPreTaskListByPreTaskIds(taskIds);
        if (CollectionUtils.isNotEmpty(preTaskList)) {
            List<String> childTaskIds = preTaskList.stream().map(PreTaskEntity::getTaskId).collect(Collectors.toList());
            List<ProjectTaskEntity> projectTaskList = projectTaskService.listByIds(childTaskIds);
            if (CollectionUtils.isNotEmpty(projectTaskList)) {
                list.addAll(projectTaskList);
            }
            //判断子任务是否还拥有子任务
            listChildrenTask(childTaskIds, list);
        }
    }


    /**
     * 批量更新前置任务
     *
     * @param taskIdList
     * @param preTaskIdList
     * @return void
     * @author yl
     * @date 2023-02-10 16:53
     */
    @Override
    public void batchUpdate(String productId, List<String> taskIdList, List<String> preTaskIdList) {


        if (CollectionUtils.isEmpty(taskIdList) || CollectionUtils.isEmpty(preTaskIdList)) {
            return;
        }

        //先删除
        if (CollectionUtils.isNotEmpty(taskIdList)) {
            LambdaQueryWrapper<PreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(PreTaskEntity::getTaskId, taskIdList);
            this.remove(queryWrapper);
        }
        List<PreTaskEntity> addList = new ArrayList<>();
        //后添加
        for (String taskId : taskIdList) {
            for (String preTaskId : preTaskIdList) {
                PreTaskEntity entity = new PreTaskEntity();
                entity.setProductId(productId);
                entity.setTaskId(taskId);
                entity.setPreTaskId(preTaskId);
                addList.add(entity);
            }
        }

        this.saveBatch(addList);
    }

    @Override
    public Map<String, List<PreTaskVO>> listByTaskIds(List<String> taskIds) {
        if (CollectionUtil.isEmpty(taskIds)){
            return MapUtil.empty();
        }
        List<PreTaskEntity> entityList = lambdaQuery().in(PreTaskEntity::getTaskId, taskIds)
                .list();
        if(CollectionUtil.isEmpty(entityList)){
            return MapUtil.empty();
        }
        Map<String, List<PreTaskVO>> groupByTaskIdMap = entityList.stream()
                .map(PreTaskVO::new)
                .collect(Collectors.groupingBy(PreTaskVO::getTaskId));
        return groupByTaskIdMap;
    }

    @Override
    public List<PreTaskListVO> ListPreTaskByTaskId(String taskId) {
        List<PreTaskEntity> entityList = lambdaQuery().eq(PreTaskEntity::getTaskId, taskId)
                .list();
        if(CollectionUtil.isEmpty(entityList)){
            return Collections.emptyList();
        }
        List<String> preTaskIds = entityList.stream().map(PreTaskEntity::getPreTaskId).distinct().collect(Collectors.toList());
        List<ProjectTaskEntity> taskEntityList = projectTaskService.listByTaskIds(preTaskIds);
        Map<String, ProjectTaskEntity> preTaskIdMap = taskEntityList.stream().collect(Collectors.toMap(ProjectTaskEntity::getId, e -> e));
        return entityList.stream().map(x -> new PreTaskListVO(x, preTaskIdMap.get(x.getPreTaskId()))).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePreTask(List<PreTaskUpdateDTO> dto) {
        if (CollectionUtil.isEmpty(dto)) {
            return false;
        }
        List<PreTaskEntity> updateList = dto.stream().map(PreTaskEntity::new).collect(Collectors.toList());
        return updateBatchById(updateList);
    }

    @Override
    public List<ProjectChildTaskDTO> listChildrenTaskOneByTaskId(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            return Collections.emptyList();
        }
        List<PreTaskEntity> preTaskList = lambdaQuery()
                .eq(PreTaskEntity::getPreTaskId, taskId)
                .list();
        if (CollectionUtils.isEmpty(preTaskList)) {
           return Collections.emptyList();
        }
        Map<String, PreTaskEntity> preTaskMap = preTaskList.stream().collect(Collectors.toMap(PreTaskEntity::getTaskId, e -> e));
        List<String> childTaskIds = preTaskList.stream().map(PreTaskEntity::getTaskId).collect(Collectors.toList());
        List<ProjectTaskEntity> projectTaskList = projectTaskService.listByIds(childTaskIds);
        List<ProjectChildTaskDTO> resultList = projectTaskList.stream()
                .map(task ->
                    new ProjectChildTaskDTO(task, preTaskMap.get(task.getId()))
                )
                .collect(Collectors.toList());
        return resultList;
    }

    @Override
    public List<PreTaskListVO> ListSysPreTaskByTaskId(String taskId) {
        List<PreTaskEntity> entityList = lambdaQuery().eq(PreTaskEntity::getTaskId, taskId)
                .list();
        if(CollectionUtil.isEmpty(entityList)){
            return Collections.emptyList();
        }
        List<String> preTaskIds = entityList.stream().map(PreTaskEntity::getPreTaskId).distinct().collect(Collectors.toList());
        List<ProjectTaskSysEntity> taskEntityList = projectTaskSysService.lambdaQuery()
                .in(ProjectTaskSysEntity::getId, preTaskIds)
                .list();
        Map<String, ProjectTaskSysEntity> preTaskIdMap = taskEntityList.stream().collect(Collectors.toMap(ProjectTaskSysEntity::getId, e -> e));
        return entityList.stream().map(x -> new PreTaskListVO(x, preTaskIdMap.get(x.getPreTaskId()))).collect(Collectors.toList());
    }


}




