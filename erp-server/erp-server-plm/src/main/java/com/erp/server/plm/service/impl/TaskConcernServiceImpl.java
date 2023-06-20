package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.model.plm.dto.TaskConcernDTO;
import com.erp.model.plm.entity.TaskConcernEntity;
import com.erp.server.plm.mapper.TaskConcernMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.TaskConcernService;
import com.common.business.service.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 任务关注的人 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-19
 */
@Service
public class TaskConcernServiceImpl extends SuperServiceImpl<TaskConcernMapper, TaskConcernEntity> implements TaskConcernService {

    @Resource
    private CommonService commonService;

    /**
     * 获取到任务id是否被登录人关注
     *
     * @param taskId
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-19 18:57
     */
    @Override
    public TaskConcernDTO.InfoDTO getConcernByTaskId(String taskId) {
        TaskConcernDTO.InfoDTO result = new TaskConcernDTO.InfoDTO();
        String userId = commonService.getUserInfo().getUid();
        TaskConcernEntity taskConcern = this.getByTaskIdAndUserId(userId, taskId);
        if (taskConcern != null) {
            result.setIsConcern(Boolean.TRUE);
        } else {
            result.setIsConcern(Boolean.FALSE);
        }
        Integer concernCount = getConcernCountByTaskId(taskId);
        result.setConcernCount(concernCount);
        return result;
    }


    /**
     * 任务关注
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean concernTask(TaskConcernDTO.ConcernDTO dto) {
        String userId = commonService.getUserInfo().getUid();
        TaskConcernEntity taskConcern = new TaskConcernEntity();
        taskConcern.setTaskId(dto.getTaskId());
        taskConcern.setUserId(userId);
        return this.save(taskConcern);
    }

    /**
     * 取消任务关注
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelConcern(TaskConcernDTO.ConcernDTO dto) {
        String userId = commonService.getUserInfo().getUid();
        String taskId = dto.getTaskId();
        TaskConcernEntity taskConcern = this.getByTaskIdAndUserId(userId, taskId);
        if (taskConcern != null) {
            return this.removeById(taskConcern.getId());
        }
        return Boolean.TRUE;
    }

    /**
     * 根据任务id 统计到关注人数
     *
     * @param taskId
     * @return
     */
    @Override
    public Integer getConcernCountByTaskId(String taskId) {
        return this.lambdaQuery().eq(TaskConcernEntity::getTaskId, taskId).list().size();
    }


    /**
     * 保存关注人
     *
     * @param taskId
     * @param productId
     * @param refUserIdList
     * @return void
     * @author yl
     * @date 2023-06-20 16:43
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(String taskId, String productId, List<String> refUserIdList) {
        if (CollectionUtils.isNotEmpty(refUserIdList)) {
            List<TaskConcernEntity> addList = new ArrayList<>(refUserIdList.size());
            for (String refUserId : refUserIdList) {
                TaskConcernEntity taskConcern = new TaskConcernEntity();
                taskConcern.setUserId(refUserId);
                taskConcern.setTaskId(taskId);
                taskConcern.setProductId(productId);
                addList.add(taskConcern);
            }
            this.saveBatch(addList);
        }


    }


    /**
     * 更改任务关注人
     *
     * @param taskId
     * @param productId
     * @param refUserIdList
     * @return void
     * @author yl
     * @date 2023-06-20 16:54
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(String taskId, String productId, List<String> refUserIdList) {
        this.removeByTaskId(taskId);
        this.batchAdd(taskId, productId, refUserIdList);
    }


    /**
     * 根据任务id 获取到对应关注人信息
     *
     * @param taskId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-06-20 17:02
     */
    @Override
    public List<String> listByTaskId(String taskId) {
        LambdaQueryWrapper<TaskConcernEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.select(TaskConcernEntity::getUserId);
        queryWrapper.eq(TaskConcernEntity::getTaskId,taskId);
        return this.listObjs(queryWrapper, Object::toString);
    }


    /**
     * 根据任务id 删除
     *
     * @param taskId
     */
    public void removeByTaskId(String taskId) {
        LambdaQueryWrapper<TaskConcernEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskConcernEntity::getTaskId, taskId);
        this.remove(queryWrapper);
    }


    private TaskConcernEntity getByTaskIdAndUserId(String userId, String taskId) {
        LambdaQueryWrapper<TaskConcernEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskConcernEntity::getUserId, userId);
        queryWrapper.eq(TaskConcernEntity::getTaskId, taskId);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);

    }
}
