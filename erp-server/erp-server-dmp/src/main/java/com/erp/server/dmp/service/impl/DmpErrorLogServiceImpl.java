package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.server.dmp.pull.mapper.DmpErrorLogMapper;
import com.erp.server.dmp.service.DmpErrorLogService;
import org.springframework.stereotype.Service;

/**
 * 错误日志服务类
 */
@Service
public class DmpErrorLogServiceImpl extends ServiceImpl<DmpErrorLogMapper, DmpErrorLogEntity>
    implements DmpErrorLogService {

    /**
     * 新增错误日志
     * @Author Luo_WG
     * @Date 2022/11/14 12:19
     * @param dmpErrorLogEntity dmpErrorLogEntity
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean add(DmpErrorLogEntity dmpErrorLogEntity) {
        return this.save(dmpErrorLogEntity);
    }
}




