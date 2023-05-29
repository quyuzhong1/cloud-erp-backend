package com.erp.server.dmp.pull.service.dmp.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.DmpDeliveryDetailItemEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.entity.DmpRefundItemEntity;
import com.erp.server.dmp.pull.mapper.DmpDeliveryDetailItemMapper;
import com.erp.server.dmp.pull.service.dmp.DmpDeliveryDetailItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 发货详情商品信息
 */
@Service
public class DmpDeliveryDetailItemServiceImpl extends ServiceImpl<DmpDeliveryDetailItemMapper, DmpDeliveryDetailItemEntity>
    implements DmpDeliveryDetailItemService {
    /**
     * 添加发货详情商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpDeliveryDetailItemEntity 退款列表信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean add(DmpDeliveryDetailItemEntity dmpDeliveryDetailItemEntity) {
        return this.save(dmpDeliveryDetailItemEntity);
    }

    /**
     * 批量添加发货详情商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpDeliveryDetailItemEntityList 发货详情商品信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean batchAdd(List<DmpDeliveryDetailItemEntity> dmpDeliveryDetailItemEntityList) {
        return this.saveBatch(dmpDeliveryDetailItemEntityList, 500);
    }

    /**
     * 根据发货详情商品表id删除发货详情商品信息
     * @Author Luo_WG
     * @Date 2022/11/16 11:05
     * @param deliveryDetailId 发货详情商品表id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean deleteDeliveryDetailItemByDetailId(String deliveryDetailId) {
        LambdaQueryWrapper<DmpDeliveryDetailItemEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DmpDeliveryDetailItemEntity::getDeliveryDetailId, deliveryDetailId);
        return this.remove(lambdaQueryWrapper);
    }
}




