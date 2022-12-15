package com.erp.server.dmp.pull.service.dmp.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.dmp.dto.ShopDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.plm.dto.CleanSkuDto;
import com.erp.model.sys.entity.SysDepartmentUserEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.dmp.pull.mapper.DmpOrderInfoMapper;
import com.erp.server.dmp.pull.service.dmp.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * 订单服务类
 */
@Service
public class DmpOrderInfoServiceImpl extends ServiceImpl<DmpOrderInfoMapper, DmpOrderInfoEntity>
    implements DmpOrderInfoService {

    private final Integer pageSize = 100;

    private static Integer pageIndex = 1;

    @Resource
    private DmpDeliveryDetailInfoService dmpDeliveryDetailInfoService;

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DmpOrderItemService dmpOrderItemService;

    @Resource
    private DmpReturnOrderInfoService dmpReturnOrderInfoService;

    /**
     * 添加订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     **/
    @Override
    public String add(DmpOrderInfoEntity dmpOrderInfoEntity) {
        this.save(dmpOrderInfoEntity);
        return dmpOrderInfoEntity.getId();
    }

    /**
     * 根据平台订单id查询订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param platformOrderId 平台订单id
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    @Override
    public DmpOrderInfoEntity getOrderByPlatformOrderId(String platformOrderId) {
        LambdaQueryWrapper<DmpOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpOrderInfoEntity::getPlatformOrderId, platformOrderId);
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据平台订单id修改订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateOrderByPlatformOrderId(DmpOrderInfoEntity dmpOrderInfoEntity) {
        LambdaQueryWrapper<DmpOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpOrderInfoEntity::getPlatformOrderId, dmpOrderInfoEntity.getPlatformOrderId());
        return this.update(dmpOrderInfoEntity, lambdaQueryWrapper);
    }

    /**
     * 校验订单在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    public String checkOrder(DmpOrderInfoEntity orderInfoEntity) {
        String orderInfoId = "";
        DmpOrderInfoEntity dmpOrderInfoEntity = this.getOrderByPlatformOrderId(orderInfoEntity.getPlatformOrderId());
        if (dmpOrderInfoEntity != null) {
            //如果数据有变动需要更新数据库订单信息
            if (!dmpOrderInfoEntity.toString().equals(orderInfoEntity.toString())) {
                this.updateOrderByPlatformOrderId(dmpOrderInfoEntity);
                orderInfoId = dmpOrderInfoEntity.getId();
            }

        } else {
            orderInfoId = this.add(orderInfoEntity);
        }
        return orderInfoId;
    }

    /**
     * 清洗订单数据
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    @Override
    public void cleanOrder() {
        List<DmpOrderInfoEntity> dmpOrderInfoEntities = baseMapper.cleanOrderList(pageSize, pageIndex);
        if (dmpOrderInfoEntities == null || dmpOrderInfoEntities.isEmpty()) {
            pageIndex = 1;
            return;
        }

        for (DmpOrderInfoEntity dmpOrderInfoEntity : dmpOrderInfoEntities) {
            //查询发货详情获取发货时间，同步到订单信息
            DmpDeliveryDetailInfoEntity deliveryDetailOrderNo = dmpDeliveryDetailInfoService.getDeliveryDetailOrderNo(dmpOrderInfoEntity.getPlatformOrderId());
            LambdaUpdateWrapper<DmpOrderInfoEntity> updateWrapper = new LambdaUpdateWrapper();
            if (deliveryDetailOrderNo != null) {
                updateWrapper.set(DmpOrderInfoEntity::getDeliveryTime, deliveryDetailOrderNo.getDeliveryDate());
                updateWrapper.set(DmpOrderInfoEntity::getCleanState, 2);
            }

/*            DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity = dmpReturnOrderInfoService.getOrderByOrderId(dmpOrderInfoEntity.getPlatformOrderId());
            if (dmpReturnOrderInfoEntity != null) {
                dmpReturnOrderInfoService.upda
            }*/

            //getOrderByPlatformOrderId
            //updateOrderByPlatformOrderId

            //dmp_refund_info

            //查询店铺信息获取'负责人','站点信息'同步到订单
            DmpShopInfoEntity shopByShopNo = dmpShopInfoService.getShopByShopNo(dmpOrderInfoEntity.getShopNo(), dmpOrderInfoEntity.getPlatformSign());
            if (shopByShopNo != null) {
                updateWrapper.set(DmpOrderInfoEntity::getSite, shopByShopNo.getSite());
                updateWrapper.set(DmpOrderInfoEntity::getChargeId, shopByShopNo.getChargeId());
                updateWrapper.set(DmpOrderInfoEntity::getChargeName, shopByShopNo.getChargeName());
            }

            //根据负责人获取部门信息，同步到订单
            DmpShopInfoDTO dmpShopInfoDTO = dmpShopInfoService.queryShopByPlatformList(dmpOrderInfoEntity.getShopNo(), dmpOrderInfoEntity.getPlatformSign());
            if (dmpShopInfoDTO != null) {
                updateWrapper.set(DmpOrderInfoEntity::getDeptId, dmpShopInfoDTO.getDeptId());
                updateWrapper.set(DmpOrderInfoEntity::getDeptName, dmpShopInfoDTO.getDeptName());
            }

            //查询订单商品明细，根据sku查询plm系统sku信息，获取'类别'、'品牌' 同步到商品信息
            List<DmpOrderItemEntity> itemEntityList = dmpOrderItemService.getByOrderId(dmpOrderInfoEntity.getId());
            for (DmpOrderItemEntity dmpOrderItemEntity : itemEntityList) {
                if (StringUtils.isNotBlank(dmpOrderItemEntity.getSkuNo())) {
                    CleanSkuDto productIdBySku = plmTaskFeign.getProductIdBySku(dmpOrderItemEntity.getSkuNo());
                    if (productIdBySku != null) {
                        dmpOrderItemEntity.setCategoryId(productIdBySku.getCategoryId());
                        dmpOrderItemEntity.setCategoryName(productIdBySku.getCategoryName());
                        dmpOrderItemEntity.setBrandId(productIdBySku.getBrandId());
                        dmpOrderItemEntity.setBrandName(productIdBySku.getBrandName());
                        LocalDate listingTime = productIdBySku.getListingTime();
                        if (null !=  listingTime) {
                            dmpOrderItemEntity.setNewSign(listingTime.getYear() == LocalDate.now().getYear() ? 1 : 0);
                        }
                        dmpOrderItemService.updateOrderItemByErpOrderItemId(dmpOrderItemEntity);
                    }
                }

            }
            updateWrapper.set(DmpOrderInfoEntity::getRetryCount, dmpOrderInfoEntity.getRetryCount() + 1);
            updateWrapper.eq(DmpOrderInfoEntity::getId, dmpOrderInfoEntity.getId());
            this.update(updateWrapper);
        }
    }

}




