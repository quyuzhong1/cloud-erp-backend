package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpSyncTaskEntity;
import com.erp.server.dmp.mapper.DmpSyncTaskMapper;
import com.erp.server.dmp.service.DmpSyncTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 中台同步任务表 服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@Slf4j
@Service
public class DmpSyncTaskServiceImpl extends SuperServiceImpl<DmpSyncTaskMapper, DmpSyncTaskEntity> implements DmpSyncTaskService {


    @Autowired
    private DmpSyncTaskMapper dmpSyncTaskMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateSyncInfo(String id, String syncStatus, String responseMsg) {
        UpdateWrapper<DmpSyncTaskEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(BaseEntity.ID,id).set(DmpSyncTaskEntity.LAST_SYNC_TIME, LocalDateTime.now())
                .set(DmpSyncTaskEntity.STATUS, syncStatus).set(DmpSyncTaskEntity.RETURN_MSG, responseMsg);
        dmpSyncTaskMapper.update(null, updateWrapper);
    }

    @Override
    public void saveOrUpdateDmpSyncTask(DmpSyncTaskEntity dmpSyncTaskEntity) {
        DmpSyncTaskEntity found = lambdaQuery()
                .eq(DmpSyncTaskEntity::getSouceType, dmpSyncTaskEntity.getSouceType())
                .eq(DmpSyncTaskEntity::getSourceId, dmpSyncTaskEntity.getSourceId())
                .eq(DmpSyncTaskEntity::getSourcePlatformName, dmpSyncTaskEntity.getSourcePlatformName())
                .eq(DmpSyncTaskEntity::getTargetPlatformName, dmpSyncTaskEntity.getTargetPlatformName())
                .eq(DmpSyncTaskEntity::getMqTopic, dmpSyncTaskEntity.getMqTopic())
                .eq(DmpSyncTaskEntity::getMqTag, dmpSyncTaskEntity.getMqTag())
                .one();
        //存在则修改
        if (ObjectUtil.isNotEmpty(found)) {
            dmpSyncTaskEntity.setId(found.getId());
        }
        this.saveOrUpdate(dmpSyncTaskEntity);
    }


}
