package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.finishDocsDTO;
import com.erp.model.plm.entity.TaskRefDocsEntity;

import java.util.List;

/**
 * <p>
 * 任务文档关系表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface TaskRefDocsService extends IService<TaskRefDocsEntity> {

    void batchRef(String taskId,List<finishDocsDTO> docsList);
}
