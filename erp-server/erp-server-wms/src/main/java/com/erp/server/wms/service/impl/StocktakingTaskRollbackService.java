package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.erp.model.wms.entity.StocktakingTaskEntity;
import com.erp.server.wms.mapper.StocktakingTaskMapper;
import com.erp.server.wms.service.StocktakingTaskDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 盘点任务下推失败时独立事务回滚 DB，避免与外层 @Transactional 一并回滚导致删库失效。
 */
@Slf4j
@Service
public class StocktakingTaskRollbackService {

    @Resource
    private StocktakingTaskMapper stocktakingTaskMapper;
    @Resource
    private StocktakingTaskDetailService stocktakingTaskDetailService;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void rollbackTasksByPlanId(String planId, String planCode) {
        List<StocktakingTaskEntity> taskEntityList = stocktakingTaskMapper.selectList(
                Wrappers.lambdaQuery(StocktakingTaskEntity.class).eq(StocktakingTaskEntity::getSourceId, planId));
        if (CollUtil.isEmpty(taskEntityList)) {
            return;
        }
        List<String> mainIds = taskEntityList.stream().map(StocktakingTaskEntity::getId).collect(Collectors.toList());
        stocktakingTaskDetailService.removeByMainId(mainIds);
        stocktakingTaskMapper.delete(Wrappers.lambdaQuery(StocktakingTaskEntity.class).in(StocktakingTaskEntity::getId, mainIds));
        log.warn("下推失败回滚盘点任务：planCode={}, taskCount={}", planCode, mainIds.size());
    }
}
