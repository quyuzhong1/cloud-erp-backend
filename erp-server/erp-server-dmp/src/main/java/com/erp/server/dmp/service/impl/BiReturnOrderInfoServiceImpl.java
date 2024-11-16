package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.entity.BiReturnOrderInfoEntity;
import com.erp.model.dmp.entity.BiReturnOrderItemEntity;
import com.erp.server.dmp.pull.mapper.BiReturnOrderInfoMapper;
import com.erp.server.dmp.service.BiOrderInfoService;
import com.erp.server.dmp.service.BiReturnOrderInfoService;
import com.erp.server.dmp.service.BiReturnOrderItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 退货订单服务
 */
@Slf4j
@Service
public class BiReturnOrderInfoServiceImpl extends ServiceImpl<BiReturnOrderInfoMapper, BiReturnOrderInfoEntity>
    implements BiReturnOrderInfoService {

    private final Integer pageSize = 100;

    private static Integer pageIndex = 1;

    @Resource
    @Lazy
    private BiOrderInfoService biOrderInfoService;

    @Resource
    private BiReturnOrderItemService biReturnOrderItemService;

    /**
     * 添加退货订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpRefundInfoEntity 退货订单信息
     * @return java.lang.Boolean
     **/
    @Override
    public String add(BiReturnOrderInfoEntity dmpRefundInfoEntity) {
        this.save(dmpRefundInfoEntity);
        return dmpRefundInfoEntity.getId();
    }

    /**
     * 根据平台订单id查询退货订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param returnOrderInfoEntity
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    @Override
    public BiReturnOrderInfoEntity getOrderByPlatformOrderId(BiReturnOrderInfoEntity returnOrderInfoEntity) {
        LambdaQueryWrapper<BiReturnOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(BiReturnOrderInfoEntity::getReturnCode, returnOrderInfoEntity.getReturnCode());
        lambdaQueryWrapper.eq(StrUtil.isNotBlank(returnOrderInfoEntity.getPlatformOrderId()), BiReturnOrderInfoEntity::getPlatformOrderId, returnOrderInfoEntity.getPlatformOrderId());
        lambdaQueryWrapper.last("limit 1");
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据订单id查询退货订单信息
     * @Author Luo_WG
     * @Date 2022/12/14 19:10
     * @param platformOrderId
     * @return com.erp.model.dmp.entity.DmpReturnOrderInfoEntity
     **/
    @Override
    public BiReturnOrderInfoEntity getOrderByOrderId(String platformOrderId) {
        LambdaQueryWrapper<BiReturnOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(BiReturnOrderInfoEntity::getPlatformOrderId, platformOrderId);
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据平台订单id修改退货订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param biReturnOrderInfoEntity 退货订单信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateOrderByPlatformOrderId(BiReturnOrderInfoEntity biReturnOrderInfoEntity) {
        LambdaQueryWrapper<BiReturnOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(BiReturnOrderInfoEntity::getReturnCode, biReturnOrderInfoEntity.getReturnCode());
        lambdaQueryWrapper.eq(BiReturnOrderInfoEntity::getPlatformOrderId, biReturnOrderInfoEntity.getPlatformOrderId());
        return this.update(biReturnOrderInfoEntity, lambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String checkOrder(BiReturnOrderInfoEntity returnOrderInfoEntity) {
        String returnOrderId = "";
        BiReturnOrderInfoEntity biReturnOrderInfoEntity = this.getOrderByPlatformOrderId(returnOrderInfoEntity);
        if(null != biReturnOrderInfoEntity && returnOrderInfoEntity.getIsDeleted()){
            return returnOrderId;
        }
        if (null != biReturnOrderInfoEntity) {
            //如果数据有变动需要更新数据库订单信息
            if (!biReturnOrderInfoEntity.toString().equals(returnOrderInfoEntity.toString())) {
                returnOrderInfoEntity.setId(biReturnOrderInfoEntity.getId());
                updateById(returnOrderInfoEntity);
            }
            returnOrderId = biReturnOrderInfoEntity.getId();
        } else {
            returnOrderId = add(returnOrderInfoEntity);
        }
        if(StrUtil.isBlank(returnOrderId)){
            throw new RuntimeException("DmpOrderInfoServiceImpl>>>checkOrder>>>销售订单保存失败");
        }
        List<BiReturnOrderItemEntity> itemList = returnOrderInfoEntity.getItemList();
        if (CollectionUtil.isEmpty(itemList)){
            return returnOrderId;
        }
        String orderId = returnOrderId;
        List<BiReturnOrderItemEntity> biReturnOrderItemEntityList = itemList.stream().peek(entity -> entity.setReturnOrderId(orderId)).collect(Collectors.toList());
        log.debug("BI退款明细：{}" , JSON.toJSONString(biReturnOrderItemEntityList));
        biReturnOrderItemService.checkOrderItem(itemList, returnOrderInfoEntity.getPlatformSign());
        return returnOrderId;
    }

    /**
     * 清洗退货订单数据
     * @Author Luo_WG
     * @Date 2022/12/14 19:15
     **/
    @Override
    public void cleanReturnOrderTask(){
        List<BiReturnOrderInfoEntity> dmpReturnOrderInfoEntities = baseMapper.cleanReturnOrderList(pageSize, pageIndex);
        if (dmpReturnOrderInfoEntities == null || dmpReturnOrderInfoEntities.isEmpty()) {
            pageIndex = 1;
            return;
        }

        for (BiReturnOrderInfoEntity biReturnOrderInfoEntity : dmpReturnOrderInfoEntities) {
            LambdaUpdateWrapper<BiReturnOrderInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
            BiOrderInfoEntity biOrderInfoEntity = biOrderInfoService.getOrderBySalesRecordNumber(biReturnOrderInfoEntity.getSalesRecordNumber(), biReturnOrderInfoEntity.getPlatformOrderId(), biReturnOrderInfoEntity.getPlatformSign());
            if (biOrderInfoEntity != null) {
                updateWrapper.set(ObjectUtil.isNotEmpty(biOrderInfoEntity.getPlatformCreateTime()), BiReturnOrderInfoEntity::getOrderTime, biOrderInfoEntity.getPlatformCreateTime());
                updateWrapper.set(StrUtil.isNotBlank(biOrderInfoEntity.getChargeId()), BiReturnOrderInfoEntity::getChargeId, biOrderInfoEntity.getChargeId());
                updateWrapper.set(StrUtil.isNotBlank(biOrderInfoEntity.getChargeName()), BiReturnOrderInfoEntity::getChargeName, biOrderInfoEntity.getChargeName());
                if (ObjectUtil.isNotEmpty(biOrderInfoEntity.getPlatformCreateTime()) && StrUtil.isNotEmpty(biOrderInfoEntity.getChargeId()) && StrUtil.isNotEmpty(biOrderInfoEntity.getChargeName())){
                    updateWrapper.set(BiReturnOrderInfoEntity::getCleanState, 2);
                }
            }
            updateWrapper.set(BiReturnOrderInfoEntity::getRetryCount, biReturnOrderInfoEntity.getRetryCount() + 1);
            updateWrapper.eq(BiReturnOrderInfoEntity::getId, biReturnOrderInfoEntity.getId());
            this.update(updateWrapper);
        }
    }

    @Override
    public void removeReturnOrderByCode(List<String> codes) {
        //删除订单
        LambdaQueryWrapper<BiReturnOrderInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(BiReturnOrderInfoEntity::getPlatformOrderId, codes);
        List<BiReturnOrderInfoEntity> list = baseMapper.selectList(queryWrapper);
        log.info("删除 bi_return_order_info 订单：{}", JSON.toJSONString(list));
        if (CollectionUtils.isNotEmpty(list)) {
            //删除明细记录
            list.forEach(dmpReturnOrderInfoEntity -> {
                List<BiReturnOrderItemEntity> itemEntities = biReturnOrderItemService.getItemByMainId(dmpReturnOrderInfoEntity.getId());
                biReturnOrderItemService.removeByIds(itemEntities.stream().map(BiReturnOrderItemEntity::getId).collect(Collectors.toList()));
                this.removeById(dmpReturnOrderInfoEntity.getId());
            });
        }
    }
}




