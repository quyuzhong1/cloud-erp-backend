package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.DistributedLockEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.*;
import com.erp.server.wms.config.InventoryHelper;
import com.erp.server.wms.mapper.InventoryMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Resource
    private InventoryHelper inventoryHelper;

    @Autowired
    private TransactionFlowService transactionFlowService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private InventoryHisService inventoryHisService;

    @Autowired
    private InventoryMapper inventoryMapper;

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

    @Override
    public List<InventoryEntity> findInventoryByWareSkuStatusCheckLocation(String orgId, String warehouseId, String skuId, String warehouseLocationId, String status) {
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryEntity::getWarehouseId,warehouseId).eq(InventoryEntity::getOrgId, orgId)
                .eq(InventoryEntity::getSkuId, skuId)
                .eq(InventoryEntity::getDictInventoryStatus, status);
        if(StrUtils.isNotEmpty(warehouseLocationId)) {
            queryWrapper.eq(InventoryEntity::getWarehouseLocation, StrUtils.null2EmptyWithTrim(warehouseLocationId));
        } else {
            log.info("组织id：【{}】，仓库id：【{}】,SKU：【{}】，状态：【{}】，库位为空，不作为查询条件", orgId, warehouseId, skuId, status, warehouseLocationId);
        }
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Integer getUsableInventoryTotal(String orgId, String warehouseId, String skuId, String warehouseLocationId) {
        return this.getInventoryTotal(orgId, warehouseId, skuId, warehouseLocationId, InventoryStatusEnum.USABLE.getCode());
    }

    @Override
    public Integer getInventoryTotal(String orgId, String warehouseId, String skuId, String warehouseLocationId, String status) {
        List<InventoryEntity> inventories = this.findInventoryByWareSkuStatusCheckLocation(orgId, warehouseId, skuId, warehouseLocationId, status);
        return CollUtil.isEmpty(inventories) ? 0 : inventories.stream().collect(Collectors.summingInt(InventoryEntity::getQty));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveInOutStockByType(List<InStockOrOutStockDTO> paramList, InventoryBusinessTypeEnum businessType) {
        log.info("出入库库存交易按业务类型，入参：{}，业务类型：{}", JSONObject.toJSONString(paramList), businessType.getName());
        // 1.验证参数
        List<TransactionRuleDTO> transactionRuleParams = inventoryHelper.wrapTransactionRule(businessType);
        inventoryHelper.checkInOutStockParam(paramList, businessType, transactionRuleParams);
        // 2.出入库业务处理
        String transactionNo = IdUtil.getSnowflake(1, 1).nextIdStr(); // 关联交易号
        this.inOutStockHandler(paramList, businessType, transactionRuleParams, transactionNo);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveTransferByType(List<TransferDTO> paramList, InventoryBusinessTypeEnum businessType) {
        log.info("调拨业务库存交易按业务类型，入参：{}，业务类型：{}", JSONObject.toJSONString(paramList), businessType.getName());
        // 1.验证参数
        List<TransactionRuleDTO> transactionRuleParams = inventoryHelper.wrapTransactionRule(businessType);
        inventoryHelper.checkTransferStockParam(paramList, businessType, transactionRuleParams);
        // 2.调拨业务处理
        String transactionNo = IdUtil.getSnowflake(1, 1).nextIdStr(); // 关联交易号
        this.transferHandler(paramList, businessType, transactionRuleParams, transactionNo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByRule(List<TransferDTO> paramList, List<TransactionRuleDTO> ruleList, InventoryBusinessTypeEnum businessType) {
        log.info("调拨业务库存交易按自定义规则，入参：{}，业务类型：{}", JSONObject.toJSONString(paramList), businessType.getName());
        // 1.验证参数
        inventoryHelper.checkTransferStockParam(paramList, businessType, ruleList);
        // 2.调拨业务处理
        String transactionNo = IdUtil.getSnowflake(1, 1).nextIdStr(); // 关联交易号
        this.transferHandler(paramList, businessType, ruleList, transactionNo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unApprove(InventoryUnApproveDTO dto) {
        log.info("库存交易反审核，单据类型：{}, 单据id：{}", dto.getSourceType().getName(), dto.getBillId());
        // 根据单据类型和单据id查询出对应的交易流水， 按创建时间正序排序
        List<TransactionFlowEntity> txnFlows = transactionFlowService.getTxnFlowSCreatTimeSorted(dto.getSourceType().getCode(), dto.getBillId());
        ValidatorUtil.isTrue(CollUtil.isNotEmpty(txnFlows),()->new ServiceException(ApiError.ERROR_99040));

    }


    /**
     * 循环处理出入库业务
     * @param paramLis
     * @param businessType
     * @param transactionRuleParams
     */
    public void inOutStockHandler(List<InStockOrOutStockDTO> paramLis, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams, String transactionNo) {
        Map<String, WarehouseEntity> warehouseMap = Maps.newHashMap();// TODO 仓库集合，后续改成从redis缓存中读取
        for(InStockOrOutStockDTO param : paramLis) {
            this.inOutStockSingleHandler(param, businessType, transactionRuleParams, warehouseMap, transactionNo);
        }
    }

    /**
     * 循环处理调拨业务
     * @param paramLis
     * @param businessType
     * @param transactionRuleParams
     */
    public void transferHandler(List<TransferDTO> paramLis, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams, String transactionNo) {
        Map<String, WarehouseEntity> warehouseMap = Maps.newHashMap();// TODO 仓库集合，后续改成从redis缓存中读取
        for(TransferDTO param : paramLis) {
            // 当前仓出入库业务处理
            InStockOrOutStockTransformDTO curWareInOrOutStock = inventoryHelper.wrapInOutStockByTransfer(param, InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT);
            this.transferStockSingleHandler(curWareInOrOutStock, businessType, transactionRuleParams, warehouseMap, transactionNo);
            // 目的仓出入库业务处理
            InStockOrOutStockTransformDTO targetWareInOrOutStock = inventoryHelper.wrapInOutStockByTransfer(param, InventoryWarehouseOptionEnum.WAREHOUSE_TARGET);
            this.transferStockSingleHandler(targetWareInOrOutStock, businessType, transactionRuleParams, warehouseMap, transactionNo);
        }
    }

    /**
     * 单个SKU调拨业务处理
     * @param param
     * @param businessType
     * @param transactionRuleParams
     * @param warehouseMap
     */
    public void transferStockSingleHandler(InStockOrOutStockTransformDTO param, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams,
                                           Map<String, WarehouseEntity> warehouseMap, String transactionNo) {
        // 状态
        if(Objects.nonNull(param.getInventoryStatus())) { // 参数传输了要改的状态
            log.info("参数已传库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", param.getInventoryStatus().getName(), businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo());
            InventoryModeEnum inventoryModeEnum = param.getInventoryMode();
            ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum),()->new ServiceException(ApiError.ERROR_400.code, "交易类型不能为空"));
            if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) { //入库
                this.inStockCore(param, businessType, param.getInventoryStatus(), "", warehouseMap, transactionNo);
            } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 出库
                this.outStockCore(param, businessType, param.getInventoryStatus(), "", warehouseMap, transactionNo);
            }
        } else {
            if(CollUtil.isEmpty(transactionRuleParams)) {
                throw new ServiceException(ApiError.ERROR_99034.code, StrUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            log.info("参数未传库存状态，从配置读取，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo());
            // 判断当前仓是入库还是出库
            transactionRuleParams = transactionRuleParams.stream().filter(r->Objects.equals(r.getWarehouseOption(), param.getWarehouseOptionEnum())).collect(Collectors.toList());
            if(CollUtil.isEmpty(transactionRuleParams)) {
                throw new ServiceException(ApiError.ERROR_99034.code, StrUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            for(TransactionRuleDTO transactionRule : transactionRuleParams) {
                InventoryWarehouseOptionEnum inventoryWarehouseOptionEnum = transactionRule.getWarehouseOption();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryWarehouseOptionEnum), () -> new ServiceException(ApiError.ERROR_99033));
                InventoryStatusEnum inventoryStatusEnum = transactionRule.getInventoryStatus();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException(ApiError.ERROR_99036));
                InventoryModeEnum inventoryModeEnum = transactionRule.getTransactionMode();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum), () -> new ServiceException(ApiError.ERROR_99038));
                // 可能某个业务类型在同一个仓库即需要做入也需要做出，分别调用逻辑
                if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) { // 入库
                    this.inStockCore(param, businessType, inventoryStatusEnum, transactionRule.getId(), warehouseMap, transactionNo);
                } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 出库
                    this.outStockCore(param, businessType, inventoryStatusEnum,  transactionRule.getId(), warehouseMap, transactionNo);
                }
            }
        }
    }

    /**
     * 单个SKU出入库业务处理
     * @param param
     * @param businessType
     */
    @SneakyThrows
    public void inOutStockSingleHandler(InStockOrOutStockDTO param, InventoryBusinessTypeEnum businessType, List<TransactionRuleDTO> transactionRuleParams,
                                        Map<String, WarehouseEntity> warehouseMap,String transactionNo) {
        // 状态
        if(Objects.nonNull(param.getInventoryStatus())) { // 参数传输了要改的状态
            log.info("参数已传库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", param.getInventoryStatus().getName(), businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo());
            InventoryModeEnum inventoryModeEnum = param.getInventoryMode();
            ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum),()->new ServiceException(ApiError.ERROR_400.code, "交易类型不能为空"));
            if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) { //入库
                this.inStockCore(param, businessType, param.getInventoryStatus(), "", warehouseMap, transactionNo);
            } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 出库
                this.outStockCore(param, businessType, param.getInventoryStatus(), "", warehouseMap, transactionNo);
            }
        } else {
            if(CollUtil.isEmpty(transactionRuleParams)) {
                throw new ServiceException(ApiError.ERROR_99034.code, StrUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
            }
            log.info("参数未传库存状态，从配置读取，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo());
            for(TransactionRuleDTO transactionRule : transactionRuleParams) {
                InventoryWarehouseOptionEnum inventoryWarehouseOptionEnum = transactionRule.getWarehouseOption();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryWarehouseOptionEnum), () -> new ServiceException(ApiError.ERROR_99033));
                InventoryStatusEnum inventoryStatusEnum = transactionRule.getInventoryStatus();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException(ApiError.ERROR_99036));
                InventoryModeEnum inventoryModeEnum = transactionRule.getTransactionMode();
                ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum), () -> new ServiceException(ApiError.ERROR_99038));
                // 可能某个业务类型在同一个仓库即需要做入也需要做出，分别调用逻辑
                if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) { // 入库
                    this.inStockCore(param, businessType, inventoryStatusEnum, transactionRule.getId(), warehouseMap, transactionNo);
                } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) { // 出库
                    this.outStockCore(param, businessType, inventoryStatusEnum,  transactionRule.getId(), warehouseMap, transactionNo);
                }
            }

        }
    }

    /**
     * 入库核心业务逻辑处理
     */
    @SneakyThrows
    public void inStockCore(InStockOrOutStockDTO param, InventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String tansactionRuleId, Map<String, WarehouseEntity> warehouseMap, String transactionNo) {
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
        SourceTypeEnum sourceTypeEnum = param.getSourceType();
        // 单据信息
        String sourceId = param.getSourceId();
        // 单据日期
        LocalDate billDate = param.getBillDate();
        log.info("交易业务：{}，来源单据：{}，单据id：【{}】，SKU编号：【{}】，开始走入库逻辑", businessType.getName(), sourceTypeEnum.getName(), param.getSourceId(), param.getSkuNo());
        // 按照仓库+SKU进行锁定，暂不考虑库位，防止数据冲突
        String lockKey = StrUtil.format( "{}:{}:{}",DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), warehouseId, skuId);
        RLock lock = redisson.getLock(lockKey);
        boolean isLock;
        try {
            isLock = lock.tryLock(5, TimeUnit.SECONDS);
            log.info("仓库：{}，组织：{}，SKU ID：{}，SKU编号：{}, 交易业务：{}，来源单据类型：{}, 单据id：【{}】，SKU编号：【{}】，是否获取到锁: {}", warehouseId, orgId, skuId, skuId, businessType.getName(), sourceTypeEnum.getName(), param.getSourceId(), param.getSkuNo(), isLock);
            if (!isLock) {
                throw new ServiceException(ApiError.ERROR_1026);
            }
            log.info("库存状态：【{}】，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】", inventoryStatusEnum.getName(), businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
            // TODO 此处后续改成读写锁
            // 此处注意，入库传不传仓位都带仓位条件查询
            InventoryEntity inventory =  this.findInventoryByWareLocalSkuStatus(orgId, warehouseId, skuId, warehouseLocationId, inventoryStatusEnum.getCode());
            Integer originInventoryQty = 0; // 库存原数量
            if(Objects.isNull(inventory)) {
                log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，状态【{}】在库存实时表中不存在数据，新增数据", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName());
                inventory = new InventoryEntity();
                inventory.setWarehouseId(warehouseId);
                inventory.setOrgId(orgId);
                inventory.setWarehouseLocation(StrUtils.null2EmptyWithTrim(param.getWarehouseLocation()));
                inventory.setSkuId(param.getSkuId());
                inventory.setSkuNo(param.getSkuNo());
                inventory.setDictInventoryStatus(inventoryStatusEnum.getCode());
                inventory.setQty(qty);
                inventory.setVersion(1);
                baseMapper.insert(inventory);
            } else {
                originInventoryQty = inventory.getQty();
                // 更新实时库存表数量
                LoginUser loginUser = commonService.getUserInfo();
                int updateCnt =  inventoryMapper.updateQtyById(inventory.getId(), qty, inventory.getVersion(), LocalDateTime.now(), StrUtils.null2EmptyWithTrim(loginUser.getUid()), StrUtils.null2EmptyWithTrim(loginUser.getUserName()));
                if(updateCnt != 1) {
                    throw new ServiceException(ApiError.ERROR_1027);
                }
            }
            String inventoryInfoId = inventory.getId();
            Integer afterInventoryQty = originInventoryQty + qty;
            log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，状态【{}】，实时库存原数量：【{}】，操作数量【{}】，操作后数量【{}】", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName(), originInventoryQty, qty, afterInventoryQty);
            // 入库批次日期取单据日期
            InventoryDetailEntity inventoryDetail =  inventoryDetailService.findByInfoIdAndInstockBatchDate(inventoryInfoId, billDate);
            if (Objects.isNull(inventoryDetail)) {
                log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，状态【{}】，单据日期：【{}】,库存表id：【{}】，不存在库存明细数据，新增数据", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName(), billDate, inventoryInfoId);
                inventoryDetail = new InventoryDetailEntity();
                inventoryDetail.setInfoId(inventoryInfoId);
                inventoryDetail.setInstockBatchDate(billDate);
                inventoryDetail.setQty(qty);
                inventoryDetail.setVersion(1);
                inventoryDetailService.save(inventoryDetail);
            } else {
                Integer originInventoryDetailQty = inventoryDetail.getQty(); // 库存明细原数量
                Integer afterInventoryDetailQty = originInventoryDetailQty + qty;
                log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，批次日期：【{}】，状态【{}】，库存明细原数量：【{}】，操作数量【{}】，操作后数量【{}】", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryDetail.getInstockBatchDate(), inventoryStatusEnum.getName(), originInventoryDetailQty, qty, afterInventoryDetailQty);
                // 更新库存明细数量
                int updateCnt = inventoryDetailService.updateQtyById(inventoryDetail.getId(), qty, inventoryDetail.getVersion());
                if(updateCnt != 1) {
                    throw new ServiceException(ApiError.ERROR_1027);
                }
            }
            String inventoryDetailId = inventoryDetail.getId();
            // 登记交易流水
            TransactionFlowDTO transactionFlowDTO = inventoryHelper.wrapTransactionFlowInOutStock(param, inventoryInfoId, businessType, inventoryDetailId, inventoryStatusEnum, inventoryDetail.getInstockBatchDate(), qty);
            transactionFlowDTO.setTransactionNo(transactionNo);
            this.recordFlowTransaction(transactionFlowDTO, businessType, tansactionRuleId, afterInventoryQty, InventoryModeEnum.IN_STOCK, warehouseMap);

            // 创建/修改库存历史
            InventoryHisEntity inventoryHis = inventoryHisService.findByInfoIdAndBillDate(inventoryInfoId, param.getBillDate());
            if (Objects.isNull(inventoryHis)) {
                log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，状态【{}】，单据日期：【{}】,库存表id：【{}】，不存在库存历史数据，新增数据", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName(), billDate, inventoryInfoId);
                inventoryHis = new InventoryHisEntity();
                inventoryHis.setInfoId(inventoryInfoId);
                inventoryHis.setBillDate(billDate);
                inventoryHis.setQty(afterInventoryQty);
                inventoryHis.setVersion(1);
                inventoryHisService.save(inventoryHis);
            } else {
                int updateCnt = inventoryHisService.updateQtyById(inventoryHis.getId(), afterInventoryQty, inventoryHis.getVersion());
                if(updateCnt != 1) {
                    throw new ServiceException(ApiError.ERROR_1027);
                }
            }
        } catch (Exception e) {
            log.info("交易业务：{}，来源单据：{}，单据id：【{}】，SKU编号：【{}】，库存操作异常", businessType.getName(), sourceTypeEnum.getName(), param.getSourceId(), param.getSkuNo(),e );
            if(e instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) e;
                throw serviceException;
            } else {
                throw e;
            }
        } finally {
            //释放锁
            if(lock.isLocked() && lock.isHeldByCurrentThread()){ // 锁是否存在，是当前执行线程的锁
                lock.unlock(); // 释放锁
            }
        }
    }

    /**
     * 出库核心业务处理
     */
    @SneakyThrows
    public void outStockCore (InStockOrOutStockDTO param, InventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String tansactionRuleId,
                              Map<String, WarehouseEntity> warehouseMap, String transactionNo) {
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
        SourceTypeEnum sourceTypeEnum = param.getSourceType();
        // 单据信息
        String sourceId = param.getSourceId();
        // 单据日期
        LocalDate billDate = param.getBillDate();
        log.info("交易业务：{}，来源单据：{}，单据id：【{}】，SKU编号：【{}】，开始走出库逻辑", businessType.getName(), sourceTypeEnum.getName(), param.getSourceId(), param.getSkuNo());
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
            // 为了防止库位数据不准，不做自动扣减
            InventoryEntity inventory = this.findInventoryByWareLocalSkuStatus(orgId, warehouseId, skuId, warehouseLocationId, inventoryStatusEnum.getCode());
            if(Objects.isNull(inventory)) {
                log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，状态【{}】在库存实时表中不存在数据，无法出库", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName());
                throw new ServiceException(ApiError.ERROR_99035);
            }
            Integer originQty = inventory.getQty();
            log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：【{}】, 来源单据：【{}】, 业务类型：【{}】，单据日期：【{}】，状态【{}】，库存原数量：【{}】，操作数量【{}】", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), param.getBillDate(), inventoryStatusEnum.getName(), originQty, qty);
            // 判断库存数量是否足够出库
            if(originQty < qty) {
                throw new ServiceException(ApiError.ERROR_99035);
            }
            // 查询库存明细，排序，雪花算法id在单机上是严格递增的，但是在分布式环境下不是严格递增的，此处改为按创建时间递增排序
            List<InventoryDetailEntity> inventoryDetails = inventoryDetailService.findByInventoryIdAndQtyGreatZero(inventory.getId());
            inventoryDetails = inventoryDetails.stream().sorted(Comparator.comparing(InventoryDetailEntity::getCreateTime)).collect(Collectors.toList());
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
                TransactionFlowDTO transactionFlowDTO = inventoryHelper.wrapTransactionFlowInOutStock(param, inventory.getId(), businessType, inventoryDetailEntity.getId(), inventoryStatusEnum, inventoryDetailEntity.getInstockBatchDate(), detailDeductQty);
                transactionFlowDTO.setTransactionNo(transactionNo);
                this.recordFlowTransaction(transactionFlowDTO, businessType, tansactionRuleId, transactionInventoryQty, InventoryModeEnum.OUT_STOCK, warehouseMap);
            }
            if(waitOutQty > 0) {
                throw new ServiceException(ApiError.ERROR_99035);
            }
            // 更新库存表
            Integer afterInventoryQty = originQty - qty;
            LoginUser loginUser = commonService.getUserInfo();
            int updateCnt =  inventoryMapper.updateQtyById(inventory.getId(), qty * -1, inventory.getVersion(), LocalDateTime.now(), StrUtils.null2EmptyWithTrim(loginUser.getUid()), StrUtils.null2EmptyWithTrim(loginUser.getUserName()));
            if(updateCnt != 1) {
                throw new ServiceException(ApiError.ERROR_1027);
            }
            log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，单据日期：【{}】，状态【{}】，库存原数量：【{}】，操作数量【{}】，操作后数量【{}】", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), param.getBillDate(), inventoryStatusEnum.getName(), originQty, qty, afterInventoryQty);
            // 创建/修改库存历史
            InventoryHisEntity inventoryHis = inventoryHisService.findByInfoIdAndBillDate(inventory.getId(), param.getBillDate());
            if (Objects.isNull(inventoryHis)) {
                log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，SKU编号：{}, 来源单据：{}, 业务类型：【{}】，状态【{}】，单据日期：【{}】,库存表id：【{}】，不存在库存历史数据，新增数据", warehouseId, orgId, param.getWarehouseLocation(),param.getSkuId(), param.getSkuNo(), sourceTypeEnum.getName(), businessType.getName(), inventoryStatusEnum.getName(), billDate, inventory.getId());
                inventoryHis = new InventoryHisEntity();
                inventoryHis.setInfoId(inventory.getId());
                inventoryHis.setBillDate(billDate);
                inventoryHis.setQty(afterInventoryQty);
                inventoryHis.setVersion(1);
                inventoryHisService.save(inventoryHis);
            } else {
                updateCnt = inventoryHisService.updateQtyById(inventoryHis.getId(), afterInventoryQty, inventoryHis.getVersion());
                if(updateCnt != 1) {
                    throw new ServiceException(ApiError.ERROR_1027);
                }
            }
        }  catch (Exception e) {
            log.info("交易业务：{}，来源单据：{}，单据id：【{}】，SKU编号：【{}】，库存操作异常", businessType.getName(), sourceTypeEnum.getName(), param.getSourceId(), param.getSkuNo(),e );
            if(e instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) e;
                throw serviceException;
            } else {
                throw e;
            }
        } finally {
            //释放锁
            if(lock.isLocked() && lock.isHeldByCurrentThread()){ // 锁是否存在，是当前执行线程的锁
                lock.unlock(); // 释放锁
            }
        }
    }


    /**
     * 记录库存交易流水
     */
    public void recordFlowTransaction(TransactionFlowDTO param, InventoryBusinessTypeEnum businessType,
                                      String transactionRuleId,Integer afterInventoryQty, InventoryModeEnum inventoryModeEnum, Map<String, WarehouseEntity> warehouseMap) {
        // 记录交易流水
        TransactionFlowEntity transactionFlowEntity = new TransactionFlowEntity();
        transactionFlowEntity.setBillDate(param.getBillDate());
        transactionFlowEntity.setInventoryId(param.getInventoryId());
        transactionFlowEntity.setInventoryDetailId(param.getInventoryDetailId());
        transactionFlowEntity.setOrgId(param.getOrgId());
        // 获取仓库名称
        WarehouseEntity warehouseEntity = warehouseMap.computeIfAbsent(param.getWarehouseId(),(v)->warehouseService.getById(v));
        ValidatorUtil.isTrue(Objects.nonNull(warehouseEntity),()->new ServiceException(ApiError.ERROR_99002));
        transactionFlowEntity.setWarehouseId(param.getWarehouseId());
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
        transactionFlowEntity.setVersion(1);
        transactionFlowEntity.setTransactionNo(param.getTransactionNo());
        transactionFlowService.save(transactionFlowEntity);
    }



}