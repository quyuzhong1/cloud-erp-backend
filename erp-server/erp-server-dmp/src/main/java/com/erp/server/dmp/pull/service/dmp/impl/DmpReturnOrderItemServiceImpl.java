package com.erp.server.dmp.pull.service.dmp.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.server.dmp.entity.dmp.DmpReturnOrderItemEntity;
import com.erp.server.dmp.pull.mapper.DmpReturnOrderItemMapper;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderItemService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 中台订单退货服务类
 */
@Service
public class DmpReturnOrderItemServiceImpl extends ServiceImpl<DmpReturnOrderItemMapper, DmpReturnOrderItemEntity>
    implements DmpReturnOrderItemService {

    /**
     * 添加退货订单商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpReturnOrderItemEntity 订单商品信息
     * @return java.lang.Boolean
     **/
    public Boolean add(DmpReturnOrderItemEntity dmpReturnOrderItemEntity) {
        return this.save(dmpReturnOrderItemEntity);
    }

    /**
     * 批量添加退货订单商品详细信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpOrderInfoEntityList 退货订单商品信息集合
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean batchAdd(List<DmpReturnOrderItemEntity> dmpOrderInfoEntityList) {
        return this.saveBatch(dmpOrderInfoEntityList);
    }

    /**
     * 根据退货订单表id查询退货订单商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:04
     * @param returnOrderId 退货订单表id
     * @return com.erp.server.dmp.entity.dmp.DmpReturnOrderItemEntity
     **/
    public DmpReturnOrderItemEntity getOrderByReturnOrderId(String returnOrderId) {
        LambdaQueryWrapper<DmpReturnOrderItemEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpReturnOrderItemEntity::getReturnOrderId, returnOrderId);
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据退货订单表id删除退货订单商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:05
     * @param returnOrderId 退货订单表id
     * @return java.lang.Boolean
     **/
    public Boolean deleteOrderByReturnOrderId(String returnOrderId) {
        LambdaQueryWrapper<DmpReturnOrderItemEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpReturnOrderItemEntity::getReturnOrderId, returnOrderId);
        return this.remove(lambdaQueryWrapper);
    }
}




