package com.erp.server.dmp.pull.service.dmp.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.pull.mapper.DmpOrderInfoMapper;
import com.erp.server.dmp.pull.service.dmp.*;
import com.xxl.job.core.context.XxlJobHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
    private DmpOrderItemService dmpOrderItemService;

    @Resource
    private DmpShopChangeLogService dmpShopChangeLogService;

    @Resource
    private DmpSkuInfoService dmpSkuInfoService;
    @Resource
    private SysUserFeign sysUserFeign;

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
        lambdaQueryWrapper.last("LIMIT 1");
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
        lambdaQueryWrapper.eq(DmpOrderInfoEntity::getPlatformSign, dmpOrderInfoEntity.getPlatformSign());
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
        DmpOrderInfoEntity dmpOrderInfoEntity = this.getOrderBySalesRecordNumber(orderInfoEntity.getSalesRecordNumber());
        if (dmpOrderInfoEntity != null) {
            //如果数据有变动需要更新数据库订单信息
            if (!dmpOrderInfoEntity.toString().equals(orderInfoEntity.toString())) {
                orderInfoEntity.setId(dmpOrderInfoEntity.getId());
                this.updateById(orderInfoEntity);
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
        Integer pageSize = 100;
        List<DmpOrderInfoEntity> list = lambdaQuery()
                .in(DmpOrderInfoEntity::getCleanState, new ArrayList<>(Arrays.asList(0, 1)))
                .and(wrapper ->
                        wrapper.isNull(DmpOrderInfoEntity::getChargeId)
                                .or().isNull(DmpOrderInfoEntity::getDeliveryTime)
                                .or().isNull(DmpOrderInfoEntity::getDeptId)
                                .or().isNull(DmpOrderInfoEntity::getSite)
                )
                .orderByAsc(DmpOrderInfoEntity::getRetryCount)
                .orderByAsc(DmpOrderInfoEntity::getId)
                .last("limit " + pageSize)
                .list();
        if (CollectionUtil.isEmpty(list)){
            XxlJobHelper.log("清洗订单数据 cleanOrder 需要清洗数据为空 pageSize={}",  pageSize);
            return;
        }
        List<SysUserDeptDTO> userDeptList = sysUserFeign.getUserDeptList();
        XxlJobHelper.log("userDeptList==> {}", JSONUtil.toJsonStr(userDeptList));
        AtomicInteger times = new AtomicInteger();
        for (DmpOrderInfoEntity dmpOrderInfoEntity : list) {
            LambdaUpdateWrapper<DmpOrderInfoEntity> updateWrapper = new LambdaUpdateWrapper();
            updateWrapper.set(DmpOrderInfoEntity::getRetryCount, dmpOrderInfoEntity.getRetryCount() + 1);
            Integer flag = 0;
            if (0 == dmpOrderInfoEntity.getCleanState()){
                //查询店铺信息获取'负责人','站点信息'同步到订单
                DmpShopInfoEntity shopByShopNo = dmpShopInfoService.getShopByShopNo(dmpOrderInfoEntity.getShopNo(), dmpOrderInfoEntity.getPlatformSign());

                if (shopByShopNo != null) {
                    updateWrapper.set(DmpOrderInfoEntity::getSite, shopByShopNo.getSite());
                    DmpShopChangeLogEntity shopChargeName = dmpShopChangeLogService.getShopChargeName(shopByShopNo.getId(), dmpOrderInfoEntity.getPlatformCreateTime());
                    if (shopChargeName != null && StringUtils.isNotBlank(shopChargeName.getChargeId())) {
                        updateWrapper.set(DmpOrderInfoEntity::getChargeId, shopChargeName.getChargeId());
                        updateWrapper.set(DmpOrderInfoEntity::getChargeName, shopChargeName.getChargeName());
                        flag ++ ;
                    }else if (StringUtils.isNotBlank(shopByShopNo.getChargeId())){
                        // 无变更日志时使用当前负责人
                        updateWrapper.set(DmpOrderInfoEntity::getChargeId, shopByShopNo.getChargeId());
                        updateWrapper.set(DmpOrderInfoEntity::getChargeName, shopByShopNo.getChargeName());
                        flag ++ ;
                    }
                }

                //根据负责人获取部门信息，同步到订单
                if (StringUtils.isNotBlank(dmpOrderInfoEntity.getShopNo())) {
                    DmpShopInfoDTO dmpShopInfoDTO = dmpShopInfoService.queryShopByPlatformList(dmpOrderInfoEntity.getShopNo(), dmpOrderInfoEntity.getPlatformSign(), userDeptList);
                    if (dmpShopInfoDTO != null) {
                        if (StringUtils.isNotBlank(dmpShopInfoDTO.getDeptId())) {
                            updateWrapper.set(DmpOrderInfoEntity::getDeptId, dmpShopInfoDTO.getDeptId());
                        }
                        if (StringUtils.isNotBlank(dmpShopInfoDTO.getDeptName())) {
                            updateWrapper.set(DmpOrderInfoEntity::getDeptName, dmpShopInfoDTO.getDeptName());
                            flag ++ ;
                        }
                    }
                }
                //查询订单商品明细，根据sku查询sku信息，获取'类别'、'品牌' 同步到商品信息
                List<DmpOrderItemEntity> itemEntityList = dmpOrderItemService.getByOrderId(dmpOrderInfoEntity.getId());
                for (DmpOrderItemEntity dmpOrderItemEntity : itemEntityList) {
                    if (StringUtils.isNotBlank(dmpOrderItemEntity.getSkuNo())) {
                        DmpSkuInfoEntity skuBySkuNo = dmpSkuInfoService.getSkuBySkuNo(dmpOrderItemEntity.getSkuNo());

                        if (skuBySkuNo != null) {
                            dmpOrderItemEntity.setCategoryName(skuBySkuNo.getParentCategoryName());
                            dmpOrderItemEntity.setBrandName(skuBySkuNo.getBrandName());
                            LocalDateTime listingTime = skuBySkuNo.getListingTime();
                            Date platformCreateTime = dmpOrderInfoEntity.getPlatformCreateTime();
                            if (null !=  listingTime && null != platformCreateTime) {
                                dmpOrderItemEntity.setNewSign(listingTime.getYear() == LocalDateUtil.date2LocalDateTime(platformCreateTime).getYear() ? 1 : 0);
                                flag ++ ;
                            }
                            flag ++ ;
                            dmpOrderItemService.updateOrderItemByErpOrderItemId(dmpOrderItemEntity);
                        }
                    }

                }
                times.set(2 + itemEntityList.size() * 2);
                updateWrapper.set(flag >= times.get(), DmpOrderInfoEntity::getCleanState, 1);
                updateWrapper.eq(DmpOrderInfoEntity::getId, dmpOrderInfoEntity.getId());
            }
            //查询发货详情获取发货时间，同步到订单信息
            DmpDeliveryDetailInfoEntity deliveryDetailOrderNo = dmpDeliveryDetailInfoService.getDeliveryDetailOrderNo(dmpOrderInfoEntity.getPlatformOrderId());
            if (deliveryDetailOrderNo != null) {
                updateWrapper.set(DmpOrderInfoEntity::getDeliveryTime, deliveryDetailOrderNo.getDeliveryDate());
                updateWrapper.set(times.get() <= flag || 1 == dmpOrderInfoEntity.getCleanState(), DmpOrderInfoEntity::getCleanState, 2);
            }
            this.update(updateWrapper);
            XxlJobHelper.log("update(updateWrapper)==> {} dmpOrderInfoEntity={}", updateWrapper.getCustomSqlSegment(), JSONUtil.toJsonStr(dmpOrderInfoEntity));

        }
    }

    @Override
    public DmpOrderInfoEntity getOrderBySalesRecordNumber(String salesRecordNumber) {
        return lambdaQuery().eq(DmpOrderInfoEntity::getSalesRecordNumber, salesRecordNumber)
                .last("limit 1")
                .one();
    }

}




