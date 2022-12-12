package com.erp.server.dmp.pull.service.dmp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.DmpErrorLogEntity;

/**
 * 错误日志服务类
 */
public interface DmpErrorLogService extends IService<DmpErrorLogEntity> {

    /**
     * 新增错误日志
     * @Author Luo_WG
     * @Date 2022/11/14 12:19
     * @param dmpErrorLogEntity dmpErrorLogEntity
     * @return java.lang.Boolean
     **/
    Boolean add(DmpErrorLogEntity dmpErrorLogEntity);
}
