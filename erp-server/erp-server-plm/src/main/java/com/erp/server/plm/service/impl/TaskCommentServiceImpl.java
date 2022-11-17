package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.TaskCommentDTO;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskCommentEntity;
import com.erp.server.plm.mapper.TaskCommentMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.NoticeMessageService;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.TaskCommentService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


/**
 *
 */
@Service
public class TaskCommentServiceImpl extends ServiceImpl<TaskCommentMapper, TaskCommentEntity>
        implements TaskCommentService {

    @Autowired
    private CommonService commonService;

    @Autowired
    @Lazy
    private NoticeMessageService noticeMessageService;

    @Autowired
    private ProjectTaskService projectTaskService;


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
        String taskId = dto.getTaskId();
        TaskCommentEntity entity = new TaskCommentEntity();
        LoginUser loginUser = commonService.getUserInfo();
        entity.setCreateUserId(loginUser.getUid());
        entity.setCreateUserName(loginUser.getUserName());
        entity.setTaskId(taskId);
        entity.setComment(dto.getComment());
        ProjectTaskEntity taskEntity = projectTaskService.getById(taskId);
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        Boolean flag = this.save(entity);
        //保存成功 发送评论提醒
        if (flag) {
            noticeMessageService.remindRemarkNotice(taskEntity.getProductId(),taskId,dto.getComment());
        }
        return flag;
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
        queryWrapper.orderByDesc(TaskCommentEntity::getCreateTime);
        return this.list(queryWrapper);
    }


    /**
     * 批量保存
     *
     * @param taskCommentList
     * @return void
     * @author yl
     * @date 2022-10-25 18:11
     */
    @Override
    public void batchSaveTaskComment(List<TaskCommentEntity> taskCommentList) {
        if (CollectionUtils.isNotEmpty(taskCommentList)) {
            this.saveBatch(taskCommentList);
        }
    }
}




