package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.ApiPlmSyncLogDTO;
import com.erp.model.dmp.entity.ApiPlmSyncLogEntity;
import com.erp.server.dmp.mapper.ApiPlmSyncLogMapper;
import com.erp.server.dmp.service.ApiPlmSyncLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 11:38
 */
@Service
public class ApiPlmSyncLogServiceImpl extends ServiceImpl<ApiPlmSyncLogMapper, ApiPlmSyncLogEntity> implements ApiPlmSyncLogService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insert(ApiPlmSyncLogDTO dto) {
        ApiPlmSyncLogEntity entity = new ApiPlmSyncLogEntity();
        BeanMapperUtils.copy(dto,entity);
        return this.save(entity);
    }

}
