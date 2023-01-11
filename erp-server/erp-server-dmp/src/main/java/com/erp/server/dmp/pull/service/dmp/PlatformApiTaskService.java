package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.dmp.dto.JobTaskDTO;

public interface PlatformApiTaskService extends IService<PlatformApiTaskEntity> {

    /**
     * 修改任务下次执行
     * @Author Luo_WG
     * @Date 2022/11/15 10:24
     * @param jobTaskDTO jobTaskDTO
     * @return java.lang.Boolean
     **/
    Boolean updateTaskStateById(JobTaskDTO jobTaskDTO);

    /**
     * 获取需要执行的任务
     * @param taskName
     * @return
     */
    PlatformApiTaskEntity getByApiCode(String taskName);
}
