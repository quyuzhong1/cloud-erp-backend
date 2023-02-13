package com.erp.server.dmp.pull.service.dmp.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.server.dmp.pull.mapper.DmpOrderItemMapper;
import com.erp.server.dmp.pull.service.dmp.DmpOrderItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOrderItem(List<DmpOrderItemEntity> orderItem) {
        List<DmpOrderItemEntity> insertList = new ArrayList<>();
        for (DmpOrderItemEntity orderItemBean : orderItem) {
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
}




