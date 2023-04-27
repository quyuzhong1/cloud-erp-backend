package com.erp.server.dmp.pull.service.dmp.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.plm.dto.NewProductDTO;
import com.erp.server.dmp.pull.mapper.DmpOrderItemMapper;
import com.erp.server.dmp.pull.service.dmp.DmpOrderItemService;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /**
     * 校验订单商品信息在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
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

    /**
     * 同步PLM的到货时间更新新老品
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNewSign(NewProductDTO dto) {

        String year = "";
        String skuNo = String.valueOf(dto.getSkuNo());
        if (dto.getNewListingTime() != null) {
            LocalDate date = dto.getNewListingTime();
            year = String.valueOf(date.getYear());

        }/* else if (map.get("pastListingTime") != null) {
            LocalDate date = LocalDate.parse(String.valueOf(map.get("pastListingTime")), fmt);
            year = String.valueOf(date.getYear());
            updateWrapper.set(DmpOrderItemEntity::getNewSign, 2);
        }*/
        if (StringUtils.isBlank(year)) {
            return;
        }
        List<String> ids = baseMapper.getItemIdBySkuAndYear(year, skuNo);
        List<List<String>> partition = Lists.partition(ids, 1000);
        partition.forEach(req -> {
            LambdaUpdateWrapper<DmpOrderItemEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(DmpOrderItemEntity::getNewSign, 1);
            updateWrapper.in(DmpOrderItemEntity::getId, req);
            this.update(updateWrapper);
        });
    }
}




