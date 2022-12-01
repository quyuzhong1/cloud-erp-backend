package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.server.dmp.entity.dmp.PlatformApiTaskEntity;
import com.erp.server.dmp.entity.dto.JobTaskDTO;

public interface PlatformApiTaskService extends IService<PlatformApiTaskEntity> {

    /**
     * 修改任务下次执行
     * @Author Luo_WG
     * @Date 2022/11/15 10:24
     * @param jobTaskDTO jobTaskDTO
     * @return java.lang.Boolean
     **/
    Boolean updateTaskStateById(JobTaskDTO jobTaskDTO);
}
