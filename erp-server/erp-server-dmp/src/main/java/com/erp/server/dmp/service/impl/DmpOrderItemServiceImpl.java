package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.DmpOrderItemGroup;
import com.erp.model.dmp.entity.DmpOrderItemSplitEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.server.dmp.mapper.DmpOrderItemMapper;
import com.erp.server.dmp.service.DmpOrderItemSplitService;
import com.erp.server.dmp.service.DmpOrderItemService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单商品信息拆分前表 服务实现类
 * </p>
 */
@Service
@Slf4j
public class DmpOrderItemServiceImpl extends ServiceImpl<DmpOrderItemMapper, DmpOrderItemEntity> implements DmpOrderItemService {
    @Resource
    private DmpOrderItemSplitService dmpOrderItemSplitService;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public void initItem() {
        //处理未拆分的数据
        handleNotSplit();
        // 处理已拆分的数据
        handleSplit();
    }

    private void handleSplit() {
        List<DmpOrderItemGroup> dmpOrderItemGroups = dmpOrderItemSplitService.listByGroup();
        List<List<DmpOrderItemGroup>> partition = Lists.partition(dmpOrderItemGroups, 500);
        for (List<DmpOrderItemGroup> orderItemGroups : partition) {
            CompletableFuture.runAsync(() -> {
                List<DmpOrderItemEntity> itemEntities = new ArrayList<>();
                List<DmpOrderItemSplitEntity> splitEntities = new ArrayList<>();
                for (DmpOrderItemGroup group : orderItemGroups) {
                    List<DmpOrderItemSplitEntity> entities = dmpOrderItemSplitService.list(Wrappers.<DmpOrderItemSplitEntity>lambdaQuery()
                            .eq(DmpOrderItemSplitEntity::getIsSplitSku, MathUtil.ONE)
                            .eq(DmpOrderItemSplitEntity::getOrderId, group.getOrderId())
                            .eq(DmpOrderItemSplitEntity::getItemId, group.getItemId())
                            .eq(DmpOrderItemSplitEntity::getPlatformSku, group.getPlatformSku())
                    );
                    DmpOrderItemSplitEntity entity = entities.stream().findFirst()
                            .orElse(null);
                    if (!ObjectUtils.isEmpty(entity)){
                        BigDecimal costPrice = entities.stream()
                                .map(DmpOrderItemSplitEntity::getCleanCostPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        DmpOrderItemEntity item = new DmpOrderItemEntity();
                        BeanUtils.copyProperties(entity, item);
                        item.setOriginalQuantity(item.getPlatformQuantity());
                        item.setOriginalAmountAfter(item.getAmountAfter());
                        item.setOriginalCostPrice(costPrice);
                        itemEntities.add(item);
                            entities.forEach(e -> e.setOriginalItemId(item.getId()));
                            splitEntities.addAll(entities);
                    }
                    saveOrUpdateBatch(itemEntities);
                    dmpOrderItemSplitService.updateBatchById(splitEntities);
                }
            }, threadPoolTaskExecutor);
        }
    }

    private void handleNotSplit() {
        int count = dmpOrderItemSplitService.count(Wrappers.<DmpOrderItemSplitEntity>lambdaQuery()
                .eq(DmpOrderItemSplitEntity::getIsSplitSku, MathUtil.TWO));
        int pageSize = 500;
        int page = count / pageSize;
        for (int i = 0; i <= page; i++) {
            int finalI = i;
            CompletableFuture.runAsync(() -> {
                try {
                    List<DmpOrderItemSplitEntity> orderItemEntities = dmpOrderItemSplitService.list(Wrappers.<DmpOrderItemSplitEntity>lambdaQuery()
                            .eq(DmpOrderItemSplitEntity::getIsSplitSku, MathUtil.TWO)
                            .last(String.format("LIMIT %s OFFSET %s", pageSize, finalI * pageSize)));
                    List<DmpOrderItemEntity> originalItems = orderItemEntities.stream()
                                    .map(item ->{
                                        DmpOrderItemEntity entity = new DmpOrderItemEntity();
                                        BeanUtils.copyProperties(item, entity);
                                        entity.setOriginalQuantity(item.getQuantity());
                                        entity.setOriginalAmountAfter(item.getAmountAfter());
                                        entity.setOriginalCostPrice(item.getCleanCostPrice());
                                        return entity;
                                    }).collect(Collectors.toList());
                    boolean update = saveOrUpdateBatch(originalItems);
                    if (update) {
                        orderItemEntities.forEach(item -> item.setOriginalItemId(item.getId()));
                        dmpOrderItemSplitService.updateBatchById(orderItemEntities);
                    }
                } catch (Exception e) {
                    log.error("第{}页数据处理失败，原因是：{}", finalI, e.getMessage(), e);
                }
            }, threadPoolTaskExecutor);
        }
    }
}