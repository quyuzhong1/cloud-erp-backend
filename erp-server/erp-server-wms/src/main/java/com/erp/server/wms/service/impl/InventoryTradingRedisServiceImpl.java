package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.erp.model.wms.dto.StocktakingTaskDetailDTO;
import com.erp.server.wms.service.*;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.InventoryClosedRecordEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.business.constant.RedisCacheConstants;
import com.erp.model.wms.dto.StocktakingTaskDetailDTO;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryTransactionDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.InventoryHisEntity;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.server.wms.config.PgUnallocLockSynchronizationAdapter;
import com.erp.server.wms.util.InventoryUnallocCheckHelper;
import com.erp.server.wms.service.*;
import com.erp.server.wms.utils.InventoryRedisUtil;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IteratorUtils;
import org.redisson.RedissonMultiLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private StocktakingTaskDetailService stocktakingTaskDetailService;

    @Resource
    private VirtualInventoryService virtualInventoryService;
    
    @Resource
    private InventoryTransactionService inventoryTransactionService;

    @Resource
    private InventoryRedisUtil inventoryRedisUtil;

    @Override
    public void doTransactionList(List<InventoryTransactionDTO> transactionList, String approveType) {

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
            // 2-检查业务是否允许交易
            this.checkAllowTransactionList(transactionList);
            // 3-移除忽略的sku 先移除避免只存在忽略的sku的单据导致错误
            transactionList.removeIf(InventoryTransactionDTO::isIgnoreTransaction);
            if(CollectionUtils.isEmpty(transactionList)) {
                log.warn("列表不存在非服务类产品，实际库存交易列表为空！");
                return;
            }
            // 排序
            transactionList = this.sortInventoryTransactionList(transactionList);
            this.executeRedisUnallocInventoryTransaction(transactionList, approveType);
            // 6-反审核时，批量删除交易记录
            if(approveType.equals(InventoryTradingService.UNAPPROVE)){
                List<String> ids = this.getTransactionFlowIds(transactionList);
                this.deleteTransactionFlowList(ids);
            }

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("redis库存交易失败", e);
            throw new ServiceException(ApiError.WAREHOUSE_INVENTORY_FAILED, e.getMessage());
        } finally {
            stopwatch.stop();
            // 计时器-结束
            if(stopwatch.elapsed(TimeUnit.SECONDS) > 30) {
                log.warn("单据编号：{}，库存交易耗时：{} ms", transactionList.get(0).getSourceCode(),stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        }
    }

    /**
     * Redis 路径：在仓+SKU 未分配锁内执行预检、扣减与 TRY，与 PG 路径共用 {@code whsku:unalloc:lock}。
     *
     * @param transactionList 已排序的库存交易列表
     * @param approveType     审批类型
     */
    private void executeRedisUnallocInventoryTransaction(List<InventoryTransactionDTO> transactionList, String approveType) {
        RedissonMultiLock unallocLock = null;
        boolean unlockInFinally = false;
        try {
            if (InventoryUnallocCheckHelper.needsUnallocSharedLock(transactionList)) {
                List<String> unallocLockKeys = InventoryUnallocCheckHelper.buildUnallocLockKeys(transactionList);
                unallocLock = inventoryRedisUtil.tryLock(unallocLockKeys, InventoryUnallocCheckHelper.UNALLOC_LOCK_WAIT_SECONDS);
                if (unallocLock == null) {
                    ServiceException.runError(ApiError.WH_UNALLOC_LOCK_FAILED);
                }
                if (TransactionSynchronizationManager.isActualTransactionActive()) {
                    PgUnallocLockSynchronizationAdapter.registerUnlockAfterTx(unallocLock, inventoryRedisUtil);
                } else {
                    unlockInFinally = true;
                }
            }
            //校验虚拟仓库存（预检计入全部在途预占；TRY 阶段再按流水 ID 解析 operationId）
            this.checkVirtualInventoryList(transactionList);
            // 4-检查每日库存是否充足
//            this.checkInventoryHisList(transactionList);
            // 5-处理库存更新逻辑
            for (InventoryTransactionDTO transactionDTO : transactionList) {
                this.doTransaction(transactionDTO, approveType.equals(InventoryTradingService.APPROVE));
            }
            //5.5-新增库存流水
            this.addInventoryTransaction(transactionList, approveType);
        } finally {
            if (unlockInFinally && unallocLock != null) {
                inventoryRedisUtil.unLock(unallocLock);
            }
        }
    }

    /**
     * 处理库存交易
     * @param transactionDTO 库存交易信息
     * @param isApprove     是否审批
     */
    public void doTransaction(InventoryTransactionDTO transactionDTO, boolean isApprove) {
        // 设置库存id
        this.setInventoryId(transactionDTO);
        // 保存当前审批的交易记录
        if(isApprove){
            this.saveCurrTransactionFlow(transactionDTO);
        }
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
            String redisKey = CharSequenceUtil.format(RedisCacheConstants.INVENTORY_LOCK,
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

        List<StocktakingTaskDetailDTO.LastDTO> lastStocktakingTaskDetailList = stocktakingTaskDetailService.maxDateByParams(warehouseIds, orgIds, skuIds);
        if(CollectionUtils.isEmpty(lastStocktakingTaskDetailList)) {
            return;
        }

        for(InventoryTransactionDTO transactionDTO:transactionList) {
            if (!InventoryStatusEnum.IN_TRANSIT.getCode().equals(transactionDTO.getInventoryStatus())){
                // 盘盈盘亏单 匹配 仓库ID, 组织ID，仓位，skuId
                StocktakingTaskDetailDTO.LastDTO lastDTO = lastStocktakingTaskDetailList.stream()
                        .filter(e -> e.getSkuId().equalsIgnoreCase(transactionDTO.getSkuId())
                                && e.getWarehouseOrgId().equalsIgnoreCase(transactionDTO.getOrgId())
                                && e.getWarehouseId().equalsIgnoreCase(transactionDTO.getWarehouseId())
                                && e.getWarehouseLocation().equals(null == transactionDTO.getWarehouseLocation() ? "" : transactionDTO.getWarehouseLocation())
                                )
                        .findFirst()
                        .orElse(null);


                // 业务规则：根据盘点任务创建日期判断，当业务单据日期 <= 盘点任务创建日期时，禁止操作
                // 判断逻辑：!billDate.isAfter(盘点日期) 等价于 billDate <= 盘点日期
                if (null != lastDTO && !billDate.isAfter(lastDTO.getBillDate())){
                    // 已有盘盈盘亏单【{}】不允许操作【{}】及之前单据
                    errList.append(CharSequenceUtil.format("sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}]单据日期:[{}],已有盘点任务单据【{}】不允许操作【{}】及之前单据\n"
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
     * 校验虚拟仓库存（Redis 路径预检，实体基量与 try.lua 同源；在途仅扣 reserve 一次）。
     * <p>
     * 指定虚拟仓时校验该虚拟仓已分配量；未指定时校验实体仓未分配（含在途预占）。
     * 最终并发放行以 {@code try.lua} 仓+SKU 未分配原子预占为准；此处用于提前失败、减少无效 TRY。
     * 预检 pending 不排除本批（流水 ID 尚未生成，与 PG 路径及 {@link InventoryUnallocCheckHelper#sumPendingReserve} 约定一致），
     * 计入全部在途预占；TRY 阶段再按流水 ID 解析 {@code operationId} 做幂等排除。
     * </p>
     *
     * @param transactionList 库存交易列表
     */
    private void checkVirtualInventoryList(List<InventoryTransactionDTO> transactionList) {
        if (CollectionUtils.isEmpty(transactionList)) {
            return;
        }
        // 预检早于 saveCurrTransactionFlow，无法解析 operationId；与 PG 预检一致传 null
        String excludeTransactionId = null;
        String excludeOperationId = null;
        List<InventoryTransactionDTO> virtualWarehouseCheckList =
                InventoryUnallocCheckHelper.filterNeedVirtualWarehouseCheck(transactionList);
        List<InventoryTransactionDTO> entityUnallocCheckList =
                InventoryUnallocCheckHelper.filterNeedEntityUnallocCheck(transactionList);
        if (CollectionUtils.isEmpty(virtualWarehouseCheckList) && CollectionUtils.isEmpty(entityUnallocCheckList)) {
            return;
        }
        List<InventoryTransactionDTO> allCheckList = new ArrayList<>();
        allCheckList.addAll(virtualWarehouseCheckList);
        allCheckList.addAll(entityUnallocCheckList);
        List<String> warehouseIdList = allCheckList.stream().map(InventoryTransactionDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<String> skuIdList = allCheckList.stream().map(InventoryTransactionDTO::getSkuId).distinct().collect(Collectors.toList());

        VirtualInventoryDTO.RedisVirtualInventoryParamDTO redisParamDTO = new VirtualInventoryDTO.RedisVirtualInventoryParamDTO();
        redisParamDTO.setSkuIdList(skuIdList);
        redisParamDTO.setWarehouseIdList(warehouseIdList);
        List<VirtualInventoryDTO.RedisVirtualInventoryReturnDTO> redisVirtualInventoryList = virtualInventoryService.getRedisVirtualInventory(redisParamDTO);

        InventoryDTO.RedisInventoryParamDTO entityParam = new InventoryDTO.RedisInventoryParamDTO();
        entityParam.setWarehouseIdList(warehouseIdList);
        entityParam.setSkuIdList(skuIdList);
        entityParam.setInventoryStatusList(Arrays.asList(InventoryStatusEnum.USABLE.getCode(), InventoryStatusEnum.FROZEN.getCode()));
        List<InventoryDTO.RedisInventoryReturnDTO> redisEntityInventoryList = inventoryService.listRedisInventoryMeta(entityParam);

        for (Map.Entry<String, List<InventoryTransactionDTO>> entry :
                InventoryUnallocCheckHelper.groupByWarehouseSkuVirtualWarehouse(virtualWarehouseCheckList).entrySet()) {
            List<InventoryTransactionDTO> value = entry.getValue();
            InventoryTransactionDTO first = value.get(0);
            String skuId = first.getSkuId();
            String warehouseId = first.getWarehouseId();
            String virtualWarehouseId = first.getVirtualWarehouseId();
            Integer virtualQty = redisVirtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), warehouseId)
                            && CharSequenceUtil.equals(obj.getSkuId(), skuId)
                            && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), virtualWarehouseId))
                    .map(VirtualInventoryDTO.RedisVirtualInventoryReturnDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer qty = value.stream().map(InventoryTransactionDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);
            log.warn("虚拟仓预检 实体仓【{}】虚拟仓库【{}】SKU【{}】已分配【{}】出库存【{}】",
                    first.getWarehouseName(), first.getVirtualWarehouseName(), first.getSkuNo(), virtualQty, qty);
            if (Math.abs(qty) > virtualQty) {
                ServiceException.runError(ApiError.VM_CHECK_OUT_VIRTUAL_WAREHOUSE_INVENTORY, first.getSkuNo(), first.getWarehouseName(),
                        first.getVirtualWarehouseName(), virtualQty, Math.abs(qty), Math.abs(qty) - virtualQty);
            }
        }

        if (CollectionUtils.isEmpty(entityUnallocCheckList)) {
            return;
        }
        Set<String> entityInventoryIds = new LinkedHashSet<>();
        for (List<InventoryTransactionDTO> group :
                InventoryUnallocCheckHelper.groupByWarehouseSku(entityUnallocCheckList).values()) {
            entityInventoryIds.addAll(InventoryUnallocCheckHelper.filterInventoryIdsByWarehouseSku(
                    redisEntityInventoryList, group.get(0).getWarehouseId(), group.get(0).getSkuId()));
        }
        Map<String, Integer> baseQtyMap = inventoryTransactionService.getRedisBaseQtyByInventoryBatch(entityInventoryIds);

        for (Map.Entry<String, List<InventoryTransactionDTO>> entry :
                InventoryUnallocCheckHelper.groupByWarehouseSku(entityUnallocCheckList).entrySet()) {
            List<InventoryTransactionDTO> value = entry.getValue();
            String skuId = value.get(0).getSkuId();
            String warehouseId = value.get(0).getWarehouseId();
            Integer virtualQty = InventoryUnallocCheckHelper.sumVirtualQty(redisVirtualInventoryList, warehouseId, skuId);
            if (MathUtil.compareTo(virtualQty, MathUtil.ZERO) == MathUtil.ZERO) {
                continue;
            }
            List<String> inventoryIds = InventoryUnallocCheckHelper.filterInventoryIdsByWarehouseSku(
                    redisEntityInventoryList, warehouseId, skuId);
            int realInventoryTotal = InventoryUnallocCheckHelper.sumEntityBaseQtyFromMap(inventoryIds, baseQtyMap);
            int pendingReserve = inventoryTransactionService.getUnallocPendingReserveQty(
                    warehouseId, skuId, excludeTransactionId, excludeOperationId);
            Integer qty = value.stream().map(InventoryTransactionDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);
            int allowedUnalloc = realInventoryTotal - virtualQty - pendingReserve;
            log.warn("未分配预检 仓库【{}】SKU【{}】已分配【{}】Redis实体【{}】在途预占【{}】",
                    value.get(0).getWarehouseName(), value.get(0).getSkuNo(), virtualQty, realInventoryTotal, pendingReserve);
            if (Math.abs(qty) > allowedUnalloc) {
                ServiceException.runError(ApiError.VM_CHECK_OUT_VIRTUAL_INVENTORY, value.get(0).getSkuNo(), value.get(0).getWarehouseName(),
                        virtualQty, Math.max(allowedUnalloc, 0));
            }
        }
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
    private void setInventoryId(InventoryTransactionDTO transactionDTO) {
        InventoryEntity inventoryEntity;
        if(null != transactionDTO.getInventoryId()) {
            inventoryEntity = inventoryService.getById(transactionDTO.getInventoryId());
        }else{
            inventoryEntity = inventoryService.getInventory(transactionDTO);
        }

        log.info("####InventoryTradingServiceImpl===>updateInventory====>inventoryEntity = {}  transactionDTO={}", JSON.toJSONString(inventoryEntity), JSON.toJSONString(transactionDTO));
        if(null == inventoryEntity || null == inventoryEntity.getId()) {
            log.warn("####InventoryTradingServiceImpl===>updateInventory====>inventoryEntity = {}  transactionDTO={}", JSON.toJSONString(inventoryEntity), JSON.toJSONString(transactionDTO));
            throw new ServiceException(ApiError.WH_INV_NOT_EXIST, transactionDTO.getWarehouseName(), transactionDTO.getSkuNo(), transactionDTO.getInventoryStatusName());
        }

        if(null == transactionDTO.getInventoryId()) {
            //方面后续记录流水与历史库存
            transactionDTO.setInventoryId(inventoryEntity.getId());
        }

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
        transactionDTO.setId(transactionFlowEntity.getId());
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

    private void addInventoryTransaction(List<InventoryTransactionDTO> transactionList, String approveType) {
    	inventoryTransactionService.addInventoryTransaction(transactionList, approveType);
	}

}