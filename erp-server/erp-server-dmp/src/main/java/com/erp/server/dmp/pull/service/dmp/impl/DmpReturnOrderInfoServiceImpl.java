package com.erp.server.dmp.pull.service.dmp.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.server.dmp.pull.mapper.DmpReturnOrderInfoMapper;
import com.erp.server.dmp.pull.service.dmp.DmpOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 退货订单服务
 */
@Service
public class DmpReturnOrderInfoServiceImpl extends ServiceImpl<DmpReturnOrderInfoMapper, DmpReturnOrderInfoEntity>
    implements DmpReturnOrderInfoService {

    private final Integer pageSize = 100;

    private static Integer pageIndex = 1;

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

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
     * 根据订单id查询退货订单信息
     * @Author Luo_WG
     * @Date 2022/12/14 19:10
     * @param platformOrderId
     * @return com.erp.model.dmp.entity.DmpReturnOrderInfoEntity
     **/
    @Override
    public DmpReturnOrderInfoEntity getOrderByOrderId(String platformOrderId) {
        LambdaQueryWrapper<DmpReturnOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpReturnOrderInfoEntity::getPlatformOrderId, platformOrderId);
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

    /**
     * 清洗退货订单数据
     * @Author Luo_WG
     * @Date 2022/12/14 19:15
     **/
    @Override
    public void cleanReturnOrderTask(){
        List<DmpReturnOrderInfoEntity> dmpReturnOrderInfoEntities = baseMapper.cleanReturnOrderList(pageSize, pageIndex);
        if (dmpReturnOrderInfoEntities == null || dmpReturnOrderInfoEntities.isEmpty()) {
            pageIndex = 1;
            return;
        }

        for (DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity : dmpReturnOrderInfoEntities) {
            LambdaUpdateWrapper<DmpReturnOrderInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
            DmpOrderInfoEntity dmpOrderInfoEntity = dmpOrderInfoService.getOrderByPlatformOrderId(dmpReturnOrderInfoEntity.getPlatformOrderId());
            if (dmpOrderInfoEntity != null) {
                updateWrapper.set(ObjectUtil.isNotEmpty(dmpOrderInfoEntity.getPlatformCreateTime()), DmpReturnOrderInfoEntity::getOrderTime, dmpOrderInfoEntity.getPlatformCreateTime());
                updateWrapper.set(StrUtil.isNotBlank(dmpOrderInfoEntity.getChargeId()), DmpReturnOrderInfoEntity::getChargeId, dmpOrderInfoEntity.getChargeId());
                updateWrapper.set(StrUtil.isNotBlank(dmpOrderInfoEntity.getChargeName()), DmpReturnOrderInfoEntity::getChargeName, dmpOrderInfoEntity.getChargeName());
                if (ObjectUtil.isNotEmpty(dmpOrderInfoEntity.getPlatformCreateTime()) && StrUtil.isNotEmpty(dmpOrderInfoEntity.getChargeId()) && StrUtil.isNotEmpty(dmpOrderInfoEntity.getChargeName())){
                    updateWrapper.set(DmpReturnOrderInfoEntity::getCleanState, 2);
                }
            }
            updateWrapper.set(DmpReturnOrderInfoEntity::getRetryCount, dmpReturnOrderInfoEntity.getRetryCount() + 1);
            updateWrapper.eq(DmpReturnOrderInfoEntity::getId, dmpReturnOrderInfoEntity.getId());
            this.update(updateWrapper);
        }
    }
}




