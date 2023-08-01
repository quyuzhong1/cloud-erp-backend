package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.DistributedLockEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
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
        // 通过对sku id顺序执行, 避免多线程死锁
        Comparator<TransactionFlowEntity> comparing = Comparator.comparing(TransactionFlowEntity::getSkuId)
                .thenComparing(TransactionFlowEntity::getWarehouseId)
                .thenComparing(x -> StrUtil.isNotEmpty(x.getWarehouseLocation()) ? x.getWarehouseLocation() : "")
                .thenComparing(x -> StrUtil.isNotEmpty(x.getDictInventoryStatus()) ? x.getDictInventoryStatus() : "");
        txnFlows = txnFlows.stream().sorted(comparing).collect(Collectors.toList());
        txnFlows.stream().forEach(txnFlow->{
            // 检测是否允许库存交易
            checkAllowTransaction(txnFlow.getSkuId(),txnFlow.getOrgId(),txnFlow.getWarehouseId(),txnFlow.getWarehouseLocation(),txnFlow.getDictInventoryStatus());

            // 获取单据业务类型
            InventoryBusinessTypeEnum businessTypeEnum = InventoryBusinessTypeEnum.getByCode(txnFlow.getDictBizType());// 取原交易流水的业务类型

            // 按照仓库+仓位+库存状态+SKU 进行锁定
            String lockKey = StrUtil.format( "{}:{}:{}:{}:{}", DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), txnFlow.getWarehouseId(), StrUtils.null2EmptyWithTrim(txnFlow.getWarehouseLocation()),txnFlow.getDictInventoryStatus() ,txnFlow.getSkuId());
            RLock rLock = redisson.getLock(lockKey);
            boolean isLock;
            try {
                // 1，获取锁
                isLock = rLock.tryLock(20,TimeUnit.SECONDS);
                if (!isLock) {
                    log.error("尝试获取锁[{}]失败,操作: 反审核》》》，仓库：【{}】，组织：【{}】，SKU ID：【{}】，SKU编号：【{}】, 交易业务：【{}】，来源单据类型：【{}】, 单据id：【{}】，单据编号：【{}】",
                            lockKey,txnFlow.getWarehouseId(), txnFlow.getOrgId(), txnFlow.getSkuId(), txnFlow.getSkuNo(),  businessTypeEnum.getName(), InventorySourceTypeEnum.getByCode(txnFlow.getSourceType()).getName(), txnFlow.getSourceId(), txnFlow.getSourceCode());
                    throw new ServiceException(ApiError.ERROR_1026);
                }
                log.info("尝试获取锁[{}]成功,操作: 反审核》》》，仓库：【{}】，组织：【{}】，SKU ID：【{}】，SKU编号：【{}】, 交易业务：【{}】，来源单据类型：【{}】, 单据id：【{}】，单据编号：【{}】",
                        lockKey,txnFlow.getWarehouseId(), txnFlow.getOrgId(), txnFlow.getSkuId(), txnFlow.getSkuNo(),  businessTypeEnum.getName(), InventorySourceTypeEnum.getByCode(txnFlow.getSourceType()).getName(), txnFlow.getSourceId(), txnFlow.getSourceCode());

                // 2，整理 交易流水信息，数量取反，操作模式=unApprove
                txnFlow.setTransactionNo(transactionNo);
                txnFlow.setQty(txnFlow.getQty()*-1);
                txnFlow.setOperationMode(InventoryOperationModeEnum.UN_APPROVE.getCode());

                // 3，获取可用的库存, 数量不够时报错
                InventoryEntity inventory = getAvaliableInventory(txnFlow);
                InventoryDetailEntity inventoryDetail = getAvaliableInventoryDetail(txnFlow);
//                InventoryHisEntity inventoryHis = getAvaliableInventoryHis(txnFlow, Localdate.now());

                // 4，记录交易明细
                txnFlow.setIsUnapproved(Boolean.TRUE);


                // 5，更新库存
                boolean updateFlag = inventoryDetailService.updateQtyById(inventoryDetail.getId(), txnFlow.getQty());
                if(!updateFlag) {
                    throw new ServiceException(ApiError.ERROR_1027);
                }
                updateFlag =  inventoryService.updateQtyById(inventory.getId(), txnFlow.getQty());
                if(!updateFlag) {
                    throw new ServiceException(ApiError.ERROR_1027);
                }
                InventoryEntity entity = inventoryService.getById(inventory.getId());
                transactionFlowService.add(txnFlow, entity.getQty());
                inventoryHisService.addOrUpdate(inventory.getId(),LocalDate.now(), entity.getQty());

//                if(updRows<1) {
//                    throw new ServiceException(ApiError.ERROR_1027);
//                }

                // 6,更新原交易流水为已反审核
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
                if(rLock.isLocked() && rLock.isHeldByCurrentThread()){
                    // 释放锁
                    rLock.unlock();
                }
            }

            log.warn("结束库存交易，耗时【{}】秒", stopwatch.elapsed(TimeUnit.SECONDS));
        });
    }

    /**
     * 检查库存交易的否允许
     * @param skuId     SKU
     * @param orgId     组织
     * @param warehouseId   仓库
     * @param warehouseLocation 仓位
     * @param dictInventoryStatus   库存状态
     */
    private void checkAllowTransaction(String skuId, String orgId, String warehouseId, String warehouseLocation, String dictInventoryStatus) {
        //TODO 今后需要做 库存关账、盘点冻结 等检测
    }

    private InventoryHisEntity getAvaliableInventoryHis(TransactionFlowEntity transactionFlow) {
        InventoryHisEntity inventoryHis = inventoryHisService.findInventory(transactionFlow.getInventoryId(),LocalDate.now());
//        TODO 暂时未考虑校验历史库存
//        if( null==inventoryHis || inventoryHis.getQty()+transactionFlow.getQty()<0 ){
//            String errMsg=StrUtil.format(ApiError.ERROR_99035.msg, transactionFlow.getSkuNo(), transactionFlow.getWarehouseName(), transactionFlow.getWarehouseLocation(), transactionFlow.getDictInventoryStatus(),(Objects.isNull(inventoryHis)?0:inventoryHis.getQty()),transactionFlow.getQty());
//            log.error(errMsg);
//            throw new ServiceException(ApiError.ERROR_99035.code, errMsg);
//        }
        return inventoryHis;
    }

    /**
     * 获取可用库存明细，用于出库场景
     * @param transactionFlow  交易流水
     * @return
     */
    private InventoryDetailEntity getAvaliableInventoryDetail(TransactionFlowEntity transactionFlow) {
        InventoryDetailEntity inventoryDetail = inventoryDetailService.getById(transactionFlow.getInventoryDetailId());

        String inventoryStatusName = InventoryStatusEnum.getNameByCode(transactionFlow.getDictInventoryStatus());
        if(null==inventoryDetail){
            String errMsg=StrUtil.format(ApiError.ERROR_99035.msg, transactionFlow.getSkuNo(), transactionFlow.getWarehouseName(), transactionFlow.getWarehouseLocation(), inventoryStatusName,(Objects.isNull(inventoryDetail)?0:inventoryDetail.getQty()),transactionFlow.getQty());
            log.error(errMsg);
            throw new ServiceException(ApiError.ERROR_99035.code, errMsg);
        }
        // 仓库是否允许负库存
        if(!inventoryHelper.allowNegativeInventory(transactionFlow.getWarehouseId())) {
            if(inventoryDetail.getQty()+transactionFlow.getQty()<0){
                String errMsg=StrUtil.format(ApiError.ERROR_99035.msg, transactionFlow.getSkuNo(), transactionFlow.getWarehouseName(), transactionFlow.getWarehouseLocation(), inventoryStatusName,(Objects.isNull(inventoryDetail)?0:inventoryDetail.getQty()),transactionFlow.getQty());
                log.error(errMsg);
                throw new ServiceException(ApiError.ERROR_99035.code, errMsg);
            }
        }
        return inventoryDetail;
    }

    /**
     * 获取可用库存，用于出库场景
     * @param transactionFlow    交易流水
     * @return
     */
    private InventoryEntity getAvaliableInventory(TransactionFlowEntity transactionFlow) {
        InventoryEntity inventory = inventoryService.getById(transactionFlow.getInventoryId());
        String inventoryStatusName = InventoryStatusEnum.getNameByCode(transactionFlow.getDictInventoryStatus());
        if(null==inventory){
            String errMsg=StrUtil.format(ApiError.ERROR_99035.msg, transactionFlow.getSkuNo(), transactionFlow.getWarehouseName(), transactionFlow.getWarehouseLocation(), inventoryStatusName,(Objects.isNull(inventory)?0:inventory.getQty()),transactionFlow.getQty());
            log.error(errMsg);
            throw new ServiceException(ApiError.ERROR_99035.code, errMsg);
        }
        // 仓库是否允许负库存
        if(!inventoryHelper.allowNegativeInventory(transactionFlow.getWarehouseId())) {
            if(inventory.getQty()+transactionFlow.getQty()<0){
                String errMsg=StrUtil.format(ApiError.ERROR_99035.msg, transactionFlow.getSkuNo(), transactionFlow.getWarehouseName(), transactionFlow.getWarehouseLocation(), inventoryStatusName,(Objects.isNull(inventory)?0:inventory.getQty()),transactionFlow.getQty());
                log.error(errMsg);
                throw new ServiceException(ApiError.ERROR_99035.code, errMsg);
            }
        }

        return inventory;
    }

    /**
     * 入库核心业务逻辑处理
     */
    @SneakyThrows
    public  void inStockCore(InOutStockCoreDTO param, InventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String tansactionRuleId, String transactionNo) {
        // 仓库信息
        WarehouseDTO.UpdateDTO warehouseInfo = warehouseService.detailWithCache(param.getWarehouseId());
        if(Objects.isNull(warehouseInfo) || StrUtil.isEmpty(warehouseInfo.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        log.warn("交易业务：【{}】，来源单据：【{}】，单据id：【{}】，SKU编号：【{}】，库存状态：【{}】，开始走入库逻辑", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getSkuNo(), inventoryStatusEnum.getName());

        // 按照仓库+仓位+库存状态+SKU 进行锁定
        String lockKey = StrUtil.format( "{}:{}:{}:{}:{}", DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), param.getWarehouseId(), StrUtils.null2EmptyWithTrim(param.getWarehouseLocation()),inventoryStatusEnum.getCode() ,param.getSkuId());
        RLock rLock = redisson.getLock(lockKey);
        boolean isLock;
        try {
            // 设置最大等待锁时间
            isLock = rLock.tryLock(20, TimeUnit.SECONDS);
            if (!isLock) {
                log.error("单据：{},SKU:{},入库加锁失败,key={}",param.getSourceCode(),param.getSkuNo(), lockKey);
                throw new ServiceException(ApiError.ERROR_1026);
            }
            // 保存库存
            InventorySaveDTO inventorySaveDTO = inventoryService.addOrUpdate(param.getWarehouseId(), warehouseInfo.getOrgId(), param.getWarehouseLocation(), param.getSkuId(), param.getSkuNo(), inventoryStatusEnum.getCode(), param.getQty());
            InventoryDetailEntity inventoryDetail = inventoryDetailService.addOrUpdate(inventorySaveDTO.getInventoryId(), param.getBillDate(), param.getQty());
            InventoryEntity entity = inventoryService.getById(inventorySaveDTO.getInventoryId());
            // 时间为交易日期
            inventoryHisService.addOrUpdate(inventorySaveDTO.getInventoryId(), LocalDate.now(), entity.getQty());

            // 登记交易流水
            TransactionFlowDTO transactionFlowDTO = InventoryUtils.wrapTransactionFlowInOutStock(param, inventorySaveDTO.getInventoryId(), businessType, inventoryDetail.getId(), inventoryStatusEnum, param.getBillDate(), param.getQty(), warehouseInfo.getOrgId());
            transactionFlowDTO.setTransactionNo(transactionNo);
            transactionFlowService.add(transactionFlowDTO, businessType, tansactionRuleId, entity.getQty(), InventoryModeEnum.IN_STOCK);
        } catch (Exception e) {
            log.error("交易业务：{}，来源单据：{}，单据id：【{}】，SKU编号：【{}】，库存操作异常", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getSkuNo(),e );
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

    /**
     * 出库核心业务处理
     */
    @SneakyThrows
    public  void outStockCore (InOutStockCoreDTO param, InventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String tansactionRuleId,
                              String transactionNo) {
        // 仓库信息
        WarehouseDTO.UpdateDTO warehouseInfo = warehouseService.detailWithCache(param.getWarehouseId());
        if(Objects.isNull(warehouseInfo) || StrUtil.isEmpty(warehouseInfo.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }

        // 待出库数量
        Integer waitOutQty = param.getQty();
        log.info("交易业务：【{}】，来源单据：{}，单据id：【{}】，SKU编号：【{}】，库存状态：【{}】，开始走出库逻辑", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), inventoryStatusEnum.getName(), param.getSkuNo());

        // 按照仓库+仓位+库存状态+SKU 进行锁定
        String lockKey = StrUtil.format( "{}:{}:{}:{}:{}", DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), param.getWarehouseId(), StrUtils.null2EmptyWithTrim(param.getWarehouseLocation()),inventoryStatusEnum.getCode() ,param.getSkuId());
        RLock rLock = redisson.getLock(lockKey);
        boolean isLock;
        try {
            // 设置最大等待锁时间
            isLock = rLock.tryLock(20, TimeUnit.SECONDS);
            if (!isLock) {
                log.error("单据：{},SKU:{},入库加锁失败,key={}",param.getSourceCode(),param.getSkuNo(), lockKey);
                throw new ServiceException(ApiError.ERROR_1026);
            }

            InventoryEntity inventory = inventoryService.findInventory(warehouseInfo.getOrgId(), param.getWarehouseId(), param.getSkuId(), param.getWarehouseLocation(), inventoryStatusEnum.getCode());
            String inventoryStatusName = Optional.ofNullable(inventoryStatusEnum).map(InventoryStatusEnum::getName).orElse("");
            if(Objects.isNull(inventory)) {
                String errMsg=StrUtil.format(ApiError.ERROR_99035.msg, param.getSkuNo(), warehouseInfo.getName(), param.getWarehouseLocation(), inventoryStatusName,(Objects.isNull(inventory)?0:inventory.getQty()),param.getQty());
                log.error(errMsg);

                throw new ServiceException(ApiError.ERROR_99035.code, errMsg);
            }
            // 仓库负库存是否允许
            if(!inventoryHelper.allowNegativeInventory(param.getWarehouseId())) {
                if(inventory.getQty()<waitOutQty) {
                    String errMsg=StrUtil.format(ApiError.ERROR_99035.msg, param.getSkuNo(), warehouseInfo.getName(), param.getWarehouseLocation(), inventoryStatusName,(Objects.isNull(inventory)?0:inventory.getQty()),param.getQty());
                    log.error(errMsg);
                    throw new ServiceException(ApiError.ERROR_99035.code, errMsg);
                }
            }
            Integer inventoryQty=inventory.getQty();
            // 查询库存明细，按入库批次日期降序排序
            List<InventoryDetailEntity> inventoryDetails = inventoryDetailService.findListQtyGreatZero(inventory.getId());
            // 允许负库存
            if(inventoryHelper.allowNegativeInventory(param.getWarehouseId()) && CollUtil.isEmpty(inventoryDetails)) {
                // 判断是否含有小于等于0库存
                inventoryDetails = inventoryDetailService.findListQtyLeZero(inventory.getId());
                if(CollUtil.isEmpty(inventoryDetails)) {
                    log.warn("仓库【{}】SKU【{}】允许负库存，即时库存id【{}】,且没有大于0的库存明细，也没有小于等于0的库存明细", warehouseInfo.getName(), param.getSkuNo(), inventory.getId());
                    String errMsg=StrUtil.format(ApiError.ERROR_99035.msg, param.getSkuNo(), warehouseInfo.getName(), param.getWarehouseLocation(), inventoryStatusName,(Objects.isNull(inventory)?0:inventory.getQty()),param.getQty());
                    log.error(errMsg);
                    throw new ServiceException(ApiError.ERROR_99035.code, errMsg);
                } else {
                    // 取最后一条负库存明细
                    log.warn("仓库【{}】SKU【{}】允许负库存，即时库存id【{}】,有小于等于0的库存明细，从最后一条库存明细出库", warehouseInfo.getName(), param.getSkuNo(), inventory.getId());
                    InventoryDetailEntity lastInventoryDetailEntity =  inventoryDetails.get(inventoryDetails.size() - 1);
                    inventoryDetails = new ArrayList<>();
                    inventoryDetails.add(lastInventoryDetailEntity);
                }
            }
            // 循环扣减
            for (int i = 0; i < inventoryDetails.size(); i++) {
                InventoryDetailEntity detailEntity = inventoryDetails.get(i);
                // 已经足额扣减完成
                if (waitOutQty <= 0) {
                    break;
                }
                Integer tradeQty=Math.min(waitOutQty,detailEntity.getQty());

                // 仓库负库存是否允许
                if(inventoryHelper.allowNegativeInventory(param.getWarehouseId())) {
                    if (i == inventoryDetails.size() -1 && tradeQty < waitOutQty ){
                        log.warn("仓库【{}】SKU【{}】允许负库存，即时库存id【{}】，剩余待出库数量【{}】，交易数量【{}】不足出库，从最后一条库存明细出库",warehouseInfo.getName(), param.getSkuNo(), inventory.getId(), waitOutQty, tradeQty);
                        tradeQty = waitOutQty;
                    }
                }
                waitOutQty=waitOutQty-tradeQty;
                inventoryQty=inventoryQty-tradeQty;

                // 更新库存明细
                boolean updateFlag = inventoryDetailService.updateQtyById(detailEntity.getId(), (tradeQty * -1));
                if(!updateFlag) {
                    throw new ServiceException(ApiError.ERROR_1027);
                }

                // 此处再次验证，防止变成负库存
                InventoryDetailEntity curInventoryDetail = inventoryDetailService.getById(detailEntity.getId());
                // 仓库允许负库存判断
                if(!inventoryHelper.allowNegativeInventory(param.getWarehouseId())) {
                    if (curInventoryDetail.getQty() < 0) {
                        log.warn("库存明细id：{}出库后的库存数量变为:{}，不允许出库", detailEntity.getId(), curInventoryDetail.getQty());
                        throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, param.getSkuNo(), warehouseInfo.getName(), param.getWarehouseLocation(), inventoryStatusName, (Objects.isNull(detailEntity) ? 0 : detailEntity.getQty()), tradeQty));
                    }
                }

                // 登记交易流水（有可能一个操作产生多条，从多个库存明细中扣除）
                TransactionFlowDTO transactionFlow = InventoryUtils.wrapTransactionFlowInOutStock(param, inventory.getId(), businessType, detailEntity.getId(), inventoryStatusEnum, detailEntity.getInstockBatchDate(), tradeQty, warehouseInfo.getOrgId());
                transactionFlow.setTransactionNo(transactionNo);
                transactionFlowService.add(transactionFlow, businessType, tansactionRuleId, inventoryQty, InventoryModeEnum.OUT_STOCK);
            }

            // 仓库允许负库存判断
            if(waitOutQty > 0) {
                // 仓库允许负库存判断
                if(!inventoryHelper.allowNegativeInventory(param.getWarehouseId())) {
                    throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, param.getSkuNo(), warehouseInfo.getName(), param.getWarehouseLocation(), inventoryStatusName,(Objects.isNull(inventory)?0:inventory.getQty()),param.getQty()));
                }
            }

            // 更新库存表
            boolean updateFlag =  inventoryService.updateQtyById(inventory.getId(), (param.getQty()*-1));
            if(!updateFlag) {
                throw new ServiceException(ApiError.ERROR_1027);
            }
            // 此处再次验证，防止变成负库存
            InventoryEntity curInventory = inventoryService.getById(inventory.getId());
            // 仓库允许负库存判断
            if(!inventoryHelper.allowNegativeInventory(param.getWarehouseId())) {
                if(curInventory.getQty() < 0) {
                    log.warn("库存id:{}出库后的库存数量变为:{}，不允许出库", inventory.getId(), curInventory.getQty());
                    throw new ServiceException(ApiError.ERROR_99035.code, StrUtil.format(ApiError.ERROR_99035.msg, param.getSkuNo(), warehouseInfo.getName(), param.getWarehouseLocation(), inventoryStatusName,(Objects.isNull(curInventory)?0:curInventory.getQty()),param.getQty()));
                }
            }
            // 创建/修改库存历史
            inventoryHisService.addOrUpdate(inventory.getId(), LocalDate.now(), curInventory.getQty());

        }  catch (Exception e) {
            log.error("交易业务：【{}】，来源单据：【{}】，单据id：【{}】，SKU编号：【{}】，库存操作异常", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getSkuNo(),e );
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