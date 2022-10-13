package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.TaskCommentDTO;
import com.erp.model.plm.entity.TaskCommentEntity;

import java.util.List;


/**
 *
 */
public interface TaskCommentService extends IService<TaskCommentEntity> {

    Boolean saveTaskComment(TaskCommentDTO dto);

    List<TaskCommentEntity> getListByTaskId(String taskId);
}
