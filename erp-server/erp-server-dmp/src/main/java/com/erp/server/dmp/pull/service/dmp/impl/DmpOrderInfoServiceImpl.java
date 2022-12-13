package com.erp.server.dmp.pull.service.dmp.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.server.dmp.pull.mapper.DmpOrderInfoMapper;
import com.erp.server.dmp.pull.service.dmp.DmpOrderInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单服务类
 */
@Service
public class DmpOrderInfoServiceImpl extends ServiceImpl<DmpOrderInfoMapper, DmpOrderInfoEntity>
    implements DmpOrderInfoService {

    /**
     * 添加订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     **/
    @Override
    public String add(DmpOrderInfoEntity dmpOrderInfoEntity) {
        this.save(dmpOrderInfoEntity);
        return dmpOrderInfoEntity.getId();
    }

    /**
     * 根据平台订单id查询订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param platformOrderId 平台订单id
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    @Override
    public DmpOrderInfoEntity getOrderByPlatformOrderId(String platformOrderId) {
        LambdaQueryWrapper<DmpOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpOrderInfoEntity::getPlatformOrderId, platformOrderId);
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据平台订单id修改订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateOrderByPlatformOrderId(DmpOrderInfoEntity dmpOrderInfoEntity) {
        LambdaQueryWrapper<DmpOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpOrderInfoEntity::getPlatformOrderId, dmpOrderInfoEntity.getPlatformOrderId());
        return this.update(dmpOrderInfoEntity, lambdaQueryWrapper);
    }

    /**
     * 校验订单在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    public String checkOrder(DmpOrderInfoEntity orderInfoEntity) {
        String orderInfoId = "";
        DmpOrderInfoEntity dmpOrderInfoEntity = this.getOrderByPlatformOrderId(orderInfoEntity.getPlatformOrderId());
        if (dmpOrderInfoEntity != null) {
            //如果数据有变动需要更新数据库订单信息
            if (!dmpOrderInfoEntity.toString().equals(orderInfoEntity.toString())) {
                this.updateOrderByPlatformOrderId(dmpOrderInfoEntity);
                orderInfoId = dmpOrderInfoEntity.getId();
            }

        } else {
            orderInfoId = this.add(orderInfoEntity);
        }
        return orderInfoId;
    }


}




