package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.CountDTO;
import com.erp.model.plm.dto.TaskChangeFileDTO;
import com.erp.model.plm.dto.TaskUploadFileDTO;
import com.erp.model.plm.entity.TaskDocsFinishEntity;

import java.util.List;

/**
 * <p>
 * 任务文档交付表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface TaskDocsFinishService extends IService<TaskDocsFinishEntity> {

    List<TaskDocsFinishEntity> getByTaskIds(List<String> taskIds);

    Boolean uploadFile(TaskUploadFileDTO dto);

    Boolean removeById(String id);

    List<CountDTO> getTaskDocsCountByProductId();

    Boolean changeFile(TaskChangeFileDTO dto);

    void checkTaskDocsUpload(List<String> allTaskIds);

    void removeByDocsIds(List<String> existDocsIds);
}
