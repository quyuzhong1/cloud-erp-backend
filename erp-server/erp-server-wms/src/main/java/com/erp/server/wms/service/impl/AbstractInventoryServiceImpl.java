package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.common.business.enums.DistributedLockEnum;
import com.common.business.enums.InventoryClosedRecordEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.StocktakingProfitLossDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.convert.InOutStockCoreConverter;
import com.erp.server.wms.service.*;
import com.erp.server.wms.utils.InventoryUtils;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Classname: AbstractInventoryServiceImpl
 * @Description: 库存交易核心处理逻辑抽象类
 * @CreateTime: 2023-05-04  15:29
 * @Author: zhangchunlin
 */
@Slf4j
public abstract class AbstractInventoryServiceImpl implements InventoryStockService {
    @Autowired
    private RedissonClient redisson;
    @Resource
    private RedisUtil redisUtil;
    @Autowired
    private WarehouseService warehouseService;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Autowired
    private CfgTransactionRulesService cfgTransactionRulesService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InventoryDetailService inventoryDetailService;
    @Autowired
    private TransactionFlowService transactionFlowService;
    @Autowired
    private InventoryHisService inventoryHisService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private InventoryClosedRecordService inventoryClosedRecordService;

    @Resource
    private AbstractInventoryServiceImpl abstractInventoryService;

    @Resource
    private StocktakingProfitLossService stocktakingProfitLossService;

    @Resource
    private VirtualInventoryService virtualInventoryService;


    /**
     * 允许录入负数的库存业务单据（临时打开）
     */
    protected final List<InventorySourceTypeEnum> allowNegativeQtyBusinessList = Lists.newArrayList(InventorySourceTypeEnum.INIT_STOCK);

    @Transactional(rollbackFor = Exception.class)
    public <T extends InventoryStockBaseDTO> void approve(List<T> paramList, List<TransactionRuleDTO> ruleList, InventoryBusinessTypeEnum businessType, Boolean byType) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.warn("》》》库存交易按【{}】，入参：{}，业务类型：{}", Objects.equals(byType, Boolean.TRUE) ? "业务类型" : "自定义规则", JSON.toJSONString(paramList), businessType.getName());
        // 1.验证参数
        List<TransactionRuleDTO> transactionRuleParams;
        if (Objects.equals(byType, Boolean.TRUE)) {
            transactionRuleParams = this.wrapTransactionRule(businessType);
        } else {
            ValidatorUtil.validateEntity(ruleList);
            transactionRuleParams = ruleList;
        }
        this.checkParam(paramList, businessType, transactionRuleParams);

        // 2.业务处理，同一个操作产生的交易流水使用同一个关联交易号
        // 关联交易号
        String transactionNo = IdUtil.getSnowflake().nextIdStr();
        this.stockHandler(paramList, businessType, transactionRuleParams, transactionNo);

        stopwatch.stop();
        log.warn("结束库存交易，耗时【{}】秒", stopwatch.elapsed(TimeUnit.SECONDS));
    }

    /**
     * 验证参数（子类实现）
     *
     * @param paramList        业务参数列表
     * @param businessType     业务类型
     * @param transactionRules 交易规则
     * @param <T>              业务参数 对象类型
     */
