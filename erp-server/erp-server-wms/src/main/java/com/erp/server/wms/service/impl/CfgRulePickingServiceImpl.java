package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.pickingstrategy.*;
import com.erp.model.wms.entity.CfgRuleConditionEntity;
import com.erp.model.wms.entity.CfgRulePackingActionEntity;
import com.erp.model.wms.entity.CfgRulePickingEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.PickingBillTypeEnum;
import com.erp.model.wms.enums.RuleTypeEnum;
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
import java.util.stream.Collectors;

/**
 * <p>
 * 拣货规则表 服务实现类
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
        cfgRuleConditionService.saveRuleCondition(entity.getId(), dto.getConditionList(), RuleTypeEnum.PICKING_STRATEGY.getCode());
        cfgRulePackingActionService.saveRuleAction(entity.getId(), dto.getActions());
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
        cfgRuleConditionService.updateRuleCondition(dto.getId(), dto.getConditionList(), ModuleTypeEnum.PICKING_STRATEGY.getCode(), RuleTypeEnum.PICKING_STRATEGY.getCode());
        cfgRulePackingActionService.updateRuleAction(dto.getId(), dto.getActions());
    }

    @Override
    public CfgRulePickingDTO.View view(String id) {
        CfgRulePickingEntity entity = getById(id);
        CfgRulePickingDTO.View view = BeanMapperUtils.map(CfgRulePickingDTO.View.class, entity);
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
        List<CfgRuleActionDTO.View> actions = BeanMapperUtils.copyList(CfgRuleActionDTO.View.class, actionEntities);
        view.setActions(actions);
        return view;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        //删除拣货规则
        removeByIds(ids);
        //删除规则
        cfgRuleConditionService.removeByRuleIds(ids);
        //删除拣货动作
        cfgRulePackingActionService.removeByRuleIds(ids);
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
    /**
     * 根据单据相关信息匹配出拣货规则===> 仓位分配规则 ===> 对应仓位 ===> 根据sku加仓位获取对应库位库存
     * 库存不满足sku对应数量   循环库位 ===> 循环库区 ===>循环仓库 ===> 循环规则
     */
    @Override
    public Pair<List<LocationInventoryResultDTO>, Map<String, Integer>> getRuleOrderMatchResult(PickingListsDTO.AddDTO dto) {
        CfgRulePickingDTO.CfgExecutionDataDTO executionData = this.getRuleExecutionData(dto);
        Pair<List<LocationInventoryResultDTO>, Map<String, Integer>> result = getSoB2CRuleOrderMatchResult(executionData);
        return result;
    }

    @Override
    public Pair<List<LocationInventoryResultDTO>, Map<String, Integer>> getSoB2CRuleOrderMatchResult(CfgRulePickingDTO.CfgExecutionDataDTO executionData) {
        // 暂时只计算数量优先
        Pair<List<CfgRulePickingDTO.CfgRulePickingInventoryDTO>, List<WarehouseLocationEntity>> listListPair = this.matchRuleActionList(executionData, "gt");
        //
        Pair<List<LocationInventoryResultDTO>, Map<String, Integer>> result = this.getSoB2CRuleOrderMatchResult(executionData, listListPair);
        return result;
    }

    /**
     *根据主单信息，获取符合拣货策略的RuleAction集合
     * */
    @Override
    public  List<CfgRulePickingDTO.CfgRulePickingInventoryDTO> getMatchRuleActionList(PickingListsDTO.AddDTO dto,String determiningCondition){
        // 拣货明细转换为规则执行数据明细
        CfgRulePickingDTO.CfgExecutionDataDTO executionData = this.getRuleExecutionData(dto);
        Pair<List<CfgRulePickingDTO.CfgRulePickingInventoryDTO>, List<WarehouseLocationEntity>> listListPair = this.matchRuleActionList(executionData, determiningCondition);
        return listListPair.getFirst();
    }

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
    public CfgRulePickingDTO.CfgExecutionDataDTO getRuleExecutionData(PickingListsDTO.AddDTO dto){
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
     * 根据拣货策略条件，进行拣货策略的匹配
     */
    @Override
    public Pair<List<CfgRulePickingDTO.CfgRulePickingInventoryDTO>, List<WarehouseLocationEntity>> matchRuleActionList(CfgRulePickingDTO.CfgExecutionDataDTO executionData,String determiningCondition){
        log.warn("单据【{}】开始执行拣货策略，开始时间为{}", executionData.getSourceCode(), System.currentTimeMillis());
        // 获取所有已启用规则
        List<CfgRulePickingEntity> cfgRulePickings = this.listOrderByPriority();
        if (CollectionUtils.isEmpty(cfgRulePickings)) {
            throw new ServiceException(ApiError.NOT_EXIST, "拣货规则");
        }
        List<String> cfgRuleIds = cfgRulePickings.stream().map(CfgRulePickingEntity::getId).collect(Collectors.toList());
        // 查询所有规则对应的规则条件
        List<CfgRuleConditionDTO.ConditionElementDTO> conditions = cfgRuleConditionService.listByRuleIds(cfgRuleIds, RuleTypeEnum.PICKING_STRATEGY.getCode());
        // 查询所有规则对应的规则动作
        List<CfgRulePackingActionEntity> actions = cfgRulePackingActionService.listByRuleIds(cfgRuleIds);
        //封装条件参数
        Map<String, Object> map = this.getRuleConditionMap(executionData);
        // 获取所有符合条件的规则 使用异步流后需要重排序
        List<CfgRulePickingEntity> rules = cfgRulePickings.parallelStream()
                .filter(v -> {
                    List<CfgRuleConditionDTO.ConditionElementDTO> conditionList = conditions.stream().
                            filter(r -> r.getRuleId().equals(v.getId())).
                            sorted(Comparator.comparing(CfgRuleConditionDTO.ConditionElementDTO::getIndex)).collect(Collectors.toList());
                    List<ConditionElement> conditionElementList = BeanMapper.copyList(conditionList, ConditionElement.class);
                    //获取到表达式,判断表达式是否匹配
                    return spElServer.matchExpressionByConditionList(conditionElementList, map,"");
                }).collect(Collectors.toList());
        log.warn("单据【{}】完成过滤拣货策略，完成时间为{}", executionData.getSourceCode(), System.currentTimeMillis());
        List<String> warehouseIds = actions.parallelStream().map(CfgRulePackingActionEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> locationList = warehouseLocationService.listByWarehouseIds(warehouseIds);
        List<String> skuIds = executionData.getDetails().parallelStream().map(CfgRulePickingDTO.CfgExecutionDataDetailDTO::getSkuId).distinct().collect(Collectors.toList());
        List<CfgRulePickingDTO.CfgRulePickingInventoryDTO> cfgRulePickingInventoryDTOS = cfgRulePackingActionService.listLocationByRule(rules, warehouseIds, skuIds,determiningCondition);
        return  Pair.create(cfgRulePickingInventoryDTOS, locationList);
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
}
