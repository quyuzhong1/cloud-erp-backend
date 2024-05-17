package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.FindUserDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.model.plm.dto.TaskFollowerDTO;
import com.erp.model.plm.entity.TaskFollowerEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.mapper.TaskFollowerMapper;
import com.erp.server.plm.service.TaskFollowerService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 任务关注的人 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-19
 */
@Service
public class TaskFollowerServiceImpl extends SuperServiceImpl<TaskFollowerMapper, TaskFollowerEntity> implements TaskFollowerService {

    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 获取到任务id是否被登录人关注
     *
     * @param taskId
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-19 18:57
     */
    @Override
    public TaskFollowerDTO.InfoDTO getFollowerByTaskId(String taskId) {
        TaskFollowerDTO.InfoDTO result = new TaskFollowerDTO.InfoDTO();
        String userId = UserContext.getDefaultLoginUser().getUid();
        TaskFollowerEntity taskConcern = this.getByTaskIdAndUserId(userId, taskId);
        if (taskConcern != null) {
            result.setIsConcern(Boolean.TRUE);
        } else {
            result.setIsConcern(Boolean.FALSE);
        }
        List<String> userIdList = listByTaskId(taskId);
        List<FindUserDTO> userList=  sysUserFeign.getUserListByUserIds(userIdList);
        result.setUserNameList(userList.stream().map(FindUserDTO::getUserName).collect(Collectors.toList()));
        Integer concernCount = userIdList.size();
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
    public Boolean followerTask(TaskFollowerDTO.FollowerDTO dto) {
        String userId = UserContext.getDefaultLoginUser().getUid();
        TaskFollowerEntity taskConcern = new TaskFollowerEntity();
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
    public Boolean cancelFollower(TaskFollowerDTO.FollowerDTO dto) {
        String userId = UserContext.getDefaultLoginUser().getUid();
        String taskId = dto.getTaskId();
        TaskFollowerEntity taskConcern = this.getByTaskIdAndUserId(userId, taskId);
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
    public Integer getFollowerCountByTaskId(String taskId) {
        return this.lambdaQuery().eq(TaskFollowerEntity::getTaskId, taskId).list().size();
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
            List<TaskFollowerEntity> addList = new ArrayList<>(refUserIdList.size());
            for (String refUserId : refUserIdList) {
                TaskFollowerEntity taskConcern = new TaskFollowerEntity();
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
        LambdaQueryWrapper<TaskFollowerEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.select(TaskFollowerEntity::getUserId);
        queryWrapper.eq(TaskFollowerEntity::getTaskId, taskId);
        return this.listObjs(queryWrapper, Object::toString).stream().distinct().collect(Collectors.toList());
    }

    @Override
    public List<TaskFollowerEntity> listByTaskIds(List<String> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(TaskFollowerEntity::getTaskId, taskIds).list();
    }


    /**
     * 根据任务id 删除
     *
     * @param taskId
     */
    public void removeByTaskId(String taskId) {
        LambdaQueryWrapper<TaskFollowerEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskFollowerEntity::getTaskId, taskId);
        this.remove(queryWrapper);
    }


    private TaskFollowerEntity getByTaskIdAndUserId(String userId, String taskId) {
        LambdaQueryWrapper<TaskFollowerEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskFollowerEntity::getUserId, userId);
        queryWrapper.eq(TaskFollowerEntity::getTaskId, taskId);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);

    }
}
