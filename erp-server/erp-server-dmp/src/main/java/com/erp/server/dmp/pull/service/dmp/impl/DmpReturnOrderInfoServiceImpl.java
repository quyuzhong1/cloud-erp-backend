package com.erp.server.dmp.pull.service.dmp.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;
import com.erp.server.dmp.pull.mapper.DmpReturnOrderInfoMapper;
import com.erp.server.dmp.pull.service.dmp.DmpOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 退货订单服务
 */
@Service
public class DmpReturnOrderInfoServiceImpl extends ServiceImpl<DmpReturnOrderInfoMapper, DmpReturnOrderInfoEntity>
    implements DmpReturnOrderInfoService {

    private final Integer pageSize = 100;

    private static Integer pageIndex = 1;

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Resource
    private DmpReturnOrderItemService dmpReturnOrderItemService;

    /**
     * 添加退货订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpRefundInfoEntity 退货订单信息
     * @return java.lang.Boolean
     **/
    @Override
    public String add(DmpReturnOrderInfoEntity dmpRefundInfoEntity) {
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
    public DmpReturnOrderInfoEntity getOrderByPlatformOrderId(DmpReturnOrderInfoEntity returnOrderInfoEntity) {
        LambdaQueryWrapper<DmpReturnOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DmpReturnOrderInfoEntity::getReturnCode, returnOrderInfoEntity.getReturnCode());
        lambdaQueryWrapper.eq(StrUtil.isNotBlank(returnOrderInfoEntity.getPlatformOrderId()), DmpReturnOrderInfoEntity::getPlatformOrderId, returnOrderInfoEntity.getPlatformOrderId());
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
    public DmpReturnOrderInfoEntity getOrderByOrderId(String platformOrderId) {
        LambdaQueryWrapper<DmpReturnOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpReturnOrderInfoEntity::getPlatformOrderId, platformOrderId);
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据平台订单id修改退货订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpReturnOrderInfoEntity 退货订单信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateOrderByPlatformOrderId(DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity) {
        LambdaQueryWrapper<DmpReturnOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpReturnOrderInfoEntity::getReturnCode, dmpReturnOrderInfoEntity.getReturnCode());
        lambdaQueryWrapper.eq(DmpReturnOrderInfoEntity::getPlatformOrderId, dmpReturnOrderInfoEntity.getPlatformOrderId());
        return this.update(dmpReturnOrderInfoEntity, lambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String checkOrder(DmpReturnOrderInfoEntity returnOrderInfoEntity) {
        String returnOrderId = "";
        DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity = this.getOrderByPlatformOrderId(returnOrderInfoEntity);
        if(null != dmpReturnOrderInfoEntity && returnOrderInfoEntity.getIsDeleted()){
            return returnOrderId;
        }
        if (null != dmpReturnOrderInfoEntity) {
            //如果数据有变动需要更新数据库订单信息
            if (!dmpReturnOrderInfoEntity.toString().equals(dmpReturnOrderInfoEntity.toString())) {
                returnOrderInfoEntity.setId(dmpReturnOrderInfoEntity.getId());
                updateById(returnOrderInfoEntity);
            }
            returnOrderId = dmpReturnOrderInfoEntity.getId();
        } else {
            returnOrderId = add(returnOrderInfoEntity);
        }
        if(StrUtil.isBlank(returnOrderId)){
            throw new RuntimeException("DmpOrderInfoServiceImpl>>>checkOrder>>>销售订单保存失败");
        }
        List<DmpReturnOrderItemEntity> itemList = returnOrderInfoEntity.getItemList();
        if (CollectionUtil.isEmpty(itemList)){
            return returnOrderId;
        }
        String orderId = returnOrderId;
        itemList.stream().peek(entity -> entity.setReturnOrderId(orderId)).collect(Collectors.toList());
        dmpReturnOrderItemService.checkOrderItem(itemList);
        return returnOrderId;
    }

    /**
     * 清洗退货订单数据
     * @Author Luo_WG
     * @Date 2022/12/14 19:15
     **/
    @Override
    public void cleanReturnOrderTask(){
        List<DmpReturnOrderInfoEntity> dmpReturnOrderInfoEntities = baseMapper.cleanReturnOrderList(pageSize, pageIndex);
        if (dmpReturnOrderInfoEntities == null || dmpReturnOrderInfoEntities.isEmpty()) {
            pageIndex = 1;
            return;
        }

        for (DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity : dmpReturnOrderInfoEntities) {
            LambdaUpdateWrapper<DmpReturnOrderInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
            DmpOrderInfoEntity dmpOrderInfoEntity = dmpOrderInfoService.getOrderBySalesRecordNumber(dmpReturnOrderInfoEntity.getSalesRecordNumber(), dmpReturnOrderInfoEntity.getPlatformOrderId(),dmpReturnOrderInfoEntity.getPlatformSign());
            if (dmpOrderInfoEntity != null) {
                updateWrapper.set(ObjectUtil.isNotEmpty(dmpOrderInfoEntity.getPlatformCreateTime()), DmpReturnOrderInfoEntity::getOrderTime, dmpOrderInfoEntity.getPlatformCreateTime());
                updateWrapper.set(StrUtil.isNotBlank(dmpOrderInfoEntity.getChargeId()), DmpReturnOrderInfoEntity::getChargeId, dmpOrderInfoEntity.getChargeId());
                updateWrapper.set(StrUtil.isNotBlank(dmpOrderInfoEntity.getChargeName()), DmpReturnOrderInfoEntity::getChargeName, dmpOrderInfoEntity.getChargeName());
                if (ObjectUtil.isNotEmpty(dmpOrderInfoEntity.getPlatformCreateTime()) && StrUtil.isNotEmpty(dmpOrderInfoEntity.getChargeId()) && StrUtil.isNotEmpty(dmpOrderInfoEntity.getChargeName())){
                    updateWrapper.set(DmpReturnOrderInfoEntity::getCleanState, 2);
                }
            }
            updateWrapper.set(DmpReturnOrderInfoEntity::getRetryCount, dmpReturnOrderInfoEntity.getRetryCount() + 1);
            updateWrapper.eq(DmpReturnOrderInfoEntity::getId, dmpReturnOrderInfoEntity.getId());
            this.update(updateWrapper);
        }
    }
}




