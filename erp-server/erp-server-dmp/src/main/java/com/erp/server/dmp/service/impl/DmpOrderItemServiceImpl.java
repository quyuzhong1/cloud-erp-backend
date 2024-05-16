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

    /**
     * 校验订单商品信息在中台是否存在，存在就修改不存在则新增
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOrderItem(List<DmpOrderItemEntity> orderItem, LocalDate platformCreateTime, String platformSign) {
        List<DmpOrderItemEntity> insertList = new ArrayList<>();
        for (DmpOrderItemEntity orderItemBean : orderItem) {
            if(StrUtil.isBlank(orderItemBean.getSkuNo())){
                continue;
            }
            Object skuListing = redisUtil.hget(RedisKeyConstant.SKU_LISTING_TIME, orderItemBean.getSkuNo());
            if (ObjectUtil.isEmpty(skuListing)) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("skuNo", orderItemBean.getSkuNo());
                resultMap.put("listingTime", platformCreateTime);
                mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.PRODUCT_LISTING_UPDATE_TAG.getName(), resultMap, orderItemBean.getId());
            } else {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate parse = LocalDate.parse(skuListing.toString(), dateTimeFormatter);
                // 新品标识 1为新品 0 为非新品
                if (platformCreateTime.getYear() == parse.getYear()) {
                    orderItemBean.setNewSign(1);
                } else {
                    orderItemBean.setNewSign(0);
                }
            }


           /* DmpOrderItemEntity dmpOrderItemEntity = this.getByErpOrderItemId(orderItemBean.getErpOrderItemId());
            if (null != dmpOrderItemEntity) {
                //如果数据有变动需要更新数据库订单商品信息
                if (!dmpOrderItemEntity.toString().equals(orderItemBean.toString())) {
                    orderItemBean.setId(dmpOrderItemEntity.getId());
//                    updateById(orderItemBean);
                    baseMapper.deleteById(dmpOrderItemEntity.getId());
                    updateList.add(orderItemBean);
                }
            } else {
                insertList.add(orderItemBean);
            }*/
            insertList.add(orderItemBean);
        }

        //删除原数据
        List<String> orderIds = orderItem.stream().map(req -> req.getOrderId()).distinct().collect(Collectors.toList());
        this.deleteByOrderIds(orderIds);

        //新增新数据
        if (CollectionUtil.isNotEmpty(insertList)) {
            //拆分sku并保存
            List<DmpOrderItemSplitEntity> itemEntityList = BeanMapper.copyList(insertList, DmpOrderItemSplitEntity.class);
            dmpOrderItemSplitService.batchAdd(itemEntityList, platformSign);

            //保存未拆分数据
            this.batchAdd(insertList, platformSign);
        }
    }
}