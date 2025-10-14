package com.erp.server.wms.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.enums.InventoryClosedRecordEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.wms.dto.StocktakingProfitLossDetailDTO;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.dto.inventory.InventoryTransactionDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.InventoryHisEntity;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.server.wms.service.InventoryClosedRecordService;
import com.erp.server.wms.service.InventoryHisService;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.InventoryTradingService;
import com.erp.server.wms.service.TransactionFlowService;
import com.erp.server.wms.service.VirtualInventoryService;
import com.erp.server.wms.service.WarehouseService;
import com.google.common.base.Stopwatch;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 库存交易辅助类，用于处理各种库存交易操作。
 * @since 2024-07-04
 * @author Edison.Qu
 */
@Slf4j
@Service
public class InventoryTradingRedisServiceImpl implements InventoryTradingService{

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private TransactionFlowService transactionFlowService;
    @Resource
    private InventoryHisService inventoryHisService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private InventoryClosedRecordService inventoryClosedRecordService;
    @Resource
    private StocktakingProfitLossServiceImpl stocktakingProfitLossService;

    @Resource
    private VirtualInventoryService virtualInventoryService;

    @Override
    public void doTransactionList(List<InventoryTransactionDTO> transactionList, String approveType) {
        // 2-移除忽略的sku 先移除避免只存在忽略的sku的单据导致错误
        transactionList.removeIf(InventoryTransactionDTO::isIgnoreTransaction);

        if(CollectionUtils.isEmpty(transactionList)) {
            log.warn("库存交易列表为空！");
            return;
        }
        // 计时器-开始
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            // 1-校验单据是否已经审批
            if(approveType.equals(InventoryTradingService.APPROVE)) {
                this.checkHasApproved(transactionList.get(0));
            }
            // 2-移除忽略的sku
//            transactionList.removeIf(InventoryTransactionDTO::isIgnoreTransaction);
            // 排序
            transactionList = this.sortInventoryTransactionList(transactionList);
            // 3-检查业务是否允许交易
            this.checkAllowTransactionList(transactionList);
            //校验虚拟仓库存
            this.checkVirtualInventoryList(transactionList);
            // 检查库存是否充足
            this.checkInventoryList(transactionList);
            // 4-检查每日库存是否充足
//            this.checkInventoryHisList(transactionList);
            // 5-处理库存更新逻辑
            for (InventoryTransactionDTO transactionDTO : transactionList) {
                this.doTransaction(transactionDTO,approveType.equals(InventoryTradingService.APPROVE));
            }
            // 6-反审核时，批量删除交易记录
            if(approveType.equals(InventoryTradingService.UNAPPROVE)){
                List<String> ids = this.getTransactionFlowIds(transactionList);
                this.deleteTransactionFlowList(ids);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            stopwatch.stop();
            // 计时器-结束
            if(stopwatch.elapsed(TimeUnit.SECONDS) > 30) {
                log.warn("单据编号：{}，库存交易耗时：{} ms", transactionList.get(0).getSourceCode(),stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        }
    }

    /**
     * 处理库存交易
     * @param transactionDTO 库存交易信息
     * @param isApprove     是否审批
     */
    public void doTransaction(InventoryTransactionDTO transactionDTO, boolean isApprove) {
        this.checkInventoryList(Collections.singletonList(transactionDTO));
        // 更新库存
        this.updateInventory(transactionDTO);
        // 更新库存历史
//        this.updateInventoryHis(transactionDTO);
        // 保存当前审批的交易记录
        if(isApprove){
            this.saveCurrTransactionFlow(transactionDTO);
        }
        // 更新库存交易记录 剩余库存
        this.updateInventoryTransaction(transactionDTO,isApprove);
    }

    /**
     * 校验单据是否已经审批
     * @param transactionDTO    交易记录
     */
    private void checkHasApproved(InventoryTransactionDTO transactionDTO) {
        //采购订单结束交货不需要校验
        if (CharSequenceUtil.equals(transactionDTO.getDictBizType(),InventoryBusinessTypeEnum.PURCHASE_ORDER_FINISH.getCode())) {
            return;
        }
        TransactionFlowEntity transactionFlow;
        LambdaQueryWrapper<TransactionFlowEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransactionFlowEntity::getSourceType, transactionDTO.getSourceType())
                .eq(TransactionFlowEntity::getSourceId, transactionDTO.getSourceId())
                .eq(TransactionFlowEntity::getDictInventoryStatus, transactionDTO.getInventoryStatus())
                .eq(TransactionFlowEntity::getDictBizType, transactionDTO.getDictBizType())
                .eq(TransactionFlowEntity::getIsUnapproved, false)
                .select(TransactionFlowEntity::getId)
                .last("limit 1");
        transactionFlow = transactionFlowService.getOne(wrapper);
        if(transactionFlow != null) {
            ServiceException.runError("重复审批！单据编号=[{}] 已存在库存流水,请刷新后查看单据状态", transactionDTO.getSourceCode());
        }
    }

    /**
     * 排序
     * @param transactionList   交易记录列表
     * @return  排序后的交易记录列表
     */
    private List<InventoryTransactionDTO> sortInventoryTransactionList(List<InventoryTransactionDTO> transactionList) {
        Comparator<InventoryTransactionDTO> comparing = Comparator.comparing(InventoryTransactionDTO::getSkuId)
                .thenComparing(InventoryTransactionDTO::getWarehouseId)
                .thenComparing(x -> CharSequenceUtil.isNotEmpty(x.getWarehouseLocation()) ? x.getWarehouseLocation() : "")
                .thenComparing(x -> CharSequenceUtil.isNotEmpty(x.getInventoryStatus()) ? x.getInventoryStatus() : "");

        transactionList = transactionList.stream().sorted(comparing).collect(Collectors.toList());
        return transactionList;
    }

    /**
     * 检查业务是否允许交易
     * @param transactionList   交易记录列表
     */
    private void checkAllowTransactionList(List<InventoryTransactionDTO> transactionList) {
        // 检查关账
        this.checkClosePeriod(transactionList);
        // 检查盘点中
        this.checkStocktaking(transactionList);
        // 检查是否晚与盘盈盘亏
        this.checkAfterProfitLoss(transactionList);
    }

    /**
     * 检查关账
     * @param transactionList   交易记录列表
     */
    private void checkClosePeriod(List<InventoryTransactionDTO> transactionList) {
        StringBuilder errList = new StringBuilder();

        if(CollectionUtils.isEmpty(transactionList)) {
            return;
        }
        LocalDate billDate = transactionList.get(0).getBillDate();

        // 查询最新库存关账记录
        Map<String, LocalDate> closedDateMap = inventoryClosedRecordService.mapByOrgId(InventoryClosedRecordEnum.STK.getCode());

        for(InventoryTransactionDTO transactionDTO:transactionList) {
            LocalDate closeDate = closedDateMap.get(transactionDTO.getOrgId());

            // 存在关账时间并非在途库存
            if(null != closeDate && !InventoryStatusEnum.WITHOUT_LIMIT_CLOSE_ACCOUNT_STATUS.contains(transactionDTO.getInventoryStatus())){
                if (!billDate.isAfter(closeDate)) {
                    errList.append(CharSequenceUtil.format("库存组织:[{}]交易时间:[{}]已关账目，sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}] 不允许交易\n"
                            , transactionDTO.getOrgName()
                            , billDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            , transactionDTO.getSkuNo()
                            , transactionDTO.getWarehouseName()
                            , transactionDTO.getWarehouseLocationName()
                            , transactionDTO.getInventoryStatusName()));
                }
            }
        }

        if(errList.length() > 0) {
            ServiceException.runError(errList.toString());
        }
    }

    /**
     * 检查盘点中
     * @param transactionList   交易记录列表
     */
    private void checkStocktaking(List<InventoryTransactionDTO> transactionList) {
        StringBuilder errList = new StringBuilder();
        for(InventoryTransactionDTO transactionDTO:transactionList) {
            String redisKey = CharSequenceUtil.format(RedisKeyConstant.INVENTORY_LOCK,
                    "*"
                    , transactionDTO.getOrgId()
                    , transactionDTO.getWarehouseId()
                    , transactionDTO.getWarehouseLocation()
                    , transactionDTO.getSkuId()
                    , transactionDTO.getInventoryStatus());
            Collection<String> keys = redisUtil.keys(redisKey);
            if (CollUtil.isEmpty(keys)) {
                continue;
            }
            WarehouseDTO.UpdateDTO updateDTO = warehouseService.detailWithCache(transactionDTO.getWarehouseId());
            String warehouseName = ObjectUtil.isNotEmpty(updateDTO) ? updateDTO.getName() : transactionDTO.getWarehouseId();

            errList.append(CharSequenceUtil.format("sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}]正在盘点中,不允许交易\n"
                    , transactionDTO.getSkuNo()
                    , warehouseName
                    , transactionDTO.getWarehouseLocationName()
                    , transactionDTO.getInventoryStatusName()));

        }

        if(errList.length() > 0) {
            ServiceException.runError(errList.toString());
        }
    }

    /**
     * 检查是否晚与盘盈盘亏
     * @param transactionList   交易记录列表
     */
    private void checkAfterProfitLoss(List<InventoryTransactionDTO> transactionList) {
        StringBuilder errList = new StringBuilder();

        if(CollectionUtils.isEmpty(transactionList)) {
            return;
        }
        LocalDate billDate = transactionList.get(0).getBillDate();

        List<String> warehouseIds = transactionList.stream().map(InventoryTransactionDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<String> orgIds = transactionList.stream().map(InventoryTransactionDTO::getOrgId).distinct().collect(Collectors.toList());
        List<String> skuIds = transactionList.stream().map(InventoryTransactionDTO::getSkuId).distinct().collect(Collectors.toList());

        List<StocktakingProfitLossDetailDTO.LastDTO> lastStocktakingProfitLossList = stocktakingProfitLossService.maxDateByParams(warehouseIds, orgIds, skuIds);
        if(CollectionUtils.isEmpty(lastStocktakingProfitLossList)) {
            return;
        }

        for(InventoryTransactionDTO transactionDTO:transactionList) {
            if (!InventoryStatusEnum.IN_TRANSIT.getCode().equals(transactionDTO.getInventoryStatus())){
                // 盘盈盘亏单 匹配 仓库ID, 组织ID，仓位，skuId
                StocktakingProfitLossDetailDTO.LastDTO lastDTO = lastStocktakingProfitLossList.stream()
                        .filter(e -> e.getSkuId().equalsIgnoreCase(transactionDTO.getSkuId())
                                && e.getWarehouseOrgId().equalsIgnoreCase(transactionDTO.getOrgId())
                                && e.getWarehouseId().equalsIgnoreCase(transactionDTO.getWarehouseId())
                                && e.getWarehouseLocation().equals(null == transactionDTO.getWarehouseLocation() ? "" : transactionDTO.getWarehouseLocation())
                                )
                        .findFirst()
                        .orElse(null);
                if (null != lastDTO && (billDate.isBefore(lastDTO.getBillDate()) || billDate.equals(lastDTO.getBillDate()))){
                    // 已有盘盈盘亏单【{}】不允许操作【{}】之前单据
                    errList.append(CharSequenceUtil.format("sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}]单据日期:[{}],已有盘盈盘亏单【{}】不允许操作【{}】之前单据\n"
                            , transactionDTO.getSkuNo()
                            , transactionDTO.getWarehouseName()
                            , transactionDTO.getWarehouseLocationName()
                            , transactionDTO.getInventoryStatusName()
                            , billDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            , lastDTO.getCode()
                            , lastDTO.getBillDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
                }
            }
        }
        if (errList.length() > 0) {
            ServiceException.runError(errList.toString());
        }
    }

    /**
     * 校验虚拟仓库存
     * @author will
     * @date 2024/8/8 16:00
     * @param transactionList
     */
    private void checkVirtualInventoryList (List<InventoryTransactionDTO> transactionList) {
        if (CollectionUtils.isEmpty(transactionList)) {
            return;
        }
        /**
         * 1. 调拨单：手动创建、调拨申请下推
         * 2. 其他出库单：
         * 3. 采购退货单：
         * 4. 委外发料：正常领料、超出领料
         * 5. 加工单：组装、拆卸
         * 6.采购入库单
         * 7.销售退货入库单
         * 8.b2c发货拦截单
         */
        List<String> typeList = Arrays.asList(InventorySourceTypeEnum.OTHER_OUTSTOCK.getCode()
                ,InventorySourceTypeEnum.OTHER_INSTOCK.getCode()
                ,InventorySourceTypeEnum.PURCHASE_RETURN_ORDER.getCode()
                ,InventorySourceTypeEnum.RECEIVE_MATERIAL.getCode()
                ,InventorySourceTypeEnum.RETURN_MATERIAL.getCode()
                ,InventorySourceTypeEnum.MACHINE_INFO.getCode()
                ,InventorySourceTypeEnum.PURCHASE_STOCK_IN.getCode()
                ,InventorySourceTypeEnum.SO_RETURN_INSTOCK.getCode()
                ,InventorySourceTypeEnum.SO_B2C_DELIVERY_INTERCEPT.getCode()
        );
        //以上类型出可用时需要进行分配数量校验
        List<InventoryTransactionDTO> checkTransactionList = transactionList.stream().filter(obj ->
                        MathUtil.compareTo(obj.getQty(), MathUtil.ZERO ) < MathUtil.ZERO
                        && InventoryStatusEnum.USABLE.getCode().equals(obj.getInventoryStatus())
                        && (typeList.contains(obj.getSourceType()) || Arrays.asList(InventoryBusinessTypeEnum.DIRECT_ALLOCATE.getCode(),InventoryBusinessTypeEnum.DIRECT_ALLOCATE_APPLY.getCode()).contains(obj.getDictBizType())))
                        .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(checkTransactionList)) {
            return;
        }
        //仓库id集合
        List<String> warehouseIdList = checkTransactionList.stream().map(InventoryTransactionDTO::getWarehouseId).distinct().collect(Collectors.toList());
        //skuId集合
        List<String> skuIdList = checkTransactionList.stream().map(InventoryTransactionDTO::getSkuId).distinct().collect(Collectors.toList());

        //虚拟仓库存
        List<VirtualInventoryDTO.WarehouseInventoryQtyDTO> warehouseInventoryQtyList = virtualInventoryService.listInventoryQtyByWarehouseId(warehouseIdList, skuIdList);

        //实体仓可用库存
        InventoryQtyDTO.SkuInventoryStatusParamDTO dto = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
        dto.setWarehouseIdList(warehouseIdList);
        dto.setSkuIdList(skuIdList);
        dto.setInventoryStatusList(Arrays.asList(InventoryStatusEnum.USABLE.getCode(),InventoryStatusEnum.FROZEN.getCode()));
        List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> skuInventoryTotalList = inventoryService.listSkuInventory(dto);

        Map<String, List<InventoryTransactionDTO>> map = checkTransactionList.stream().collect(Collectors.groupingBy(obj -> obj.getSkuId().concat(obj.getWarehouseId())));
        for (Map.Entry<String, List<InventoryTransactionDTO>> entry : map.entrySet()) {
            List<InventoryTransactionDTO> value = entry.getValue();
            String skuId = value.get(0).getSkuId();
            String warehouseId = value.get(0).getWarehouseId();
            //虚拟库存校验
            Integer virtualQty = warehouseInventoryQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(),warehouseId) && CharSequenceUtil.equals(obj.getSkuId(),skuId))
                    .map(VirtualInventoryDTO.WarehouseInventoryQtyDTO::getQty).findFirst().orElse(MathUtil.ZERO);
            if(MathUtil.compareTo(virtualQty,MathUtil.ZERO) == MathUtil.ZERO) {
                continue;
            }
            //仓库可用库存
            Integer realInventoryTotal = skuInventoryTotalList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(),warehouseId) && CharSequenceUtil.equals(obj.getSkuId(),skuId))
                    .map(InventoryQtyDTO.SkuInventoryStatusTotalDTO::getInventoryTotal).reduce(MathUtil.ZERO,Integer::sum);
            //需要出库存数量
            Integer qty = value.stream().map(InventoryTransactionDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);
            log.info("仓库【{}】，SKU【{}】，已分配库存【{}】，实体参可用库存【{}】",value.get(0).getWarehouseName(),value.get(0).getSkuNo(),virtualQty,realInventoryTotal);
            if (Math.abs(qty) > realInventoryTotal - virtualQty) {
                ServiceException.runError(ApiError.ERROR_CHECK_OUT_VIRTUAL_INVENTORY,value.get(0).getSkuNo(),value.get(0).getWarehouseName(),virtualQty,realInventoryTotal - virtualQty);
            }
        }


    }

    /**
     * 检查所有库存交易，库存是否充足
     * @param transactionList   交易记录列表
     */
    private void checkInventoryList(List<InventoryTransactionDTO> transactionList) {
        
    }

    /**
     * 检查所有库存交易，每日库存是否充足
     * @param transactionList   交易记录列表
     */
    private void checkInventoryHisList(List<InventoryTransactionDTO> transactionList) {
        StringBuilder errList = new StringBuilder();
        for(InventoryTransactionDTO transactionDTO:transactionList) {
            errList.append(checkInventoryHis(transactionDTO));
        }
        if(errList.length() > 0) {
            ServiceException.runError(errList.toString());
        }
    }

    /**
     * 检查库存是否充足
     * @param transactionDTO    库存交易信息
     */
    private String checkInventory(InventoryTransactionDTO transactionDTO) {
        int inventoryQty = 0;

        if(transactionDTO.isAllowNegativeInventory()) {
            return "";
        }

        if(null != transactionDTO.getInventoryId()) {
            InventoryEntity inventoryEntity = inventoryService.getById(transactionDTO.getInventoryId());
            inventoryQty = (null == inventoryEntity ? 0 : inventoryEntity.getQty());

        }
        if(inventoryQty + transactionDTO.getQty() < 0) {
            return CharSequenceUtil.format("库存不足：sku=[{}],仓库=[{}],仓位=[{}],库存状态=[{}],库存:{},交易数:{},缺少数：{}\n"
                    , transactionDTO.getSkuNo()
                    , transactionDTO.getWarehouseName()
                    , transactionDTO.getWarehouseLocationName()
                    , transactionDTO.getInventoryStatusName()
                    , inventoryQty
                    , transactionDTO.getQty()
                    , -(inventoryQty + transactionDTO.getQty())
            );
        }
        return "";
    }

    /**
     * 检查每日库存是否充足
     * @param transactionDTO    库存交易信息
     * @return  错误信息
     */
    private String checkInventoryHis(InventoryTransactionDTO transactionDTO) {
        if(transactionDTO.isAllowNegativeInventory()) {
            return "";
        }

        if(null != transactionDTO.getInventoryId()) {
            List<InventoryHisEntity> inventoryHisList = inventoryHisService.list(new LambdaQueryWrapper<InventoryHisEntity>()
                    .eq(InventoryHisEntity::getInfoId, transactionDTO.getInventoryId())
                    .ge(InventoryHisEntity::getBillDate, transactionDTO.getBillDate()));

            for (InventoryHisEntity inventoryHisEntity : inventoryHisList) {
                int inventoryQty = inventoryHisEntity.getQty();
                if (inventoryQty + transactionDTO.getQty() < 0) {
                    return CharSequenceUtil.format("交易会导致[{}]库存不足：sku=[{}],仓库=[{}],仓位=[{}],库存状态=[{}]，当日库存:{},交易数:{}\n"
                            , inventoryHisEntity.getBillDate()
                            , transactionDTO.getSkuNo()
                            , transactionDTO.getWarehouseName()
                            , transactionDTO.getWarehouseLocationName()
                            , transactionDTO.getInventoryStatusName()
                            , inventoryQty
                            , transactionDTO.getQty());
                }
            }
        }

        return "";
    }

    /**
     * 更新库存
     * @param transactionDTO    库存交易信息
     */
    private void updateInventory(InventoryTransactionDTO transactionDTO) {
        InventoryEntity inventoryEntity;
        if(null != transactionDTO.getInventoryId()) {
            inventoryEntity = inventoryService.getById(transactionDTO.getInventoryId());
        }else{
            inventoryEntity = inventoryService.getInventory(transactionDTO);
        }

        log.info("####InventoryTradingServiceImpl===>updateInventory====>inventoryEntity = {}  transactionDTO={}", JSON.toJSONString(inventoryEntity), JSON.toJSONString(transactionDTO));
        if(null == inventoryEntity || null == inventoryEntity.getId()) {
            log.warn("####InventoryTradingServiceImpl===>updateInventory====>inventoryEntity = {}  transactionDTO={}", JSON.toJSONString(inventoryEntity), JSON.toJSONString(transactionDTO));
            throw new ServiceException(ApiError.ERROR_INVENTORY_NOT_EXIST, transactionDTO.getWarehouseName(), transactionDTO.getSkuNo(), transactionDTO.getInventoryStatusName());
        }

        if(null == transactionDTO.getInventoryId()) {
            //方面后续记录流水与历史库存
            transactionDTO.setInventoryId(inventoryEntity.getId());
        }

    }

    /**
     * 更新库存历史
     * @param transactionDTO    库存交易信息
     */
    public void updateInventoryHis(InventoryTransactionDTO transactionDTO) {
        if(null == transactionDTO.getInventoryId()) {
            ServiceException.runError("sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}],inventory_id为空,请让【实施工程师】协调开发人员处理",
                    transactionDTO.getSkuNo(),transactionDTO.getWarehouseName(),transactionDTO.getWarehouseLocationName(),transactionDTO.getInventoryStatusName());
        }

        // 查询当天历史库存
        saveInventoryCurrentday(transactionDTO);

        // 更新当天之后的历史库存
        LambdaUpdateWrapper<InventoryHisEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.setSql("qty = qty + " + transactionDTO.getQty())
                .set(InventoryHisEntity::getUpdateTime, LocalDateTime.now())
                .set(InventoryHisEntity::getUpdateUserId, transactionDTO.getUserId())
                .set(InventoryHisEntity::getUpdateUserName, transactionDTO.getUserName())
                //条件
                .eq(InventoryHisEntity::getInfoId, transactionDTO.getInventoryId())
                .gt(InventoryHisEntity::getBillDate, transactionDTO.getBillDate());

        inventoryHisService.update(wrapper);
    }

    /**
     * 保存当天历史库存
     * @param transactionDTO    库存交易信息
     */
    private void saveInventoryCurrentday(InventoryTransactionDTO transactionDTO) {
        InventoryHisEntity inventoryHis = this.queryInventoryHisLast(transactionDTO.getInventoryId(), transactionDTO.getBillDate(),true);

        if(null == inventoryHis) {
            // 查询当天以前的库存
            inventoryHis = this.queryInventoryHisLast(transactionDTO.getInventoryId(), transactionDTO.getBillDate(),false);
            int inventoryQty = (null == inventoryHis) ? 0 : inventoryHis.getQty();

            inventoryHis = new InventoryHisEntity();
            inventoryHis.setInfoId(transactionDTO.getInventoryId());
            inventoryHis.setBillDate(transactionDTO.getBillDate());
            inventoryHis.setQty(inventoryQty+ transactionDTO.getQty());
            inventoryHis.setCreateUserId(transactionDTO.getUserId());
            inventoryHis.setCreateUserName(transactionDTO.getUserName());
            inventoryHis.setCreateTime(LocalDateTime.now());
            inventoryHis.setUpdateUserId(transactionDTO.getUserId());
            inventoryHis.setUpdateUserName(transactionDTO.getUserName());
            inventoryHis.setUpdateTime(LocalDateTime.now());

            inventoryHisService.save(inventoryHis);
        }else {
            // 更新当天历史库存
            LambdaUpdateWrapper<InventoryHisEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.setSql("qty = qty + " + transactionDTO.getQty())
                    .set(InventoryHisEntity::getUpdateTime, LocalDateTime.now())
                    .set(InventoryHisEntity::getUpdateUserId, transactionDTO.getUserId())
                    .set(InventoryHisEntity::getUpdateUserName, transactionDTO.getUserName())
                    //条件
                    .eq(InventoryHisEntity::getId, inventoryHis.getId());

            inventoryHisService.update(wrapper);
        }
    }

    /**
     * 查询库存历史
     * @param inventoryId   库存id
     * @param billDate      交易日期
     * @param isOnlyCurrBillDate   是否只查询当天的历史
     * @return  库存历史
     */
    private InventoryHisEntity queryInventoryHisLast(String inventoryId, LocalDate billDate, boolean isOnlyCurrBillDate) {
        LambdaQueryWrapper<InventoryHisEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(InventoryHisEntity::getInfoId, inventoryId)
                .orderByDesc(InventoryHisEntity::getBillDate)
                .orderByDesc(InventoryHisEntity::getId)
                .last("limit 1");
        if(isOnlyCurrBillDate) {
            queryWrapper.eq(InventoryHisEntity::getBillDate, billDate);
        }else{
            queryWrapper.lt(InventoryHisEntity::getBillDate, billDate);
        }
        return inventoryHisService.getOne(queryWrapper);

    }

    /**
     * 更新库存交易 的库存数量
     * @param transactionDTO    库存交易信息
     * @param isApprove     是否审批
     */
    private void updateInventoryTransaction(InventoryTransactionDTO transactionDTO,boolean isApprove) {
        if(null == transactionDTO.getInventoryId()) {
            ServiceException.runError("sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}],inventory_id为空,请让【实施工程师】协调开发人员处理",
                    transactionDTO.getSkuNo(),transactionDTO.getWarehouseName(),transactionDTO.getWarehouseLocationName(),transactionDTO.getInventoryStatusName());
        }
        // 日期大于当前单据日期的流水更新
        LambdaUpdateWrapper<TransactionFlowEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.setSql("cur_inventory_qty = cur_inventory_qty + " + transactionDTO.getQty())
                .set(TransactionFlowEntity::getUpdateTime, LocalDateTime.now())
                //条件
                .eq(TransactionFlowEntity::getInventoryId, transactionDTO.getInventoryId())
                .gt(TransactionFlowEntity::getBillDate, transactionDTO.getBillDate());
        transactionFlowService.update(wrapper);

        if(!isApprove) {
            // 反审核如果当天存在晚于当前流水创建的流水,需要进行流水重算
            LambdaUpdateWrapper<TransactionFlowEntity> wrapperToday = new LambdaUpdateWrapper<>();
            wrapperToday.setSql("cur_inventory_qty = cur_inventory_qty + " + transactionDTO.getQty())
                    .set(TransactionFlowEntity::getUpdateTime, LocalDateTime.now())
                    //条件
                    .eq(TransactionFlowEntity::getInventoryId, transactionDTO.getInventoryId())
                    .eq(TransactionFlowEntity::getBillDate, transactionDTO.getBillDate())
                    .gt(TransactionFlowEntity::getId, transactionDTO.getId());
            transactionFlowService.update(wrapperToday);
        }
    }

    /**
     * 获取最后一次交易记录的剩余库存数量
     * @param transactionDTO   交易记录
     * @return  最后一次交易记录的剩余库存数量
     */
    private int getLastTransactionInventoryQty(InventoryTransactionDTO transactionDTO) {
        int initInventoryQty = 0;
        LambdaQueryWrapper<TransactionFlowEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(TransactionFlowEntity::getInventoryId, transactionDTO.getInventoryId())
                .le(TransactionFlowEntity::getBillDate, transactionDTO.getBillDate())
                .eq(TransactionFlowEntity::getIsUnapproved, false)
                .orderByDesc(TransactionFlowEntity::getBillDate)
                .orderByDesc(TransactionFlowEntity::getId)
                .last("limit 1");

        TransactionFlowEntity transactionFlow =transactionFlowService.getOne(queryWrapper);
        return null == transactionFlow ? initInventoryQty : transactionFlow.getCurInventoryQty();
    }

    /**
     * 保存交易记录
     * @param transactionDTO   交易记录列表
     */
    private void saveCurrTransactionFlow(InventoryTransactionDTO transactionDTO) {
        TransactionFlowEntity transactionFlowEntity = new TransactionFlowEntity();

        // 交易头部信息
        transactionFlowEntity.setId(transactionDTO.getId());
        transactionFlowEntity.setTransactionNo(transactionDTO.getTransactionNo());
        transactionFlowEntity.setTransactionRuleId(transactionDTO.getTransactionRuleId());

        // 交易明细信息
        transactionFlowEntity.setInventoryId(transactionDTO.getInventoryId());
        transactionFlowEntity.setSkuId(transactionDTO.getSkuId());
        transactionFlowEntity.setSkuNo(transactionDTO.getSkuNo());
        transactionFlowEntity.setOrgId(transactionDTO.getOrgId());
        transactionFlowEntity.setWarehouseId(transactionDTO.getWarehouseId());
        transactionFlowEntity.setWarehouseName(transactionDTO.getWarehouseName());
        transactionFlowEntity.setWarehouseLocation(transactionDTO.getWarehouseLocation());
        transactionFlowEntity.setDictInventoryStatus(transactionDTO.getInventoryStatus());

        // 交易时间 & 单据类型
        transactionFlowEntity.setBillDate(transactionDTO.getBillDate());
        transactionFlowEntity.setTradeTime(LocalDateTime.now());
        transactionFlowEntity.setDictBizType(transactionDTO.getDictBizType());
        transactionFlowEntity.setSourceType(transactionDTO.getSourceType());
        transactionFlowEntity.setSourceId(transactionDTO.getSourceId());
        transactionFlowEntity.setSourceCode(transactionDTO.getSourceCode());
        transactionFlowEntity.setSourceDetailId(transactionDTO.getSourceDetailId());

        // 交易数量
        transactionFlowEntity.setQty(transactionDTO.getQty());
        transactionFlowEntity.setCurInventoryQty(getLastTransactionInventoryQty(transactionDTO)+transactionDTO.getQty());

        // 交易人员信息
        transactionFlowEntity.setCreateUserId(transactionDTO.getUserId());
        transactionFlowEntity.setCreateUserName(transactionDTO.getUserName());
        transactionFlowEntity.setCreateTime(LocalDateTime.now());

        transactionFlowEntity.setUpdateUserId(transactionDTO.getUserId());
        transactionFlowEntity.setUpdateUserName(transactionDTO.getUserName());
        transactionFlowEntity.setUpdateTime(LocalDateTime.now());

        transactionFlowEntity.setUserId(transactionDTO.getUserId());
        transactionFlowEntity.setTradeTime(LocalDateTime.now());
        transactionFlowEntity.setOperationMode("approve");

        transactionFlowService.save(transactionFlowEntity);
    }

    /**
     * 获取交易记录id列表
     * @param transactionList   交易记录列表
     * @return  交易记录id列表
     */
    private List<String> getTransactionFlowIds(List<InventoryTransactionDTO> transactionList) {
        return transactionList.stream()
                .map(InventoryTransactionDTO::getId)
                .collect(Collectors.toList());
    }

    /**
     * 批量删除交易记录
     * @param idList    交易记录id列表
     */
    private void deleteTransactionFlowList(List<String> idList) {
        // 设置每页大小
        int pageSize = 1000;

        // 创建迭代器
        Iterator<String> iterator = idList.iterator();

        // 按页批量保存
        while (iterator.hasNext()) {
            List<String> page = IteratorUtils.toList(IteratorUtils.boundedIterator(iterator, pageSize));
            transactionFlowService.removeByIds(page);
        }

    }


}