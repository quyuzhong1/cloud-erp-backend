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

    /**
     * 删除任务  后删除前置任务
     * @param taskId
     */
    void deleteByTaskId(String taskId);
    /**
     * 查询所有子集任务
     */
    void listChildrenTask(List<String> taskIds,List<ProjectTaskEntity> list);

    
    /**
     * 批量更新前置任务
     * @author yl
     * @date 2023-02-10 16:52
     *  @param productId
     * @param taskIdList
     * @param preTaskIdList
     * @return void
     */
    void batchUpdate(String productId, List<String> taskIdList, List<String> preTaskIdList);
}
