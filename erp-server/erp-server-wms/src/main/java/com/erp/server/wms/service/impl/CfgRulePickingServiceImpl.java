package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.pickingstrategy.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.InWarehouseLocationEnum;
import com.erp.model.wms.enums.RuleTypeEnum;
import com.erp.model.wms.enums.WarehouseAreaTypeEnum;
import com.erp.rpc.plm.feign.ProductDetailFeign;
import com.erp.server.wms.convert.CfgRuleConverter;
import com.erp.server.wms.mapper.CfgRulePickingMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 仓位推荐表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
@Service
@Slf4j
public class CfgRulePickingServiceImpl extends SuperServiceImpl<CfgRulePickingMapper, CfgRulePickingEntity> implements CfgRulePickingService {

    @Resource
    private CfgRuleConditionService cfgRuleConditionService;
    @Resource
    private CfgRulePackingActionService cfgRulePackingActionService;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private SpElServer spElServer;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private CfgConditionService cfgConditionService;
    @Resource
    private PickingListsService pickingListsService;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private ProductDetailFeign productDetailFeign;
    @Resource
    private TransactionFlowService transactionFlowService;

    @Override
    public PagingVO<CfgRulePickingDTO.PagingView> paging(PagingDTO<CfgRulePickingDTO.PagingParam> dto) {
        IPage<CfgRulePickingDTO.PagingView> page = baseMapper.paging(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        return new PagingVO<>(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(CfgRulePickingDTO.Add dto) {
        CfgRulePickingEntity entity = BeanMapperUtils.map(CfgRulePickingEntity.class, dto);
        save(entity);
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "拣货策略规则", entity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PICKING_STRATEGY.getCode(), entity.getId(), "新增操作");
        //默认使用拣货类型作为规则条件
        cfgRuleConditionService.saveRuleCondition(entity.getId(), dto.getConditionList(), RuleTypeEnum.PICKING_STRATEGY.getCode());
        //保存对应的规则动作
        cfgRulePackingActionService.saveRuleAction(entity.getId(), dto.getPickActions(), RuleTypeEnum.PICKING_STRATEGY.getCode());
        cfgRulePackingActionService.saveRuleAction(entity.getId(), dto.getReplenishActions(), RuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode());
        cfgRulePackingActionService.saveRuleAction(entity.getId(), dto.getOutStockActions(), RuleTypeEnum.WAREHOUSE_LOCATION_OUT_STOCK.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("all")
    public void update(CfgRulePickingDTO.Update dto) {
        CfgRulePickingEntity old = getById(dto.getId());
        CfgRulePickingEntity entity = BeanMapperUtils.map(CfgRulePickingEntity.class, dto);
        updateById(entity);
        String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dto.getId(), "拣货策略规则");
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.PICKING_STRATEGY.getCode(), dto.getId(), msg);
        //保存条件
        cfgRuleConditionService.updateRuleCondition(dto.getId(), dto.getConditionList(), ModuleTypeEnum.PICKING_STRATEGY.getCode(), RuleTypeEnum.PICKING_STRATEGY.getCode());
        //更新动作
        cfgRulePackingActionService.updateRuleAction(dto.getId(), dto.getPickActions(), RuleTypeEnum.PICKING_STRATEGY.getCode());
        cfgRulePackingActionService.updateRuleAction(dto.getId(), dto.getReplenishActions(), RuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode());
        cfgRulePackingActionService.updateRuleAction(dto.getId(), dto.getOutStockActions(), RuleTypeEnum.WAREHOUSE_LOCATION_OUT_STOCK.getCode());
    }

    @Override
    public CfgRulePickingDTO.View view(String id) {
        CfgRulePickingEntity entity = getById(id);
        CfgRulePickingDTO.View view = BeanMapperUtils.map(CfgRulePickingDTO.View.class, entity);
        if (CharSequenceUtil.isNotBlank(view.getInWarehouseLocation())){
            view.setInWarehouseLocationName(InWarehouseLocationEnum.getName(view.getInWarehouseLocation()));
        }
        //查询规则条件
        List<CfgRuleConditionEntity> ruleConditionEntities = cfgRuleConditionService.list(Wrappers.<CfgRuleConditionEntity>lambdaQuery()
                .eq(CfgRuleConditionEntity::getRuleId, id)
                .orderByAsc(CfgRuleConditionEntity::getIndex));
        List<CfgRuleConditionDTO.View> ruleConditions = BeanMapperUtils.copyList(CfgRuleConditionDTO.View.class, ruleConditionEntities);
        view.setConditionList(ruleConditions);
        // 查询规则动作
        List<CfgRulePackingActionEntity> actionEntities = cfgRulePackingActionService.list(Wrappers.<CfgRulePackingActionEntity>lambdaQuery()
                .eq(CfgRulePackingActionEntity::getRuleId, id)
                .orderByAsc(CfgRulePackingActionEntity::getIndex));
        Map<String, List<CfgRulePackingActionEntity>> actionMap = actionEntities.stream().collect(Collectors.groupingBy(CfgRulePackingActionEntity::getRuleType));
        List<CfgRulePackingActionEntity> pickingActionEntityList = actionMap.get(RuleTypeEnum.PICKING_STRATEGY.getCode());
        if (CollUtil.isNotEmpty(pickingActionEntityList)) {
            List<CfgRuleActionDTO.View> packingActions = CfgRuleConverter.INSTANCE.entityToView(pickingActionEntityList);
            view.setPickActions(packingActions);
        }
        List<CfgRulePackingActionEntity> replenishActionEntityList = actionMap.get(RuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode());
        if (CollUtil.isNotEmpty(replenishActionEntityList)) {
            List<CfgRuleActionDTO.View> packingActions = CfgRuleConverter.INSTANCE.entityToView(replenishActionEntityList);
            view.setReplenishActions(packingActions);
        }
        List<CfgRulePackingActionEntity> outStockActionEntityList = actionMap.get(RuleTypeEnum.WAREHOUSE_LOCATION_OUT_STOCK.getCode());
        if (CollUtil.isNotEmpty(outStockActionEntityList)) {
            List<CfgRuleActionDTO.View> packingActions = CfgRuleConverter.INSTANCE.entityToView(outStockActionEntityList);
            view.setOutStockActions(packingActions);
        }
        return view;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> delete(List<String> ids) {
        List<CfgRulePickingEntity> list = this.listByIds(ids);
        //删除仓位推荐
        removeByIds(ids);
        //删除规则
        cfgRuleConditionService.removeByRuleIds(ids);
        //删除拣货动作
        cfgRulePackingActionService.removeByRuleIds(ids);
        List<BatchResultDTO> resultDTOList=new ArrayList<>();
        for (CfgRulePickingEntity entity : list) {
            resultDTOList.add(BatchResultDTO.success(entity.getId(), entity.getName(),"删除成功"));
        }
        return resultDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(UpdateStateDTO.BatchUpdateDTO dto) {
        List<CfgRulePickingEntity> list = listByIds(dto.getIds());
        List<Pair<String, String>> pairs = list.stream().map(e -> Pair.create(e.getId(), Boolean.TRUE.equals(e.getDisabled()) ? "停用" : "启用")).collect(Collectors.toList());
        LoginUser user = UserContext.getDefaultLoginUser();
        update(Wrappers.<CfgRulePickingEntity>lambdaUpdate()
                .set(CfgRulePickingEntity::getDisabled, dto.getDisabled())
                .set(CfgRulePickingEntity::getUpdateTime, LocalDateTime.now())
                .set(CfgRulePickingEntity::getUpdateUserId, user.getUid())
                .set(CfgRulePickingEntity::getUpdateUserName, user.getUserName())
                .in(CfgRulePickingEntity::getId, dto.getIds()));
        String content = "启用状态由[%s]变更为" + (Boolean.TRUE.equals(dto.getDisabled()) ? "停用" : "启用");
        operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.PICKING_STRATEGY.getCode(), pairs, "状态变更");
    }

    @Override
    public Pair<List<LocationInventoryResultDTO>, Map<String, Integer>> getSoB2CRuleOrderMatchResult(CfgRulePickingDTO.CfgExecutionDataDTO executionData) {
        // 暂时只计算数量优先
        Pair<List<CfgRulePickingDTO.CfgRulePickingInventoryDTO>, List<WarehouseLocationEntity>> listListPair = this.matchRuleActionList(executionData, "gt", RuleTypeEnum.PICKING_STRATEGY.getCode());
        //
        Pair<List<LocationInventoryResultDTO>, Map<String, Integer>> result = this.getSoB2CRuleOrderMatchResult(executionData, listListPair);
        return result;
    }

    /**
     * 按明细需求，在这些候选上做数量优先占用，输出「拣货结果 + 缺货清单」。
     * @param executionData
     * @param listListPair
     * @return
     */
    @Override
    public Pair<List<LocationInventoryResultDTO>, Map<String, Integer>> getSoB2CRuleOrderMatchResult(CfgRulePickingDTO.CfgExecutionDataDTO executionData,Pair<List<CfgRulePickingDTO.CfgRulePickingInventoryDTO>, List<WarehouseLocationEntity>> listListPair) {
        List<LocationInventoryResultDTO> result = new ArrayList<>();
        Map<String, Integer> stockSku = new HashMap<>();
        List<CfgRulePickingDTO.CfgRulePickingInventoryDTO> cfgRulePickingInventoryDTOS = listListPair.getFirst();
        List<WarehouseLocationEntity> locationList = listListPair.getSecond();
        for (CfgRulePickingDTO.CfgExecutionDataDetailDTO detail : executionData.getDetails()) {
            AtomicInteger quantity = new AtomicInteger(detail.getQty());
            List<CfgRulePickingDTO.CfgRulePickingInventoryDTO> inventoryByWarehouse = cfgRulePickingInventoryDTOS.stream()
                    .filter(v -> v.getWarehouseId().equals(detail.getWarehouseId()))
                    .filter(v -> v.getSkuId().equals(detail.getSkuId()))
                    .filter(v -> v.getQty() > 0)
                    .collect(Collectors.toList());
            for (CfgRulePickingDTO.CfgRulePickingInventoryDTO inventory : inventoryByWarehouse) {
                log.warn("单据【{}】执行拣货策略，规则{},sku{},仓位{},数量{}", executionData.getSourceCode(), inventory.getRuleId(), inventory.getSkuNo(), inventory.getWarehouseLocation(), inventory.getQty());
                LocationInventoryResultDTO inventoryResultDTO = new LocationInventoryResultDTO();
                inventoryResultDTO.setSkuId(detail.getSkuId());
                inventoryResultDTO.setSkuNo(detail.getSkuNo());
                inventoryResultDTO.setPlatformSkuNo(detail.getPlatformSkuNo());
                WarehouseLocationEntity entity = locationList.stream().filter(location -> location.getCode().equals(inventory.getWarehouseLocation()))
                        .findFirst().orElse(new WarehouseLocationEntity());
                inventoryResultDTO.setWarehouseId(inventory.getWarehouseId());
                inventoryResultDTO.setWarehouseAreaId(inventory.getWarehouseAreaId());
                inventoryResultDTO.setWarehouseLocationId(entity.getId());
                inventoryResultDTO.setWarehouseLocation(inventory.getWarehouseLocation());
                inventoryResultDTO.setSourceDetailId(detail.getSourceDetailId());
                if (inventory.getQty() >= quantity.get()) {
                    inventoryResultDTO.setQuantity(quantity.get());
                    result.add(inventoryResultDTO);
                    inventory.setQty(inventory.getQty() - quantity.get());
                    quantity.set(0);
                    break;
                } else {
                    inventoryResultDTO.setQuantity(inventory.getQty());
                    quantity.set(quantity.get() - inventory.getQty());
                    inventory.setQty(0);
                    result.add(inventoryResultDTO);
                }
            }
            if (0 != quantity.get()) {
                if (stockSku.containsKey(detail.getSkuNo())) {
                    stockSku.put(detail.getSkuNo(), stockSku.get(detail.getSkuNo()) + quantity.get());
                }else {
                    stockSku.put(detail.getSkuNo(), quantity.get());
                }
                result = result.stream().filter(v -> !v.getSkuNo().equals(detail.getSkuNo())).collect(Collectors.toList());
            }
        }
        log.warn("单据【{}】完成执行拣货策略，完成时间为{}", executionData.getSourceCode(), System.currentTimeMillis());
        return Pair.create(result, stockSku);
    }

    /**
     * 拣货明细转换为规则执行数据明细
     */
    @Override
    public CfgRulePickingDTO.CfgExecutionDataDTO getPickingRuleExecutionData(PickingListsDTO.AddDTO dto){
        pickingListsService.generatePicking(dto);
        List<CfgRulePickingDTO.CfgExecutionDataDetailDTO> details = dto.getDetails().stream()
                .map(v -> new CfgRulePickingDTO.CfgExecutionDataDetailDTO(v.getWarehouseId(), v.getSkuId(), v.getSkuNo(),v.getPlatformSkuNo(), v.getQty(), v.getSourceDetailId())).collect(Collectors.toList());
        CfgRulePickingDTO.CfgExecutionDataDTO executionData = new CfgRulePickingDTO.CfgExecutionDataDTO();
        executionData.setBillType(dto.getBillType());
        executionData.setCustomerId(dto.getCustomerId());
        executionData.setDeliveryWarehouseId(dto.getDeliveryWarehouseId());
        executionData.setSourceCode(dto.getSourceCode());
        executionData.setCountryCode(dto.getCountryCode());
        executionData.setDetails(details);
        return executionData;
    }

    /**
     * 按单据条件命中规则，查出「能从哪些仓位操作、各有多少可用库存」。
     * <p>
     * 无命中规则时抛业务异常；有规则但无动作仓库时返回空候选。
     */
    @Override
    public Pair<List<CfgRulePickingDTO.CfgRulePickingInventoryDTO>, List<WarehouseLocationEntity>> matchRuleActionList(CfgRulePickingDTO.CfgExecutionDataDTO executionData,String determiningCondition, String ruleType){
        log.warn("单据【{}】开始执行拣货策略，开始时间为{}", executionData.getSourceCode(), System.currentTimeMillis());
        List<CfgRulePickingEntity> rules = this.listMatchedRules(executionData, ruleType);
        if (CollectionUtils.isEmpty(rules)) {
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "仓位推荐");
        }
        log.warn("单据【{}】完成过滤拣货策略，完成时间为{}", executionData.getSourceCode(), System.currentTimeMillis());
        List<String> ruleIds = rules.stream().map(CfgRulePickingEntity::getId).collect(Collectors.toList());
        List<CfgRulePackingActionEntity> actions = cfgRulePackingActionService.listByRuleIds(ruleIds, ruleType);
        List<String> warehouseIds = actions.stream().map(CfgRulePackingActionEntity::getWarehouseId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(warehouseIds)) {
            return Pair.create(Collections.emptyList(), Collections.emptyList());
        }
        List<WarehouseLocationEntity> locationList = warehouseLocationService.listByWarehouseIds(warehouseIds);
        List<String> skuIds = executionData.getDetails().stream().map(CfgRulePickingDTO.CfgExecutionDataDetailDTO::getSkuId).distinct().collect(Collectors.toList());
        List<CfgRulePickingDTO.CfgRulePickingInventoryDTO> cfgRulePickingInventoryDTOS = cfgRulePackingActionService.listLocationByRule(rules, warehouseIds, skuIds, determiningCondition, ruleType);
        return Pair.create(cfgRulePickingInventoryDTOS, locationList);
    }

    /**
     * 仅做规则命中匹配（不含库存），供拣货分配、缺货补货仓位推荐等复用。
     */
    @Override
    public List<CfgRulePickingEntity> listMatchedRules(CfgRulePickingDTO.CfgExecutionDataDTO executionData, String ruleType) {
        List<CfgRulePickingEntity> cfgRulePickings = this.filterByRuleTypeDisabled(this.listOrderByPriority(), ruleType);
        if (CollectionUtils.isEmpty(cfgRulePickings)) {
            return Collections.emptyList();
        }
        List<String> cfgRuleIds = cfgRulePickings.stream().map(CfgRulePickingEntity::getId).collect(Collectors.toList());
        List<CfgRuleConditionDTO.ConditionElementDTO> conditions = cfgRuleConditionService.listByRuleIds(cfgRuleIds, RuleTypeEnum.PICKING_STRATEGY.getCode());
        Map<String, Object> map = this.getRuleConditionMap(executionData);
        return cfgRulePickings.stream()
                .filter(v -> {
                    List<CfgRuleConditionDTO.ConditionElementDTO> conditionList = conditions.stream()
                            .filter(r -> r.getRuleId().equals(v.getId()))
                            .sorted(Comparator.comparing(CfgRuleConditionDTO.ConditionElementDTO::getIndex))
                            .collect(Collectors.toList());
                    List<ConditionElement> conditionElementList = BeanMapper.copyList(conditionList, ConditionElement.class);
                    return spElServer.matchExpressionByConditionList(conditionElementList, map, "");
                })
                .sorted(Comparator.comparing(CfgRulePickingEntity::getPriority, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(CfgRulePickingEntity::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    /**
     * 按规则类型过滤业务禁用标识：字段为 true 表示该业务已禁用，不参与匹配。
     * <ul>
     *   <li>拣货 → pickDisabled</li>
     *   <li>补货 → replenishDisabled</li>
     *   <li>出库 → outStockDisabled</li>
     * </ul>
     */
    private List<CfgRulePickingEntity> filterByRuleTypeDisabled(List<CfgRulePickingEntity> cfgRulePickings, String ruleType) {
        if (CollectionUtils.isEmpty(cfgRulePickings) || CharSequenceUtil.isBlank(ruleType)) {
            return cfgRulePickings;
        }
        return cfgRulePickings.stream().filter(rule -> {
            if (RuleTypeEnum.PICKING_STRATEGY.getCode().equals(ruleType)) {
                return !Boolean.TRUE.equals(rule.getPickDisabled());
            }
            if (RuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode().equals(ruleType)) {
                return !Boolean.TRUE.equals(rule.getReplenishDisabled());
            }
            if (RuleTypeEnum.WAREHOUSE_LOCATION_OUT_STOCK.getCode().equals(ruleType)) {
                return !Boolean.TRUE.equals(rule.getOutStockDisabled());
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * 拣货策略条件组装
     */
    private Map<String, Object> getRuleConditionMap(CfgRulePickingDTO.CfgExecutionDataDTO executionData){
        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put("billType", executionData.getBillType());
        detailMap.put("customerId", executionData.getCustomerId());
        detailMap.put("countryCode",executionData.getCountryCode());
        detailMap.put("deliveryWarehouseId", executionData.getDeliveryWarehouseId());
        detailMap.put("waveType", executionData.getWaveType());
        Map<String, Object> map = new HashMap<>();
        map.put("detailList", Collections.singletonList(detailMap));
        map.put("billType", executionData.getBillType());
        map.put("customerId", executionData.getCustomerId());
        map.put("countryCode",executionData.getCountryCode());
        map.put("deliveryWarehouseId", executionData.getDeliveryWarehouseId());
        map.put("waveType", executionData.getWaveType());
        return map;
    }

    private List<CfgRulePickingEntity> listOrderByPriority() {
        return list(Wrappers.<CfgRulePickingEntity>lambdaQuery()
                .eq(CfgRulePickingEntity::getDisabled, false)
                .orderByAsc(CfgRulePickingEntity::getPriority)
                .orderByDesc(CfgRulePickingEntity::getUpdateTime)
        );
    }

    /**
     * 按补货仓位推荐解析缺货 SKU 的取货/上架仓位（B2C 缺货补货、要货一键移动共用）。
     * <ul>
     *   <li>取货：补货动作库区优先级 + 单仓位可用量 ≥ 缺货数量；找不到 → {@link ApiError#WH_REPLENISH_FROM_LOCATION_NOT_FOUND}</li>
     *   <li>上架 large/small：SKU 大/小件推荐仓位；无效 → {@link ApiError#WH_REPLENISH_TO_LOCATION_NOT_CONFIGURED}</li>
     *   <li>上架 recent：优先产品小货区；否则拣货区最新出入库流水；无流水 → {@link ApiError#WH_REPLENISH_TO_LOCATION_NOT_CONFIGURED}</li>
     *   <li>无规则/本仓无动作 → {@link ApiError#WH_LOCATION_SUGGEST_NOT_FOUND}</li>
     * </ul>
     */
    @Override
    public List<CfgRulePickingDTO.ReplenishLocationSuggestDTO> resolveReplenishLocations(
            CfgRulePickingDTO.CfgExecutionDataDTO executionData,
            String warehouseId,
            List<CfgRulePickingDTO.ReplenishShortageItemDTO> shortageItems) {
        if (CollectionUtils.isEmpty(shortageItems) || CharSequenceUtil.isBlank(warehouseId)) {
            return Collections.emptyList();
        }
        List<CfgRulePickingEntity> replenishRules = this.listMatchedRules(executionData, RuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode());
        if (CollectionUtils.isEmpty(replenishRules)) {
            throw new ServiceException(ApiError.WH_LOCATION_SUGGEST_NOT_FOUND);
        }
        CfgRulePickingEntity hitRule = replenishRules.get(0);
        List<CfgRulePackingActionEntity> replenishActions = cfgRulePackingActionService.listByRuleIds(
                Collections.singletonList(hitRule.getId()), RuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode());
        replenishActions = replenishActions.stream()
                .filter(action -> warehouseId.equals(action.getWarehouseId()))
                .sorted(Comparator.comparing(CfgRulePackingActionEntity::getIndex, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(replenishActions)) {
            throw new ServiceException(ApiError.WH_LOCATION_SUGGEST_NOT_FOUND);
        }

        List<String> skuIds = shortageItems.stream().map(CfgRulePickingDTO.ReplenishShortageItemDTO::getSkuId)
                .filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuIds)) {
            String skuNo = shortageItems.stream().map(CfgRulePickingDTO.ReplenishShortageItemDTO::getSkuNo)
                    .filter(CharSequenceUtil::isNotBlank).findFirst().orElse("");
            throw new ServiceException(ApiError.WH_REPLENISH_FROM_LOCATION_NOT_FOUND, skuNo);
        }
        // 已按 crpa.index、qty desc 排序，保证库区优先级
        List<CfgRulePickingDTO.CfgRulePickingInventoryDTO> replenishInventories = cfgRulePackingActionService.listLocationByRule(
                Collections.singletonList(hitRule),
                Collections.singletonList(warehouseId),
                skuIds,
                "gt",
                RuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode());

        // 同 SKU 多明细时扣减候选库存，避免重复占用同一仓位
        Map<String, Integer> remainingQtyMap = new LinkedHashMap<>();
        for (CfgRulePickingDTO.CfgRulePickingInventoryDTO inv : replenishInventories) {
            if (!warehouseId.equals(inv.getWarehouseId()) || CharSequenceUtil.isBlank(inv.getWarehouseLocation())
                    || inv.getQty() == null || inv.getQty() <= 0) {
                continue;
            }
            String key = inv.getSkuId() + "#" + inv.getWarehouseLocation();
            remainingQtyMap.merge(key, inv.getQty(), Integer::sum);
        }

        List<WarehouseLocationEntity> warehouseLocations = warehouseLocationService.listByWarehouseIds(Collections.singletonList(warehouseId));
        Map<String, WarehouseLocationEntity> locationByCode = warehouseLocations.stream()
                .filter(loc -> "location".equals(loc.getType()))
                .filter(loc -> !Boolean.TRUE.equals(loc.getDisabled()))
                .collect(Collectors.toMap(WarehouseLocationEntity::getCode, Function.identity(), (a, b) -> a));
        Map<String, WarehouseLocationEntity> areaById = warehouseLocations.stream()
                .filter(loc -> "area".equals(loc.getType()))
                .collect(Collectors.toMap(WarehouseLocationEntity::getId, Function.identity(), (a, b) -> a));

        List<ProductDetailEntity> productDetails = CollectionUtils.isEmpty(skuIds)
                ? Collections.emptyList() : productDetailFeign.listByIds(skuIds);
        Map<String, ProductDetailEntity> productMap = productDetails.stream()
                .collect(Collectors.toMap(ProductDetailEntity::getId, Function.identity(), (a, b) -> a));

        List<String> pickLocationCodes = listPickingAreaLocationCodes(warehouseId, warehouseLocations);
        List<CfgRulePickingDTO.ReplenishLocationSuggestDTO> result = new ArrayList<>();
        for (CfgRulePickingDTO.ReplenishShortageItemDTO item : shortageItems) {
            if (CharSequenceUtil.isBlank(item.getSkuId()) && CharSequenceUtil.isBlank(item.getSkuNo())) {
                throw new ServiceException(ApiError.WH_REPLENISH_FROM_LOCATION_NOT_FOUND, "");
            }
            int needQty = item.getQty() == null ? 0 : item.getQty();
            if (needQty <= 0) {
                continue;
            }
            CfgRulePickingDTO.CfgRulePickingInventoryDTO outInventory = null;
            for (CfgRulePickingDTO.CfgRulePickingInventoryDTO inv : replenishInventories) {
                boolean skuMatch = (CharSequenceUtil.isNotBlank(item.getSkuId()) && item.getSkuId().equals(inv.getSkuId()))
                        || (CharSequenceUtil.isNotBlank(item.getSkuNo()) && item.getSkuNo().equals(inv.getSkuNo()));
                if (!warehouseId.equals(inv.getWarehouseId()) || !skuMatch || CharSequenceUtil.isBlank(inv.getWarehouseLocation())) {
                    continue;
                }
                String key = inv.getSkuId() + "#" + inv.getWarehouseLocation();
                Integer remain = remainingQtyMap.get(key);
                if (remain != null && remain >= needQty) {
                    outInventory = inv;
                    remainingQtyMap.put(key, remain - needQty);
                    break;
                }
            }
            if (outInventory == null) {
                throw new ServiceException(ApiError.WH_REPLENISH_FROM_LOCATION_NOT_FOUND, item.getSkuNo());
            }
            String fromLocation = outInventory.getWarehouseLocation();
            WarehouseLocationEntity fromLocEntity = locationByCode.get(fromLocation);
            String fromArea = null;
            if (fromLocEntity != null && CharSequenceUtil.isNotBlank(fromLocEntity.getParentId())) {
                WarehouseLocationEntity area = areaById.get(fromLocEntity.getParentId());
                fromArea = area != null ? area.getCode() : null;
            }

            String toLocation = resolveToWarehouseLocation(hitRule.getInWarehouseLocation(), productMap.get(item.getSkuId()),
                    warehouseId, item.getSkuId(), item.getSkuNo(), pickLocationCodes, locationByCode);
            WarehouseLocationEntity toLocEntity = locationByCode.get(toLocation);
            if (toLocEntity == null) {
                // 可能 listByWarehouseIds 未覆盖（如禁用过滤），再查一次
                toLocEntity = warehouseLocationService.getOne(Wrappers.<WarehouseLocationEntity>lambdaQuery()
                        .eq(WarehouseLocationEntity::getWarehouseId, warehouseId)
                        .eq(WarehouseLocationEntity::getCode, toLocation)
                        .eq(WarehouseLocationEntity::getDisabled, false)
                        .last("limit 1"));
            }
            if (toLocEntity == null) {
                throw new ServiceException(ApiError.WH_REPLENISH_TO_LOCATION_NOT_CONFIGURED, item.getSkuNo());
            }
            String toArea = null;
            if (CharSequenceUtil.isNotBlank(toLocEntity.getParentId())) {
                WarehouseLocationEntity area = areaById.get(toLocEntity.getParentId());
                if (area == null) {
                    area = warehouseLocationService.getById(toLocEntity.getParentId());
                }
                toArea = area != null ? area.getCode() : null;
            }

            CfgRulePickingDTO.ReplenishLocationSuggestDTO suggest = new CfgRulePickingDTO.ReplenishLocationSuggestDTO();
            suggest.setSkuId(item.getSkuId());
            suggest.setSkuNo(item.getSkuNo());
            suggest.setQty(needQty);
            suggest.setFromWarehouseArea(fromArea);
            suggest.setFromWarehouseLocation(fromLocation);
            suggest.setToWarehouseArea(toArea);
            suggest.setToWarehouseLocation(toLocation);
            suggest.setRuleId(hitRule.getId());
            result.add(suggest);
        }
        return result;
    }

    /**
     * 按规则上架类型解析上架仓位编码。
     * <ul>
     *   <li>{@code large}：产品推荐仓位（大货区），多值取第一个并校验系统仓位</li>
     *   <li>{@code small}：产品推荐仓位（小货区），多值取第一个并校验系统仓位</li>
     *   <li>{@code recent}：先小货区；无效则查拣货区出入库流水最新仓位；仍无则抛业务异常</li>
     * </ul>
     *
     * @param inWarehouseLocationConfig 规则上的上架类型配置
     * @param product                   SKU 产品主数据（可空）
     * @param warehouseId               仓库 ID
     * @param skuId                     SKU ID（recent 查流水必填）
     * @param skuNo                     SKU 编码（异常文案）
     * @param pickLocationCodes         拣货区仓位编码集合
     * @param locationByCode            本仓库位缓存
     * @return 上架仓位编码
     */
    private String resolveToWarehouseLocation(String inWarehouseLocationConfig,
                                              ProductDetailEntity product,
                                              String warehouseId,
                                              String skuId,
                                              String skuNo,
                                              List<String> pickLocationCodes,
                                              Map<String, WarehouseLocationEntity> locationByCode) {
        if (CharSequenceUtil.isBlank(inWarehouseLocationConfig)) {
            throw new ServiceException(ApiError.WH_REPLENISH_TO_LOCATION_NOT_CONFIGURED, skuNo);
        }
        InWarehouseLocationEnum locationEnum = InWarehouseLocationEnum.getEnum(inWarehouseLocationConfig);
        if (locationEnum == null) {
            throw new ServiceException(ApiError.WH_REPLENISH_TO_LOCATION_NOT_CONFIGURED, skuNo);
        }
        if (InWarehouseLocationEnum.LARGE.equals(locationEnum)) {
            String loc = firstLocationCode(product == null ? null : product.getWarehouseLocationLarge());
            if (CharSequenceUtil.isBlank(loc) || !isValidSystemLocation(loc, locationByCode, warehouseId)) {
                throw new ServiceException(ApiError.WH_REPLENISH_TO_LOCATION_NOT_CONFIGURED, skuNo);
            }
            return loc;
        }
        if (InWarehouseLocationEnum.SMALL.equals(locationEnum)) {
            String loc = firstLocationCode(product == null ? null : product.getWarehouseLocation());
            if (CharSequenceUtil.isBlank(loc) || !isValidSystemLocation(loc, locationByCode, warehouseId)) {
                throw new ServiceException(ApiError.WH_REPLENISH_TO_LOCATION_NOT_CONFIGURED, skuNo);
            }
            return loc;
        }
        // recent：优先产品小货区，否则拣货区最新出入库流水
        String smallLoc = firstLocationCode(product == null ? null : product.getWarehouseLocation());
        if (CharSequenceUtil.isNotBlank(smallLoc) && isValidSystemLocation(smallLoc, locationByCode, warehouseId)) {
            return smallLoc;
        }
        if (CollectionUtils.isEmpty(pickLocationCodes) || CharSequenceUtil.isBlank(skuId)) {
            throw new ServiceException(ApiError.WH_REPLENISH_TO_LOCATION_NOT_CONFIGURED, skuNo);
        }
        TransactionFlowEntity transactionFlow = transactionFlowService.lambdaQuery()
                .eq(TransactionFlowEntity::getWarehouseId, warehouseId)
                .eq(TransactionFlowEntity::getSkuId, skuId)
                .in(TransactionFlowEntity::getWarehouseLocation, pickLocationCodes)
                .orderByDesc(TransactionFlowEntity::getBillDate, TransactionFlowEntity::getId)
                .last("limit 1")
                .one();
        if (transactionFlow == null || CharSequenceUtil.isBlank(transactionFlow.getWarehouseLocation())) {
            throw new ServiceException(ApiError.WH_REPLENISH_TO_LOCATION_NOT_CONFIGURED, skuNo);
        }
        return transactionFlow.getWarehouseLocation();
    }

    /**
     * 逗号分隔仓位配置取第一个有效编码
     */
    private String firstLocationCode(String locationConfig) {
        if (CharSequenceUtil.isBlank(locationConfig)) {
            return null;
        }
        return locationConfig.split(",")[0].trim();
    }

    /**
     * 校验仓位编码在本仓是否存在且未禁用
     */
    private boolean isValidSystemLocation(String locationCode,
                                          Map<String, WarehouseLocationEntity> locationByCode,
                                          String warehouseId) {
        if (locationByCode.containsKey(locationCode)) {
            return true;
        }
        WarehouseLocationEntity entity = warehouseLocationService.getOne(Wrappers.<WarehouseLocationEntity>lambdaQuery()
                .eq(WarehouseLocationEntity::getWarehouseId, warehouseId)
                .eq(WarehouseLocationEntity::getCode, locationCode)
                .eq(WarehouseLocationEntity::getDisabled, false)
                .last("limit 1"));
        return entity != null;
    }

    /**
     * 按出库仓位推荐解析明细出库仓位。
     * <ul>
     *   <li>无出库规则/本仓无动作 → {@link ApiError#WH_OUT_STOCK_RULE_NOT_FOUND}</li>
     *   <li>按出库动作库区优先级找不到足够可用库存 → {@link ApiError#WH_OUT_STOCK_LOCATION_NOT_FOUND}</li>
     *   <li>拣货区仓位或空仓位 → 直接出库；其它库区 → needMove，目标空仓位 {@code ""}</li>
     * </ul>
     */
    @Override
    public List<CfgRulePickingDTO.OutStockLocationSuggestDTO> resolveOutStockLocations(
            CfgRulePickingDTO.CfgExecutionDataDTO executionData,
            String warehouseId,
            List<CfgRulePickingDTO.OutStockItemDTO> items) {
        if (CollectionUtils.isEmpty(items) || CharSequenceUtil.isBlank(warehouseId)) {
            return Collections.emptyList();
        }
        List<CfgRulePickingEntity> outStockRules = this.listMatchedRules(executionData, RuleTypeEnum.WAREHOUSE_LOCATION_OUT_STOCK.getCode());
        if (CollectionUtils.isEmpty(outStockRules)) {
            throw new ServiceException(ApiError.WH_OUT_STOCK_RULE_NOT_FOUND);
        }
        List<String> matchedRuleIds = outStockRules.stream()
                .map(CfgRulePickingEntity::getId)
                .collect(Collectors.toList());
        List<CfgRulePackingActionEntity> matchedActions = cfgRulePackingActionService.listByRuleIds(
                matchedRuleIds, RuleTypeEnum.WAREHOUSE_LOCATION_OUT_STOCK.getCode());
        Map<String, List<CfgRulePackingActionEntity>> actionsByRuleId = matchedActions.stream()
                .filter(action -> warehouseId.equals(action.getWarehouseId()))
                .collect(Collectors.groupingBy(CfgRulePackingActionEntity::getRuleId));
        CfgRulePickingEntity hitRule = null;
        for (CfgRulePickingEntity rule : outStockRules) {
            List<CfgRulePackingActionEntity> currentActions = actionsByRuleId.get(rule.getId());
            if (!CollectionUtils.isEmpty(currentActions)) {
                hitRule = rule;
                break;
            }
        }
        if (hitRule == null) {
            throw new ServiceException(ApiError.WH_OUT_STOCK_RULE_NOT_FOUND);
        }

        List<String> skuIds = items.stream().map(CfgRulePickingDTO.OutStockItemDTO::getSkuId)
                .filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuIds)) {
            String skuNo = items.stream().map(CfgRulePickingDTO.OutStockItemDTO::getSkuNo)
                    .filter(CharSequenceUtil::isNotBlank).findFirst().orElse("");
            throw new ServiceException(ApiError.WH_OUT_STOCK_LOCATION_NOT_FOUND, skuNo);
        }
        List<CfgRulePickingDTO.CfgRulePickingInventoryDTO> outInventories = cfgRulePackingActionService.listLocationByRule(
                Collections.singletonList(hitRule),
                Collections.singletonList(warehouseId),
                skuIds,
                "gt",
                RuleTypeEnum.WAREHOUSE_LOCATION_OUT_STOCK.getCode());

        List<WarehouseLocationEntity> warehouseLocations = warehouseLocationService.listByWarehouseIds(Collections.singletonList(warehouseId));
        List<String> pickLocationCodes = listPickingAreaLocationCodes(warehouseId, warehouseLocations);
        Set<String> pickLocationSet = new HashSet<>(pickLocationCodes);

        // 同 SKU 多明细时扣减候选库存，避免重复占用（空仓位 code 为 ""，不可用 isBlank 过滤）
        Map<String, Integer> remainingQtyMap = new LinkedHashMap<>();
        for (CfgRulePickingDTO.CfgRulePickingInventoryDTO inv : outInventories) {
            if (!warehouseId.equals(inv.getWarehouseId()) || inv.getWarehouseLocation() == null
                    || inv.getQty() == null || inv.getQty() <= 0) {
                continue;
            }
            String key = inv.getSkuId() + "#" + inv.getWarehouseLocation();
            remainingQtyMap.merge(key, inv.getQty(), Integer::sum);
        }

        List<CfgRulePickingDTO.OutStockLocationSuggestDTO> result = new ArrayList<>();
        for (CfgRulePickingDTO.OutStockItemDTO item : items) {
            if (CharSequenceUtil.isBlank(item.getSkuId()) && CharSequenceUtil.isBlank(item.getSkuNo())) {
                throw new ServiceException(ApiError.WH_OUT_STOCK_LOCATION_NOT_FOUND, "");
            }
            int needQty = item.getQty() == null ? 0 : item.getQty();
            if (needQty <= 0) {
                continue;
            }
            CfgRulePickingDTO.CfgRulePickingInventoryDTO chosen = null;
            for (CfgRulePickingDTO.CfgRulePickingInventoryDTO inv : outInventories) {
                boolean skuMatch = (CharSequenceUtil.isNotBlank(item.getSkuId()) && item.getSkuId().equals(inv.getSkuId()))
                        || (CharSequenceUtil.isNotBlank(item.getSkuNo()) && item.getSkuNo().equals(inv.getSkuNo()));
                if (!warehouseId.equals(inv.getWarehouseId()) || !skuMatch || inv.getWarehouseLocation() == null) {
                    continue;
                }
                String key = inv.getSkuId() + "#" + inv.getWarehouseLocation();
                Integer remain = remainingQtyMap.get(key);
                if (remain != null && remain >= needQty) {
                    chosen = inv;
                    remainingQtyMap.put(key, remain - needQty);
                    break;
                }
            }
            if (chosen == null) {
                throw new ServiceException(ApiError.WH_OUT_STOCK_LOCATION_NOT_FOUND, item.getSkuNo());
            }
            String stockLocation = chosen.getWarehouseLocation();
            // 空仓位（code=""）或拣货区：直接出库；其它库区需先移到空仓位
            boolean inPickingArea = "".equals(stockLocation) || pickLocationSet.contains(stockLocation);
            CfgRulePickingDTO.OutStockLocationSuggestDTO suggest = new CfgRulePickingDTO.OutStockLocationSuggestDTO();
            suggest.setDetailId(item.getDetailId());
            suggest.setSkuId(item.getSkuId());
            suggest.setSkuNo(item.getSkuNo());
            suggest.setQty(needQty);
            suggest.setStockLocation(stockLocation);
            suggest.setInPickingArea(inPickingArea);
            suggest.setNeedMove(!inPickingArea);
            suggest.setTargetLocation(inPickingArea ? stockLocation : "");
            suggest.setRuleId(hitRule.getId());
            result.add(suggest);
        }
        return result;
    }

    /**
     * 列出仓库下拣货区（pickingArea）内全部仓位编码，供 recent 上架查出入库流水
     */
    private List<String> listPickingAreaLocationCodes(String warehouseId, List<WarehouseLocationEntity> warehouseLocations) {
        List<String> pickAreaIds = warehouseLocations.stream()
                .filter(loc -> "area".equals(loc.getType()))
                .filter(loc -> WarehouseAreaTypeEnum.PICKING_AREA.getCode().equals(loc.getAreaType()))
                .filter(loc -> !Boolean.TRUE.equals(loc.getDisabled()))
                .map(WarehouseLocationEntity::getId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(pickAreaIds)) {
            return Collections.emptyList();
        }
        return warehouseLocations.stream()
                .filter(loc -> "location".equals(loc.getType()))
                .filter(loc -> pickAreaIds.contains(loc.getParentId()))
                .filter(loc -> !Boolean.TRUE.equals(loc.getDisabled()))
                .map(WarehouseLocationEntity::getCode)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }
}
