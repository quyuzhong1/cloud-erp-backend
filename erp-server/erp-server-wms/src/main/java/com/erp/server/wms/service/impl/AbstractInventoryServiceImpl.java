package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.DistributedLockEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.inventory.*;
import com.erp.server.wms.config.InventoryHelper;
import com.erp.server.wms.service.*;
import com.erp.server.wms.utils.InventoryUtils;
import com.google.common.base.Stopwatch;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Classname: AbstractInventoryServiceImpl
 * @Description: 库存交易核心处理逻辑抽象类
 * @CreateTime: 2023-05-04  15:29
 * @Author: zhangchunlin
 */
@Slf4j
public abstract class AbstractInventoryServiceImpl {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private InventoryDetailService inventoryDetailService;

    @Autowired
    private TransactionFlowService transactionFlowService;

    @Autowired
    private InventoryHisService inventoryHisService;

    @Autowired
    public InventoryHelper inventoryHelper;

    @Autowired
    private WarehouseService warehouseService;

    /**
     *
     * @param paramList      参数
     * @param ruleList       规则（调拨自定义时必填）
     * @param businessType   业务类型
     * @param byType         按业务类型
     */
    @Transactional(rollbackFor = Exception.class)
    public <T extends InventoryStockBaseDTO> void approve(List<T> paramList, List<TransactionRuleDTO> ruleList, InventoryBusinessTypeEnum businessType, Boolean byType) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Boolean isInOrOutStock = Objects.equals(byType, Boolean.TRUE) && CollUtil.isEmpty(ruleList);
        log.info("》》》库存交易按【{}】，出入库或调拨【{}】，入参：{}，业务类型：{}", Objects.equals(byType, Boolean.TRUE) ? "业务类型" : "自定义规则", isInOrOutStock ? "出入库" : "调拨", JSONObject.toJSONString(paramList), businessType.getName());
        // 1.验证参数
        List<TransactionRuleDTO> transactionRuleParams;
        if(Objects.equals(byType,Boolean.TRUE)) {
            transactionRuleParams = inventoryHelper.wrapTransactionRule(businessType);
        } else {
            transactionRuleParams = ruleList;
        }
        this.checkParam(paramList, businessType, transactionRuleParams);
        // 2.业务处理，同一个操作产生的交易流水使用同一个关联交易号
        String transactionNo = IdUtil.getSnowflake(1, 1).nextIdStr(); // 关联交易号
        this.stockHandler(paramList, businessType, transactionRuleParams, transactionNo);
        stopwatch.stop();
        log.info("结束库存交易，耗时【{}】秒", stopwatch.elapsed(TimeUnit.SECONDS));
    }

    /**
     * 验证参数（子类实现）
     * @param paramList
     * @param businessType
     * @param transactionRules
     * @param <T>
     */
    public abstract <T extends InventoryStockBaseDTO> void checkParam(List<T> paramList, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRules);

    /**
     * 循环处理业务（子类实现）
     * @param paramLis
     * @param businessType
     * @param transactionRuleParams
     */
    public abstract <T extends InventoryStockBaseDTO> void stockHandler(List<T> paramLis, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams, String transactionNo);

    /**
     * 单个sku处理（子类实现）
     * @param baseParam
     * @param businessType
     * @param transactionRuleParams
     * @param transactionNo
     */
    public abstract <T extends InventoryStockBaseDTO> void singleHandler(T baseParam,  InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams,
                               String transactionNo);

    /**
     * 反审核
     * @param dto
     */
    @Transactional(rollbackFor = Exception.class)
    @SneakyThrows
    public void unApprove(InventoryUnApproveDTO dto) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.info("库存交易反审核，单据类型：{}, 单据id：{}", dto.getSourceType().getName(), dto.getBillId());
        // 根据单据类型和单据id查询出未反审核过的对应的交易流水，一个单据对应多个SKU， 按创建时间正序排序
        List<TransactionFlowEntity> txnFlows = transactionFlowService.getUnApprovedTxnFlows(dto.getSourceType().getCode(), dto.getBillId());
        ValidatorUtil.isTrue(CollUtil.isNotEmpty(txnFlows),()->new ServiceException(ApiError.ERROR_99040));
        // 先按交易时间升序排
        txnFlows = txnFlows.stream().sorted(Comparator.comparing(TransactionFlowEntity::getTradeTime).thenComparing(TransactionFlowEntity::getId)).collect(Collectors.toList());
        String transactionNo = IdUtil.getSnowflake(1, 1).nextIdStr(); // 关联交易号
        txnFlows.stream().forEach(txnFlow->{
            // 此处需注意：1.已经反审核过的单据不允许再次反审核，以免库存数据错乱（前面查询条件已过滤）；2.可能会出现负数，如入库后被出库了反审核后仓库数量不够反审核，增加验证不允许反审核
            // 登记反审核的交易流水（有可能一个操作产生多条，从多个库存明细中扣除）
            InOutStockCoreDTO param = InventoryUtils.convertInoutStockForTxn(txnFlow, InventoryOperationModeEnum.UN_APPROVE);

            InventoryBusinessTypeEnum inventoryBusinessType = InventoryBusinessTypeEnum.of(txnFlow.getDictBizType());// 取原交易流水的业务类型
            Integer operationQty = Math.abs(txnFlow.getQty());// 原操作流水操作数量（取绝对值正数）
            TransactionFlowDTO transactionFlowDTO = InventoryUtils.wrapTransactionFlowInOutStock(param, txnFlow.getInventoryId(),inventoryBusinessType, txnFlow.getInventoryDetailId(), InventoryStatusEnum.of(txnFlow.getDictInventoryStatus()), txnFlow.getInstockBatchDate(), Math.abs(txnFlow.getQty()), txnFlow.getOrgId());
            transactionFlowDTO.setTransactionNo(transactionNo);

            // 按照仓库+SKU进行锁定，考虑库位，防止数据冲突
            String lockKey = StrUtil.format( "{}:{}:{}:{}",DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), txnFlow.getWarehouseId(), StrUtils.null2EmptyWithTrim(txnFlow.getWarehouseLocation()), txnFlow.getSkuId());
            RReadWriteLock rwLock = redisson.getReadWriteLock(lockKey);
            RLock rlock = rwLock.writeLock();// 获取写锁
            boolean isLock;
            try {
                isLock = rlock.tryLock(5, TimeUnit.SECONDS);
                log.info("反审核》》》，仓库：【{}】，组织：【{}】，SKU ID：【{}】，SKU编号：【{}】, 交易业务：【{}】，来源单据类型：【{}】, 单据id：【{}】，SKU编号：【{}】，是否获取到锁: {}", txnFlow.getWarehouseId(), txnFlow.getOrgId(), txnFlow.getSkuNo(), txnFlow.getSkuId(), inventoryBusinessType.getName(), InventorySourceTypeEnum.of(txnFlow.getSourceType()).getName(), param.getSourceId(), param.getSkuNo(), isLock);
                if (!isLock) {
                    throw new ServiceException(ApiError.ERROR_1026);
                }

                Integer originQty = txnFlow.getQty();
                InventoryModeEnum inventoryModeCur = originQty < 0 ? InventoryModeEnum.IN_STOCK : InventoryModeEnum.OUT_STOCK;
                // 判断原库存明细id是否足以扣除库存（反审核入库的时候），反审核库存明细可能需要从别的库存明细出
                InventoryDetailEntity inventoryDetail = inventoryDetailService.getById(txnFlow.getInventoryDetailId());
                WarehouseDTO.UpdateDTO warehouseDetail = warehouseService.detailWithCache(txnFlow.getWarehouseId());
                InventoryStatusEnum inventoryStatusEnum = InventoryStatusEnum.of(txnFlow.getDictInventoryStatus());
                String inventoryStatusName = Optional.ofNullable(inventoryStatusEnum).map(InventoryStatusEnum::getName).orElse("");
                String skuNo = txnFlow.getSkuNo();
                if(Objects.equals(inventoryModeCur, InventoryModeEnum.OUT_STOCK)) {
                    if(inventoryDetail.getQty() < txnFlow.getQty()) {
                        log.info("反审核》》》，仓库：【{}】，组织：【{}】，SKU ID：【{}】，SKU编号：【{}】, 交易业务：【{}】，来源单据类型：【{}】, 单据id：【{}】，SKU编号：【{}】，原库存明细id：【{}】，原库存明细数量【{}】，原交易流水数量【{}】，不足以反审核", txnFlow.getWarehouseId(), txnFlow.getOrgId(), txnFlow.getSkuId(), txnFlow.getSkuNo(), txnFlow.getSkuId(), inventoryBusinessType.getName(), InventorySourceTypeEnum.of(txnFlow.getSourceType()).getName(), param.getSourceId(), param.getSkuNo(), inventoryDetail.getQty(), txnFlow.getQty());
                        if (Objects.nonNull(warehouseDetail) && StrUtil.isNotEmpty(warehouseDetail.getId())) {
                            throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, warehouseDetail.getName(), skuNo, inventoryStatusName));
                        } else {
                            throw new ServiceException(StrUtil.format("【{}】【{}】库存数量不足", skuNo, inventoryStatusName));
                        }
                    }
                }
                // 计算当前反审核后库存数量
                InventoryEntity inventory = inventoryService.getById(txnFlow.getInventoryId());
                Integer transactionInventoryQty = inventory.getQty();
                if(Objects.equals(inventoryModeCur, InventoryModeEnum.IN_STOCK)) {
                    transactionInventoryQty = transactionInventoryQty + operationQty;
                } else {
                    // 检查库存数量是否足够反审核，否则会出现负库存数
                    if(inventory.getQty() < txnFlow.getQty()) {
                        log.info("反审核》》》，仓库：{}，组织：{}，SKU ID：{}，SKU编号：{}, 交易业务：{}，来源单据类型：{}, 单据id：【{}】，SKU编号：【{}】，原库存明细id：【{}】，原库存数量【{}】，原交易流水数量【{}】，不足以反审核", txnFlow.getWarehouseId(), txnFlow.getOrgId(), txnFlow.getSkuNo(), txnFlow.getSkuId(), inventoryBusinessType.getName(), InventorySourceTypeEnum.of(txnFlow.getSourceType()).getName(), param.getSourceId(), param.getSkuNo(), inventory.getQty(), txnFlow.getQty());
                        if (Objects.nonNull(warehouseDetail) && StrUtil.isNotEmpty(warehouseDetail.getId())) {
                            throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, warehouseDetail.getName(), skuNo, inventoryStatusName));
                        } else {
                            throw new ServiceException(StrUtil.format("【{}】【{}】库存数量不足", skuNo, inventoryStatusName));
                        }
                    }
                    transactionInventoryQty = transactionInventoryQty - operationQty;
                }
                Integer symbolQty = Objects.equals(inventoryModeCur, InventoryModeEnum.OUT_STOCK) ? operationQty * -1 : operationQty;
                // 更新库存明细表
                int updateCnt =  inventoryDetailService.updateQtyById(txnFlow.getInventoryDetailId(), symbolQty, inventoryDetail.getVersion());
                if(updateCnt != 1) {
                    throw new ServiceException(ApiError.ERROR_1027);
                }
                // 记录交易流水（反审核的）
                transactionFlowService.add(transactionFlowDTO, inventoryBusinessType, txnFlow.getTransactionRuleId(), transactionInventoryQty, inventoryModeCur);
                // 更新原交易流水为已反审核
                transactionFlowService.updateUnapprovedById(txnFlow.getId(), txnFlow.getVersion());
                // 更新实时库存表数量
                updateCnt =  inventoryService.updateQtyById(inventory.getId(), symbolQty, inventory.getVersion());
                if(updateCnt != 1) {
                    throw new ServiceException(ApiError.ERROR_1027);
                }
                // 更新库存历史表
                InventoryHisEntity inventoryHis = inventoryHisService.findInventory(inventory.getId(), param.getBillDate());
                Integer afterHisQty = Objects.equals(inventoryModeCur, InventoryModeEnum.OUT_STOCK) ? inventoryHis.getQty() - operationQty : inventoryHis.getQty() + operationQty;
                updateCnt =  inventoryHisService.updateQtyById(inventoryHis.getId(), afterHisQty, inventoryHis.getVersion());
                if(updateCnt != 1) {
                    throw new ServiceException(ApiError.ERROR_1027);
                }
            }  catch (Exception e) {
                log.error("反审核》》》，交易业务：【{}】，来源单据：【{}】，单据id：【{}】，SKU编号：【{}】，库存操作异常", inventoryBusinessType.getName(), InventorySourceTypeEnum.of(txnFlow.getSourceType()).getName(), param.getSourceId(), param.getSkuNo(),e );
                if(e instanceof ServiceException) {
                    ServiceException serviceException = (ServiceException) e;
                    throw serviceException;
                } else if(e instanceof InterruptedException) {
                    throw new ServiceException(ApiError.Default);
                }
            } finally {
                //释放锁
                if(rlock.isLocked() && rlock.isHeldByCurrentThread()){ // 锁是否存在，是当前执行线程的锁
                    rlock.unlock(); // 释放锁
                }
            }
            log.info("结束库存交易，耗时【{}】秒", stopwatch.elapsed(TimeUnit.SECONDS));
        });
    }

    /**
     * 入库核心业务逻辑处理
     */
    @SneakyThrows
    public void inStockCore(InOutStockCoreDTO param, InventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String tansactionRuleId, String transactionNo) {
        // 仓库
        String warehouseId = param.getWarehouseId();
        // SKU
        String skuId = param.getSkuId();
        String skuNo = param.getSkuNo();
        // 库位
        String warehouseLocation = param.getWarehouseLocation();
        // 数量
        Integer qty = param.getQty();
        // 来源
        InventorySourceTypeEnum sourceTypeEnum = param.getSourceType();
        // 单据信息
        String sourceId = param.getSourceId();
        // 仓库信息
        WarehouseDTO.UpdateDTO warehouseDetail = warehouseService.detailWithCache(warehouseId);
        if(Objects.isNull(warehouseDetail) || StrUtil.isEmpty(warehouseDetail.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        // 仓库组织
        String orgId = warehouseDetail.getOrgId();
        // 单据日期
        LocalDate billDate = param.getBillDate();
        log.info("交易业务：【{}】，来源单据：【{}】，单据id：【{}】，SKU编号：【{}】，库存状态：【{}】，开始走入库逻辑", businessType.getName(), sourceTypeEnum.getName(), param.getSourceId(), param.getSkuNo(), inventoryStatusEnum.getName());
        // 按照仓库+SKU进行锁定，暂不考虑库位，防止数据冲突
        String lockKey = StrUtil.format( "{}:{}:{}:{}", DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), warehouseId, StrUtils.null2EmptyWithTrim(warehouseLocation) ,skuId);
        RReadWriteLock rwLock = redisson.getReadWriteLock(lockKey);
        RLock rlock = rwLock.writeLock();// 获取写锁
        boolean isLock;
        try {
            isLock = rlock.tryLock(5, TimeUnit.SECONDS);
            log.info("库存状态：【{}】，仓库：【{}】，组织：【{}】，SKU ID：【{}】，SKU编号：【{}】, 交易业务：【{}】，来源单据类型：【{}】, 单据id：【{}】，SKU编号：【{}】，是否获取到锁: {}", inventoryStatusEnum.getName(), warehouseId, orgId, skuId, skuNo, businessType.getName(), sourceTypeEnum.getName(), param.getSourceId(), param.getSkuNo(), isLock);
            if (!isLock) {
                throw new ServiceException(ApiError.ERROR_1026);
            }
            log.info("库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", inventoryStatusEnum.getName(), businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
            log.info("库存状态：【{}】，仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，状态【{}】新增或修改库存", inventoryStatusEnum.getName(), warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName());

            // 此处注意，入库传不传仓位都带仓位条件查询
            InventorySaveDTO inventorySaveDTO = inventoryService.addOrUpdate(warehouseId, orgId, warehouseLocation, skuId, skuNo, inventoryStatusEnum.getCode(), qty);
            String inventoryInfoId = inventorySaveDTO.getInventoryId();
            Integer originInventoryQty = inventorySaveDTO.getQty();// 库存原数量
            Integer afterInventoryQty = originInventoryQty + qty;
            log.info("库存状态：【{}】，仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，状态【{}】，实时库存原数量：【{}】，操作数量【{}】，操作后数量【{}】", inventoryStatusEnum.getName(), warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName(), originInventoryQty, qty, afterInventoryQty);
            log.info("库存状态：【{}】，仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，状态【{}】，单据日期：【{}】,库存表id：【{}】，新增或修改库存明细数据", inventoryStatusEnum.getName(), warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName(), billDate, inventoryInfoId);
            // 入库批次日期取单据日期
            String inventoryDetailId = inventoryDetailService.addOrUpdate(inventoryInfoId, billDate, qty);
            // 登记交易流水
            TransactionFlowDTO transactionFlowDTO = InventoryUtils.wrapTransactionFlowInOutStock(param, inventoryInfoId, businessType, inventoryDetailId, inventoryStatusEnum, billDate, qty, orgId);
            transactionFlowDTO.setTransactionNo(transactionNo);
            transactionFlowService.add(transactionFlowDTO, businessType, tansactionRuleId, afterInventoryQty, InventoryModeEnum.IN_STOCK);

            log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，状态【{}】，单据日期：【{}】,库存表id：【{}】，新增或修改库存历史数据", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName(), billDate, inventoryInfoId);
            // 创建/修改库存历史
            inventoryHisService.addOrUpdate(inventoryInfoId, param.getBillDate(), afterInventoryQty);
        } catch (Exception e) {
            log.error("交易业务：{}，来源单据：{}，单据id：【{}】，SKU编号：【{}】，库存操作异常", businessType.getName(), sourceTypeEnum.getName(), param.getSourceId(), param.getSkuNo(),e );
            if(e instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) e;
                throw serviceException;
            } else {
                throw e;
            }
        } finally {
            //释放锁
            if(rlock.isLocked() && rlock.isHeldByCurrentThread()){ // 锁是否存在，是当前执行线程的锁
                rlock.unlock(); // 释放锁
            }
        }
    }

    /**
     * 出库核心业务处理
     */
    @SneakyThrows
    public void outStockCore (InOutStockCoreDTO param, InventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String tansactionRuleId,
                              String transactionNo) {
        // 仓库
        String warehouseId = param.getWarehouseId();
        // SKU
        String skuId = param.getSkuId();
        String skuNo = param.getSkuNo();
        // 库位
        String warehouseLocation = param.getWarehouseLocation();
        // 数量
        Integer qty = param.getQty();
        // 来源
        InventorySourceTypeEnum sourceTypeEnum = param.getSourceType();
        // 单据信息
        String sourceId = param.getSourceId();
        // 仓库信息
        WarehouseDTO.UpdateDTO warehouseDetail = warehouseService.detailWithCache(warehouseId);
        if(Objects.isNull(warehouseDetail) || StrUtil.isEmpty(warehouseDetail.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        // 仓库组织
        String orgId = warehouseDetail.getOrgId();
        // 单据日期
        LocalDate billDate = param.getBillDate();
        log.info("交易业务：【{}】，来源单据：{}，单据id：【{}】，SKU编号：【{}】，库存状态：【{}】，开始走出库逻辑", businessType.getName(), sourceTypeEnum.getName(), param.getSourceId(), inventoryStatusEnum.getName(), param.getSkuNo());
        // 按照仓库+SKU进行锁定，暂不考虑库位，防止数据冲突
        String lockKey = StrUtil.format( "{}:{}:{}:{}",DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), warehouseId, StrUtils.null2EmptyWithTrim(warehouseLocation), skuId);
        RReadWriteLock rwLock = redisson.getReadWriteLock(lockKey);
        RLock rlock = rwLock.writeLock();// 获取写锁
        boolean isLock;
        try {
            isLock = rlock.tryLock(5, TimeUnit.SECONDS);
            log.info("库存状态：【{}】，仓库：【{}】，组织：【{}】，SKU ID：【{}】，SKU编号：【{}】, 交易业务：【{}】，来源单据：【{}】, 是否获取到锁: {}", inventoryStatusEnum.getName(), warehouseId, orgId, skuId, skuNo, businessType.getName(), sourceTypeEnum.getName(), isLock);
            if (!isLock) {
                throw new ServiceException(ApiError.ERROR_1026);
            }
            log.info("库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", inventoryStatusEnum.getName(), businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
            InventoryEntity inventory = inventoryService.findInventory(orgId, warehouseId, skuId, warehouseLocation, inventoryStatusEnum.getCode());
            String inventoryStatusName = Optional.ofNullable(inventoryStatusEnum).map(InventoryStatusEnum::getName).orElse("");
            if(Objects.isNull(inventory)) {
                log.error("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，状态【{}】在库存实时表中不存在数据，无法出库", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName());
                throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, warehouseDetail.getName(), warehouseLocation, skuNo, inventoryStatusName));
            }
            Integer originQty = inventory.getQty();
            log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，单据日期：【{}】，状态【{}】，库存原数量：【{}】，操作数量【{}】", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), param.getBillDate(), inventoryStatusEnum.getName(), originQty, qty);
            // 判断库存数量是否足够出库
            if(originQty < qty) {
                throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, warehouseDetail.getName(), warehouseLocation, skuNo, inventoryStatusName));
            }
            // 查询库存明细，排序，雪花算法id在单机上是严格递增的，但是在分布式环境下不是严格递增的（因为不同的机器的MAC地址/机器ID/数据中心不一样等等），此处改为按创建时间递增排序
            List<InventoryDetailEntity> inventoryDetails = inventoryDetailService.findListQtyGreatZero(inventory.getId());
            // 循环扣减
            Integer waitOutQty = qty; // 待出库数量
            Integer transactionInventoryQty = originQty;
            for(InventoryDetailEntity inventoryDetailEntity : inventoryDetails) {
                if (waitOutQty == 0) { // 已经足额扣减完成
                    break;
                }
                // 扣减库存明细
                Integer originDetailQty = inventoryDetailEntity.getQty();
                Integer detailDeductQty; // 扣减数量
                if(originDetailQty >= waitOutQty) { //库存明细足够扣减
                    detailDeductQty = waitOutQty;
                    waitOutQty = 0;
                } else { // 不足够扣减，全部扣完库存明细
                    waitOutQty = waitOutQty - originDetailQty;
                    detailDeductQty = originDetailQty;
                }
                // 更新库存明细
                int updateCnt = inventoryDetailService.updateQtyById(inventoryDetailEntity.getId(), detailDeductQty * -1, inventoryDetailEntity.getVersion());
                if(updateCnt != 1) {
                    throw new ServiceException(ApiError.ERROR_1027);
                }
                // 本次更新后库存剩余数量
                transactionInventoryQty = transactionInventoryQty - detailDeductQty;
                // 登记交易流水（有可能一个操作产生多条，从多个库存明细中扣除）
                TransactionFlowDTO transactionFlowDTO = InventoryUtils.wrapTransactionFlowInOutStock(param, inventory.getId(), businessType, inventoryDetailEntity.getId(), inventoryStatusEnum, inventoryDetailEntity.getInstockBatchDate(), detailDeductQty, orgId);
                transactionFlowDTO.setTransactionNo(transactionNo);
                transactionFlowService.add(transactionFlowDTO, businessType, tansactionRuleId, transactionInventoryQty, InventoryModeEnum.OUT_STOCK);
            }
            if(waitOutQty > 0) {
                throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, warehouseDetail.getName(), warehouseLocation, skuNo,  inventoryStatusName));
            }
            // 更新库存表
            Integer afterInventoryQty = originQty - qty;
            int updateCnt =  inventoryService.updateQtyById(inventory.getId(), qty * -1, inventory.getVersion());
            if(updateCnt != 1) {
                throw new ServiceException(ApiError.ERROR_1027);
            }
            log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，单据日期：【{}】，状态【{}】，库存原数量：【{}】，操作数量【{}】，操作后数量【{}】", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), param.getBillDate(), inventoryStatusEnum.getName(), originQty, qty, afterInventoryQty);
            log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，状态【{}】，单据日期：【{}】,库存表id：【{}】，新增或修改库存历史数据", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName(), billDate, inventory.getId());
            // 创建/修改库存历史
            inventoryHisService.addOrUpdate(inventory.getId(), param.getBillDate(), afterInventoryQty);
        }  catch (Exception e) {
            log.error("交易业务：【{}】，来源单据：【{}】，单据id：【{}】，SKU编号：【{}】，库存操作异常", businessType.getName(), sourceTypeEnum.getName(), param.getSourceId(), param.getSkuNo(),e );
            if(e instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) e;
                throw serviceException;
            } else {
                throw e;
            }
        } finally {
            //释放锁
            if(rlock.isLocked() && rlock.isHeldByCurrentThread()){ // 锁是否存在，是当前执行线程的锁
                rlock.unlock(); // 释放锁
            }
        }
    }

}