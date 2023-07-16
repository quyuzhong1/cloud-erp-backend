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
     * @param ruleList       规则
     * @param businessType   业务类型
     * @param byType         按业务类型
     */
    @Transactional(rollbackFor = Exception.class)
    public <T extends InventoryStockBaseDTO> void approve(List<T> paramList, List<TransactionRuleDTO> ruleList, InventoryBusinessTypeEnum businessType, Boolean byType) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.warn("》》》库存交易按【{}】，入参：{}，业务类型：{}", Objects.equals(byType, Boolean.TRUE) ? "业务类型" : "自定义规则", JSONObject.toJSONString(paramList), businessType.getName());
        // 1.验证参数
        List<TransactionRuleDTO> transactionRuleParams;
        if(Objects.equals(byType,Boolean.TRUE)) {
            transactionRuleParams = inventoryHelper.wrapTransactionRule(businessType);
        } else {
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
    public  void unApprove(InventoryUnApproveDTO dto) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.warn("库存交易反审核，单据类型：{}, 单据id：{}", dto.getSourceType().getName(), dto.getBillId());

        // 根据单据类型和单据id查询出未反审核过的对应的交易流水，一个单据对应多个SKU， 按创建时间正序排序
        List<TransactionFlowEntity> txnFlows = transactionFlowService.getUnApprovedTxnFlows(dto.getSourceType().getCode(), dto.getBillId());
        // 没有流水 则不做反向操作：兼容产品属性为费用或服务的sku没有交易流水的情况
        if(CollUtil.isEmpty(txnFlows)) {
            log.warn("库存交易反审核，单据类型：{}, 单据id：{}，未找到未审核过的交易流水，不处理", dto.getSourceType().getName(), dto.getBillId());
            return;
        }

        // 关联交易号
        String transactionNo = IdUtil.getSnowflake().nextIdStr();
        txnFlows.stream().forEach(txnFlow->{
            // 获取单据业务类型
            InventoryBusinessTypeEnum businessTypeEnum = InventoryBusinessTypeEnum.getByCode(txnFlow.getDictBizType());// 取原交易流水的业务类型

            // 按照仓库+SKU进行锁定，考虑库位，防止数据冲突
            String lockKey = StrUtil.format( "{}:{}:{}:{}",DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), txnFlow.getWarehouseId(), StrUtils.null2EmptyWithTrim(txnFlow.getWarehouseLocation()), txnFlow.getSkuId());
            RLock rlock = redisson.getLock(lockKey);
            // 获取锁
            boolean isLock;
            try {
                isLock = rlock.tryLock(20,TimeUnit.SECONDS);
                if (!isLock) {
                    log.error("尝试获取锁[{}]失败,操作: 反审核》》》，" +
                            "仓库：【{}】，组织：【{}】，SKU ID：【{}】，SKU编号：【{}】, 交易业务：【{}】，" +
                            "来源单据类型：【{}】, 单据id：【{}】，SKU编号：【{}】",
                            lockKey,txnFlow.getWarehouseId(), txnFlow.getOrgId(), txnFlow.getSkuNo(), txnFlow.getSkuId(), businessTypeEnum.getName(),
                            InventorySourceTypeEnum.getByCode(txnFlow.getSourceType()).getName(), txnFlow.getSourceId(), txnFlow.getSkuNo());
                    throw new ServiceException(ApiError.ERROR_1026);
                }
                log.info("尝试获取锁[{}]成功,操作: 反审核》》》，" +
                                "仓库：【{}】，组织：【{}】，SKU ID：【{}】，SKU编号：【{}】, 交易业务：【{}】，" +
                                "来源单据类型：【{}】, 单据id：【{}】，SKU编号：【{}】",
                        lockKey,txnFlow.getWarehouseId(), txnFlow.getOrgId(), txnFlow.getSkuNo(), txnFlow.getSkuId(), businessTypeEnum.getName(),
                        InventorySourceTypeEnum.getByCode(txnFlow.getSourceType()).getName(), txnFlow.getSourceId(), txnFlow.getSkuNo());

                InventoryModeEnum inventoryModeCur = (txnFlow.getQty()*-1) < 0 ? InventoryModeEnum.OUT_STOCK : InventoryModeEnum.IN_STOCK;

                // 校验即时库存
                InventoryEntity inventory = inventoryService.getById(txnFlow.getInventoryId());
                if(inventory.getQty()+txnFlow.getQty()*(-1)<0){
                    log.warn("反审核》》》，仓库：【{}】，组织：【{}】，SKU ID：【{}】，SKU编号：【{}】, 交易业务：【{}】，来源单据类型：【{}】, 单据id：【{}】，SKU编号：【{}】，原库存明细id：【{}】，原库存明细数量【{}】，原交易流水数量【{}】，不足以反审核",
                            txnFlow.getWarehouseId(), txnFlow.getOrgId(), txnFlow.getSkuId(), txnFlow.getSkuNo(), txnFlow.getSkuId(), businessTypeEnum.getName(), InventorySourceTypeEnum.getByCode(txnFlow.getSourceType()).getName(), txnFlow.getSourceId(), txnFlow.getSkuNo(), inventory.getQty(), txnFlow.getQty());
                    throw new ServiceException("库存已被使用，原入库型单据无法反审核");
                }
                // 校验明细库存
                InventoryDetailEntity inventoryDetail = inventoryDetailService.getById(txnFlow.getInventoryDetailId());
                // 示例：inventoryQty=100, qty=200, 反向操作：100+200*-1<0 不足于扣减；反之则没有问题
                if(inventoryDetail.getQty()+txnFlow.getQty()*(-1)<0){
                    log.warn("反审核》》》，仓库：【{}】，组织：【{}】，SKU ID：【{}】，SKU编号：【{}】, 交易业务：【{}】，来源单据类型：【{}】, 单据id：【{}】，SKU编号：【{}】，原库存明细id：【{}】，原库存明细数量【{}】，原交易流水数量【{}】，不足以反审核",
                            txnFlow.getWarehouseId(), txnFlow.getOrgId(), txnFlow.getSkuId(), txnFlow.getSkuNo(), txnFlow.getSkuId(), businessTypeEnum.getName(), InventorySourceTypeEnum.getByCode(txnFlow.getSourceType()).getName(), txnFlow.getSourceId(), txnFlow.getSkuNo(), inventoryDetail.getQty(), txnFlow.getQty());
                    throw new ServiceException("库存已被使用，原入库型单据无法反审核");
                }
                // 校验历史库存
                InventoryHisEntity inventoryHis = inventoryHisService.findInventory(txnFlow.getInventoryId(),txnFlow.getBillDate());
                if(inventoryHis.getQty()+txnFlow.getQty()*(-1)<0){
                    log.warn("反审核》》》，仓库：【{}】，组织：【{}】，SKU ID：【{}】，SKU编号：【{}】, 交易业务：【{}】，来源单据类型：【{}】, 单据id：【{}】，SKU编号：【{}】，原库存明细id：【{}】，原库存明细数量【{}】，原交易流水数量【{}】，不足以反审核",
                            txnFlow.getWarehouseId(), txnFlow.getOrgId(), txnFlow.getSkuId(), txnFlow.getSkuNo(), txnFlow.getSkuId(), businessTypeEnum.getName(), InventorySourceTypeEnum.getByCode(txnFlow.getSourceType()).getName(), txnFlow.getSourceId(), txnFlow.getSkuNo(), inventoryHis.getQty(), txnFlow.getQty());
                    throw new ServiceException("库存已被使用，原入库型单据无法反审核");
                }

                // 记录交易流水（反审核的）
                txnFlow.setTransactionNo(transactionNo);
                txnFlow.setOperationMode(InventoryOperationModeEnum.UN_APPROVE.getCode());
                transactionFlowService.add(txnFlow, businessTypeEnum, txnFlow.getTransactionRuleId(), (inventoryDetail.getQty()+txnFlow.getQty()*(-1)), inventoryModeCur);

                // 更新库存明细表
                boolean updateFlag = inventoryDetailService.updateQtyById(txnFlow.getInventoryDetailId(), txnFlow.getQty());
                if(!updateFlag) {
                    throw new ServiceException(ApiError.ERROR_1027);
                }
                // 更新实时库存表数量
                updateFlag =  inventoryService.updateQtyById(inventory.getId(), inventoryDetail.getQty());
                if(!updateFlag) {
                    throw new ServiceException(ApiError.ERROR_1027);
                }
                // 更新库存历史表
                inventoryHisService.updateQtyById(inventoryHis.getId(), inventoryDetail.getQty());

                // 更新原交易流水为已反审核
                transactionFlowService.updateUnapprovedById(txnFlow.getId(), txnFlow.getVersion());
            }  catch (Exception e) {
                log.error("反审核》》》，交易业务：【{}】，来源单据：【{}】，单据id：【{}】，SKU编号：【{}】，库存操作异常", businessTypeEnum.getName(), InventorySourceTypeEnum.getByCode(txnFlow.getSourceType()).getName(), txnFlow.getSourceId(), txnFlow.getSkuNo(),e );
                if(e instanceof ServiceException) {
                    ServiceException serviceException = (ServiceException) e;
                    throw serviceException;
                } else {
                    throw new ServiceException(ApiError.Default.code, e.getMessage());
                }
            } finally {
                //释放锁  锁是否存在，是当前执行线程的锁
                if(rlock.isLocked() && rlock.isHeldByCurrentThread()){
                    // 释放锁
                    rlock.unlock();
                }
            }
            log.warn("结束库存交易，耗时【{}】秒", stopwatch.elapsed(TimeUnit.SECONDS));
        });
    }

    /**
     * 入库核心业务逻辑处理
     */
    @SneakyThrows
    public  void inStockCore(InOutStockCoreDTO param, InventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String tansactionRuleId, String transactionNo) {
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
        log.warn("交易业务：【{}】，来源单据：【{}】，单据id：【{}】，SKU编号：【{}】，库存状态：【{}】，开始走入库逻辑", businessType.getName(), sourceTypeEnum.getName(), param.getSourceId(), param.getSkuNo(), inventoryStatusEnum.getName());
        // 按照仓库+SKU进行锁定，暂不考虑库位，防止数据冲突
        String lockKey = StrUtil.format( "{}:{}:{}:{}", DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), warehouseId, StrUtils.null2EmptyWithTrim(warehouseLocation) ,skuId);
        RLock rlock = redisson.getLock(lockKey);
        boolean isLock;
        try {
            // 设置最大等待锁时间
            isLock = rlock.tryLock(10, TimeUnit.SECONDS);
            log.warn("库存状态：【{}】，仓库：【{}】，组织：【{}】，SKU ID：【{}】，SKU编号：【{}】, 交易业务：【{}】，来源单据类型：【{}】, 单据id：【{}】，SKU编号：【{}】，是否获取到锁: {}", inventoryStatusEnum.getName(), warehouseId, orgId, skuId, skuNo, businessType.getName(), sourceTypeEnum.getName(), param.getSourceId(), param.getSkuNo(), isLock);
            if (!isLock) {
                throw new ServiceException(ApiError.ERROR_1026);
            }
            log.warn("库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", inventoryStatusEnum.getName(), businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
            log.info("库存状态：【{}】，仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，状态【{}】新增或修改库存", inventoryStatusEnum.getName(), warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName());

            // 此处注意，入库传不传仓位都带仓位条件查询
            InventorySaveDTO inventorySaveDTO = inventoryService.addOrUpdate(warehouseId, orgId, warehouseLocation, skuId, skuNo, inventoryStatusEnum.getCode(), qty);
            String inventoryInfoId = inventorySaveDTO.getInventoryId();
            // 库存原数量
            Integer originInventoryQty = inventorySaveDTO.getQty();
            Integer afterInventoryQty = originInventoryQty + qty;
            log.warn("库存状态：【{}】，仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，状态【{}】，实时库存原数量：【{}】，操作数量【{}】，操作后数量【{}】", inventoryStatusEnum.getName(), warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName(), originInventoryQty, qty, afterInventoryQty);
            log.info("库存状态：【{}】，仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，状态【{}】，单据日期：【{}】,库存表id：【{}】，新增或修改库存明细数据", inventoryStatusEnum.getName(), warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName(), billDate, inventoryInfoId);
            // 入库批次日期取单据日期
            String inventoryDetailId = inventoryDetailService.addOrUpdate(inventoryInfoId, billDate, qty);
            // 登记交易流水
            TransactionFlowDTO transactionFlowDTO = InventoryUtils.wrapTransactionFlowInOutStock(param, inventoryInfoId, businessType, inventoryDetailId, inventoryStatusEnum, billDate, qty, orgId);
            transactionFlowDTO.setTransactionNo(transactionNo);
            transactionFlowService.add(transactionFlowDTO, businessType, tansactionRuleId, afterInventoryQty, InventoryModeEnum.IN_STOCK);

            log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，状态【{}】，单据日期：【{}】,库存表id：【{}】，新增或修改库存历史数据", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName(), billDate, inventoryInfoId);
            // 创建/修改库存历史
            inventoryHisService.addOrUpdate(inventoryInfoId, param.getBillDate(), qty);
        } catch (Exception e) {
            log.error("交易业务：{}，来源单据：{}，单据id：【{}】，SKU编号：【{}】，库存操作异常", businessType.getName(), sourceTypeEnum.getName(), param.getSourceId(), param.getSkuNo(),e );
            if(e instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) e;
                throw serviceException;
            } else {
                throw e;
            }
        } finally {
            //释放锁  锁是否存在，是当前执行线程的锁
            if(rlock.isLocked() && rlock.isHeldByCurrentThread()){
                // 释放锁
                rlock.unlock();
            }
        }
    }

    /**
     * 出库核心业务处理
     */
    @SneakyThrows
    public  void outStockCore (InOutStockCoreDTO param, InventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String tansactionRuleId,
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
        RLock rLock = redisson.getLock(lockKey);
        boolean isLock;
        try {
            // 设置最大等待锁时间
            isLock = rLock.tryLock(10, TimeUnit.SECONDS);
            log.warn("库存状态：【{}】，仓库：【{}】，组织：【{}】，SKU ID：【{}】，SKU编号：【{}】, 交易业务：【{}】，来源单据：【{}】, 是否获取到锁: {}", inventoryStatusEnum.getName(), warehouseId, orgId, skuId, skuNo, businessType.getName(), sourceTypeEnum.getName(), isLock);
            if (!isLock) {
                throw new ServiceException(ApiError.ERROR_1026);
            }
            log.info("库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", inventoryStatusEnum.getName(), businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
            InventoryEntity inventory = inventoryService.findInventory(orgId, warehouseId, skuId, warehouseLocation, inventoryStatusEnum.getCode());
            String inventoryStatusName = Optional.ofNullable(inventoryStatusEnum).map(InventoryStatusEnum::getName).orElse("");

            if(Objects.isNull(inventory)||inventory.getQty()<qty) {
                log.warn("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】," +
                                " 业务类型：【{}】，单据日期：【{}】，状态【{}】，库存原数量：【{}】，操作数量【{}】",
                        warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(),
                        businessType.getName(), param.getBillDate(), inventoryStatusEnum.getName(), (null==inventory?0:inventory.getQty()), qty);

                throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, warehouseDetail.getName(), warehouseLocation, skuNo, inventoryStatusName));
            }

            // 查询库存明细，按入库批次日期降序排序
            List<InventoryDetailEntity> inventoryDetails = inventoryDetailService.findListQtyGreatZero(inventory.getId());

            // 循环扣减
            for(InventoryDetailEntity detailEntity : inventoryDetails) {
                // 已经足额扣减完成
                if (qty <= 0) {
                    break;
                }
                Integer tradeQty=Math.min(qty,detailEntity.getQty());
                qty=qty-tradeQty;

                // 更新库存明细
                boolean updateFlag = inventoryDetailService.updateQtyById(detailEntity.getId(), tradeQty * -1);
                if(!updateFlag) {
                    throw new ServiceException(ApiError.ERROR_1027);
                }

                // 此处再次验证，防止变成负库存
                InventoryDetailEntity curInventoryDetail = inventoryDetailService.getById(detailEntity.getId());
                if(curInventoryDetail.getQty() < 0) {
                    log.warn("库存明细id：{}出库后的库存数量变为:{}，不允许出库", detailEntity.getId(), curInventoryDetail.getQty());
                    throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, warehouseDetail.getName(), warehouseLocation, skuNo,  inventoryStatusName));
                }

                // 登记交易流水（有可能一个操作产生多条，从多个库存明细中扣除）
                TransactionFlowDTO transactionFlow = InventoryUtils.wrapTransactionFlowInOutStock(param, inventory.getId(), businessType, detailEntity.getId(), inventoryStatusEnum, detailEntity.getInstockBatchDate(), tradeQty, orgId);
                transactionFlow.setTransactionNo(transactionNo);
                transactionFlowService.add(transactionFlow, businessType, tansactionRuleId, (detailEntity.getQty()-tradeQty), InventoryModeEnum.OUT_STOCK);
            }

            if(qty > 0) {
                throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, warehouseDetail.getName(), warehouseLocation, skuNo,  inventoryStatusName));
            }

            // 更新库存表
            boolean updateFlag =  inventoryService.updateQtyById(inventory.getId(), qty * -1);
            if(!updateFlag) {
                throw new ServiceException(ApiError.ERROR_1027);
            }

            // 此处再次验证，防止变成负库存
            InventoryEntity curInventory = inventoryService.getById(inventory.getId());
            if(curInventory.getQty() < 0) {
                log.warn("库存id:{}出库后的库存数量变为:{}，不允许出库", inventory.getId(), curInventory.getQty());
                throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, warehouseDetail.getName(), warehouseLocation, skuNo,  inventoryStatusName));
            }

            log.warn("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，单据日期：【{}】，状态【{}】，库存原数量：【{}】，操作数量【{}】，操作后数量【{}】", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), param.getBillDate(), inventoryStatusEnum.getName(), inventory.getQty(), qty, inventory.getQty()-qty);
            log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，状态【{}】，单据日期：【{}】,库存表id：【{}】，新增或修改库存历史数据", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName(), billDate, inventory.getId());

            // 创建/修改库存历史
            inventoryHisService.addOrUpdate(inventory.getId(), param.getBillDate(), qty * -1);
        }  catch (Exception e) {
            log.error("交易业务：【{}】，来源单据：【{}】，单据id：【{}】，SKU编号：【{}】，库存操作异常", businessType.getName(), sourceTypeEnum.getName(), param.getSourceId(), param.getSkuNo(),e );
            if(e instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) e;
                throw serviceException;
            } else {
                throw e;
            }
        } finally {
            //释放锁  锁是否存在，是当前执行线程的锁
            if(rLock.isLocked() && rLock.isHeldByCurrentThread()){
                // 释放锁
                rLock.unlock();
            }
        }
    }
}