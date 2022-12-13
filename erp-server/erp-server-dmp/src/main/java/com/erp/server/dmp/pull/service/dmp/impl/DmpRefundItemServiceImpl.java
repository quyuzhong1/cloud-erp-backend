package com.erp.server.dmp.pull.service.dmp.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.DmpRefundItemEntity;
import com.erp.server.dmp.pull.mapper.DmpRefundItemMapper;
import com.erp.server.dmp.pull.service.dmp.DmpRefundItemService;
import org.springframework.stereotype.Service;

import java.util.List;

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
}




