package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.TaskCommentDTO;
import com.erp.model.plm.entity.TaskCommentEntity;

import java.util.List;


/**
 *
 */
public interface TaskCommentService extends IService<TaskCommentEntity> {

    
    /**
     * 保存任务评论
     * @author yl
     * @date 2023-06-20 15:35
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean saveTaskComment(TaskCommentDTO.AddDTO dto);

    List<TaskCommentEntity> getListByTaskId(String taskId);

    void batchSaveTaskComment(List<TaskCommentEntity> taskCommentList);
}
