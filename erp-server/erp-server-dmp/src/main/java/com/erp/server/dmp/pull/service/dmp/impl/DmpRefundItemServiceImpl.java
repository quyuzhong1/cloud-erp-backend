package com.erp.server.dmp.pull.service.dmp.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.entity.DmpRefundItemEntity;
import com.erp.server.dmp.pull.mapper.DmpRefundItemMapper;
import com.erp.server.dmp.pull.service.dmp.DmpRefundItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 退款商品列表服务类
 */
@Service
public class DmpRefundItemServiceImpl extends ServiceImpl<DmpRefundItemMapper, DmpRefundItemEntity>
    implements DmpRefundItemService {
    /**
     * 添加退款商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpRefundItemEntity 退款列表信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean add(DmpRefundItemEntity dmpRefundItemEntity) {
        return this.save(dmpRefundItemEntity);
    }

    /**
     * 批量添加退款商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpRefundItemEntityList 退款列表信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean batchAdd(List<DmpRefundItemEntity> dmpRefundItemEntityList) {
        return this.saveBatch(dmpRefundItemEntityList);
    }

    /**
     * 根据退货订单表id删除退款商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:05
     * @param refundId 退货订单表id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean deleteRefundItemByRefundId(String refundId) {
        LambdaQueryWrapper<DmpRefundItemEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpRefundItemEntity::getRefundId, refundId);
        return this.remove(lambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOrderItem(List<DmpRefundItemEntity> itemList) {
        List<DmpRefundItemEntity> insertList = new ArrayList<>();
        for (DmpRefundItemEntity orderItemBean : itemList) {
            Optional<DmpRefundItemEntity> dmpRefundItemEntity = lambdaQuery()
                    .eq(DmpRefundItemEntity::getErpOrderItemId, orderItemBean.getErpOrderItemId())
                    .oneOpt();
            if (dmpRefundItemEntity.isPresent()) {
                //如果数据有变动需要更新数据库订单商品信息
                if (!dmpRefundItemEntity.get().toString().equals(orderItemBean.toString())) {
                    orderItemBean.setId(dmpRefundItemEntity.get().getId());
                    updateById(orderItemBean);
                }
            } else {
                insertList.add(orderItemBean);
            }
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            saveBatch(insertList, 500);
        }
    }
}




