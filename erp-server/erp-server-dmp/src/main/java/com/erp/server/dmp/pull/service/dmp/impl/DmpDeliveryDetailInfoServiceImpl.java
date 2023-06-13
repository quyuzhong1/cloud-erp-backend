package com.erp.server.dmp.pull.service.dmp.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.DmpDeliveryDetailInfoEntity;
import com.erp.model.dmp.entity.DmpDeliveryDetailItemEntity;
import com.erp.server.dmp.pull.mapper.DmpDeliveryDetailInfoMapper;
import com.erp.server.dmp.pull.service.dmp.DmpDeliveryDetailInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpDeliveryDetailItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 货详情信息
 */
@Slf4j
@Service
public class DmpDeliveryDetailInfoServiceImpl extends ServiceImpl<DmpDeliveryDetailInfoMapper, DmpDeliveryDetailInfoEntity>
    implements DmpDeliveryDetailInfoService {

    @Resource
    private DmpDeliveryDetailItemService dmpDeliveryDetailItemService;



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
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    @Override
    public DmpDeliveryDetailInfoEntity getDeliveryDetailByBillNo(DmpDeliveryDetailInfoEntity dmpDeliveryDetailInfoEntity) {
        LambdaQueryWrapper<DmpDeliveryDetailInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpDeliveryDetailInfoEntity::getBillNo, dmpDeliveryDetailInfoEntity.getBillNo());
        lambdaQueryWrapper.eq(DmpDeliveryDetailInfoEntity::getOrderNo, dmpDeliveryDetailInfoEntity.getOrderNo());
        return this.getOne(lambdaQueryWrapper);
    }


    /**
     * 根据订单编号查询发货详情信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param orderNo
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    @Override
    public DmpDeliveryDetailInfoEntity getDeliveryDetailOrderNo(String orderNo) {
        LambdaQueryWrapper<DmpDeliveryDetailInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpDeliveryDetailInfoEntity::getOrderNo, orderNo);
        lambdaQueryWrapper.last("LIMIT 1");
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
    @Transactional(rollbackFor = Exception.class)
    public String checkOrder(DmpDeliveryDetailInfoEntity dmpDeliveryDetailInfoEntity) {
        String deliveryDetailId = "";
        DmpDeliveryDetailInfoEntity deliveryDetailInfoEntity = getDeliveryDetailByBillNo(dmpDeliveryDetailInfoEntity);
        if (null != deliveryDetailInfoEntity) {
            //如果数据有变动需要更新数据库订单信息
            if (!deliveryDetailInfoEntity.toString().equals(deliveryDetailInfoEntity.toString())) {
                deliveryDetailInfoEntity.setId(dmpDeliveryDetailInfoEntity.getId());
                updateById(deliveryDetailInfoEntity);
                deliveryDetailId = deliveryDetailInfoEntity.getId();
            } else {
                return deliveryDetailId ;
            }
        } else {
            deliveryDetailId = add(dmpDeliveryDetailInfoEntity);
        }
        if(StrUtil.isBlank(deliveryDetailId)){
            throw new RuntimeException("DmpDeliveryDetailInfoServiceImpl>>>checkOrder>>>发货订单保存失败");
        }
        List<DmpDeliveryDetailItemEntity> itemList = dmpDeliveryDetailInfoEntity.getDetails();
        if (CollectionUtil.isEmpty(itemList)){
            return deliveryDetailId;
        }
        String orderId = deliveryDetailId;
        itemList.stream().peek(entity -> entity.setDeliveryDetailId(orderId)).collect(Collectors.toList());
        dmpDeliveryDetailItemService.deleteDeliveryDetailItemByDetailId(deliveryDetailId);
        dmpDeliveryDetailItemService.batchAdd(itemList);
        return deliveryDetailId;
    }
}




