package com.erp.server.dmp.pull.service.dmp.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.server.dmp.entity.dmp.DmpOrderItemEntity;
import com.erp.server.dmp.pull.mapper.DmpOrderItemMapper;
import com.erp.server.dmp.pull.service.dmp.DmpOrderItemService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单商品详细信息
 */
@Service
public class DmpOrderItemServiceImpl extends ServiceImpl<DmpOrderItemMapper, DmpOrderItemEntity>
    implements DmpOrderItemService {

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
        return this.saveBatch(dmpOrderInfoEntityList);
    }

    /**
     * 根据erp平台商品id查询订单商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 22:11
     * @param erpOrderItemId erp平台商品id
     * @return com.erp.server.dmp.entity.dmp.DmpOrderItemEntity
     **/
    @Override
    public DmpOrderItemEntity getByErpOrderItemId(String erpOrderItemId) {
        LambdaQueryWrapper<DmpOrderItemEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpOrderItemEntity::getErpOrderItemId, erpOrderItemId);
        return this.getOne(lambdaQueryWrapper);
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
    public void checkOrderItem(List<DmpOrderItemEntity> orderItem) {
        for (DmpOrderItemEntity orderItemBean : orderItem) {
            DmpOrderItemEntity dmpOrderItemEntity = this.getByErpOrderItemId(orderItemBean.getErpOrderItemId());
            if (dmpOrderItemEntity != null) {
                //如果数据有变动需要更新数据库订单商品信息
                if (!dmpOrderItemEntity.toString().equals(orderItemBean.toString())) {
                    this.updateOrderItemByErpOrderItemId(dmpOrderItemEntity);
                }
            } else {
                this.save(orderItemBean);
            }
        }
    }
}




