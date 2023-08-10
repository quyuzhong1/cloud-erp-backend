package com.erp.server.wms.service.impl;

import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.entity.StocktakingProfitLossEntity;
import com.erp.model.wms.entity.StocktakingTaskDetailEntity;
import com.erp.model.wms.entity.StocktakingTaskEntity;
import com.erp.server.wms.mapper.StocktakingProfitLossMapper;
import com.erp.server.wms.service.StocktakingProfitLossService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.wms.service.StocktakingTaskDetailService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 盘盈盘亏单 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Service
public class StocktakingProfitLossServiceImpl extends SuperServiceImpl<StocktakingProfitLossMapper, StocktakingProfitLossEntity> implements StocktakingProfitLossService {

    @Reference
    private StocktakingTaskDetailService stocktakingTaskDetailService;

    /**
     * 盘点任务单审核通过生成盘盈盘亏单
     *
     * @param taskEntity
     * @return void
     * 如果当前存在事务就加入 如果不存在就创建一个新的
     * @author yl
     * @date 2023-08-10 11:52
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void autoCreateBill(StocktakingTaskEntity taskEntity) {
        if (Objects.isNull(taskEntity)) {
            return;
        }
        String taskCode = taskEntity.getCode();
        String taskId = taskEntity.getId();
        List<StocktakingTaskDetailEntity> taskDetailList = stocktakingTaskDetailService.listBaseByMainIds(Arrays.asList(taskId));
        //以仓库分组
        Map<String, List<StocktakingTaskDetailEntity>> warehouseMap = taskDetailList.stream().collect(Collectors.groupingBy(StocktakingTaskDetailEntity::getWarehouseId));

        for (Map.Entry<String, List<StocktakingTaskDetailEntity>> item : warehouseMap.entrySet()) {


        }

    }
}
