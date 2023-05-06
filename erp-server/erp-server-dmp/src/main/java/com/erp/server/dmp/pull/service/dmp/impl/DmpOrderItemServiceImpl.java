package com.erp.server.dmp.pull.service.dmp.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.message.constant.RedisKeyConstant;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.plm.dto.NewProductDTO;
import com.erp.server.dmp.pull.mapper.DmpOrderItemMapper;
import com.erp.server.dmp.pull.service.dmp.DmpOrderItemService;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单商品详细信息
 */
@Service
public class DmpOrderItemServiceImpl extends ServiceImpl<DmpOrderItemMapper, DmpOrderItemEntity>
        implements DmpOrderItemService {

    @Resource
    private MQProducerService mQProducerService;


    @Resource
    private RedisUtil redisUtil;

    /**
     * 添加订单商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpOrderInfoEntity 订单商品信息集合
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean add(DmpOrderItemEntity dmpOrderInfoEntity) {
        return this.save(dmpOrderInfoEntity);
    }

    /**
     * 批量添加订单商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpOrderInfoEntityList 订单商品信息集合
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean batchAdd(List<DmpOrderItemEntity> dmpOrderInfoEntityList) {
        return this.saveBatch(dmpOrderInfoEntityList, 500);
    }

    /**
     * 根据erp平台商品id查询订单商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 22:11
     * @param erpOrderItemId erp平台商品id
     * @return com.erp.model.dmp.entity.DmpOrderItemEntity
     **/
    @Override
    public DmpOrderItemEntity getByErpOrderItemId(String erpOrderItemId) {
        LambdaQueryWrapper<DmpOrderItemEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpOrderItemEntity::getErpOrderItemId, erpOrderItemId);
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据订单表id查询订单商品信息
     * @Author Luo_WG
     * @Date 2022/12/14 16:10
     * @param orderId 订单表id
     * @return java.util.List<com.erp.model.dmp.entity.DmpOrderItemEntity>
     **/
    @Override
    public List<DmpOrderItemEntity> getByOrderId(String orderId) {
        LambdaQueryWrapper<DmpOrderItemEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpOrderItemEntity::getOrderId, orderId);
        return this.list(lambdaQueryWrapper);
    }

    /**
     * 根据erp平台商品id修改订单商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpOrderItemEntity 订单商品信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateOrderItemByErpOrderItemId(DmpOrderItemEntity dmpOrderItemEntity) {
        LambdaQueryWrapper<DmpOrderItemEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpOrderItemEntity::getErpOrderItemId, dmpOrderItemEntity.getErpOrderItemId());
        return this.update(dmpOrderItemEntity, lambdaQueryWrapper);
    }

    /**
     * 校验订单商品信息在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOrderItem(List<DmpOrderItemEntity> orderItem, LocalDate platformCreateTime) {
        List<DmpOrderItemEntity> insertList = new ArrayList<>();
        for (DmpOrderItemEntity orderItemBean : orderItem) {
            Object skuListing = redisUtil.hget(RedisKeyConstant.SKU_LISTING_TIME, orderItemBean.getSkuNo());
            if (ObjectUtil.isEmpty(skuListing)) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("skuNo", orderItemBean.getSkuNo());
                resultMap.put("listingTime", platformCreateTime);
                mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.PRODUCT_LISTING_UPDATE_TAG.getName(), resultMap, orderItemBean.getId());
            } else {
                // 新品标识 1为新品 0 为非新品
                if (platformCreateTime.getYear() == LocalDate.now().getYear()) {
                    orderItemBean.setNewSign(1);
                } else {
                    orderItemBean.setNewSign(2);
                }
            }
            DmpOrderItemEntity dmpOrderItemEntity = this.getByErpOrderItemId(orderItemBean.getErpOrderItemId());
            if (null != dmpOrderItemEntity) {
                //如果数据有变动需要更新数据库订单商品信息
                if (!dmpOrderItemEntity.toString().equals(orderItemBean.toString())) {
                    orderItemBean.setId(dmpOrderItemEntity.getId());
                    updateById(orderItemBean);
                }
            } else {
                insertList.add(orderItemBean);
            }
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            saveBatch(insertList);
        }
    }

    /**
     * 同步PLM的到货时间更新新老品
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNewSign(Map<String, List<NewProductDTO>> dto) {
        List<NewProductDTO> listingNotNullList = dto.get("listingNotNullList");
        listingNotNullList.forEach(req -> {
            LocalDate date = req.getNewListingTime();
            String year = String.valueOf(date.getYear());
            List<String> ids = baseMapper.getItemIdBySkuAndYear(year, req.getSkuNo());

            List<List<String>> partition = Lists.partition(ids, 200);
            partition.forEach(obj -> {
                LambdaUpdateWrapper<DmpOrderItemEntity> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(DmpOrderItemEntity::getNewSign, 1);
                updateWrapper.in(DmpOrderItemEntity::getId, obj);
                this.update(updateWrapper);
            });
        });
/*
        List<NewProductDTO> listingNullList = dto.get("listingNullList");

        List<String> skuNoList = listingNullList.stream().map(NewProductDTO::getSkuNo).collect(Collectors.toList());
        List<Map<String, String>> orderListingTime1 = baseMapper.getOrderListingTime(skuNoList);

        for (Map<String, String> stringStringMap : orderListingTime1) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("skuNo", stringStringMap.get("skuno"));
            resultMap.put("listingTime", stringStringMap.get("listingtime"));
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.PRODUCT_LISTING_UPDATE_TAG.getName(), resultMap, UUID.randomUUID().toString());
        }
*/

      /*  String year = "";
        String skuNo = String.valueOf(dto.getSkuNo());
        if (dto.getNewListingTime() != null) {
            LocalDate date = dto.getNewListingTime();
            year = String.valueOf(date.getYear());

        }*//* else if (map.get("pastListingTime") != null) {
            LocalDate date = LocalDate.parse(String.valueOf(map.get("pastListingTime")), fmt);
            year = String.valueOf(date.getYear());
            updateWrapper.set(DmpOrderItemEntity::getNewSign, 2);
        }*//* else {
            Object sku = redisUtil.hget(RedisKeyConstant.SKU_NOT_LISTING_TIME, skuNo);
            if (ObjectUtil.isNotEmpty(sku)) {
                return;
            }
            LocalDateTime orderListingTime = baseMapper.getOrderListingTime(dto.getSkuNo());
            if (orderListingTime == null) {
                redisUtil.hset(RedisKeyConstant.SKU_NOT_LISTING_TIME, skuNo, null, 24 * 3600);
                return;
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("skuNo", dto.getSkuNo());
            resultMap.put("listingTime", orderListingTime.toLocalDate());
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.PRODUCT_LISTING_UPDATE_TAG.getName(), resultMap, dto.getId());
        }
        if (StringUtils.isBlank(year)) {
            return;
        }
        List<String> ids = baseMapper.getItemIdBySkuAndYear(year, skuNo);
        List<List<String>> partition = Lists.partition(ids, 200);
        partition.forEach(req -> {
            LambdaUpdateWrapper<DmpOrderItemEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(DmpOrderItemEntity::getNewSign, 1);
            updateWrapper.in(DmpOrderItemEntity::getId, req);
            this.update(updateWrapper);
        });*/
    }

    /**
     * 同步PLM的到货时间更新新老品
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void getProductListing(Map<String, List<NewProductDTO>> dto) {
        List<NewProductDTO> listingNullList = dto.get("listingNullList");
        List<String> skuNoList = listingNullList.stream().map(NewProductDTO::getSkuNo).collect(Collectors.toList());
        List<Map<String, String>> orderListingTime1 = baseMapper.getOrderListingTime(skuNoList);
        for (Map<String, String> stringStringMap : orderListingTime1) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("skuNo", stringStringMap.get("skuno"));
            resultMap.put("listingTime", stringStringMap.get("listingtime"));
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.PRODUCT_LISTING_UPDATE_TAG.getName(), resultMap, UUID.randomUUID().toString());
        }
    }
}




