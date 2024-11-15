package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.common.business.config.DocNoGenHelper;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.VirtualTransFlowDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.dto.inventory.VirtualTransRuleDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.service.*;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 虚拟库存处理
 * @author will
 * @date 2024/6/4 10:48
 */
@Slf4j
public abstract class AbstractVirtualInventoryServiceImpl implements VirtualInventoryStockService {
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private VirtualWarehouseService virtualWarehouseService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private CfgVirtualTransRulesService cfgVirtualTransRulesService;

    @Resource
    private VirtualInventoryService virtualInventoryService;

    @Resource
    private VirtualTransFlowService virtualTransFlowService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    /**
     * 允许录入负数的库存业务单据（临时打开）
     */
    protected final List<InventorySourceTypeEnum> allowNegativeQtyBusinessList = Lists.newArrayList(InventorySourceTypeEnum.INIT_STOCK);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void approve(List<T> paramList, List<VirtualTransRuleDTO.StockParamDTO> ruleList, VirtualInventoryBusinessTypeEnum businessType, Boolean byType) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.warn("》》》库存交易按【{}】，入参：{}，业务类型：{}", Objects.equals(byType, Boolean.TRUE) ? "业务类型" : "自定义规则", JSON.toJSONString(paramList), businessType.getName());

        List<RLock> rLockList = handleLockKey(paramList);
        RedissonMultiLock multiLock = null;
        boolean isLock = false;
        try {
            RLock[] arrayLock = rLockList.stream().toArray(RLock[]::new);
            //将多个RLock整合为一个大锁对象
            multiLock = new RedissonMultiLock(arrayLock);
            // 设置最大等待锁时间
            isLock = multiLock.tryLock(60, TimeUnit.SECONDS);
            if (!isLock) {
                log.error("加锁失败,rLockList = {}", rLockList);
                throw new ServiceException(ApiError.ERROR_1026);
            }
            // 1.验证参数
            List<VirtualTransRuleDTO.StockParamDTO> stockParamList = ruleList;
            //如果走配置则取已配置的规则
            if(Objects.equals(byType,Boolean.TRUE)) {
                stockParamList = this.wrapTransactionRule(businessType);
            }
            this.checkParam(paramList, businessType, stockParamList);
            // 2.业务处理，同一个操作产生的交易流水使用同一个关联交易号
            String transactionNo =  docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XLS);
            this.stockHandler(paramList, businessType, stockParamList, transactionNo);
        } catch (Exception e) {
            log.error("交易业务：{}，数据：{}，库存操作异常：{}", businessType.getName(), paramList,e );
            throw new ServiceException(10000,e.getMessage());
        } finally {
            //释放锁  锁是否存在
            if(isLock && multiLock != null){
                // 释放锁
                multiLock.unlock();
            }
        }
        stopwatch.stop();
        log.warn("结束库存交易，耗时【{}】秒", stopwatch.elapsed(TimeUnit.SECONDS));
    }

    abstract <T extends VirtualInventoryStockDTO.StockBaseDTO> List<RLock> handleLockKey(List<T> paramList);

    /**
     * 验证参数（子类实现）
     * @param paramList             业务参数列表
     * @param businessType          业务类型
     * @param ruleList              交易规则
     * @param <T>                   业务参数 对象类型
     */
    abstract <T extends VirtualInventoryStockDTO.StockBaseDTO> void checkParam(List<T> paramList, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> ruleList);

    /**
     * 循环处理业务（子类实现）
     * @param paramList                 业务参数列表
     * @param businessType              业务类型
     * @param transactionRuleParams     交易规则
     */
    abstract <T extends VirtualInventoryStockDTO.StockBaseDTO> void stockHandler(List<T> paramList, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> transactionRuleParams, String transactionNo);

    /**
     * 单个sku处理（子类实现）
     * @param baseParam                 业务参数列表
     * @param businessType              业务类型
     * @param transactionRuleParams     交易规则
     * @param transactionNo             交易编号
     */
    abstract <T extends VirtualInventoryStockDTO.StockBaseDTO> void singleHandler(T baseParam, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> transactionRuleParams,
                                                                                  String transactionNo);

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SneakyThrows
    public  void unApprove(InventoryUnApproveDTO dto) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.warn("库存交易反审核，单据类型：{}, 单据id：{}", dto.getSourceType().getName(), dto.getBillId());

        // 根据单据类型和单据id查询出未反审核过的对应的交易流水，一个单据对应多个SKU， 按创建时间正序排序
        List<VirtualTransFlowEntity> virtualTransFlowList = virtualTransFlowService.getUnApprovedTxnFlows(dto.getSourceType().getCode(), dto.getBillId());
        // 没有流水 则不做反向操作：兼容产品属性为费用或服务的sku没有交易流水的情况
        if(CollUtil.isEmpty(virtualTransFlowList)) {
            log.warn("库存交易反审核，单据类型：{}, 单据id：{}，未找到未审核过的交易流水，不处理", dto.getSourceType().getName(), dto.getBillId());
            return;
        }

        //虚拟仓库信息
        List<String> virtualWarehouseIdList = virtualTransFlowList.stream().map(VirtualTransFlowEntity::getVirtualWarehouseId).collect(Collectors.toList());
        List<VirtualWarehouseEntity> virtualWarehouseList = virtualWarehouseService.listByIds(virtualWarehouseIdList);

        //实际仓库信息
        List<String> warehouseIdList = virtualTransFlowList.stream().map(VirtualTransFlowEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);

        // 业务处理，同一个操作产生的交易流水使用同一个关联交易号
        String transactionNo =  docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XLS);
        // 通过对sku id顺序执行, 避免多线程死锁
        Comparator<VirtualTransFlowEntity> comparing = Comparator.comparing(VirtualTransFlowEntity::getSkuId)
                .thenComparing(VirtualTransFlowEntity::getWarehouseId)
                .thenComparing(VirtualTransFlowEntity::getVirtualWarehouseId)
                .thenComparing(x -> CharSequenceUtil.isNotEmpty(x.getDictInventoryStatus()) ? x.getDictInventoryStatus() : "");
        virtualTransFlowList = virtualTransFlowList.stream().sorted(comparing).collect(Collectors.toList());
        virtualTransFlowList.forEach(txnFlow->{
            // 2，整理 交易流水信息，数量取反，操作模式 = unApprove
            txnFlow.setTransactionNo(transactionNo);
            txnFlow.setQty(txnFlow.getQty()*-1);
            txnFlow.setOperationMode(InventoryOperationModeEnum.UN_APPROVE.getCode());

            // 3，获取可用的库存, 数量不够时报错
            VirtualInventoryEntity virtualInventoryEntity = getAvaliableInventory(virtualWarehouseList,warehouseList,txnFlow);

            // 4，记录交易明细
            txnFlow.setIsUnapproved(Boolean.TRUE);

            // 5，更新库存
            boolean updateFlag =  virtualInventoryService.updateQtyById(virtualInventoryEntity.getId(), txnFlow.getQty());
            if(!updateFlag) {
                throw new ServiceException(ApiError.ERROR_1027);
            }
            //添加流水
            VirtualInventoryEntity entity = virtualInventoryService.getById(virtualInventoryEntity.getId());
            virtualTransFlowService.add(txnFlow, entity.getQty());

            // 6,更新原交易流水为已反审核
            virtualTransFlowService.updateUnapprovedById(txnFlow.getId(), txnFlow.getVersion());
            log.warn("结束库存交易，耗时【{}】秒", stopwatch.elapsed(TimeUnit.SECONDS));
        });
    }

    /**
     * 获取可用库存，用于出库场景
     * @param virtualTransFlowEntity    交易流水
     * @return                   返回可用库存
     */
    private VirtualInventoryEntity getAvaliableInventory(List<VirtualWarehouseEntity> virtualWarehouseList,List<WarehouseEntity> warehouseList,
                                                  VirtualTransFlowEntity virtualTransFlowEntity) {
        //虚拟仓库信息
        VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), virtualTransFlowEntity.getVirtualWarehouseId()))
                .findFirst().orElse(null);
        if (ObjectUtil.isEmpty(virtualWarehouseEntity)) {
            throw new ServiceException(ApiError.ERROR_VIRTUAL_WAREHOUSE_NOT_EXIST);
        }
        //实物仓库
        WarehouseEntity warehouseEntity = warehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), virtualTransFlowEntity.getWarehouseId()))
                .findFirst().orElse(null);
        if (ObjectUtil.isEmpty(warehouseEntity)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }

        VirtualInventoryEntity virtualInventoryEntity = virtualInventoryService.getById(virtualTransFlowEntity.getVirtualInventoryId());
        String inventoryStatusName = InventoryStatusEnum.getNameByCode(virtualTransFlowEntity.getDictInventoryStatus());
        //判断反审核回退库存是否足够
        if(ObjectUtil.isEmpty(virtualInventoryEntity) || (virtualInventoryEntity.getQty() + virtualTransFlowEntity.getQty() < 0 )) {
            String errMsg = CharSequenceUtil.format(ApiError.ERROR_VIRTUAL_INVENTORY_INSUFFICIENT.msg, virtualTransFlowEntity.getSkuNo(), virtualWarehouseEntity.getName(),warehouseEntity.getName(),inventoryStatusName,virtualInventoryEntity == null ? "无":virtualInventoryEntity.getQty(),virtualTransFlowEntity.getQty());
            log.error(errMsg);
            throw new ServiceException(ApiError.ERROR_VIRTUAL_INVENTORY_INSUFFICIENT.code, errMsg);
        }
        return virtualInventoryEntity;
    }

    /**
     * 入库核心业务逻辑处理
     */
    @SneakyThrows
    public  void inStockCore(VirtualInventoryStockDTO.StockCoreDTO param, VirtualInventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String transRuleId, String transactionNo) {
        // 实物仓库信息
        WarehouseDTO.UpdateDTO warehouseInfo = warehouseService.detailWithCache(param.getWarehouseId());
        if(Objects.isNull(warehouseInfo) || CharSequenceUtil.isEmpty(warehouseInfo.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        log.warn("交易业务：【{}】，来源单据：【{}】，单据id：【{}】，SKU编号：【{}】，库存状态：【{}】，开始走入库逻辑", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getSkuNo(), inventoryStatusEnum.getName());
        VirtualInventoryEntity virtualInventoryEntity = virtualInventoryService.addOrUpdate(param.getVirtualWarehouseId(),param.getWarehouseId(),param.getSkuId(), param.getSkuNo(), inventoryStatusEnum.getCode(),param.getQty());

        // 登记交易流水
        VirtualTransFlowDTO.AddDTO transactionFlowDTO = wrapTransactionFlow(param, virtualInventoryEntity, businessType, inventoryStatusEnum, param.getQty(), warehouseInfo.getOrgId());
        transactionFlowDTO.setTransactionNo(transactionNo);
        transactionFlowDTO.setVirtualTransRuleId(transRuleId);
        virtualTransFlowService.add(transactionFlowDTO, transRuleId, InventoryModeEnum.IN_STOCK);
    }

    /**
     * 出库核心业务处理
     */
    @SneakyThrows
    public  void outStockCore (VirtualInventoryStockDTO.StockCoreDTO param, VirtualInventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String transRuleId,
                               String transactionNo) {
        //虚拟仓库信息
        VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseService.getById(param.getVirtualWarehouseId());
        if (ObjectUtil.isEmpty(virtualWarehouseEntity)) {
            throw new ServiceException(ApiError.ERROR_VIRTUAL_WAREHOUSE_NOT_EXIST);
        }
        //实体仓库信息
        WarehouseDTO.UpdateDTO warehouseInfo = warehouseService.detailWithCache(param.getWarehouseId());
        if(Objects.isNull(warehouseInfo) || CharSequenceUtil.isEmpty(warehouseInfo.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        // 待出库数量
        Integer waitOutQty = param.getQty();
        log.info("交易业务：【{}】，来源单据：{}，单据id：【{}】，SKU编号：【{}】，库存状态：【{}】，开始走出库逻辑", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), inventoryStatusEnum.getName(), param.getSkuNo());

        VirtualInventoryEntity found = virtualInventoryService.findVirtualInventoryStock(param.getVirtualWarehouseId(),param.getWarehouseId(),param.getSkuId(), inventoryStatusEnum.getCode());

        String inventoryStatusName = Optional.of(inventoryStatusEnum).map(InventoryStatusEnum::getName).orElse("");
        // 仓库负库存是否允许
        if(ObjectUtil.isNotEmpty(found) &&  found.getQty() < waitOutQty ) {
            String errMsg = CharSequenceUtil.format(ApiError.ERROR_VIRTUAL_INVENTORY_INSUFFICIENT.msg, param.getSkuNo(),virtualWarehouseEntity.getName(), warehouseInfo.getName(), inventoryStatusName,found.getQty(),param.getQty());
            log.error(errMsg);
            throw new ServiceException(ApiError.ERROR_VIRTUAL_INVENTORY_INSUFFICIENT.code, errMsg);
        }

        // 更新库存表
        VirtualInventoryEntity virtualInventoryEntity = virtualInventoryService.addOrUpdate(param.getVirtualWarehouseId(), param.getWarehouseId(), param.getSkuId(), param.getSkuNo(), inventoryStatusEnum.getCode(), param.getQty() * MathUtil.ONE_NEGATIVE);

        // 登记交易流水（有可能一个操作产生多条，从多个库存明细中扣除）
        VirtualTransFlowDTO.AddDTO transactionFlowDTO = wrapTransactionFlow(param, virtualInventoryEntity, businessType, inventoryStatusEnum, param.getQty(), warehouseInfo.getOrgId());
        transactionFlowDTO.setTransactionNo(transactionNo);
        virtualTransFlowService.add(transactionFlowDTO, transRuleId, InventoryModeEnum.OUT_STOCK);

        // 此处再次验证，防止变成负库存
        VirtualInventoryEntity curInventory = virtualInventoryService.getById(virtualInventoryEntity.getId());
        // 仓库库存判断是否小于0
        if(curInventory.getQty() < 0 ) {
            log.warn("库存id:{}出库后的库存数量变为:{}，不允许出库", virtualInventoryEntity.getId(), curInventory.getQty());
            throw new ServiceException(ApiError.ERROR_VIRTUAL_INVENTORY_INSUFFICIENT.code, CharSequenceUtil.format(ApiError.ERROR_VIRTUAL_INVENTORY_INSUFFICIENT.msg, param.getSkuNo(),virtualWarehouseEntity.getName(), warehouseInfo.getName(), inventoryStatusName,curInventory.getQty(),param.getQty()));
        }
    }

    /**
     * 获取忽略库存计算的sku
     * @return  返回忽略的SKU ID列表
     */
    protected List<String> getIgnoreSkuIds() {
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if(CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }
        return ignoreInventorySkuIds;
    }

    /**
     * 出库检查库存是否足够（走交易规则，不能手工传输库存状态）
     */
    protected void checkStockByRule(VirtualInventoryStockDTO.InventoryDTO param, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> ruleList) {
        InventorySourceTypeEnum sourceTypeEnum = param.getSourceType();
        String sourceId = param.getSourceId();
        LocalDate billDate = param.getBillDate();
        log.info("库存状态从配置中取，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】，SKU编号：【{}】", businessType.getName(), sourceTypeEnum.getName(), sourceId, billDate, param.getSkuNo());
        if(CollUtil.isEmpty(ruleList)) {
            throw new ServiceException(ApiError.ERROR_99034.code, CharSequenceUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
        }
        //对需要出库的仓库库存进行校验
        List<VirtualTransRuleDTO.StockParamDTO> outTransactionRules;
        if(Objects.nonNull(param.getWarehouseOption())) {
            // 调拨类业务，包含当前仓和目的仓
            outTransactionRules = ruleList.stream().filter(r->Objects.equals(r.getTransactionMode(), InventoryModeEnum.OUT_STOCK)
                    && Objects.equals(r.getWarehouseOption().getCode(), param.getWarehouseOption().getCode())).collect(Collectors.toList());
        } else {
            // 直接过滤得到出库类型的数据
            outTransactionRules = ruleList.stream().filter(r->Objects.equals(r.getTransactionMode(), InventoryModeEnum.OUT_STOCK)).collect(Collectors.toList());
        }
        if (CollUtil.isEmpty(outTransactionRules)) {
            return;
        }
        for (VirtualTransRuleDTO.StockParamDTO rule : outTransactionRules) {
            InventoryStatusEnum ruleInventoryStatusEnum = rule.getInventoryStatus();
            ValidatorUtil.isTrue(Objects.nonNull(ruleInventoryStatusEnum),()->new ServiceException(ApiError.ERROR_99036));
            this.checkStockQtyByWareLocalSkuStatus( param, ruleInventoryStatusEnum);
        }
    }

    /**
     * 检查库存是否足够
     * @param param             业务参数
     * @param status            库存状态
     */
    protected void checkStockQtyByWareLocalSkuStatus(VirtualInventoryStockDTO.InventoryDTO param, InventoryStatusEnum status) {
        // 虚拟仓库信息
        VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseService.getById(param.getVirtualWarehouseId());
        if(Objects.isNull(virtualWarehouseEntity) || CharSequenceUtil.isEmpty(virtualWarehouseEntity.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        // 仓库信息
        WarehouseDTO.UpdateDTO warehouseInfo = warehouseService.detailWithCache(param.getWarehouseId());
        if(Objects.isNull(warehouseInfo) || CharSequenceUtil.isEmpty(warehouseInfo.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        //查询是否存在库存数据
        VirtualInventoryEntity inventory = virtualInventoryService.findVirtualInventoryStock(param.getVirtualWarehouseId(),param.getWarehouseId(),param.getSkuId(),status.getCode());
        String inventoryStatusName = status.getName();
        if(ObjectUtil.isEmpty(inventory) || inventory.getQty() < param.getQty()) {
            throw new ServiceException(ApiError.ERROR_VIRTUAL_INVENTORY_INSUFFICIENT.code, CharSequenceUtil.format(ApiError.ERROR_VIRTUAL_INVENTORY_INSUFFICIENT.msg, param.getSkuNo(), virtualWarehouseEntity.getName(), warehouseInfo.getName(), inventoryStatusName,ObjectUtil.isEmpty(inventory) ? MathUtil.ZERO : inventory.getQty(),param.getQty()));
        }
    }

    /**
     * 查询配置的交易规则
     * @author will
     * @date 2024/6/4 11:24
     * @param businessType  业务类型
     * @return List<StockParamDTO> 交易规则
     */
    protected List<VirtualTransRuleDTO.StockParamDTO> wrapTransactionRule(VirtualInventoryBusinessTypeEnum businessType) {
        // 查询配置的交易规则
        List<CfgVirtualTransRulesEntity> list = cfgVirtualTransRulesService.findByDictBizType(businessType.getCode());
        if(CollUtil.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        List<VirtualTransRuleDTO.StockParamDTO> ruleList = Lists.newArrayListWithExpectedSize(list.size());
        list.forEach(r->{
            VirtualTransRuleDTO.StockParamDTO stockParamDTO = new VirtualTransRuleDTO.StockParamDTO();
            stockParamDTO.setId(r.getId());
            stockParamDTO.setDictBizType(VirtualInventoryBusinessTypeEnum.getByCode(r.getDictBizType()));
            stockParamDTO.setWarehouseOption(InventoryWarehouseOptionEnum.getByCode(r.getWarehouseOption()));
            stockParamDTO.setInventoryStatus(InventoryStatusEnum.getByCode(r.getInventoryStatus()));
            stockParamDTO.setTransactionMode(InventoryModeEnum.getByCode(r.getTransactionMode()));
            ruleList.add(stockParamDTO);
        });
        return ruleList;
    }

    /**
     * 格式化流水数据
     * @author will
     * @date 2024/6/4 18:31
     * @param param
     * @param virtualInventoryEntity
     * @param businessType
     * @param inventoryStatusEnum
     * @param qty
     * @param orgId
     * @return AddDTO
     */
    protected VirtualTransFlowDTO.AddDTO wrapTransactionFlow(VirtualInventoryStockDTO.StockCoreDTO param, VirtualInventoryEntity virtualInventoryEntity, VirtualInventoryBusinessTypeEnum businessType,
                                                                    InventoryStatusEnum inventoryStatusEnum, Integer qty, String orgId) {
        VirtualTransFlowDTO.AddDTO virtualTransFlowDTO = new VirtualTransFlowDTO.AddDTO();
        // 复制对象性能慢，改为手工赋值
        virtualTransFlowDTO.setOrgId(orgId);
        virtualTransFlowDTO.setVirtualWarehouseId(param.getVirtualWarehouseId());
        virtualTransFlowDTO.setWarehouseId(param.getWarehouseId());
        virtualTransFlowDTO.setSkuId(param.getSkuId());
        virtualTransFlowDTO.setSkuNo(param.getSkuNo());
        virtualTransFlowDTO.setSourceId(param.getSourceId());
        virtualTransFlowDTO.setSourceCode(param.getSourceCode());
        virtualTransFlowDTO.setSourceDetailId(param.getSourceDetailId());
        virtualTransFlowDTO.setVirtualInventoryId(virtualInventoryEntity.getId());
        virtualTransFlowDTO.setDictInventoryStatus(inventoryStatusEnum.getCode());
        virtualTransFlowDTO.setDictBizType(businessType.getCode());
        virtualTransFlowDTO.setBillDate(param.getBillDate());
        virtualTransFlowDTO.setSourceType(param.getSourceType().getCode());
        virtualTransFlowDTO.setQty(qty);
        virtualTransFlowDTO.setCurInventoryQty(virtualInventoryEntity.getAfterQty());
        virtualTransFlowDTO.setOperationMode(Objects.nonNull(param.getOperationMode()) ? param.getOperationMode().getCode() : "");
        return virtualTransFlowDTO;
    }

}