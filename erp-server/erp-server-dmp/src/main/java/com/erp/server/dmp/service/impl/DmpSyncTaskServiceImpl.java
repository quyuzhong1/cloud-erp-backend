package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpSyncTaskEntity;
import com.erp.server.dmp.mapper.DmpSyncTaskMapper;
import com.erp.server.dmp.service.DmpSyncTaskService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

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


}
