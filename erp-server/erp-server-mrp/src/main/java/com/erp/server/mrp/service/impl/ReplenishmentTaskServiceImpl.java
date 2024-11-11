package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.entity.ReplenishmentTaskEntity;
import com.erp.server.mrp.mapper.ReplenishmentTaskMapper;
import com.erp.server.mrp.service.ReplenishmentTaskService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 补货建议同步任务表 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-10-16
 */
@Service
public class ReplenishmentTaskServiceImpl extends SuperServiceImpl<ReplenishmentTaskMapper, ReplenishmentTaskEntity> implements ReplenishmentTaskService {

    @Override
    public void saveTask(List<String> suggestionIds) {
        if (CollectionUtils.isEmpty(suggestionIds)) {
            return;
        }
        List<ReplenishmentTaskEntity> taskList = list(Wrappers.<ReplenishmentTaskEntity>lambdaQuery().in(ReplenishmentTaskEntity::getReplenishmentId, suggestionIds));
        Map<String, ReplenishmentTaskEntity> taskEntityMap = taskList.stream().collect(Collectors.toMap(ReplenishmentTaskEntity::getReplenishmentId, v -> v, (o1, o2) -> o1));
        List<ReplenishmentTaskEntity> list = suggestionIds.stream()
                .map(id -> {
                    ReplenishmentTaskEntity entity = Optional.ofNullable(taskEntityMap.get(id)).orElse(new ReplenishmentTaskEntity());
                    entity.setReplenishmentId(id);
                    entity.setStatus(SyncStatusEnum.TO_BE_SYNC.getCode());
                    return entity;
                }).collect(Collectors.toList());
        saveOrUpdateBatch(list);
    }

    @Override
    public List<String> listByWaitAndReplenishment(List<String> suggestionIds) {
        List<ReplenishmentTaskEntity> taskList = list(Wrappers.<ReplenishmentTaskEntity>lambdaQuery()
                .in(ReplenishmentTaskEntity::getStatus, SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode())
                .in(ReplenishmentTaskEntity::getReplenishmentId, suggestionIds));
        return taskList.stream()
                .map(ReplenishmentTaskEntity::getReplenishmentId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(String suggestionId, String code) {
        updateStatus(suggestionId, code, null);
    }

    @Override
    public void updateStatus(String suggestionId, String code, String msg) {
        update(Wrappers.<ReplenishmentTaskEntity>lambdaUpdate().eq(ReplenishmentTaskEntity::getReplenishmentId, suggestionId)
                .set(ReplenishmentTaskEntity::getStatus, code)
                .set(ReplenishmentTaskEntity::getReturnMsg, msg));
    }
}
