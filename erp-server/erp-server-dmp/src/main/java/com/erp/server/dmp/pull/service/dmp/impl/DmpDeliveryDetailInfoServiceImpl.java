package com.erp.server.dmp.pull.service.dmp.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.server.dmp.entity.dmp.DmpDeliveryDetailInfoEntity;
import com.erp.server.dmp.entity.dmp.DmpOrderInfoEntity;
import com.erp.server.dmp.entity.dmp.DmpRefundInfoEntity;
import com.erp.server.dmp.pull.mapper.DmpDeliveryDetailInfoMapper;
import com.erp.server.dmp.pull.service.dmp.DmpDeliveryDetailInfoService;
import org.springframework.stereotype.Service;

/**
 * 货详情信息
 */
@Service
public class DmpDeliveryDetailInfoServiceImpl extends ServiceImpl<DmpDeliveryDetailInfoMapper, DmpDeliveryDetailInfoEntity>
    implements DmpDeliveryDetailInfoService {

    /**
     * 添加发货详情信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpDeliveryDetailInfoEntity
     * @return java.lang.Boolean
     **/
    @Override
    public String add(DmpDeliveryDetailInfoEntity dmpDeliveryDetailInfoEntity) {
        this.save(dmpDeliveryDetailInfoEntity);
        return dmpDeliveryDetailInfoEntity.getId();
    }

    /**
     * 根据单据编号查询发货详情信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param dmpDeliveryDetailInfoEntity
     * @return com.erp.server.dmp.entity.dmp.DmpOrderInfoEntity
     **/
    @Override
    public DmpDeliveryDetailInfoEntity getDeliveryDetailByBillNo(DmpDeliveryDetailInfoEntity dmpDeliveryDetailInfoEntity) {
        LambdaQueryWrapper<DmpDeliveryDetailInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpDeliveryDetailInfoEntity::getBillNo, dmpDeliveryDetailInfoEntity.getBillNo());
        lambdaQueryWrapper.eq(DmpDeliveryDetailInfoEntity::getOrderNo, dmpDeliveryDetailInfoEntity.getOrderNo());
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据单据编号修改发货详情信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpDeliveryDetailInfoEntity
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateDeliveryDetailByBillNo(DmpDeliveryDetailInfoEntity dmpDeliveryDetailInfoEntity) {
        LambdaQueryWrapper<DmpDeliveryDetailInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpDeliveryDetailInfoEntity::getBillNo, dmpDeliveryDetailInfoEntity.getBillNo());
        lambdaQueryWrapper.eq(DmpDeliveryDetailInfoEntity::getOrderNo, dmpDeliveryDetailInfoEntity.getOrderNo());
        return this.update(dmpDeliveryDetailInfoEntity, lambdaQueryWrapper);
    }

    /**
     * 校验发货详情信息在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    @Override
    public String checkOrder(DmpDeliveryDetailInfoEntity dmpDeliveryDetailInfoEntity) {
        String deliveryDetailId = "";
        DmpDeliveryDetailInfoEntity deliveryDetailInfoEntity = this.getDeliveryDetailByBillNo(dmpDeliveryDetailInfoEntity);
        if (deliveryDetailInfoEntity != null) {
            //如果数据有变动需要更新数据库订单信息
            if (!deliveryDetailInfoEntity.toString().equals(deliveryDetailInfoEntity.toString())) {
                this.updateDeliveryDetailByBillNo(dmpDeliveryDetailInfoEntity);
                deliveryDetailId = dmpDeliveryDetailInfoEntity.getId();
            }
        } else {
            deliveryDetailId = this.add(dmpDeliveryDetailInfoEntity);
        }
        return deliveryDetailId;
    }
}




