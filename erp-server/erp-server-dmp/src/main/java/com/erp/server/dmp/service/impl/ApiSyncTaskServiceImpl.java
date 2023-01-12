package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.ApiSyncTaskDTO;
import com.erp.model.dmp.entity.ApiSyncTaskEntity;
import com.erp.server.dmp.mapper.ApiSyncTaskMapper;
import com.erp.server.dmp.service.ApiSyncTaskService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 11:38
 */
@Service
public class ApiSyncTaskServiceImpl extends ServiceImpl<ApiSyncTaskMapper, ApiSyncTaskEntity> implements ApiSyncTaskService {

    @Override
    public Boolean insert(ApiSyncTaskDTO dto) {
        ApiSyncTaskEntity entity = new ApiSyncTaskEntity();
        BeanMapperUtils.copy(dto,entity);
        return this.save(entity);
    }

    @Override
    public void update(ApiSyncTaskDTO dto) {
        ApiSyncTaskEntity entity = new ApiSyncTaskEntity();
        BeanMapperUtils.copy(dto,entity);
        this.updateById(entity);
    }


    /**
     * @description: 根据平台、模块类型、业务单据查询
     * @author Will
     * @date: 2023/1/11 17:38
     * @param dto
     * @return ApiSyncTaskEntity
     */
    @Override
    public ApiSyncTaskEntity getByApiSyncTask(ApiSyncTaskDTO dto){
        LambdaQueryWrapper<ApiSyncTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiSyncTaskEntity::getApiPlatformId,dto.getApiPlatformId());
        queryWrapper.eq(ApiSyncTaskEntity::getModuleType,dto.getModuleType());
        queryWrapper.eq(ApiSyncTaskEntity::getBusinessId,dto.getBusinessId());
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }
}
