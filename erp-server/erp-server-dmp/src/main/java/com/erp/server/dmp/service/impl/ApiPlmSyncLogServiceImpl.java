package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.ApiPlmSyncLogDTO;
import com.erp.model.dmp.entity.ApiPlmSyncLogEntity;
import com.erp.server.dmp.mapper.ApiPlmSyncLogMapper;
import com.erp.server.dmp.service.ApiPlmSyncLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Will
 * @version 1.0

 * @date 2023/1/11 11:38
 */
@Service
public class ApiPlmSyncLogServiceImpl extends ServiceImpl<ApiPlmSyncLogMapper, ApiPlmSyncLogEntity> implements ApiPlmSyncLogService {

    @Autowired
    private ApiPlmSyncLogMapper apiPlmSyncLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insert(ApiPlmSyncLogDTO dto) {
        ApiPlmSyncLogEntity entity = new ApiPlmSyncLogEntity();
        BeanMapperUtils.copy(dto,entity);
        return this.save(entity);
    }

    @Override
    public ApiPlmSyncLogEntity find(String apiPlatFormId, Integer moduleType, String businessId) {
        LambdaQueryWrapper<ApiPlmSyncLogEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiPlmSyncLogEntity::getApiPlatformId, apiPlatFormId).eq(ApiPlmSyncLogEntity::getModuleType, moduleType)
                .eq(ApiPlmSyncLogEntity::getBusinessId, businessId).last("LIMIT 1");
        return this.baseMapper.selectOne(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void updateLog(String id, String requestParam, String msg) {
        UpdateWrapper<ApiPlmSyncLogEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(BaseEntity.ID,id).set("msg", msg)
                .set("request_param_json", requestParam);
        apiPlmSyncLogMapper.update(null, updateWrapper);
    }

}
