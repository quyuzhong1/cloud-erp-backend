package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.SetPreTaskDTO;
import com.erp.model.plm.entity.PreTaskEntity;

import java.util.List;


/**
 *
 */
public interface PreTaskService extends IService<PreTaskEntity> {

    void savePreTask(String id, List<String> preTaskIdList);

    Boolean addPreTask(SetPreTaskDTO dto);

    Boolean removePreTask(SetPreTaskDTO dto);

    List<String> getPreTaskIdList(String taskId);
}
