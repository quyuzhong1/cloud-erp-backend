package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.ApiSyncTaskDTO;
import com.erp.model.dmp.entity.ApiSyncTaskEntity;
import com.erp.server.dmp.mapper.ApiSyncTaskMapper;
import com.erp.server.dmp.service.ApiSyncTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Will
 * @version 1.0
 * @description: 推送任务业务方法
 * @date 2023/7/10 16:07
 */
@Service
public class ApiSyncTaskServiceImpl extends ServiceImpl<ApiSyncTaskMapper, ApiSyncTaskEntity> implements ApiSyncTaskService {


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insert(ApiSyncTaskDTO dto) {
        ApiSyncTaskEntity entity = new ApiSyncTaskEntity();
        BeanMapperUtils.copy(dto,entity);
        return this.save(entity);
    }
}
