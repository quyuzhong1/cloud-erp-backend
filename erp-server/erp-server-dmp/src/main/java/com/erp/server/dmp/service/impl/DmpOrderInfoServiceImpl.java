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
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SalesDataReportEnum;
import com.erp.model.dmp.vo.CleanAmountAfterVO;
import com.erp.model.dmp.vo.SyncDataReportVO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.pull.mapper.DmpOrderInfoMapper;
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
    private SysUserFeign sysUserFeign;

    @Resource
    private CustomerFeign customerFeign;


    /**
     * 添加订单信息
     *
     * @param dmpOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     **/
    @Override
    public String add(DmpOrderInfoEntity dmpOrderInfoEntity) {
        this.save(dmpOrderInfoEntity);
        return dmpOrderInfoEntity.getId();
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
    public DmpOrderInfoEntity getOrderByPlatformOrderId(String platformOrderId) {
        LambdaQueryWrapper<DmpOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpOrderInfoEntity::getPlatformOrderId, platformOrderId);
        lambdaQueryWrapper.last("LIMIT 1");
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据平台订单id修改订单信息
     *
     * @param dmpOrderInfoEntity 订单信息
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     **/
    @Override
    public Boolean updateOrderByPlatformOrderId(DmpOrderInfoEntity dmpOrderInfoEntity) {
        LambdaQueryWrapper<DmpOrderInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpOrderInfoEntity::getPlatformOrderId, dmpOrderInfoEntity.getPlatformOrderId());
        lambdaQueryWrapper.eq(DmpOrderInfoEntity::getPlatformSign, dmpOrderInfoEntity.getPlatformSign());
        return this.update(dmpOrderInfoEntity, lambdaQueryWrapper);
    }

    @Override

    @Transactional(rollbackFor = Exception.class)
    public Boolean removeOrderByIds(List<String> ids) {
        //删除订单
        List<DmpOrderInfoEntity> list = baseMapper.selectBatchIds(ids);
        log.info("删除dmp_order_info订单：{}", JSON.toJSONString(list));
        if (CollectionUtils.isNotEmpty(list)) {
            //删除明细记录
            list.forEach(dmpOrderInfoEntity -> {
                List<DmpOrderItemEntity> itemEntities = dmpOrderItemService.getByOrderId(dmpOrderInfoEntity.getId());
                dmpOrderItemService.removeByIds(itemEntities.stream().map(DmpOrderItemEntity::getId).collect(Collectors.toList()));
                this.removeById(dmpOrderInfoEntity.getId());
            });
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean removeOrderByCode(List<String> codes) {
        //删除订单
        LambdaQueryWrapper<DmpOrderInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DmpOrderInfoEntity::getPlatformOrderId, codes);
        List<DmpOrderInfoEntity> list = baseMapper.selectList(queryWrapper);
        log.info("删除dmp_order_info订单：{}", JSON.toJSONString(list));
        if (CollectionUtils.isNotEmpty(list)) {
            //删除明细记录
            list.forEach(dmpOrderInfoEntity -> {
                List<DmpOrderItemEntity> itemEntities = dmpOrderItemService.getByOrderId(dmpOrderInfoEntity.getId());
                dmpOrderItemService.removeByIds(itemEntities.stream().map(DmpOrderItemEntity::getId).collect(Collectors.toList()));
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
    public String checkOrder(DmpOrderInfoEntity orderInfoEntity) {
        String orderInfoId = "";
        DmpOrderInfoEntity dmpOrderInfoEntity = this.getOrderBySalesRecordNumber(orderInfoEntity.getSalesRecordNumber(), orderInfoEntity.getPlatformOrderId(), orderInfoEntity.getPlatformSign());
        // 是否为销售订单 避免订单类型修改
        Boolean skipOrderType = StringUtils.isEmpty(orderInfoEntity.getOrderTypeName()) || !"销售订单".equals(orderInfoEntity.getOrderTypeName());
        // 跳过取消订单 避免状态变更为取消
        Boolean skipCancel = StrUtil.isNotBlank(orderInfoEntity.getPlatformOrderStatus()) && orderInfoEntity.getPlatformOrderStatus().contains("取消");
        boolean isGyyPlatform = PlatformEnum.GYY.getDesc().equals(orderInfoEntity.getPlatformSign());
        if (null != dmpOrderInfoEntity) {
            // 删除已存在取消订单  和非销售订单
            if (isGyyPlatform && (skipOrderType || skipCancel)) {
                removeById(dmpOrderInfoEntity.getId());
                List<DmpOrderItemEntity> list = dmpOrderItemService.lambdaQuery()
                        .eq(DmpOrderItemEntity::getOrderId, dmpOrderInfoEntity.getId())
                        .list();
                if (CollectionUtil.isNotEmpty(list)) {
                    dmpOrderItemService.removeByIds(list.stream().map(DmpOrderItemEntity::getId).collect(Collectors.toList()));
                }
                return orderInfoId;
            }
            //如果数据有变动需要更新数据库订单信息
            if (!dmpOrderInfoEntity.toString().equals(orderInfoEntity.toString())) {
                orderInfoEntity.setId(dmpOrderInfoEntity.getId());
                updateById(orderInfoEntity);
            }
            orderInfoId = dmpOrderInfoEntity.getId();
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
        List<DmpOrderItemEntity> itemList = orderInfoEntity.getItemList();
        if (CollectionUtil.isEmpty(itemList)) {
            return orderInfoId;
        }
        String orderId = orderInfoId;
        itemList.stream().peek(entity -> entity.setOrderId(orderId)).collect(Collectors.toList());
        dmpOrderItemService.checkOrderItem(itemList, orderInfoEntity.getPlatformCreateTime().toLocalDate(), orderInfoEntity.getPlatformSign());
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
        List<DmpOrderInfoEntity> list = new ArrayList<>();
        try {
            list = lambdaQuery()
                    .in(DmpOrderInfoEntity::getCleanState, new ArrayList<>(Arrays.asList(0, 1)))
                    .and(wrapper ->
                            wrapper.eq(DmpOrderInfoEntity::getChargeId, "")
                                    .or().isNull(DmpOrderInfoEntity::getDeliveryTime)
                                    .or().eq(DmpOrderInfoEntity::getDeptId, "")
                                    .or().eq(DmpOrderInfoEntity::getSite, "")
                    )
                    .orderByAsc(DmpOrderInfoEntity::getRetryCount, DmpOrderInfoEntity::getId)
                    .last("limit " + pageSize)
                    .list();
        } catch (Exception e) {
            XxlJobHelper.log("查询需要清洗的数据时报错， message={}", e.getMessage());
        }

        if (CollectionUtil.isEmpty(list)) {
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
    public void cleanDmpOrderInfo(List<SysUserDeptDTO> userDeptList, DmpOrderInfoEntity dmpOrderInfoEntity, List<CustomerDTO.SellerUserDeptDTO> sellerUserDeptDTOS) {
        LambdaUpdateWrapper<DmpOrderInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(DmpOrderInfoEntity::getRetryCount, dmpOrderInfoEntity.getRetryCount() + 1);
        boolean deliveryTimeTag = false;
        if (0 == dmpOrderInfoEntity.getCleanState()) {
            if (PlatformEnum.KINGDEE.getDesc().equals(dmpOrderInfoEntity.getPlatformSign())) {
                CustomerDTO.SellerUserDeptDTO sellerUserDeptDTO = sellerUserDeptDTOS.stream().filter(req -> req.getCode().equals(dmpOrderInfoEntity.getShopNo())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(sellerUserDeptDTO)) {
                    updateWrapper.set(DmpOrderInfoEntity::getChargeId, sellerUserDeptDTO.getSellerId());
                    updateWrapper.set(DmpOrderInfoEntity::getChargeName, sellerUserDeptDTO.getSellerName());
                    updateWrapper.set(DmpOrderInfoEntity::getDeptId, sellerUserDeptDTO.getDeptId());
                    updateWrapper.set(DmpOrderInfoEntity::getDeptName, sellerUserDeptDTO.getDeptName());
                }
            } else {
                //查询店铺信息获取'负责人','站点信息'同步到订单
                DmpShopInfoEntity shopByShopNo = dmpShopInfoService.getShopByShopNo(dmpOrderInfoEntity.getShopNo());
                if (null != shopByShopNo) {
                    if (StringUtils.isNotBlank(shopByShopNo.getSite())) {
                        updateWrapper.set(DmpOrderInfoEntity::getSite, shopByShopNo.getSite());
                    }

                    DmpShopChangeLogEntity shopChargeName = dmpShopChangeLogService.getShopChargeName(shopByShopNo.getId(), dmpOrderInfoEntity.getPlatformCreateTime());
                    if (null != shopChargeName && StringUtils.isNotBlank(shopChargeName.getChargeId())) {
                        if (!(Objects.equals(shopChargeName.getChargeId(), dmpOrderInfoEntity.getChargeId()) && Objects.equals(shopChargeName.getChargeName(), dmpOrderInfoEntity.getChargeName()))) {
                            updateWrapper.set(DmpOrderInfoEntity::getChargeId, shopChargeName.getChargeId());
                            updateWrapper.set(DmpOrderInfoEntity::getChargeName, shopChargeName.getChargeName());
                        }
                    } else if (StringUtils.isNotBlank(shopByShopNo.getChargeId())) {
                        // 无变更日志时使用当前负责人
                        if (!(Objects.equals(shopByShopNo.getChargeId(), dmpOrderInfoEntity.getChargeId()) && Objects.equals(shopByShopNo.getChargeName(), dmpOrderInfoEntity.getChargeName()))) {
                            updateWrapper.set(DmpOrderInfoEntity::getChargeId, shopByShopNo.getChargeId());
                            updateWrapper.set(DmpOrderInfoEntity::getChargeName, shopByShopNo.getChargeName());
                        }
                    }
                }

                //根据负责人获取部门信息，同步到订单
                if (StringUtils.isNotBlank(dmpOrderInfoEntity.getShopNo())) {
                    DmpShopInfoDTO dmpShopInfoDTO = dmpShopInfoService.queryShopByPlatformList(dmpOrderInfoEntity.getShopNo(), dmpOrderInfoEntity.getPlatformSign(), userDeptList);
                    if (dmpShopInfoDTO != null && StringUtils.isNotBlank(dmpShopInfoDTO.getDeptId()) && StringUtils.isNotBlank(dmpShopInfoDTO.getDeptName())) {
                        if (!(Objects.equals(dmpShopInfoDTO.getDeptId(), dmpOrderInfoEntity.getDeptId()) && Objects.equals(dmpShopInfoDTO.getDeptName(), dmpOrderInfoEntity.getDeptName()))) {
                            updateWrapper.set(DmpOrderInfoEntity::getDeptId, dmpShopInfoDTO.getDeptId());
                            updateWrapper.set(DmpOrderInfoEntity::getDeptName, dmpShopInfoDTO.getDeptName());
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
        DmpDeliveryDetailInfoEntity deliveryDetailOrderNo = dmpDeliveryDetailInfoService.getByPlatformOrderId(dmpOrderInfoEntity.getPlatformOrderId());
        if (null != deliveryDetailOrderNo) {
            updateWrapper.set(DmpOrderInfoEntity::getDeliveryTime, deliveryDetailOrderNo.getDeliveryDate());
            deliveryTimeTag = true;
        }
        updateWrapper.set(deliveryTimeTag, DmpOrderInfoEntity::getCleanState, dmpOrderInfoEntity.getCleanState() + 1);
        updateWrapper.eq(DmpOrderInfoEntity::getId, dmpOrderInfoEntity.getId());
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

    public DmpOrderInfoEntity getOrderBySalesRecordNumber(String salesRecordNumber, String platformOrderId, String platformSign) {
        return lambdaQuery().eq(DmpOrderInfoEntity::getSalesRecordNumber, salesRecordNumber)
                .eq(DmpOrderInfoEntity::getPlatformOrderId, platformOrderId)
                .eq(DmpOrderInfoEntity::getPlatformSign, platformSign)
                .one();
    }

}




