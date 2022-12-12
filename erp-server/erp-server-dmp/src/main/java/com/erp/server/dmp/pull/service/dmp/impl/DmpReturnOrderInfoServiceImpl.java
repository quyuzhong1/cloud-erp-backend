package com.erp.server.dmp.pull.service.dmp.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.server.dmp.pull.mapper.DmpReturnOrderInfoMapper;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderInfoService;
import org.springframework.stereotype.Service;

/**
 * 退货订单服务
 */
@Service
public class DmpReturnOrderInfoServiceImpl extends ServiceImpl<DmpReturnOrderInfoMapper, DmpReturnOrderInfoEntity>
    implements DmpReturnOrderInfoService {
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
        LambdaQueryWrapper<DmpReturnOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpReturnOrderInfoEntity::getPlatformOrderId, returnOrderInfoEntity.getPlatformOrderId());
        lambdaQueryWrapper.eq(DmpReturnOrderInfoEntity::getReturnOrderId, returnOrderInfoEntity.getReturnOrderId());
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
        lambdaQueryWrapper.eq(DmpReturnOrderInfoEntity::getPlatformOrderId, dmpReturnOrderInfoEntity.getPlatformOrderId());
        lambdaQueryWrapper.eq(DmpReturnOrderInfoEntity::getReturnOrderId, dmpReturnOrderInfoEntity.getReturnOrderId());
        return this.update(dmpReturnOrderInfoEntity, lambdaQueryWrapper);
    }

    /**
     * 校验退货订单在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/16 11:18
     * @param returnOrderInfoEntity returnOrderInfoEntity
     * @return java.lang.String
     **/
    @Override
    public String checkOrder(DmpReturnOrderInfoEntity returnOrderInfoEntity) {
        String returnOrderId = "";
        DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity = this.getOrderByPlatformOrderId(returnOrderInfoEntity);
        if (dmpReturnOrderInfoEntity != null) {
            //如果数据有变动需要更新数据库订单信息
            if (!dmpReturnOrderInfoEntity.toString().equals(dmpReturnOrderInfoEntity.toString())) {
                this.updateOrderByPlatformOrderId(returnOrderInfoEntity);
                returnOrderId = returnOrderInfoEntity.getId();
            }

        } else {
            returnOrderId = this.add(returnOrderInfoEntity);
        }
        return returnOrderId;
    }
}




