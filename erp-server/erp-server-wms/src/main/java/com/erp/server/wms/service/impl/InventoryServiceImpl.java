package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.DistributedLockEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.inventory.InStockOrOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryTransferDTO;
import com.erp.model.wms.dto.inventory.TransactionFlowDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.*;
import com.erp.server.wms.config.InventoryHelper;
import com.erp.server.wms.mapper.InventoryMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Classname: InventoryServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-25  12:17
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class InventoryServiceImpl extends SuperServiceImpl<InventoryMapper, InventoryEntity> implements InventoryService {

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private InventoryDetailService inventoryDetailService;

    @Autowired
    private TransactionRuleService transactionRuleService;

    @Resource
    private InventoryHelper inventoryHelper;

    @Autowired
    private TransactionFlowService transactionFlowService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private CommonService commonService;

    @Override
    public InventoryEntity findInventoryByWareLocalSkuStatus(String orgId, String warehouseId, String skuId, String warehouseLocationId, String status) {
        // 组织+仓库+库位+SKU+状态 确定唯一一条记录
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryEntity::getWarehouseId,warehouseId).eq(InventoryEntity::getOrgId, orgId)
                .eq(InventoryEntity::getSkuId, skuId)
                .eq(InventoryEntity::getWarehouseLocation, StrUtils.null2EmptyWithTrim(warehouseLocationId))
                .eq(InventoryEntity::getDictInventoryStatus, status);
        InventoryEntity inventory = baseMapper.selectOne(queryWrapper);
        return inventory;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveInOutStockByType(List<InStockOrOutStockDTO> paramList, InventoryBusinessTypeEnum businessType) {
        ValidatorUtil.isTrueCall(Objects.nonNull(businessType),()->new ServiceException(ApiError.ERROR_400.code, "业务类型不能为空"));
        // 查询配置的交易规则
        List<TransactionRuleEntity> transactionRules = transactionRuleService.findByDictBizType(businessType.getCode());
        // 1.验证参数
        this.checkInOutStockParam(paramList, businessType, transactionRules);
        // 2.仓库集合
        Map<String, WarehouseEntity> warehouseMap = Maps.newHashMap();
        // 3.出入库业务处理
        this.inOutStockHandler(paramList, businessType, transactionRules, warehouseMap);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveTransferByType(List<InventoryTransferDTO> paramList, InventoryBusinessTypeEnum businessType) {

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByRule(List<InventoryTransferDTO> paramList, List<InventoryTransferDTO> ruleList, InventoryBusinessTypeEnum businessType) {

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unApprove(String billId) {

    }


    /**
     * 出入库业务验证参数
     * @param paramLis
     */
    public void checkInOutStockParam(List<InStockOrOutStockDTO> paramLis, InventoryBusinessTypeEnum businessType, List<TransactionRuleEntity> transactionRules) {
        for(InStockOrOutStockDTO param : paramLis) {
            ValidatorUtil.validateEntity(param);
            inventoryHelper.checkCommonBiz(param);// 通用检查
            inventoryHelper.checkAllowTrade(param);// 关账检查
            inventoryHelper.checkEnoughStockIfNecessary(param, businessType, transactionRules);// 出库库存数量检查
        }
    }

    /**
     * 循环处理出入库业务
     * @param paramLis
     * @param businessType
     * @param transactionRules
     * @param warehouseMap
     */
    public void inOutStockHandler(List<InStockOrOutStockDTO> paramLis, InventoryBusinessTypeEnum businessType, List<TransactionRuleEntity> transactionRules, Map<String,WarehouseEntity> warehouseMap) {
        for(InStockOrOutStockDTO param : paramLis) {
            this.inOutStockSingleHandler(param, businessType, transactionRules, warehouseMap);
        }
    }


    /**
     * 单个SKU出入库业务处理
     * @param param
     * @param businessType
     * @param warehouseMap
     */
    public void inOutStockSingleHandler(InStockOrOutStockDTO param, InventoryBusinessTypeEnum businessType, List<TransactionRuleEntity> transactionRules, Map<String,WarehouseEntity> warehouseMap) {
        // 状态
        if(Objects.nonNull(param.getInventoryStatusEnum())) { // 参数传输了要改的状态
            log.info("参数已传库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", param.getInventoryStatusEnum().getName(), businessType.getName(), param.getSourceTypeEnum().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo());
            this.inStockCore(param, businessType, param.getInventoryStatusEnum(), "", warehouseMap);
        } else {
            if(CollUtil.isEmpty(transactionRules)) {
                throw new ServiceException(ApiError.ERROR_99034.code, StrUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            log.info("参数未传库存状态，从配置读取，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", businessType.getName(), param.getSourceTypeEnum().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo());
            for(TransactionRuleEntity transactionRule : transactionRules) {
                InventoryWarehouseOptionEnum inventoryWarehouseOptionEnum = InventoryWarehouseOptionEnum.of(transactionRule.getWarehouseOption());
                ValidatorUtil.isTrue(Objects.nonNull(inventoryWarehouseOptionEnum), () -> new ServiceException(ApiError.ERROR_99033));
                InventoryStatusEnum inventoryStatusEnum = InventoryStatusEnum.of(transactionRule.getInventoryStatus());
                ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException(ApiError.ERROR_99036));
                InventoryModeEnum inventoryModeEnum = InventoryModeEnum.of(transactionRule.getTransactionMode());
                ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum), () -> new ServiceException(ApiError.ERROR_99038));
                // 可能某个业务类型在同一个仓库即需要做入也需要做出，分别调用逻辑
                if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) { // 入库
                    this.inStockCore(param, businessType, inventoryStatusEnum, transactionRule.getId(), warehouseMap);
                } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 出库
                    // TODO
                }
            }

        }
    }

    /**
     * 入库核心业务逻辑处理
     */
    public void inStockCore(InStockOrOutStockDTO param, InventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String tansactionRuleId, Map<String,WarehouseEntity> warehouseMap) {
        // 仓库组织
        String orgId = param.getOrgId();
        // 仓库
        String warehouseId = param.getWarehouseId();
        // SKU
        String skuId = param.getSkuId();
        String skuNo = param.getSkuNo();
        // 库位
        String warehouseLocationId = param.getWarehouseLocation();
        // 数量
        Integer qty = param.getQty();
        // 来源
        SourceTypeEnum sourceTypeEnum = param.getSourceTypeEnum();
        // 单据信息
        String sourceId = param.getSourceId();
        // 单据日期
        LocalDate billDate = param.getBillDate();

        // 按照仓库+SKU进行锁定，暂不考虑库位，防止数据冲突
        String lockKey = StrUtil.format( "{}:{}:{}",DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), warehouseId, skuId);
        RLock lock = redisson.getLock(lockKey);
        boolean isLock;
        try {
            isLock = lock.tryLock(5, TimeUnit.SECONDS);
            log.info("仓库：{}，组织：{}，SKU ID：{}，SKU编号：{}, 交易业务：{}，来源单据：{}, 是否获取到锁: {}", warehouseId, orgId, skuId, skuId, businessType.getName(), sourceTypeEnum.getName(), isLock);
            if (!isLock) {
                throw new ServiceException(ApiError.ERROR_1026);
            }
            log.info("库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", inventoryStatusEnum.getName(), businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
            InventoryEntity inventory =  this.findInventoryByWareLocalSkuStatus(orgId, warehouseId, skuId, warehouseLocationId, inventoryStatusEnum.getCode());
            if(Objects.isNull(inventory)) {
                log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，状态【{}】在库存实时表中不存在数据，新增数据", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName());
                inventory = new InventoryEntity();
                inventory.setWarehouseId(warehouseId);
                inventory.setOrgId(orgId);
                inventory.setWarehouseLocation(param.getWarehouseLocation());
                inventory.setSkuId(param.getSkuId());
                inventory.setSkuNo(param.getSkuNo());
                inventory.setDictInventoryStatus(inventoryStatusEnum.getCode());
                inventory.setQty(0); // 初始为0
                super.save(inventory);
            }
            Integer originInventoryQty = inventory.getQty(); // 库存原数量
            String inventoryInfoId = inventory.getId();
            Integer afterInventoryQty = originInventoryQty + qty;
            log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，状态【{}】，实时库存原数量：【{}】，操作数量【{}】，操作后数量【{}】", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName(), originInventoryQty, qty, afterInventoryQty);
            // 入库批次日期取单据日期
            InventoryDetailEntity inventoryDetail =  inventoryDetailService.findByInfoIdAndInstockBatchDate(inventoryInfoId, billDate);
            if (Objects.nonNull(inventoryDetail)) {
                log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，状态【{}】，单据日期：【{}】,库存表id：【{}】，不存在库存明细数据，新增数据", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName(), billDate, inventoryInfoId);
                inventoryDetail = new InventoryDetailEntity();
                inventoryDetail.setInfoId(inventoryInfoId);
                inventoryDetail.setInstockBatchDate(billDate);
                inventoryDetail.setQty(0); // 初始为0
                inventoryDetailService.save(inventoryDetail);
            }
            String inventoryDetailId = inventoryDetail.getId();
            Integer originInventoryDetailQty = inventoryDetail.getQty(); // 库存明细原数量
            Integer afterInventoryDetailQty = originInventoryDetailQty + qty;
            log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，批次日期：【{}】，状态【{}】，库存明细原数量：【{}】，操作数量【{}】，操作后数量【{}】", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryDetail.getInstockBatchDate(), inventoryStatusEnum.getName(), originInventoryDetailQty, qty, afterInventoryDetailQty);

            TransactionFlowDTO transactionFlowDTO = new TransactionFlowDTO();
            BeanMapperUtils.copy(param, transactionFlowDTO);
            transactionFlowDTO.setInventoryId(inventoryInfoId);
            transactionFlowDTO.setInventoryDetailId(inventoryDetailId);
            transactionFlowDTO.setDictInventoryStatus(inventoryStatusEnum.getCode());
            transactionFlowDTO.setDictBizType(businessType.getCode());
            transactionFlowDTO.setInstockBatchDate(inventoryDetail.getInstockBatchDate());
            transactionFlowDTO.setBillDate(param.getBillDate());
            transactionFlowDTO.setSourceType(param.getSourceTypeEnum().getCode());
            transactionFlowDTO.setQty(param.getQty());
            transactionFlowDTO.setOperationMode(Objects.nonNull(param.getOperationMode()) ? param.getOperationMode().getCode() : "");
            this.recordFlowTransaction(transactionFlowDTO, businessType, tansactionRuleId, afterInventoryQty, InventoryModeEnum.IN_STOCK, warehouseMap);

            // 更新实时库存表数量
            inventory.setQty(afterInventoryQty);
            super.updateById(inventory);
            // 更新库存明细数量
            inventoryDetail.setQty(afterInventoryDetailQty);
            inventoryDetailService.updateById(inventoryDetail);
            // TODO 创建/修改库存历史
        } catch (Exception e) {
            log.error("仓库：{}，SKU：{}库存操作时获取锁异常",e);
            throw new ServiceException(ApiError.ERROR_1026);
        } finally {
            //释放锁
            if(lock.isLocked() && lock.isHeldByCurrentThread()){ // 锁是否存在，是当前执行线程的锁
                lock.unlock(); // 释放锁
            }
        }
    }

    /**
     * 出库业务处理
     */
    public void outStockHandler(InStockOrOutStockDTO param, InventoryBusinessTypeEnum businessType, List<TransactionRuleEntity> transactionRules) {

    }

    /**
     * 出库核心业务处理
     */
    public void outStockCore () {

    }


    /**
     * 记录库存交易流水
     */
    public void recordFlowTransaction(TransactionFlowDTO param, InventoryBusinessTypeEnum businessType,
                                      String transactionRuleId,Integer afterInventoryQty, InventoryModeEnum inventoryModeEnum,
                                      Map<String, WarehouseEntity> warehouseMap) {
        // 记录交易流水
        TransactionFlowEntity transactionFlowEntity = new TransactionFlowEntity();
        transactionFlowEntity.setBillDate(param.getBillDate());
        transactionFlowEntity.setInventoryId(param.getInventoryId());
        transactionFlowEntity.setInventoryDetailId(param.getInventoryDetailId());
        transactionFlowEntity.setOrgId(param.getOrgId());
        // 获取仓库名称
        WarehouseEntity warehouseEntity = warehouseMap.computeIfAbsent(param.getWarehouseId(),(v)->warehouseService.getById(v));
        ValidatorUtil.isTrueCall(Objects.nonNull(warehouseEntity),()->new ServiceException(ApiError.ERROR_99002));
        transactionFlowEntity.setWarehouseName(warehouseEntity.getName());
        transactionFlowEntity.setWarehouseLocation(param.getWarehouseLocation());
        // TODO 暂时还没有库位表
        transactionFlowEntity.setWarehouseLocationName("");
        transactionFlowEntity.setDictInventoryStatus(param.getDictInventoryStatus());
        // 批次日期取库存明细表上关联的日期
        transactionFlowEntity.setInstockBatchDate(param.getInstockBatchDate());
        transactionFlowEntity.setSkuId(param.getSkuId());
        transactionFlowEntity.setSkuNo(param.getSkuNo());
        transactionFlowEntity.setSourceType(param.getSourceType());
        transactionFlowEntity.setSourceId(param.getSourceId());
        transactionFlowEntity.setSourceCode(param.getSourceCode());
        transactionFlowEntity.setSourceDetailId(param.getSourceDetailId());
        transactionFlowEntity.setDictBizType(businessType.getCode());
        LoginUser loginUser = commonService.getUserInfo();
        transactionFlowEntity.setUserId(Objects.nonNull(loginUser) ? loginUser.getUid() : "");
        transactionFlowEntity.setTradeTime(LocalDateTime.now());
        transactionFlowEntity.setTransactionRuleId(StrUtils.null2EmptyWithTrim(transactionRuleId));
        Integer qty = param.getQty();
        if(Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) {
            qty = qty * -1;
        }
        transactionFlowEntity.setQty(qty);
        transactionFlowEntity.setCurInventoryQty(afterInventoryQty);
        transactionFlowEntity.setOperationMode(StrUtils.null2EmptyWithTrim(param.getOperationMode()));
        transactionFlowService.save(transactionFlowEntity);
    }



}