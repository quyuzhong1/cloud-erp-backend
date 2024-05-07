package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.DmpOrderItemGroup;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.entity.DmpOrderOriginalItemEntity;
import com.erp.server.dmp.mapper.DmpOrderOriginalItemMapper;
import com.erp.server.dmp.service.DmpOrderItemService;
import com.erp.server.dmp.service.DmpOrderOriginalItemService;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单商品信息拆分前表 服务实现类
 * </p>
 */
@Service
public class DmpOrderOriginalItemServiceImpl extends ServiceImpl<DmpOrderOriginalItemMapper, DmpOrderOriginalItemEntity> implements DmpOrderOriginalItemService {
    @Resource
    private DmpOrderItemService dmpOrderItemService;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public void initOriginalItem() {
        //处理未拆分的数据
        handleNotSplit();
        // 处理已拆分的数据
        handleSplit();
    }

    private void handleSplit() {
        List<DmpOrderItemGroup> dmpOrderItemGroups = dmpOrderItemService.listByGroup();
        List<List<DmpOrderItemGroup>> partition = Lists.partition(dmpOrderItemGroups, 500);
        for (List<DmpOrderItemGroup> orderItemGroups : partition) {
            CompletableFuture.runAsync(() -> {
                for (DmpOrderItemGroup group : orderItemGroups) {
                    List<DmpOrderItemEntity> entities = dmpOrderItemService.list(Wrappers.<DmpOrderItemEntity>lambdaQuery()
                            .eq(DmpOrderItemEntity::getIsSplitSku, MathUtil.ONE)
                            .eq(DmpOrderItemEntity::getOrderId, group.getOrderId())
                            .eq(DmpOrderItemEntity::getItemId, group.getItemId())
                            .eq(DmpOrderItemEntity::getPlatformSku, group.getPlatformSku())
                    );
                    DmpOrderItemEntity entity = entities.stream().findFirst()
                            .orElse(null);
                    if (!ObjectUtils.isEmpty(entity)){
                        BigDecimal costPrice = entities.stream()
                                .map(DmpOrderItemEntity::getCleanCostPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        DmpOrderOriginalItemEntity item = new DmpOrderOriginalItemEntity();
                        BeanUtils.copyProperties(entity, item);
                        item.setOriginalQuantity(item.getPlatformQuantity());
                        item.setOriginalAmountAfter(item.getAmountAfter());
                        item.setOriginalCostPrice(costPrice);
                        boolean update = saveOrUpdate(item);
                        if (update) {
                            entities.forEach(e -> e.setOriginalItemId(item.getId()));
                            dmpOrderItemService.updateBatchById(entities);
                        }
                    }
                }
            }, threadPoolTaskExecutor);
        }
    }

    private void handleNotSplit() {
        int count = dmpOrderItemService.count(Wrappers.<DmpOrderItemEntity>lambdaQuery()
                .eq(DmpOrderItemEntity::getIsSplitSku, MathUtil.TWO));
        int pageSize = 500;
        int page = count / pageSize;
        for (int i = 0; i <= page; i++) {
            int finalI = i;
            CompletableFuture.runAsync(() -> {
                List<DmpOrderItemEntity> orderItemEntities = dmpOrderItemService.list(Wrappers.<DmpOrderItemEntity>lambdaQuery()
                        .eq(DmpOrderItemEntity::getIsSplitSku, MathUtil.TWO)
                        .last(String.format("LIMIT %s OFFSET %s", pageSize, finalI * pageSize))
                );
                List<DmpOrderOriginalItemEntity> originalItems = orderItemEntities.stream()
                                .map(item ->{
                                    DmpOrderOriginalItemEntity entity = new DmpOrderOriginalItemEntity();
                                    BeanUtils.copyProperties(item, entity);
                                    entity.setOriginalQuantity(item.getQuantity());
                                    entity.setOriginalAmountAfter(item.getAmountAfter());
                                    entity.setOriginalCostPrice(item.getCleanCostPrice());
                                    return entity;
                                }).collect(Collectors.toList());
                boolean update = saveOrUpdateBatch(originalItems);
                if (update) {
                    orderItemEntities.forEach(item -> item.setOriginalItemId(item.getId()));
                    dmpOrderItemService.updateBatchById(orderItemEntities);
                }
            }, threadPoolTaskExecutor);
        }
    }
}