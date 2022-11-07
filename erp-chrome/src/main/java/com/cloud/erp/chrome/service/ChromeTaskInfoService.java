package com.cloud.erp.chrome.service;

import com.cloud.erp.chrome.dto.FindTaskDTO;
import com.cloud.erp.chrome.entity.ScheduleTaskEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yl
 * @since 2022-08-25
 */
public interface ChromeTaskInfoService extends IService<ScheduleTaskEntity> {

    /**
     * 查询任务列表
     * @param dto   查询参数
     * @return  任务列表
     */
    List<ScheduleTaskEntity> getChromeTaskList(FindTaskDTO dto);

    /**
     * 更新任务状态
     * @param taskId 任务Id
     * @param status 状态
     */
    void updateTaskState(Integer taskId, Integer status);
    /**
     * 更新任务状态
     * @param taskId 任务Id
     * @param status 状态
     * @param remark 备注
     */
    void updateTaskState(Integer taskId, Integer status,String remark);

}
