package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.PreTaskDTO;
import com.erp.model.plm.dto.PreTaskUpdateDTO;
import com.erp.model.plm.dto.SetPreTaskDTO;
import com.erp.model.plm.entity.PreTaskEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.vo.PreTaskListVO;
import com.erp.model.plm.vo.PreTaskVO;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;


/**
 *
 */
public interface PreTaskService extends IService<PreTaskEntity> {

    void savePreTask(String id, List<String> preTaskList, String productId);

    Boolean addPreTask(SetPreTaskDTO dto);

    Boolean removePreTask(SetPreTaskDTO dto);

    List<PreTaskVO> getPreTaskIdList(String taskId);



    void checkPreTaskFinish(List<String> taskIds);

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

    /**
     * 根据任务id获取前置任务
     * @param taskIds
     * @return
     */
    Map<String, List<PreTaskVO>> listByTaskIds(List<String> taskIds);

    /**
     * 根据任务id获取前置任务列表
     * @param taskId
     * @return
     */
    List<PreTaskListVO> ListPreTaskByTaskId(String taskId);

    /**
     * 更新
     * @param dto
     * @return
     */
    Boolean updatePreTask(List<PreTaskUpdateDTO> dto);
}
