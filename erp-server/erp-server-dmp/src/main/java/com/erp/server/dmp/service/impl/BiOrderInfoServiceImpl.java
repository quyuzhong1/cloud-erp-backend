package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.enums.SyncStatusEnum;
import com.erp.model.dmp.dto.BiShopInfoDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SalesDataReportEnum;
import com.erp.model.dmp.vo.CleanAmountAfterVO;
import com.erp.model.dmp.vo.SyncDataReportVO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.pull.mapper.BiOrderInfoMapper;
import com.erp.server.dmp.service.*;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 订单服务类
 */
@Slf4j
@Service
public class BiOrderInfoServiceImpl extends ServiceImpl<BiOrderInfoMapper, BiOrderInfoEntity>
        implements BiOrderInfoService {

    @Resource
    private BiDeliveryDetailInfoService biDeliveryDetailInfoService;

    @Resource
    private BiDmpShopInfoService biDmpShopInfoService;

    @Resource
    private BiOrderItemSplitService biOrderItemSplitService;

    @Resource
    private BiShopChangeLogService biShopChangeLogService;
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CustomerFeign customerFeign;

    private static Integer pageNumber = 1;

    /**
     * 添加订单信息
     *
     * @param biOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     **/
    @Override
    public String add(BiOrderInfoEntity biOrderInfoEntity) {
        this.save(biOrderInfoEntity);
        return biOrderInfoEntity.getId();
    }

    /**
     * 根据平台订单id查询订单信息
     *
     * @param platformOrderId 平台订单id
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     **/
    @Override
    public BiOrderInfoEntity getOrderByPlatformOrderId(String platformOrderId) {
        LambdaQueryWrapper<BiOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(BiOrderInfoEntity::getPlatformOrderId, platformOrderId);
        lambdaQueryWrapper.last("LIMIT 1");
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据平台订单id修改订单信息
     *
     * @param biOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     **/
    @Override
    public Boolean updateOrderByPlatformOrderId(BiOrderInfoEntity biOrderInfoEntity) {
        LambdaQueryWrapper<BiOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(BiOrderInfoEntity::getPlatformOrderId, biOrderInfoEntity.getPlatformOrderId());
        lambdaQueryWrapper.eq(BiOrderInfoEntity::getPlatformSign, biOrderInfoEntity.getPlatformSign());
        return this.update(biOrderInfoEntity, lambdaQueryWrapper);
    }


    @Override
    public Boolean removeOrderByCode(List<String> codes) {
        //删除订单
        LambdaQueryWrapper<BiOrderInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(BiOrderInfoEntity::getPlatformOrderId, codes);
        List<BiOrderInfoEntity> list = baseMapper.selectList(queryWrapper);
        log.info("删除bi_order_info订单：{}", JSON.toJSONString(list));
        if (CollectionUtils.isNotEmpty(list)) {
            //删除明细记录
            list.forEach(dmpOrderInfoEntity -> {
                List<BiOrderItemSplitEntity> itemEntities = biOrderItemSplitService.getByOrderId(dmpOrderInfoEntity.getId());
                biOrderItemSplitService.removeByIds(itemEntities.stream().map(BiOrderItemSplitEntity::getId).collect(Collectors.toList()));
                this.removeById(dmpOrderInfoEntity.getId());
            });
        }
        return Boolean.TRUE;
    }

    /**
     * 校验订单在中台是否存在，存在就修改不存在则新增
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String checkOrder(BiOrderInfoEntity orderInfoEntity) {
        String orderInfoId = "";
        BiOrderInfoEntity biOrderInfoEntity = this.getOrderBySalesRecordNumber(orderInfoEntity.getSalesRecordNumber(), orderInfoEntity.getPlatformOrderId(), orderInfoEntity.getPlatformSign());
        // 是否为销售订单 避免订单类型修改
        Boolean skipOrderType = StringUtils.isEmpty(orderInfoEntity.getOrderTypeName()) || !"销售订单".equals(orderInfoEntity.getOrderTypeName());
        // 跳过取消订单 避免状态变更为取消
        Boolean skipCancel = StrUtil.isNotBlank(orderInfoEntity.getPlatformOrderStatus()) && orderInfoEntity.getPlatformOrderStatus().contains("取消");
        boolean isGyyPlatform = PlatformEnum.GYY.getDesc().equals(orderInfoEntity.getPlatformSign());
        if (null != biOrderInfoEntity) {
        	if("erp-oms".equals(biOrderInfoEntity.getPlatformSign()) && "APPROVE".equals(biOrderInfoEntity.getPlatformOrderStatus())) {
        		return biOrderInfoEntity.getId();
        	}
            // 删除已存在取消订单  和非销售订单
            if (isGyyPlatform && (skipOrderType || skipCancel)) {
                removeById(biOrderInfoEntity.getId());
                List<BiOrderItemSplitEntity> list = biOrderItemSplitService.lambdaQuery()
                        .eq(BiOrderItemSplitEntity::getOrderId, biOrderInfoEntity.getId())
                        .list();
                if (CollectionUtil.isNotEmpty(list)) {
                    biOrderItemSplitService.removeByIds(list.stream().map(BiOrderItemSplitEntity::getId).collect(Collectors.toList()));
                }
                return orderInfoId;
            }
            //如果数据有变动需要更新数据库订单信息
            if (!biOrderInfoEntity.toString().equals(orderInfoEntity.toString())) {
                orderInfoEntity.setId(biOrderInfoEntity.getId());
                updateById(orderInfoEntity);
            }
            orderInfoId = biOrderInfoEntity.getId();
        } else {
            // 跳过不存在取消订单 和非销售订单
            if (isGyyPlatform && (skipOrderType || skipCancel)) {
                return orderInfoId;
            }
            // 修正状态同步
            orderInfoEntity.setCorrectionStatus(orderInfoEntity.getOrderStatus());
            orderInfoId = add(orderInfoEntity);
        }
        if (StrUtil.isBlank(orderInfoId)) {
            throw new RuntimeException("DmpOrderInfoServiceImpl>>>checkOrder>>>销售订单保存失败");
        }
        List<BiOrderItemSplitEntity> itemList = orderInfoEntity.getItemList();
        if (CollectionUtil.isEmpty(itemList)) {
            return orderInfoId;
        }
        String orderId = orderInfoId;
        List<BiOrderItemSplitEntity> biOrderItemSplitEntityList = itemList.stream().peek(entity -> entity.setOrderId(orderId)).collect(Collectors.toList());
        log.debug("订单明细拆分：{}" , JSON.toJSONString(biOrderItemSplitEntityList));

        //保存未拆分数据
        biOrderItemSplitService.checkOrderItem(itemList, orderInfoEntity.getPlatformCreateTime().toLocalDate(), orderInfoEntity.getPlatformSign());
        return orderInfoId;
    }

    /**
     * 清洗订单数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    @Override
    public void cleanOrder(Integer pageSize) {
        System.setProperty("sun.net.client.defaultConnectTimeout", String
                .valueOf(20000));// （单位：毫秒）
        System.setProperty("sun.net.client.defaultReadTimeout", String
                .valueOf(20000)); // （单位：毫秒）
        List<BiOrderInfoEntity> list = new ArrayList<>();
        try {
            list = lambdaQuery()
                    .in(BiOrderInfoEntity::getCleanState, new ArrayList<>(Arrays.asList(0, 1)))
                    .and(wrapper ->
                            wrapper.eq(BiOrderInfoEntity::getChargeId, "")
                                    .or().isNull(BiOrderInfoEntity::getDeliveryTime)
                                    .or().eq(BiOrderInfoEntity::getDeptId, "")
                                    .or().eq(BiOrderInfoEntity::getSite, "")
                    )
                    .orderByAsc(BiOrderInfoEntity::getRetryCount, BiOrderInfoEntity::getId)
                    .last("LIMIT " + pageSize + " OFFSET " + (pageNumber-1) * pageSize)
                    .list();
        } catch (Exception e) {
            XxlJobHelper.log("查询需要清洗的数据时报错， message={}", e.getMessage());
        }
        pageNumber++;
        if (CollectionUtil.isEmpty(list)) {
            pageNumber = 1;
            XxlJobHelper.log("清洗订单数据 cleanOrder 需要清洗数据为空 pageSize={}", pageSize);
            return;
        }

        //根据店铺编码查询ERP客户的负责人和部门
        List<String> shopNoList = list.stream().filter(req -> PlatformEnum.KINGDEE.getDesc().equals(req.getPlatformSign())).map(req -> req.getShopNo()).collect(Collectors.toList());
        List<CustomerDTO.SellerUserDeptDTO> sellerUserDeptDTOS = customerFeign.listSellerUserDepByCodes(shopNoList);


        List<SysUserDeptDTO> userDeptList = sysUserFeign.getUserDeptList();
        XxlJobHelper.log("userDeptList==> {}", JSONUtil.toJsonStr(userDeptList));
        list.parallelStream().forEach(dmpOrderInfoEntity -> {
            try {
                this.cleanDmpOrderInfo(userDeptList, dmpOrderInfoEntity, sellerUserDeptDTOS);
                XxlJobHelper.log("update( dmpOrderInfoEntity={})完成", JSONUtil.toJsonStr(dmpOrderInfoEntity));
            } catch (Exception e) {
                XxlJobHelper.log("update( dmpOrderInfoEntity={})失败====》", JSONUtil.toJsonStr(dmpOrderInfoEntity));
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanDmpOrderInfo(List<SysUserDeptDTO> userDeptList, BiOrderInfoEntity biOrderInfoEntity, List<CustomerDTO.SellerUserDeptDTO> sellerUserDeptDTOS) {
        LambdaUpdateWrapper<BiOrderInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(BiOrderInfoEntity::getRetryCount, biOrderInfoEntity.getRetryCount() + 1);
        boolean deliveryTimeTag = false;
        if (0 == biOrderInfoEntity.getCleanState()) {
            if (PlatformEnum.KINGDEE.getDesc().equals(biOrderInfoEntity.getPlatformSign())) {
                CustomerDTO.SellerUserDeptDTO sellerUserDeptDTO = sellerUserDeptDTOS.stream().filter(req -> req.getCode().equals(biOrderInfoEntity.getShopNo())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(sellerUserDeptDTO)) {
                    updateWrapper.set(BiOrderInfoEntity::getSite, sellerUserDeptDTO.getCountryId());
                    updateWrapper.set(BiOrderInfoEntity::getChargeId, sellerUserDeptDTO.getSellerId());
                    updateWrapper.set(BiOrderInfoEntity::getChargeName, sellerUserDeptDTO.getSellerName());
                    updateWrapper.set(BiOrderInfoEntity::getDeptId, sellerUserDeptDTO.getDeptId());
                    updateWrapper.set(BiOrderInfoEntity::getDeptName, sellerUserDeptDTO.getDeptName());
                }
            } else {
                //查询店铺信息获取'负责人','站点信息'同步到订单
                BiShopInfoEntity shopByShopNo = biDmpShopInfoService.getShopByShopNo(biOrderInfoEntity.getShopNo());
                if (null != shopByShopNo) {
                    if (StringUtils.isNotBlank(shopByShopNo.getSite())) {
                        updateWrapper.set(BiOrderInfoEntity::getSite, shopByShopNo.getSite());
                    }

                    BiShopChangeLogEntity shopChargeName = biShopChangeLogService.getShopChargeName(shopByShopNo.getId(), biOrderInfoEntity.getPlatformCreateTime());
                    if (null != shopChargeName && StringUtils.isNotBlank(shopChargeName.getChargeId())) {
                        if (!(Objects.equals(shopChargeName.getChargeId(), biOrderInfoEntity.getChargeId()) && Objects.equals(shopChargeName.getChargeName(), biOrderInfoEntity.getChargeName()))) {
                            updateWrapper.set(BiOrderInfoEntity::getChargeId, shopChargeName.getChargeId());
                            updateWrapper.set(BiOrderInfoEntity::getChargeName, shopChargeName.getChargeName());
                        }
                    } else if (StringUtils.isNotBlank(shopByShopNo.getChargeId())) {
                        // 无变更日志时使用当前负责人
                        if (!(Objects.equals(shopByShopNo.getChargeId(), biOrderInfoEntity.getChargeId()) && Objects.equals(shopByShopNo.getChargeName(), biOrderInfoEntity.getChargeName()))) {
                            updateWrapper.set(BiOrderInfoEntity::getChargeId, shopByShopNo.getChargeId());
                            updateWrapper.set(BiOrderInfoEntity::getChargeName, shopByShopNo.getChargeName());
                        }
                    }
                }

                //根据负责人获取部门信息，同步到订单
                if (StringUtils.isNotBlank(biOrderInfoEntity.getShopNo())) {
                    BiShopInfoDTO dmpShopInfoDTO = biDmpShopInfoService.queryShopByPlatformList(biOrderInfoEntity.getShopNo(), biOrderInfoEntity.getPlatformSign(), userDeptList);
                    if (dmpShopInfoDTO != null && StringUtils.isNotBlank(dmpShopInfoDTO.getDeptId()) && StringUtils.isNotBlank(dmpShopInfoDTO.getDeptName())) {
                        if (!(Objects.equals(dmpShopInfoDTO.getDeptId(), biOrderInfoEntity.getDeptId()) && Objects.equals(dmpShopInfoDTO.getDeptName(), biOrderInfoEntity.getDeptName()))) {
                            updateWrapper.set(BiOrderInfoEntity::getDeptId, dmpShopInfoDTO.getDeptId());
                            updateWrapper.set(BiOrderInfoEntity::getDeptName, dmpShopInfoDTO.getDeptName());
                        }
                    }
                }
            }



            /*           //查询订单商品明细，根据sku查询sku信息，获取'类别'、'品牌' 同步到商品信息
            List<DmpOrderItemEntity> itemEntityList = dmpOrderItemService.getByOrderId(dmpOrderInfoEntity.getId());
            for (DmpOrderItemEntity dmpOrderItemEntity : itemEntityList) {
                if (StringUtils.isNotBlank(dmpOrderItemEntity.getSkuNo())) {
                    DmpSkuInfoEntity skuBySkuNo = dmpSkuInfoService.getSkuBySkuNo(dmpOrderItemEntity.getSkuNo(), ApiKingdeeOrganizationEnum.ORGANIZATION_WEIJI.getCode());
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
                            updateStatus = true;
                        }
                        if (updateStatus){
                            dmpOrderItemService.updateOrderItemByErpOrderItemId(dmpOrderItemEntity);
                        }
                    }
                }
            }*/
        }
        //查询发货详情获取发货时间，同步到订单信息
        BiDeliveryDetailInfoEntity deliveryDetailOrderNo = biDeliveryDetailInfoService.getByPlatformOrderId(biOrderInfoEntity.getPlatformOrderId());
        if (null != deliveryDetailOrderNo) {
            updateWrapper.set(BiOrderInfoEntity::getDeliveryTime, deliveryDetailOrderNo.getDeliveryDate());
            deliveryTimeTag = true;
        }
        updateWrapper.set(deliveryTimeTag, BiOrderInfoEntity::getCleanState, biOrderInfoEntity.getCleanState() + 1);
        updateWrapper.eq(BiOrderInfoEntity::getId, biOrderInfoEntity.getId());
        this.update(updateWrapper);
    }

    @Override
    public List<CleanAmountAfterVO> getCleanOrderList() {
        List<CleanAmountAfterVO> vo = baseMapper.getCleanList();
        return vo;
    }

    @Override
    @Async("AsyncDataPhysicalThreadPool")
    public CompletableFuture<SyncDataReportVO> salesDataToPhysical(SalesDataReportEnum reportEnum) {
        String threadName = Thread.currentThread().getName();
        log.info("线程:{} 开始调用，输出：{}",threadName,JSONObject.toJSONString(reportEnum));
        SyncDataReportVO syncDataReportVO = new SyncDataReportVO();
        syncDataReportVO.setName(reportEnum.getName());
        syncDataReportVO.setCode(reportEnum.getCode());
        try {
            baseMapper.runSalesDataToPhysicalSql(reportEnum.getCode());
            syncDataReportVO.setStatus(SyncStatusEnum.SUCCESS_SYNC.getCode());
        } catch (Exception e) {
            log.info("调用异常：{}", JSONObject.toJSONString(e));
            syncDataReportVO.setStatus(SyncStatusEnum.FAILED_SYNC.getCode());
            syncDataReportVO.setMsg(e.getMessage());
        }

        log.info("线程:{} 结束调用，输出：{}",threadName,JSONObject.toJSONString(reportEnum));
        return CompletableFuture.completedFuture(syncDataReportVO);
    }

    public BiOrderInfoEntity getOrderBySalesRecordNumber(String salesRecordNumber, String platformOrderId, String platformSign) {
        return lambdaQuery().eq(BiOrderInfoEntity::getSalesRecordNumber, salesRecordNumber)
                .eq(BiOrderInfoEntity::getPlatformOrderId, platformOrderId)
                .eq(BiOrderInfoEntity::getPlatformSign, platformSign)
                .one();
    }

}




