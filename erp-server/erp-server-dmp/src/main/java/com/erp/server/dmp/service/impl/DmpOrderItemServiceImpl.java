package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RedisKeyConstant;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.entity.DmpOrderItemSplitEntity;
import com.erp.server.dmp.mapper.DmpOrderItemMapper;
import com.erp.server.dmp.service.DmpOrderItemService;
import com.erp.server.dmp.service.DmpOrderItemSplitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                    List<DmpOrderItemEntity> orderItemEntities = list(Wrappers.<DmpOrderItemEntity>lambdaQuery()
                            .last(String.format("LIMIT %s OFFSET %s", pageSize, finalI * pageSize)));
                    for (DmpOrderItemEntity entity : orderItemEntities) {
                        List<DmpOrderItemSplitEntity> entities = dmpOrderItemSplitService.list(Wrappers.<DmpOrderItemSplitEntity>lambdaQuery()
                                .eq(DmpOrderItemSplitEntity::getIsSplitSku, MathUtil.ONE)
                                .eq(DmpOrderItemSplitEntity::getOrderId, entity.getOrderId())
                                .eq(DmpOrderItemSplitEntity::getItemId, entity.getItemId())
                                .eq(DmpOrderItemSplitEntity::getPlatformSku, entity.getPlatformSku()));
                        BigDecimal costPrice = entities.stream()
                                .map(DmpOrderItemSplitEntity::getCleanCostPrice)
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
    public Boolean add(DmpOrderItemEntity dmpOrderInfoEntity, String platformSign) {
        return this.save(dmpOrderInfoEntity);
    }

    @Override
    public Boolean batchAdd(List<DmpOrderItemEntity> dmpOrderInfoEntityList, String platformSign) {
        return this.saveBatch(dmpOrderInfoEntityList);
    }

    @Override
    public Boolean deleteByOrderIds(List<String> orderIds) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate().in(DmpOrderItemEntity::getOrderId, orderIds).remove();
    }
}