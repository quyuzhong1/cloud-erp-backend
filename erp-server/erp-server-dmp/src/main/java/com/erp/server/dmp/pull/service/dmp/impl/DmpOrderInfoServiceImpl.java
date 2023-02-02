package com.erp.server.dmp.pull.service.dmp.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.vo.CleanAmountAfterVO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.pull.mapper.DmpOrderInfoMapper;
import com.erp.server.dmp.pull.service.dmp.*;
import com.xxl.job.core.context.XxlJobHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单服务类
 */
@Service
public class DmpOrderInfoServiceImpl extends ServiceImpl<DmpOrderInfoMapper, DmpOrderInfoEntity>
    implements DmpOrderInfoService {

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
    public void cleanOrder(Integer pageSize) {
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
        list.parallelStream().forEach(dmpOrderInfoEntity -> {
            try {
                this.cleanDmpOrderInfo(userDeptList, dmpOrderInfoEntity);
                XxlJobHelper.log("update( dmpOrderInfoEntity={})完成", JSONUtil.toJsonStr(dmpOrderInfoEntity));
            }catch (Exception e) {
                XxlJobHelper.log("update( dmpOrderInfoEntity={})失败====》", JSONUtil.toJsonStr(dmpOrderInfoEntity));
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanDmpOrderInfo(List<SysUserDeptDTO> userDeptList, DmpOrderInfoEntity dmpOrderInfoEntity) {
        LambdaUpdateWrapper<DmpOrderInfoEntity> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.set(DmpOrderInfoEntity::getRetryCount, dmpOrderInfoEntity.getRetryCount() + 1);
        Boolean tag = false;
        if (0 == dmpOrderInfoEntity.getCleanState()){
            //查询店铺信息获取'负责人','站点信息'同步到订单
            DmpShopInfoEntity shopByShopNo = dmpShopInfoService.getShopByShopNo(dmpOrderInfoEntity.getShopNo(), dmpOrderInfoEntity.getPlatformSign());
            if (shopByShopNo != null) {
                if(!Objects.equals(dmpOrderInfoEntity.getSite(), shopByShopNo.getSite())){
                    updateWrapper.set(DmpOrderInfoEntity::getSite, shopByShopNo.getSite());
                }
                DmpShopChangeLogEntity shopChargeName = dmpShopChangeLogService.getShopChargeName(shopByShopNo.getId(), dmpOrderInfoEntity.getPlatformCreateTime());
                if (shopChargeName != null && StringUtils.isNotBlank(shopChargeName.getChargeId())) {
                    if(!(Objects.equals(shopChargeName.getChargeId(), dmpOrderInfoEntity.getChargeId()) && Objects.equals(shopChargeName.getChargeName(), dmpOrderInfoEntity.getChargeName()))){
                        updateWrapper.set(DmpOrderInfoEntity::getChargeId, shopChargeName.getChargeId());
                        updateWrapper.set(DmpOrderInfoEntity::getChargeName, shopChargeName.getChargeName());
                    }
                    tag = true;
                }else if (StringUtils.isNotBlank(shopByShopNo.getChargeId())){
                    // 无变更日志时使用当前负责人
                    if(!(Objects.equals(shopByShopNo.getChargeId(), dmpOrderInfoEntity.getChargeId()) && Objects.equals(shopByShopNo.getChargeName(), dmpOrderInfoEntity.getChargeName()))){
                        updateWrapper.set(DmpOrderInfoEntity::getChargeId, shopByShopNo.getChargeId());
                        updateWrapper.set(DmpOrderInfoEntity::getChargeName, shopByShopNo.getChargeName());
                    }
                    tag = true;
                }
            }

            //根据负责人获取部门信息，同步到订单
            tag = false;
            if (StringUtils.isNotBlank(dmpOrderInfoEntity.getShopNo())) {
                DmpShopInfoDTO dmpShopInfoDTO = dmpShopInfoService.queryShopByPlatformList(dmpOrderInfoEntity.getShopNo(), dmpOrderInfoEntity.getPlatformSign(), userDeptList);
                if (dmpShopInfoDTO != null &&StringUtils.isNotBlank(dmpShopInfoDTO.getDeptId()) &&StringUtils.isNotBlank(dmpShopInfoDTO.getDeptName())) {
                    if (!(Objects.equals(dmpShopInfoDTO.getDeptId(),dmpOrderInfoEntity.getDeptId()) && Objects.equals(dmpShopInfoDTO.getDeptName(), dmpOrderInfoEntity.getDeptName()))) {
                        updateWrapper.set(DmpOrderInfoEntity::getDeptId, dmpShopInfoDTO.getDeptId());
                        updateWrapper.set(DmpOrderInfoEntity::getDeptName, dmpShopInfoDTO.getDeptName());
                    }
                    tag = true;
                }
            }
            //查询订单商品明细，根据sku查询sku信息，获取'类别'、'品牌' 同步到商品信息
            List<DmpOrderItemEntity> itemEntityList = dmpOrderItemService.getByOrderId(dmpOrderInfoEntity.getId());
            tag = false;
            for (DmpOrderItemEntity dmpOrderItemEntity : itemEntityList) {
                tag = false;
                if (StringUtils.isNotBlank(dmpOrderItemEntity.getSkuNo())) {
                    DmpSkuInfoEntity skuBySkuNo = dmpSkuInfoService.getSkuBySkuNo(dmpOrderItemEntity.getSkuNo());
                    boolean updateStatus = false;
                    if (skuBySkuNo != null) {
                        if(!Objects.equals(skuBySkuNo.getParentCategoryName(),dmpOrderItemEntity.getCategoryName())){
                            dmpOrderItemEntity.setCategoryName(skuBySkuNo.getParentCategoryName());
                            updateStatus = true;
                        }
                        if(!Objects.equals(skuBySkuNo.getBrandName(),dmpOrderItemEntity.getBrandName())){
                            dmpOrderItemEntity.setCategoryName(skuBySkuNo.getBrandName());
                            updateStatus = true;
                        }
                        LocalDateTime listingTime = skuBySkuNo.getListingTime();
                        LocalDateTime platformCreateTime = dmpOrderInfoEntity.getPlatformCreateTime();
                        if (null !=  listingTime && null != platformCreateTime) {
                            dmpOrderItemEntity.setNewSign(listingTime.getYear() == platformCreateTime.getYear() ? 1 : 0);
                            tag = true;
                            updateStatus = true;
                        }
                        if (updateStatus){
                            dmpOrderItemService.updateOrderItemByErpOrderItemId(dmpOrderItemEntity);
                        }
                    }
                }
            }
            updateWrapper.set(tag, DmpOrderInfoEntity::getCleanState, 1);
            updateWrapper.eq(DmpOrderInfoEntity::getId, dmpOrderInfoEntity.getId());
        }
        //查询发货详情获取发货时间，同步到订单信息
        DmpDeliveryDetailInfoEntity deliveryDetailOrderNo = dmpDeliveryDetailInfoService.getDeliveryDetailOrderNo(dmpOrderInfoEntity.getPlatformOrderId());
        if (null != deliveryDetailOrderNo) {
            updateWrapper.set(DmpOrderInfoEntity::getDeliveryTime, deliveryDetailOrderNo.getDeliveryDate());
            updateWrapper.set(tag || 1 == dmpOrderInfoEntity.getCleanState(), DmpOrderInfoEntity::getCleanState, 2);
        }
        this.update(updateWrapper);
    }

    @Override
    public List<CleanAmountAfterVO> getCleanOrderList() {
        List<CleanAmountAfterVO> vo = baseMapper.getCleanList();
        return vo;
    }

    @Override
    public DmpOrderInfoEntity getOrderBySalesRecordNumber(String salesRecordNumber) {
        return lambdaQuery().eq(DmpOrderInfoEntity::getSalesRecordNumber, salesRecordNumber)
                .last("limit 1")
                .one();
    }

}




