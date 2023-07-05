package com.erp.server.plm.service;
import com.erp.model.plm.dto.DocHistoryDTO;
import com.erp.model.plm.entity.TaskDocHistoryEntity;
import com.common.business.service.SuperService;
import com.erp.model.plm.entity.TaskDocsFinishEntity;

import java.util.List;


/**
 * <p>
 * 任务文档历史表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
 */
public interface TaskDocHistoryService extends SuperService<TaskDocHistoryEntity> {


    /**
     * 添加历史文档
     * @author yl
     * @date 2023-06-25 9:02
     * @param oldDocs
     * @return void
     */
    void addHistory(TaskDocsFinishEntity oldDocs);

    /**
     * 获取文档历史记录
     * @author yl
     * @date 2023-06-25 10:00
     * @param finishDocsId
     * @return java.util.List<com.erp.model.plm.dto.DocHistoryDTO.InfoDTO>
     */
    List<DocHistoryDTO.InfoDTO> historyList(String finishDocsId);

    /**
     * 任务审核通过
     * @author yl
     * @date 2023-06-25 10:59
     * @param taskId
     * @return void
     */
    void updateChangeResult(String taskId);
    /**
     * 根据任务ids 更改文档历史
     * @author yl
     * @date 2023-07-04 16:58
     * @param isChangeDocsTaskIdList
     * @return void
     */
    void updateChangeResultByTaskIds(List<String> isChangeDocsTaskIdList);

    
    /**
     * 根据任务ids 查询历史文档
     * @author yl
     * @date 2023-07-04 20:20
     * @param taskIdList
     * @return java.util.List<com.erp.model.plm.entity.TaskDocHistoryEntity>
     */
    List<TaskDocHistoryEntity> listByTaskIdList(List<String> taskIdList);

    
    /**
     * 批量提交
     * @author yl
     * @date 2023-07-05 19:50
     * @param resultList
     * @return void
     */
    void addBatchHistory(List<TaskDocsFinishEntity> resultList);
}
