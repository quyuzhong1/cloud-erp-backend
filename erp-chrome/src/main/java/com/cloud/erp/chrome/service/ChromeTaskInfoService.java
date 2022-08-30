package com.cloud.erp.chrome.service;

import com.cloud.erp.chrome.dto.FindTaskDTO;
import com.cloud.erp.chrome.entity.ChromeTaskInfoEntity;
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
public interface ChromeTaskInfoService extends IService<ChromeTaskInfoEntity> {

    List<ChromeTaskInfoEntity> getChromeTaskList(FindTaskDTO dto);

    void updateTaskState(Integer taskId, Integer finish);
}
