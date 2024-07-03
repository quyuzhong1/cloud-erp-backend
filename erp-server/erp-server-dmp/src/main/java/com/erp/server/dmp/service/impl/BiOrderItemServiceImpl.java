package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.core.utils.MathUtil;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.BiOrderItemEntity;
import com.erp.model.dmp.entity.BiOrderItemSplitEntity;
import com.erp.server.dmp.mapper.BiOrderItemMapper;
import com.erp.server.dmp.service.BiOrderItemService;
import com.erp.server.dmp.service.BiOrderItemSplitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * 订单商品信息拆分前表 服务实现类
 * </p>
 */
@Service
@Slf4j
public class BiOrderItemServiceImpl extends ServiceImpl<BiOrderItemMapper, BiOrderItemEntity> implements BiOrderItemService {
    @Resource
    @Lazy
    private BiOrderItemSplitService biOrderItemSplitService;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private MQProducerService mQProducerService;

    @Override
    public void initItem() {
        int count = count();
        int pageSize = 500;
        int page = count / pageSize;
        for (int i = 0; i <= page; i++) {
            int finalI = i;
            CompletableFuture.runAsync(() -> {
                try {
                    List<BiOrderItemEntity> orderItemEntities = list(Wrappers.<BiOrderItemEntity>lambdaQuery()
                            .last(String.format("LIMIT %s OFFSET %s", pageSize, finalI * pageSize)));
                    for (BiOrderItemEntity entity : orderItemEntities) {
                        List<BiOrderItemSplitEntity> entities = biOrderItemSplitService.list(Wrappers.<BiOrderItemSplitEntity>lambdaQuery()
                                .eq(BiOrderItemSplitEntity::getIsSplitSku, MathUtil.ONE)
                                .eq(BiOrderItemSplitEntity::getOrderId, entity.getOrderId())
                                .eq(BiOrderItemSplitEntity::getItemId, entity.getItemId())
                                .eq(BiOrderItemSplitEntity::getPlatformSku, entity.getPlatformSku()));
                        BigDecimal costPrice = entities.stream()
                                .map(BiOrderItemSplitEntity::getCleanCostPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        entity.setOriginalCostPrice(costPrice);
                    }
                    updateBatchById(orderItemEntities);
                } catch (Exception e) {
                    log.error("更新失败 第{}页", finalI);
                }
            }, threadPoolTaskExecutor);
        }
    }

    @Override
    public Boolean add(BiOrderItemEntity dmpOrderInfoEntity, String platformSign) {
        return this.save(dmpOrderInfoEntity);
    }

    @Override
    public Boolean batchAdd(List<BiOrderItemEntity> dmpOrderInfoEntityList, String platformSign) {
        return this.saveBatch(dmpOrderInfoEntityList);
    }

    @Override
    public Boolean deleteByOrderIds(List<String> orderIds) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate().in(BiOrderItemEntity::getOrderId, orderIds).remove();
    }
}