//    @Validated({AddGroup.class})
    abstract <T extends InventoryStockBaseDTO> void checkParam(List<T> paramList, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRules);

    /**
     * 循环处理业务（子类实现）
     *
     * @param paramList             业务参数列表
     * @param businessType          业务类型
     * @param transactionRuleParams 交易规则
     */
    abstract <T extends InventoryStockBaseDTO> void stockHandler(List<T> paramList, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams, String transactionNo);

    /**
     * 单个sku处理（子类实现）
     *
     * @param baseParam             业务参数列表
     * @param businessType          业务类型
     * @param transactionRuleParams 交易规则
     * @param transactionNo         交易编号
     */
    abstract <T extends InventoryStockBaseDTO> void singleHandler(T baseParam, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams,
                                                                  String transactionNo);

    @Transactional(rollbackFor = Exception.class)
    @SneakyThrows
    public void unApprove(InventoryUnApproveDTO dto) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.warn("库存交易反审核，单据类型：{}, 单据id：{}", dto.getSourceType().getName(), dto.getBillId());

        // 根据单据类型和单据id查询出未反审核过的对应的交易流水，一个单据对应多个SKU， 按创建时间正序排序
        List<TransactionFlowEntity> txnFlows = transactionFlowService.getUnApprovedTxnFlows(dto.getSourceType().getCode(), dto.getBillId());
        // 没有流水 则不做反向操作：兼容产品属性为费用或服务的sku没有交易流水的情况
        if (CollUtil.isEmpty(txnFlows)) {
            log.warn("库存交易反审核，单据类型：{}, 单据id：{}，未找到未审核过的交易流水，不处理", dto.getSourceType().getName(), dto.getBillId());
            return;
        }
        // 查询最新库存关账记录
        Map<String, LocalDate> closedDateMap = inventoryClosedRecordService.mapByOrgId(InventoryClosedRecordEnum.STK.getCode());
        // 最新盘盈盘亏单有效单据日期列表
        List<String> warehouseIds = txnFlows.stream().map(TransactionFlowEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<String> orgIds = txnFlows.stream().map(TransactionFlowEntity::getOrgId).distinct().collect(Collectors.toList());
        List<String> skuIds = txnFlows.stream().map(TransactionFlowEntity::getSkuId).distinct().collect(Collectors.toList());
        List<StocktakingProfitLossDetailDTO.LastDTO> lastStocktakingProfitLossList = stocktakingProfitLossService.maxDateByParams(warehouseIds, orgIds, skuIds);

        // 关联交易号
        String transactionNo = IdUtil.getSnowflake().nextIdStr();
        // 通过对sku id顺序执行, 避免多线程死锁
        Comparator<TransactionFlowEntity> comparing = Comparator.comparing(TransactionFlowEntity::getSkuId)
                .thenComparing(TransactionFlowEntity::getWarehouseId)
                .thenComparing(x -> StrUtil.isNotEmpty(x.getWarehouseLocation()) ? x.getWarehouseLocation() : "")
                .thenComparing(x -> StrUtil.isNotEmpty(x.getDictInventoryStatus()) ? x.getDictInventoryStatus() : "");
        txnFlows = txnFlows.stream().sorted(comparing).collect(Collectors.toList());
        txnFlows.forEach(txnFlow -> {
            // 检测是否允许库存交易=
            InventoryStatusEnum inventoryStatusEnum = InventoryStatusEnum.getAndCheckByCode(txnFlow.getDictInventoryStatus());
            InventorySourceTypeEnum sourceTypeEnum = InventorySourceTypeEnum.getByCode(txnFlow.getSourceCode());
            checkAllowTransaction(closedDateMap.get(txnFlow.getOrgId()), txnFlow.getOrgId(), txnFlow.getWarehouseId(), txnFlow.getWarehouseLocation(), txnFlow.getSkuId(), txnFlow.getSkuNo(), txnFlow.getDictInventoryStatus(), txnFlow.getBillDate(), inventoryStatusEnum, lastStocktakingProfitLossList, sourceTypeEnum);

            // 获取单据业务类型
            InventoryBusinessTypeEnum businessTypeEnum = InventoryBusinessTypeEnum.getByCode(txnFlow.getDictBizType());// 取原交易流水的业务类型

            // 按照仓库+仓位+库存状态+SKU 进行锁定
            String lockKey = StrUtil.format("{}:{}:{}:{}:{}", DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), txnFlow.getWarehouseId(), StrUtils.null2EmptyWithTrim(txnFlow.getWarehouseLocation()), txnFlow.getDictInventoryStatus(), txnFlow.getSkuId());
            RLock rLock = redisson.getLock(lockKey);
            boolean isLock;
            try {
                // 1，获取锁
                isLock = rLock.tryLock(5, TimeUnit.SECONDS);
                if (!isLock) {
                    log.error("尝试获取锁[{}]失败,操作: 反审核》》》，仓库：【{}】，组织：【{}】，SKU ID：【{}】，SKU编号：【{}】, 交易业务：【{}】，来源单据类型：【{}】, 单据id：【{}】，单据编号：【{}】",
                            lockKey, txnFlow.getWarehouseId(), txnFlow.getOrgId(), txnFlow.getSkuId(), txnFlow.getSkuNo(), businessTypeEnum.getName(), InventorySourceTypeEnum.getByCode(txnFlow.getSourceType()).getName(), txnFlow.getSourceId(), txnFlow.getSourceCode());
                    ServiceException.runError(ApiError.ERROR_1026);
                }
                log.info("尝试获取锁[{}]成功,操作: 反审核》》》，仓库：【{}】，组织：【{}】，SKU ID：【{}】，SKU编号：【{}】, 交易业务：【{}】，来源单据类型：【{}】, 单据id：【{}】，单据编号：【{}】",
                        lockKey, txnFlow.getWarehouseId(), txnFlow.getOrgId(), txnFlow.getSkuId(), txnFlow.getSkuNo(), businessTypeEnum.getName(), InventorySourceTypeEnum.getByCode(txnFlow.getSourceType()).getName(), txnFlow.getSourceId(), txnFlow.getSourceCode());


                // 2，整理 交易流水信息，数量取反，操作模式=unApprove
                txnFlow.setTransactionNo(transactionNo);
                txnFlow.setQty(txnFlow.getQty() * -1);
                txnFlow.setOperationMode(InventoryOperationModeEnum.UN_APPROVE.getCode());

                // 3，获取可用的库存, 数量不够时报错
                InventoryEntity inventory = getAvaliableInventory(txnFlow);
                InventoryDetailEntity inventoryDetail = getAvaliableInventoryDetail(txnFlow);

                // 4，记录交易明细
                txnFlow.setIsUnapproved(Boolean.TRUE);


                // 5，更新库存
                boolean updateFlag = inventoryDetailService.updateQtyById(inventoryDetail.getId(), txnFlow.getQty());
                if (!updateFlag) {
                    ServiceException.runError(ApiError.ERROR_1027);
                }
                updateFlag = inventoryService.updateQtyById(inventory.getId(), txnFlow.getQty());
                if (!updateFlag) {
                    ServiceException.runError(ApiError.ERROR_1027);
                }

                InventoryEntity entity = inventoryService.getById(inventory.getId());
                transactionFlowService.add(txnFlow, entity.getQty());
                inventoryHisService.addOrUpdate(inventory.getId(), LocalDate.now(), entity.getQty());

                // 6,更新原交易流水为已反审核
                transactionFlowService.updateUnapprovedById(txnFlow.getId(), txnFlow.getVersion());
            } catch (Exception e) {
                log.error("反审核》》》，交易业务：【{}】，来源单据：【{}】，单据id：【{}】，SKU编号：【{}】，库存操作异常", businessTypeEnum.getName(), InventorySourceTypeEnum.getByCode(txnFlow.getSourceType()).getName(), txnFlow.getSourceId(), txnFlow.getSkuNo(), e);
                ServiceException.runError(ApiError.Default.code, e.getMessage());
            } finally {
                //释放锁  锁是否存在，是当前执行线程的锁
                if (rLock.isLocked() && rLock.isHeldByCurrentThread()) {
                    // 释放锁
                    rLock.unlock();
                }
            }

            log.warn("结束库存交易，耗时【{}】秒", stopwatch.elapsed(TimeUnit.SECONDS));
        });
    }

    /**
     * 检查库存交易的否允许
     *
     * @param closeDate                     关账时间
     * @param orgId                         组织
     * @param warehouseId                   仓库
     * @param warehouseLocation             仓位
     * @param skuId                         SKU
     * @param dictInventoryStatus           库存状态
     * @param billDate                      单据日期
     * @param inventoryStatusEnum
     * @param lastStocktakingProfitLossList
     * @param sourceType
     */
    private void checkAllowTransaction(LocalDate closeDate, String orgId, String warehouseId, String warehouseLocation, String skuId, String skuNo, String dictInventoryStatus, LocalDate billDate, InventoryStatusEnum inventoryStatusEnum, List<StocktakingProfitLossDetailDTO.LastDTO> lastStocktakingProfitLossList, InventorySourceTypeEnum sourceType) {
        // 库存关账时间检测
        log.info("closeDate:{}", closeDate);

        /**
         * 获取当前登录人
         * 当前登录人为system（金蝶拉取时为system,处理数据时需要）则无需判断关账时间
         * 获取到非system的用户时正常校验
         */
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (!StrUtil.isBlank(userInfo.getUid())) {
            // 存在关账时间并非在途库存
            if (null != closeDate && !InventoryStatusEnum.IN_TRANSIT.equals(inventoryStatusEnum)) {
                if (billDate.isBefore(closeDate) || billDate.equals(closeDate)) {
                    ServiceException.runError(ApiError.ERROR_INVENTORY_CLOSED, closeDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                }
            }
            // 检查盘盈盘亏单最新单据时间并非在途库存
            if (!CollectionUtils.isEmpty(lastStocktakingProfitLossList) && !InventoryStatusEnum.IN_TRANSIT.equals(inventoryStatusEnum)) {
                // 盘盈盘亏单 匹配 仓库ID, 组织ID，仓位，skuId
                StocktakingProfitLossDetailDTO.LastDTO lastDTO = lastStocktakingProfitLossList.stream()
                        .filter(e -> e.getWarehouseId().equalsIgnoreCase(warehouseId)
                                && e.getWarehouseLocation().equals(null == warehouseLocation ? "" : warehouseLocation)
                                && e.getWarehouseOrgId().equalsIgnoreCase(orgId)
                                && e.getSkuId().equalsIgnoreCase(skuId))
                        .findFirst()
                        .orElse(null);
                if (null != lastDTO && (billDate.isBefore(lastDTO.getBillDate()) || billDate.equals(lastDTO.getBillDate()))) {
                    // 已有盘盈盘亏单【{}】不允许操作【{}】之前单据
                    ServiceException.runError(ApiError.ERROR_STOCKTAKING_PROFIT_LOSS_CLOSED, lastDTO.getCode(), lastDTO.getBillDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                }
            }
        }

        // 盘点冻结
        String redisKey = StrUtil.format(RedisKeyConstant.INVENTORY_LOCK, "*", orgId, warehouseId, warehouseLocation, skuId, dictInventoryStatus);
        Collection<String> keys = redisUtil.keys(redisKey);
        if (CollUtil.isEmpty(keys)) {
            return;
        }
        WarehouseDTO.UpdateDTO updateDTO = warehouseService.detailWithCache(warehouseId);
        String warehouseName = ObjectUtil.isNotEmpty(updateDTO) ? updateDTO.getName() : warehouseId;
        ServiceException.runError(ApiError.STOCK_FREEZE_NOT_ALLOW, warehouseName, warehouseLocation, skuNo, dictInventoryStatus, "盘点");
    }

    /**
     * 获取可用库存明细，用于出库场景
     *
     * @param transactionFlow 交易流水
     * @return 返回明细库存
     */
    private InventoryDetailEntity getAvaliableInventoryDetail(TransactionFlowEntity transactionFlow) {
        InventoryDetailEntity inventoryDetail = inventoryDetailService.getById(transactionFlow.getInventoryDetailId());

        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Collections.singletonList(transactionFlow.getWarehouseId()));

        WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(transactionFlow.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());

        String inventoryStatusName = InventoryStatusEnum.getNameByCode(transactionFlow.getDictInventoryStatus());
        // 仓库是否允许负库存
        if (null == inventoryDetail) {
            String errMsg = StrUtil.format(ApiError.ERROR_99035.msg, transactionFlow.getSkuNo(), transactionFlow.getWarehouseName(), warehouseLocationEntity.getName(), inventoryStatusName, (Objects.isNull(inventoryDetail) ? "无" : inventoryDetail.getQty()), transactionFlow.getQty());
            log.error(errMsg);
            ServiceException.runError(ApiError.ERROR_99035.code, errMsg);
        }
        return inventoryDetail;
    }

    /**
     * 获取可用库存，用于出库场景
     *
     * @param transactionFlow 交易流水
     * @return 返回可用库存
     */
    private InventoryEntity getAvaliableInventory(TransactionFlowEntity transactionFlow) {
        InventoryEntity inventory = inventoryService.getById(transactionFlow.getInventoryId());
        String inventoryStatusName = InventoryStatusEnum.getNameByCode(transactionFlow.getDictInventoryStatus());
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Collections.singletonList(transactionFlow.getWarehouseId()));

        WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(transactionFlow.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());

        // 仓库是否允许负库存
        if (null == inventory || (inventory.getQty() + transactionFlow.getQty() < 0 && !this.allowNegativeInventory(transactionFlow.getWarehouseId()))) {
            String errMsg = StrUtil.format(ApiError.ERROR_99035.msg, transactionFlow.getSkuNo(), transactionFlow.getWarehouseName(), warehouseLocationEntity.getName(), inventoryStatusName, inventory == null ? "无" : inventory.getQty(), transactionFlow.getQty());
            log.error(errMsg);
            ServiceException.runError(ApiError.ERROR_99035.code, errMsg);
        }

        return inventory;
    }

    /**
     * 入库核心业务逻辑处理
     */
    @SneakyThrows
    public void inStockCore(InOutStockCoreDTO param, InventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String tansactionRuleId, String transactionNo) {
        // 仓库信息
        WarehouseDTO.UpdateDTO warehouseInfo = warehouseService.detailWithCache(param.getWarehouseId());
        if (Objects.isNull(warehouseInfo) || StrUtil.isEmpty(warehouseInfo.getId())) {
            ServiceException.runError(ApiError.ERROR_99002);
        }
        // 查询最新库存关账记录
        Map<String, LocalDate> closedDateMap = inventoryClosedRecordService.mapByOrgId(InventoryClosedRecordEnum.STK.getCode());
        // 最新盘盈盘亏单有效单据日期列表
        List<StocktakingProfitLossDetailDTO.LastDTO> lastStocktakingProfitLossList = stocktakingProfitLossService.maxDateByParams(
                Collections.singletonList(param.getWarehouseId()),
                Collections.singletonList(warehouseInfo.getOrgId()),
                Collections.singletonList(param.getSkuId())
        );

        checkAllowTransaction(closedDateMap.get(warehouseInfo.getOrgId()), warehouseInfo.getOrgId(), param.getWarehouseId(), param.getWarehouseLocation(), param.getSkuId(), param.getSkuNo(), inventoryStatusEnum.getCode(), param.getBillDate(), inventoryStatusEnum, lastStocktakingProfitLossList, param.getSourceType());
        log.warn("交易业务：【{}】，来源单据：【{}】，单据id：【{}】，SKU编号：【{}】，库存状态：【{}】，开始走入库逻辑", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getSkuNo(), inventoryStatusEnum.getName());

        // 按照仓库+仓位+库存状态+SKU 进行锁定
        String lockKey = StrUtil.format("{}:{}:{}:{}:{}", DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), param.getWarehouseId(), StrUtils.null2EmptyWithTrim(param.getWarehouseLocation()), inventoryStatusEnum.getCode(), param.getSkuId());
        RLock rLock = redisson.getLock(lockKey);
        boolean isLock;
        try {
            // 设置最大等待锁时间
            isLock = rLock.tryLock(5, TimeUnit.SECONDS);
            if (!isLock) {
                log.error("单据：{},SKU:{},入库加锁失败,key={}", param.getSourceCode(), param.getSkuNo(), lockKey);
                ServiceException.runError(ApiError.ERROR_1026);
            }
            InventoryRelationDTO inventoryRelationDTO = abstractInventoryService.saveOrUpdateRelationInventory(param, inventoryStatusEnum, warehouseInfo.getOrgId());
            InventoryEntity inventorySaveDTO = inventoryRelationDTO.getInventory();
            InventoryDetailEntity inventoryDetail = inventoryRelationDTO.getInventoryDetail();

            // 登记交易流水
            TransactionFlowDTO transactionFlowDTO = InventoryUtils.wrapTransactionFlowInOutStock(param, inventorySaveDTO.getId(), businessType, inventoryDetail.getId(), inventoryStatusEnum, param.getBillDate(), param.getQty(), warehouseInfo.getOrgId());
            transactionFlowDTO.setTransactionNo(transactionNo);
            transactionFlowService.add(transactionFlowDTO, businessType, tansactionRuleId, inventorySaveDTO.getQty(), InventoryModeEnum.IN_STOCK);
        } catch (Exception e) {
            log.error("交易业务：{}，来源单据：{}，单据id：【{}】，SKU编号：【{}】，库存操作异常", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getSkuNo(), e);
            throw e;
        } finally {
            //释放锁  锁是否存在，是当前执行线程的锁
            if (rLock.isLocked() && rLock.isHeldByCurrentThread()) {
                // 释放锁
                rLock.unlock();
            }
        }
    }

    /**
     * 出库核心业务处理
     */
    @SneakyThrows
    public void outStockCore(InOutStockCoreDTO param, InventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String tansactionRuleId,
                             String transactionNo) {
        // 仓库信息
        WarehouseDTO.UpdateDTO warehouseInfo = warehouseService.detailWithCache(param.getWarehouseId());
        if (Objects.isNull(warehouseInfo) || StrUtil.isEmpty(warehouseInfo.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        // 查询最新库存关账记录
        Map<String, LocalDate> closedDateMap = inventoryClosedRecordService.mapByOrgId(InventoryClosedRecordEnum.STK.getCode());

        // 最新盘盈盘亏单有效单据日期列表
        List<StocktakingProfitLossDetailDTO.LastDTO> lastStocktakingProfitLossList = stocktakingProfitLossService.maxDateByParams(
                Collections.singletonList(warehouseInfo.getId()),
                Collections.singletonList(warehouseInfo.getOrgId()),
                Collections.singletonList(param.getSkuId())
        );


        checkAllowTransaction(closedDateMap.get(warehouseInfo.getOrgId()), warehouseInfo.getOrgId(), param.getWarehouseId(), param.getWarehouseLocation(), param.getSkuId(), param.getSkuNo(), inventoryStatusEnum.getCode(), param.getBillDate(), inventoryStatusEnum, lastStocktakingProfitLossList, param.getSourceType());
        // 待出库数量
        Integer waitOutQty = param.getQty();
        log.info("交易业务：【{}】，来源单据：{}，单据id：【{}】，SKU编号：【{}】，库存状态：【{}】，开始走出库逻辑", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), inventoryStatusEnum.getName(), param.getSkuNo());

        // 按照仓库+仓位+库存状态+SKU 进行锁定
        String lockKey = StrUtil.format("{}:{}:{}:{}:{}", DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), param.getWarehouseId(), StrUtils.null2EmptyWithTrim(param.getWarehouseLocation()), inventoryStatusEnum.getCode(), param.getSkuId());
        RLock rLock = redisson.getLock(lockKey);
        boolean isLock;
        try {
            // 设置最大等待锁时间
            long waitTime = Objects.nonNull(param.getLockWaitTime()) ? param.getLockWaitTime() : 5;
            log.info("交易业务：【{}】:单据id：【{}】:等待时间：{}:", businessType.getName(), param.getSourceId(), waitTime);
            isLock = rLock.tryLock(waitTime, TimeUnit.SECONDS);
            if (!isLock) {
                log.error("单据：{},SKU:{},入库加锁失败,key={}", param.getSourceCode(), param.getSkuNo(), lockKey);
                throw new ServiceException(ApiError.ERROR_1026);
            }
            List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Collections.singletonList(param.getWarehouseId()));

            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(param.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());

            InventoryEntity inventory = getInventoryEntity(param, inventoryStatusEnum, warehouseInfo);
            String inventoryStatusName = Optional.of(inventoryStatusEnum).map(InventoryStatusEnum::getName).orElse("");
            // 仓库负库存是否允许
            checkHasNegativeInventory(param, inventory, waitOutQty, warehouseInfo, warehouseLocationEntity, inventoryStatusName);
            Integer inventoryQty = inventory.getQty();
            // 查询库存明细，按入库批次日期降序排序
            List<InventoryDetailEntity> inventoryDetails = inventoryDetailService.findListQtyGreatZero(inventory.getId());
            // 允许负库存
            inventoryDetails = getInventoryDetailEntities(param, inventoryDetails, inventory, warehouseInfo, warehouseLocationEntity, inventoryStatusName);
            // 循环扣减
            for (int i = 0; i < inventoryDetails.size(); i++) {
                InventoryDetailEntity detailEntity = inventoryDetails.get(i);
                // 已经足额扣减完成
                if (waitOutQty <= 0) {
                    break;
                }
                Integer tradeQty = getTradeQty(param, waitOutQty, detailEntity, i, inventoryDetails, warehouseInfo, inventory);
                waitOutQty = waitOutQty - tradeQty;
                inventoryQty = inventoryQty - tradeQty;
                // 更新库存明细
                inventoryDetailService.updateQtyById(detailEntity.getId(), (tradeQty * -1));
                // 登记交易流水（有可能一个操作产生多条，从多个库存明细中扣除）
                TransactionFlowDTO transactionFlow = InventoryUtils.wrapTransactionFlowInOutStock(param, inventory.getId(), businessType, detailEntity.getId(), inventoryStatusEnum, detailEntity.getInstockBatchDate(), tradeQty, warehouseInfo.getOrgId());
                transactionFlow.setTransactionNo(transactionNo);
                transactionFlowService.add(transactionFlow, businessType, tansactionRuleId, inventoryQty, InventoryModeEnum.OUT_STOCK);
            }
            // 仓库允许负库存判断
            checkHasGtZeroNegativeInventory(param, waitOutQty, warehouseInfo, warehouseLocationEntity, inventoryStatusName, inventory);
            // 更新库存表
            inventoryService.updateQtyById(inventory.getId(), (param.getQty() * -1));
            // 此处再次验证，防止变成负库存
            InventoryEntity curInventory = inventoryService.getById(inventory.getId());
            // 仓库允许负库存判断
            checkHasLtZeroNegativeInventory(param, curInventory, inventory, warehouseInfo, warehouseLocationEntity, inventoryStatusName);
            // 创建/修改库存历史
            inventoryHisService.addOrUpdate(inventory.getId(), LocalDate.now(), curInventory.getQty());
        } catch (Exception e) {
            log.error("交易业务：【{}】，来源单据：【{}】，单据id：【{}】，SKU编号：【{}】，库存操作异常", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getSkuNo(), e);
            throw e;
        } finally {
            //释放锁  锁是否存在，是当前执行线程的锁
            if (rLock.isLocked() && rLock.isHeldByCurrentThread()) {
                // 释放锁
                rLock.unlock();
            }
        }
    }

    private void checkHasLtZeroNegativeInventory(InOutStockCoreDTO param, InventoryEntity curInventory, InventoryEntity inventory, WarehouseDTO.UpdateDTO warehouseInfo, WarehouseLocationEntity warehouseLocationEntity, String inventoryStatusName) {
        if (curInventory.getQty() < 0 && !this.allowNegativeInventory(param.getWarehouseId())) {
            log.warn("库存id:{}出库后的库存数量变为:{}，不允许出库", inventory.getId(), curInventory.getQty());
            throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, param.getSkuNo(), warehouseInfo.getName(), warehouseLocationEntity.getName(), inventoryStatusName, curInventory.getQty(), param.getQty()));
        }
    }

    private void checkHasGtZeroNegativeInventory(InOutStockCoreDTO param, Integer waitOutQty, WarehouseDTO.UpdateDTO warehouseInfo, WarehouseLocationEntity warehouseLocationEntity, String inventoryStatusName, InventoryEntity inventory) {
        if (waitOutQty > 0 && !this.allowNegativeInventory(param.getWarehouseId())) {
            // 仓库允许负库存判断
            throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, param.getSkuNo(), warehouseInfo.getName(), warehouseLocationEntity.getName(), inventoryStatusName, inventory.getQty(), param.getQty()));
        }
    }

    private static Integer getTradeQty(InOutStockCoreDTO param, Integer waitOutQty, InventoryDetailEntity detailEntity, int i, List<InventoryDetailEntity> inventoryDetails, WarehouseDTO.UpdateDTO warehouseInfo, InventoryEntity inventory) {
        Integer tradeQty = Math.min(waitOutQty, detailEntity.getQty());

        // 仓库负库存是否允许
        if (i == inventoryDetails.size() - 1 && tradeQty < waitOutQty) {
            log.warn("仓库【{}】SKU【{}】允许负库存，即时库存id【{}】，剩余待出库数量【{}】，交易数量【{}】不足出库，从最后一条库存明细出库", warehouseInfo.getName(), param.getSkuNo(), inventory.getId(), waitOutQty, tradeQty);
            tradeQty = waitOutQty;
        }
        return tradeQty;
    }

    private List<InventoryDetailEntity> getInventoryDetailEntities(InOutStockCoreDTO param, List<InventoryDetailEntity> inventoryDetails, InventoryEntity inventory, WarehouseDTO.UpdateDTO warehouseInfo, WarehouseLocationEntity warehouseLocationEntity, String inventoryStatusName) {
        if (CollUtil.isEmpty(inventoryDetails)) {
            // 判断是否含有小于等于0库存
            inventoryDetails = inventoryDetailService.findListQtyLeZero(inventory.getId());
            if (CollUtil.isEmpty(inventoryDetails)) {
                log.warn("仓库【{}】SKU【{}】允许负库存，即时库存id【{}】,且没有大于0的库存明细，也没有小于等于0的库存明细", warehouseInfo.getName(), param.getSkuNo(), inventory.getId());
                String errMsg = StrUtil.format(ApiError.ERROR_99035.msg, param.getSkuNo(), warehouseInfo.getName(), warehouseLocationEntity.getName(), inventoryStatusName, inventory.getQty(), param.getQty());
                log.error(errMsg);
                ServiceException.runError(ApiError.ERROR_99035.code, errMsg);
            } else {
                // 取最后一条负库存明细
                log.warn("仓库【{}】SKU【{}】允许负库存，即时库存id【{}】,有小于等于0的库存明细，从最后一条库存明细出库", warehouseInfo.getName(), param.getSkuNo(), inventory.getId());
                InventoryDetailEntity lastInventoryDetailEntity = inventoryDetails.get(inventoryDetails.size() - 1);
                inventoryDetails = new ArrayList<>();
                inventoryDetails.add(lastInventoryDetailEntity);
            }
        }
        return inventoryDetails;
    }

    private void checkHasNegativeInventory(InOutStockCoreDTO param, InventoryEntity inventory, Integer waitOutQty, WarehouseDTO.UpdateDTO warehouseInfo, WarehouseLocationEntity warehouseLocationEntity, String inventoryStatusName) {
        if (inventory.getQty() < waitOutQty && !this.allowNegativeInventory(param.getWarehouseId())) {
            String errMsg = StrUtil.format(ApiError.ERROR_99035.msg, param.getSkuNo(), warehouseInfo.getName(), warehouseLocationEntity.getName(), inventoryStatusName, inventory.getQty(), param.getQty());
            log.error(errMsg);
            throw new ServiceException(ApiError.ERROR_99035, errMsg);
        }
    }

    private InventoryEntity getInventoryEntity(InOutStockCoreDTO param, InventoryStatusEnum inventoryStatusEnum, WarehouseDTO.UpdateDTO warehouseInfo) {
        InventoryEntity inventory = inventoryService.findInventory(warehouseInfo.getOrgId(), param.getWarehouseId(), param.getSkuId(), param.getWarehouseLocation(), inventoryStatusEnum.getCode());
        if (Objects.isNull(inventory)) {
            //如果库存为空，新增一条0库存的记录,不加入当前事务，避免下面的负库存校验抛异常后导致0库存的记录被回滚
            InOutStockCoreDTO zeroInventoryParam = InOutStockCoreConverter.INSTANCE.copyInOutStockCoreDTO(param);
            zeroInventoryParam.setQty(0);
            InventoryRelationDTO inventoryRelationDTO = abstractInventoryService.createZeroInventoryRecord(zeroInventoryParam, inventoryStatusEnum, warehouseInfo.getOrgId());
            inventory = inventoryRelationDTO.getInventory();
        }
        return inventory;
    }

    /**
     * 获取忽略库存计算的sku
     *
     * @return 返回忽略的SKU ID列表
     */
    protected List<String> getIgnoreSkuIds() {
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if (CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }
        return ignoreInventorySkuIds;
    }

    /**
     * 出库检查库存是否足够（走交易规则，不能手工传输库存状态）
     */
    protected void checkStockByRule(InventoryBaseInfoDTO param, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRules) {
        InventorySourceTypeEnum sourceTypeEnum = param.getSourceType();
        String sourceId = param.getSourceId();
        LocalDate billDate = param.getBillDate();
        List<TransactionRuleDTO> outTransactionRules;
        log.info("库存状态从配置中取，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】，SKU编号：【{}】", businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
        if (CollUtil.isEmpty(transactionRules)) {
            ServiceException.runError(ApiError.ERROR_99034.code, StrUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
        }
        if (Objects.nonNull(param.getWarehouseOption())) { // 调拨类业务，包含当前仓和目的仓
            outTransactionRules = transactionRules.stream().filter(r -> Objects.equals(r.getTransactionMode(), InventoryModeEnum.OUT_STOCK)
                    && Objects.equals(r.getWarehouseOption().getCode(), param.getWarehouseOption().getCode())).collect(Collectors.toList());
        } else {
            // 直接过滤得到出库类型的数据
            outTransactionRules = transactionRules.stream().filter(r -> Objects.equals(r.getTransactionMode(), InventoryModeEnum.OUT_STOCK)).collect(Collectors.toList());
        }
        if (CollUtil.isNotEmpty(outTransactionRules)) {
            for (TransactionRuleDTO rule : outTransactionRules) {
                InventoryStatusEnum ruleInventoryStatusEnum = rule.getInventoryStatus();
                ValidatorUtil.isTrue(Objects.nonNull(ruleInventoryStatusEnum), () -> new ServiceException(ApiError.ERROR_99036));
                this.checkStockQtyByWareLocalSkuStatus(businessType, param, ruleInventoryStatusEnum);
            }
        }
    }

    /**
     * 检查库存是否足够
     *
     * @param businessType 业务类型
     * @param param        业务参数
     * @param status       库存状态
     */
    protected void checkStockQtyByWareLocalSkuStatus(InventoryBusinessTypeEnum businessType, InventoryBaseInfoDTO param, InventoryStatusEnum status) {
        // 仓库
        String warehouseId = param.getWarehouseId();
        // SKU
        String skuId = param.getSkuId();
        String skuNo = param.getSkuNo();
        // 库位
        String warehouseLocation = StrUtils.null2EmptyWithTrim(param.getWarehouseLocation());
        // 操作数量
        Integer qty = param.getQty();
        // 来源
        InventorySourceTypeEnum sourceTypeEnum = param.getSourceType();
        // 仓库信息
        WarehouseDTO.UpdateDTO warehouseDetail = warehouseService.detailWithCache(warehouseId);
        if (Objects.isNull(warehouseDetail) || StrUtil.isEmpty(warehouseDetail.getId())) {
            ServiceException.runError(ApiError.ERROR_99002);
        }
        // 仓库组织
        String orgId = warehouseDetail.getOrgId();

        InventoryEntity inventory = inventoryService.findInventoryLock(orgId, warehouseId, skuId, warehouseLocation, status.getCode());

        String inventoryStatusName = Optional.of(status).map(InventoryStatusEnum::getName).orElse("");

        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Collections.singletonList(warehouseId));

        WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(warehouseLocation)).findFirst().orElse(new WarehouseLocationEntity());

        if (Objects.isNull(inventory)) {
            //如果库存为空，新增一条0库存的记录,不加入当前事务，避免下面的负库存校验抛异常后导致0库存的记录被回滚
            WarehouseDTO.UpdateDTO warehouseInfo = warehouseService.detailWithCache(param.getWarehouseId());
            InOutStockCoreDTO zeroInventoryParam = InOutStockCoreConverter.INSTANCE.baseToInOutStockCoreDTO(param);
            zeroInventoryParam.setQty(0);
            InventoryRelationDTO inventoryRelationDTO = abstractInventoryService.createZeroInventoryRecord(zeroInventoryParam, status, warehouseInfo.getOrgId());
            inventory = inventoryRelationDTO.getInventory();
        }
        log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，状态【{}】，操作数量：【{}】，库存状态对应的总数量：【{}】", warehouseId, orgId, warehouseLocation, skuId, skuNo, sourceTypeEnum.getName(),
                businessType.getName(), status.getName(), qty, inventory.getQty());

        if (inventory.getQty() < qty && !allowNegativeInventory(warehouseId)) {
            ServiceException.runError(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, skuNo, warehouseDetail.getName(), warehouseLocationEntity.getName(), inventoryStatusName, inventory.getQty(), qty));
        }
        /**
         * 1. 调拨单：手动创建、调拨申请下推
         * 2. 其他出库单：
         * 3. 采购退货单：
         * 4. 委外发料：正常领料、超出领料
         * 5. 加工单：组装、拆卸
         *
         */
        List<String> typeList = Arrays.asList(InventorySourceTypeEnum.OTHER_OUTSTOCK.getCode(), InventorySourceTypeEnum.OTHER_INSTOCK.getCode(), InventorySourceTypeEnum.PURCHASE_RETURN_ORDER.getCode()
                , InventorySourceTypeEnum.RECEIVE_MATERIAL.getCode(), InventorySourceTypeEnum.RETURN_MATERIAL.getCode(), InventorySourceTypeEnum.MACHINE_INFO.getCode());
        //虚拟库存校验
        if (InventoryStatusEnum.USABLE.equals(status) && (typeList.contains(sourceTypeEnum.getCode()) || Arrays.asList(InventoryBusinessTypeEnum.DIRECT_ALLOCATE.getCode(), InventoryBusinessTypeEnum.DIRECT_ALLOCATE_APPLY.getCode()).contains(businessType.getCode()))) {
            //虚拟库存
            Integer virtualQty = virtualInventoryService.getInventoryQtyByWarehouseId(warehouseId, skuId);
            //有分配虚拟库存则校验
            if (MathUtil.compareTo(virtualQty, MathUtil.ZERO) > MathUtil.ZERO) {
                //仓库可用库存
                Integer realInventoryTotal = inventoryService.getRealInventoryTotal(warehouseId, skuId);
                log.info("仓库【{}】，SKU【{}】，已分配库存【{}】，实体参可用库存【{}】", warehouseDetail.getName(), skuNo, virtualQty, realInventoryTotal);
                if (Math.abs(qty) > realInventoryTotal - virtualQty) {
                    ServiceException.runError(ApiError.ERROR_CHECK_OUT_VIRTUAL_INVENTORY, skuNo, warehouseDetail.getName(), virtualQty, realInventoryTotal - virtualQty);
                }
            }
        }
    }

    /**
     * 查询配置的交易规则
     *
     * @param businessType 业务类型
     * @return 交易规则
     */
    protected List<TransactionRuleDTO> wrapTransactionRule(InventoryBusinessTypeEnum businessType) {
        // 查询配置的交易规则
        List<CfgTransactionRulesEntity> transactionRulesEntities = cfgTransactionRulesService.findByDictBizType(businessType.getCode());
        if (CollUtil.isNotEmpty(transactionRulesEntities)) {
            List<TransactionRuleDTO> transactionRuleDTOS = Lists.newArrayListWithExpectedSize(transactionRulesEntities.size());
            transactionRulesEntities.forEach(r -> {
                TransactionRuleDTO transactionRuleDTO = new TransactionRuleDTO();
                transactionRuleDTO.setId(r.getId());
                transactionRuleDTO.setDictBizType(InventoryBusinessTypeEnum.getByCode(r.getDictBizType()));
                transactionRuleDTO.setWarehouseOption(InventoryWarehouseOptionEnum.getByCode(r.getWarehouseOption()));
                transactionRuleDTO.setInventoryStatus(InventoryStatusEnum.getByCode(r.getInventoryStatus()));
                transactionRuleDTO.setTransactionMode(InventoryModeEnum.getByCode(r.getTransactionMode()));
                transactionRuleDTOS.add(transactionRuleDTO);
            });
            return transactionRuleDTOS;
        }
        return null;
    }

    /**
     * 仓库是否允许负库存
     *
     * @param warehouseId 仓库ID
     * @return true表示允许, false表示不允许
     */
    protected boolean allowNegativeInventory(String warehouseId) {
        // 仓库信息
        WarehouseDTO.UpdateDTO warehouseDetail = warehouseService.detailWithCache(warehouseId);
        if (Objects.isNull(warehouseDetail) || StrUtil.isEmpty(warehouseDetail.getId())) {
            ServiceException.runError(ApiError.ERROR_99002);
        }
        Boolean warehouseAllowNegativeInventory = warehouseDetail.getAllowNegativeInventory();
        log.warn("仓库【{}】【{}】负库存", warehouseDetail.getName(), Objects.equals(warehouseAllowNegativeInventory, Boolean.TRUE) ? "允许" : "不允许");
        return Objects.equals(warehouseAllowNegativeInventory, Boolean.TRUE);
    }

    public InventoryRelationDTO saveOrUpdateRelationInventory(InOutStockCoreDTO param, InventoryStatusEnum inventoryStatusEnum, String orgId) {
        // 保存库存
        InventorySaveDTO inventorySaveDTO = inventoryService.addOrUpdate(param.getWarehouseId(), orgId, param.getWarehouseLocation(), param.getSkuId(), param.getSkuNo(), inventoryStatusEnum.getCode(), param.getQty());
        InventoryDetailEntity inventoryDetail = inventoryDetailService.addOrUpdate(inventorySaveDTO.getInventoryId(), param.getBillDate(), param.getQty(), inventoryStatusEnum);
        InventoryEntity entity = inventoryService.getById(inventorySaveDTO.getInventoryId());
        // 时间为交易日期
        inventoryHisService.addOrUpdate(inventorySaveDTO.getInventoryId(), LocalDate.now(), entity.getQty());
        return new InventoryRelationDTO(entity, inventoryDetail);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @GlobalTransactional(rollbackFor = Exception.class, propagation = io.seata.tm.api.transaction.Propagation.REQUIRES_NEW)
    public InventoryRelationDTO createZeroInventoryRecord(InOutStockCoreDTO param, InventoryStatusEnum inventoryStatusEnum, String orgId) {
        return this.saveOrUpdateRelationInventory(param, inventoryStatusEnum, orgId);
    }
}