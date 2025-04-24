package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.entity.BiRefundInfoEntity;
import com.erp.model.dmp.entity.BiRefundItemEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.pull.mapper.BiRefundInfoMapper;
import com.erp.server.dmp.service.BiOrderInfoService;
import com.erp.server.dmp.service.BiRefundInfoService;
import com.erp.server.dmp.service.BiRefundItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 退款列表服务类
 */
@Slf4j
@Service
public class BiRefundInfoServiceImpl extends ServiceImpl<BiRefundInfoMapper, BiRefundInfoEntity>
    implements BiRefundInfoService {

    private final Integer pageSize = 100;

    private static Integer pageIndex = 1;

    @Resource
    private BiOrderInfoService biOrderInfoService;
    @Resource
    private BiRefundItemService biRefundItemService;

    /**
     * 添加退款列表信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param biRefundInfoEntity 退款列表信息
     * @return java.lang.Boolean
     **/
    @Override
    public String add(BiRefundInfoEntity biRefundInfoEntity) {
        this.save(biRefundInfoEntity);
        return biRefundInfoEntity.getId();
    }

    /**
     * 根据平台订单id查询退款信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param returnOrderInfoEntity
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    @Override
    public BiRefundInfoEntity getRefundByPlatformOrderId(BiRefundInfoEntity returnOrderInfoEntity) {
        LambdaQueryWrapper<BiRefundInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(BiRefundInfoEntity::getPlatformOrderId, returnOrderInfoEntity.getPlatformOrderId());
        lambdaQueryWrapper.eq(BiRefundInfoEntity::getRefundCode, returnOrderInfoEntity.getRefundCode());
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据平台订单id修改退款信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param biRefundInfoEntity 订单信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateRefundByPlatformOrderId(BiRefundInfoEntity biRefundInfoEntity) {
        LambdaQueryWrapper<BiRefundInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(BiRefundInfoEntity::getPlatformOrderId, biRefundInfoEntity.getPlatformOrderId());
        lambdaQueryWrapper.eq(BiRefundInfoEntity::getRefundCode, biRefundInfoEntity.getRefundCode());
        return this.update(biRefundInfoEntity, lambdaQueryWrapper);
    }

    /**
     * 校验退款数据在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/16 11:18
     * @param returnOrderInfoEntity returnOrderInfoEntity
     * @return java.lang.String
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String checkOrder(BiRefundInfoEntity returnOrderInfoEntity) {
        String refundInfoId = "";
        BiRefundInfoEntity dmpReturnOrderInfoEntity = this.getRefundByPlatformOrderId(returnOrderInfoEntity);
        if (dmpReturnOrderInfoEntity != null) {
            if(PlatformEnum.GYY.getDesc().equals(returnOrderInfoEntity.getPlatformSign()) && null != returnOrderInfoEntity.getCancel() && returnOrderInfoEntity.getCancel()){
                removeById(dmpReturnOrderInfoEntity.getId());
                biRefundItemService.deleteRefundItemByRefundId(dmpReturnOrderInfoEntity.getId());
                return refundInfoId;
            }
            //如果数据有变动需要更新数据库订单信息
            if (!dmpReturnOrderInfoEntity.toString().equals(returnOrderInfoEntity.toString())) {
                returnOrderInfoEntity.setId(dmpReturnOrderInfoEntity.getId());
                updateById(returnOrderInfoEntity);
            }
            refundInfoId = dmpReturnOrderInfoEntity.getId();
        } else {
            if(PlatformEnum.GYY.getDesc().equals(returnOrderInfoEntity.getPlatformSign()) && null != returnOrderInfoEntity.getCancel() && returnOrderInfoEntity.getCancel()){
                return refundInfoId;
            }
            refundInfoId = add(returnOrderInfoEntity);
        }
        if(StrUtil.isBlank(refundInfoId)){
            throw new RuntimeException("DmpOrderInfoServiceImpl>>>checkOrder>>>销售订单保存失败");
        }
        List<BiRefundItemEntity> itemList = returnOrderInfoEntity.getItemList();
        if (CollectionUtil.isEmpty(itemList)){
            return refundInfoId;
        }
        String orderId = refundInfoId;
        List<BiRefundItemEntity> biRefundItemEntityList = itemList.stream().peek(entity -> entity.setRefundId(orderId)).collect(Collectors.toList());
        log.debug("BI退款：{}" , JSON.toJSONString(biRefundItemEntityList));
        biRefundItemService.checkOrderItem(itemList, returnOrderInfoEntity.getPlatformSign());
        return refundInfoId;
    }

    /**
     * 清洗退款数据
     * @Author Luo_WG
     * @Date 2022/12/14 19:15
     **/
    @Override
    public void cleanRefundTask(){
        List<BiRefundInfoEntity> dmpRefundInfoEntities = baseMapper.cleanRefundList(pageSize, pageIndex);
        if (dmpRefundInfoEntities == null || dmpRefundInfoEntities.isEmpty()) {
            pageIndex = 1;
            return;
        }

        for (BiRefundInfoEntity biRefundInfoEntity : dmpRefundInfoEntities) {
            LambdaUpdateWrapper<BiRefundInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
            BiOrderInfoEntity biOrderInfoEntity = biOrderInfoService.getOrderBySalesRecordNumber(biRefundInfoEntity.getSalesRecordNumber(), biRefundInfoEntity.getPlatformOrderId(), biRefundInfoEntity.getPlatformSign());
            if (biOrderInfoEntity != null) {
                updateWrapper.set(ObjectUtil.isNotEmpty(biOrderInfoEntity.getPlatformCreateTime()), BiRefundInfoEntity::getOrderTime, biOrderInfoEntity.getPlatformCreateTime());
                updateWrapper.set(StrUtil.isNotEmpty(biOrderInfoEntity.getChargeId()), BiRefundInfoEntity::getChargeId, biOrderInfoEntity.getChargeId());
                updateWrapper.set(StrUtil.isNotEmpty(biOrderInfoEntity.getChargeName()), BiRefundInfoEntity::getChargeName, biOrderInfoEntity.getChargeName());
                if (ObjectUtil.isNotEmpty(biOrderInfoEntity.getPlatformCreateTime()) && StrUtil.isNotEmpty(biOrderInfoEntity.getChargeId()) && StrUtil.isNotEmpty(biOrderInfoEntity.getChargeName())){
                    updateWrapper.set(BiRefundInfoEntity::getCleanState, 2);
                }
            }
            updateWrapper.set(BiRefundInfoEntity::getRetryCount, biRefundInfoEntity.getRetryCount() + 1);
            updateWrapper.eq(BiRefundInfoEntity::getId, biRefundInfoEntity.getId());
            this.update(updateWrapper);
        }
    }
}




