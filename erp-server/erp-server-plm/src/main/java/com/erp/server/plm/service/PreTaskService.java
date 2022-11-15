package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.SetPreTaskDTO;
import com.erp.model.plm.entity.PreTaskEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;

import java.util.List;


/**
 *
 */
public interface PreTaskService extends IService<PreTaskEntity> {

    void savePreTask(String id, List<String> preTaskIdList,String productId);

    Boolean addPreTask(SetPreTaskDTO dto);

    Boolean removePreTask(SetPreTaskDTO dto);

    List<String> getPreTaskIdList(String taskId);



    void checkPreTaskFinish(List<String> taskIds);

    List<ProjectTaskEntity> getPreTaskList(String taskId);

    List<PreTaskEntity> getPreTaskByProductId(String productId);

    List<PreTaskEntity> getSysPreTask(List<String> sysTaskIds);

    List<PreTaskEntity> getPreTaskListBytaskIds(List<String> taskIds);

    List<PreTaskEntity> getPreTaskListByPreTaskIds(List<String> preTaskIds);
}
