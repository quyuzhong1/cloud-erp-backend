package com.erp.server.dmp.pull.service.dmp.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.server.dmp.pull.mapper.DmpRefundInfoMapper;
import com.erp.server.dmp.pull.service.dmp.DmpRefundInfoService;
import org.springframework.stereotype.Service;

/**
 * 退款列表服务类
 */
@Service
public class DmpRefundInfoServiceImpl extends ServiceImpl<DmpRefundInfoMapper, DmpRefundInfoEntity>
    implements DmpRefundInfoService {

    /**
     * 添加退款列表信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpRefundInfoEntity 退款列表信息
     * @return java.lang.Boolean
     **/
    @Override
    public String add(DmpRefundInfoEntity dmpRefundInfoEntity) {
        this.save(dmpRefundInfoEntity);
        return dmpRefundInfoEntity.getId();
    }

    /**
     * 根据平台订单id查询退款信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param returnOrderInfoEntity
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    @Override
    public DmpRefundInfoEntity getRefundByPlatformOrderId(DmpRefundInfoEntity returnOrderInfoEntity) {
        LambdaQueryWrapper<DmpRefundInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpRefundInfoEntity::getPlatformOrderId, returnOrderInfoEntity.getPlatformOrderId());
        lambdaQueryWrapper.eq(DmpRefundInfoEntity::getPlatformOrderId, returnOrderInfoEntity.getRefundId());
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据平台订单id修改退款信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpRefundInfoEntity 订单信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateRefundByPlatformOrderId(DmpRefundInfoEntity dmpRefundInfoEntity) {
        LambdaQueryWrapper<DmpRefundInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpRefundInfoEntity::getPlatformOrderId, dmpRefundInfoEntity.getPlatformOrderId());
        lambdaQueryWrapper.eq(DmpRefundInfoEntity::getPlatformOrderId, dmpRefundInfoEntity.getRefundId());
        return this.update(dmpRefundInfoEntity, lambdaQueryWrapper);
    }

    /**
     * 校验退款数据在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/16 11:18
     * @param returnOrderInfoEntity returnOrderInfoEntity
     * @return java.lang.String
     **/
    @Override
    public String checkOrder(DmpRefundInfoEntity returnOrderInfoEntity) {
        String refundInfoId = "";
        DmpRefundInfoEntity dmpReturnOrderInfoEntity = this.getRefundByPlatformOrderId(returnOrderInfoEntity);
        if (dmpReturnOrderInfoEntity != null) {
            //如果数据有变动需要更新数据库订单信息
            if (!dmpReturnOrderInfoEntity.toString().equals(dmpReturnOrderInfoEntity.toString())) {
                this.updateRefundByPlatformOrderId(returnOrderInfoEntity);
                refundInfoId = returnOrderInfoEntity.getId();
            }
        } else {
            refundInfoId = this.add(returnOrderInfoEntity);
        }
        return refundInfoId;
    }
}




