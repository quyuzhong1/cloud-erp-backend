package com.erp.server.dmp.pull.service.dmp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.server.dmp.entity.dmp.DmpErrorLogEntity;
import com.erp.server.dmp.pull.mapper.DmpErrorLogMapper;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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




