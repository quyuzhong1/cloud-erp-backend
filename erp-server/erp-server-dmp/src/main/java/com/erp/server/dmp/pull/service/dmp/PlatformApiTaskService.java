package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.dmp.dto.JobTaskDTO;

public interface PlatformApiTaskService extends IService<PlatformApiTaskEntity> {

    /**
     * 修改任务下次执行
     *
     * @param jobTaskDTO jobTaskDTO
     * @param type
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/15 10:24
     **/
    Boolean updateTaskStateById(JobTaskDTO jobTaskDTO, Integer type);

    /**
     * 获取需要执行的任务
     * @param taskName
     * @return
     */
    PlatformApiTaskEntity getByApiCode(String taskName);
}
