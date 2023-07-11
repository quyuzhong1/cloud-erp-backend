package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.ApiSyncTaskDTO;
import com.erp.model.dmp.entity.ApiSyncTaskEntity;
import com.erp.server.dmp.mapper.ApiSyncTaskMapper;
import com.erp.server.dmp.service.ApiSyncTaskService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 推送任务业务方法
 * @date 2023/7/10 16:07
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
    public Boolean update(ApiSyncTaskDTO dto) {
        ApiSyncTaskEntity entity = new ApiSyncTaskEntity();
        BeanMapperUtils.copy(dto,entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean addOrUpdateApiSyncTask (ApiSyncTaskDTO apiSyncTaskDTO) {
        List<ApiSyncTaskEntity> list = this.listByApiSyncTaskDTO(apiSyncTaskDTO);
        //更新推送任务
        if (CollectionUtils.isEmpty(list)) {
            this.insert(apiSyncTaskDTO);
        } else {
            apiSyncTaskDTO.setId(list.get(0).getId());
            apiSyncTaskDTO.setRetryCount(list.get(0).getRetryCount().intValue() + 1);
            this.update(apiSyncTaskDTO);
        }
        return Boolean.TRUE;
    }


    @Override
    public List<ApiSyncTaskEntity> listByApiSyncTaskDTO(ApiSyncTaskDTO apiSyncTaskDTO) {
        if (ObjectUtils.isEmpty(apiSyncTaskDTO)) {
            return Collections.EMPTY_LIST;
        }
        List<ApiSyncTaskEntity> list = lambdaQuery()
                .eq(StringUtils.isNotBlank(apiSyncTaskDTO.getBusinessId()), ApiSyncTaskEntity::getBusinessId, apiSyncTaskDTO.getBusinessId())
                .eq(StringUtils.isNotBlank(apiSyncTaskDTO.getApiPlatformId()), ApiSyncTaskEntity::getApiPlatformId, apiSyncTaskDTO.getApiPlatformId())
                .eq(StringUtils.isNotBlank(apiSyncTaskDTO.getApiAuthId()), ApiSyncTaskEntity::getApiAuthId, apiSyncTaskDTO.getApiAuthId())
                .eq(ObjectUtils.isNotEmpty(apiSyncTaskDTO.getModuleType()), ApiSyncTaskEntity::getModuleType, apiSyncTaskDTO.getModuleType())
                .list();
        return list;
    }
}
