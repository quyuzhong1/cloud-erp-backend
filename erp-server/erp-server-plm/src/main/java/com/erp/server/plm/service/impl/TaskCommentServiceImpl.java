package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.TaskCommentDTO;
import com.erp.model.plm.entity.TaskCommentEntity;
import com.erp.server.plm.mapper.TaskCommentMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.TaskCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 *
 */
@Service
public class TaskCommentServiceImpl extends ServiceImpl<TaskCommentMapper, TaskCommentEntity>
        implements TaskCommentService {

    @Autowired
    private CommonService commonService;


    /**
     * 添加任务评论
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-13 17:58
     */
    @Override
    public Boolean saveTaskComment(TaskCommentDTO dto) {
        TaskCommentEntity entity = new TaskCommentEntity();
        LoginUser loginUser = commonService.getUserInfo();
        entity.setCreateUserId(loginUser.getUid());
        entity.setCreateUserName(loginUser.getUserName());
        entity.setTaskId(dto.getTaskId());
        entity.setComment(dto.getComment());
        return this.save(entity);
    }

    /**
     * 获取任务评论
     *
     * @param taskId
     * @return java.util.List<com.erp.model.plm.entity.TaskCommentEntity>
     * @author yl
     * @date 2022-10-13 18:07
     */

    @Override
    public List<TaskCommentEntity> getListByTaskId(String taskId) {
        LambdaQueryWrapper<TaskCommentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskCommentEntity::getTaskId, taskId);
        return this.list(queryWrapper);
    }
}




