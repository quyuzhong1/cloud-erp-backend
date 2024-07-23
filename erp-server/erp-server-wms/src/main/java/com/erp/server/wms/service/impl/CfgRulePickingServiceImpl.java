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
import com.erp.model.wms.dto.pickingstrategy.CfgRuleActionDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRuleConditionDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.dto.pickingstrategy.LocationInventoryResultDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.server.wms.mapper.CfgRulePickingMapper;
import com.erp.server.wms.service.*;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

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
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dto.getId(), "拣货策略规则");
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
    public List<LocationInventoryResultDTO> getRuleOrderMatchResult(CfgRulePickingDTO.CfgExecutionDataDTO dto) {
        Pair<List<LocationInventoryResultDTO>, List<String>> result = getSoB2CRuleOrderMatchResult(dto);
        return result.getFirst();
    }

    @Override
    public Pair<List<LocationInventoryResultDTO>, List<String>> getSoB2CRuleOrderMatchResult(CfgRulePickingDTO.CfgExecutionDataDTO dto) {
        // 获取所有已启用规则
        List<CfgRulePickingEntity> cfgRulePickings = this.listOrderByPriority();
        if (CollectionUtils.isEmpty(cfgRulePickings)) {
            throw new ServiceException(ApiError.NOT_EXIST, "拣货规则");
        }
        List<String> cfgRuleIds = cfgRulePickings.stream().map(CfgRulePickingEntity::getId).collect(Collectors.toList());
        // 查询所有规则对应的规则条件
        List<CfgRuleConditionDTO.ConditionElementDTO> conditions = cfgRuleConditionService.listByRuleIds(cfgRuleIds,RuleTypeEnum.PICKING_STRATEGY.getCode());
        // 查询所有规则对应的规则动作
        List<CfgRulePackingActionEntity> actions = cfgRulePackingActionService.listByRuleIds(cfgRuleIds);
        List<LocationInventoryResultDTO> result = new ArrayList<>();
        List<String> stockSku = new ArrayList<>();
        List<WarehouseLocationEntity> locationList = warehouseLocationService.list();
        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put("billType", dto.getBillType());
        detailMap.put("customerId", dto.getCustomerId());
        detailMap.put("deliveryWarehouseId", dto.getDeliveryWarehouseId());
        Map<String, Object> map = new HashMap<>();
        map.put("detailList", Collections.singletonList(detailMap));
        map.put("billType", dto.getBillType());
        map.put("customerId", dto.getCustomerId());
        map.put("deliveryWarehouseId", dto.getDeliveryWarehouseId());
        Map<String, List<CfgRulePickingDTO.CfgExecutionDataDetailDTO>> warehouseGroupMap = dto.getDetails().stream().collect(Collectors.groupingBy(CfgRulePickingDTO.CfgExecutionDataDetailDTO::getWarehouseId));
        for (Map.Entry<String, List<CfgRulePickingDTO.CfgExecutionDataDetailDTO>> entry : warehouseGroupMap.entrySet()) {
            for (CfgRulePickingDTO.CfgExecutionDataDetailDTO detail : entry.getValue()) {
                AtomicInteger quantity = new AtomicInteger(detail.getQty());
                for (CfgRulePickingEntity picking : cfgRulePickings) {
                    List<CfgRuleConditionDTO.ConditionElementDTO> conditionList = conditions.stream().
                            filter(r -> r.getRuleId().equals(picking.getId())).
                            sorted(Comparator.comparing(CfgRuleConditionDTO.ConditionElementDTO::getIndex)).collect(Collectors.toList());
                    List<ConditionElement> conditionElementList = BeanMapper.copyList(conditionList, ConditionElement.class);
                    //获取到表达式,判断表达式是否匹配
                    Boolean matchResult = spElServer.matchExpressionByConditionList(conditionElementList, map);
                    if (Boolean.TRUE.equals(matchResult)) {
                        List<CfgRulePackingActionEntity> actionList = actions.stream()
                                .filter(r -> r.getRuleId().equals(picking.getId()))
                                .filter(r -> ObjectUtils.isEmpty(detail.getWarehouseId()) || r.getWarehouseId().equals(detail.getWarehouseId()))
                                .sorted(Comparator.comparing(CfgRulePackingActionEntity::getIndex))
                                .collect(Collectors.toList());
                        handlerAction(actionList, result, locationList, detail, quantity);
                        if (0 == quantity.get()) {
                            break;
                        }
                    }
                }
                if (0 != quantity.get()) {
                    if (PickingBillTypeEnum.B2C.getCode().equals(dto.getBillType())) {
                        stockSku.add(detail.getSkuNo());
                        result = result.stream().filter(v -> !v.getSkuNo().equals(detail.getSkuNo())).collect(Collectors.toList());
                    }else {
                        throw new ServiceException(ApiError.SKU_INVENTORY_SHORTAGE, detail.getSkuNo());
                    }                }
            }

        }

        return Pair.create(result, stockSku);
    }


    private void handlerAction(List<CfgRulePackingActionEntity> actionList,
                               List<LocationInventoryResultDTO> result,
                               List<WarehouseLocationEntity> locationList,
                               CfgRulePickingDTO.CfgExecutionDataDetailDTO detail, AtomicInteger quantity) {

        // 循环仓位分配规则
        for (CfgRulePackingActionEntity action : actionList) {
            // 获取该规则下仓位
            List<WarehouseLocationEntity> locations = locationList.stream()
                    .filter(location -> action.getWarehouseId().equals(location.getWarehouseId()))
                    .filter(location -> action.getWarehouseAreaId().equals(location.getParentId()))
                    .filter(location -> !location.getDisabled())
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(locations)) {
                return;
            }
            List<String> locationCodes = locations.stream().map(WarehouseLocationEntity::getCode).collect(Collectors.toList());
            if (OutStockModeEnum.FIFO.getCode().equals(action.getOutStockMode())) {
                // todo FIFO 数据和表不支持，后续优化
            } else {
                List<InventoryEntity> inventoryList = inventoryService.list(Wrappers.<InventoryEntity>lambdaQuery()
                        .eq(InventoryEntity::getSkuId, detail.getSkuId())
                        .eq(InventoryEntity::getWarehouseId, action.getWarehouseId())
                        .in(InventoryEntity::getWarehouseLocation, locationCodes)
                        .ne(InventoryEntity::getQty, 0)
                        .eq(InventoryEntity::getDictInventoryStatus, InventoryStatusEnum.USABLE.getCode())
                        .orderByDesc(InventoryEntity::getQty)
                );
                //循环计算需要占用多少库位及对应库存
                for (InventoryEntity inventoryEntity : inventoryList) {
                    LocationInventoryResultDTO inventoryResultDTO = new LocationInventoryResultDTO();
                    inventoryResultDTO.setSkuId(detail.getSkuId());
                    inventoryResultDTO.setSkuNo(detail.getSkuNo());
                    WarehouseLocationEntity entity = locations.stream().filter(location -> location.getCode().equals(inventoryEntity.getWarehouseLocation()))
                            .findFirst().orElse(new WarehouseLocationEntity());
                    inventoryResultDTO.setWarehouseId(action.getWarehouseId());
                    inventoryResultDTO.setWarehouseAreaId(action.getWarehouseAreaId());
                    inventoryResultDTO.setWarehouseLocationId(entity.getId());
                    inventoryResultDTO.setWarehouseLocation(inventoryEntity.getWarehouseLocation());
                    inventoryResultDTO.setSourceDetailId(detail.getSourceDetailId());
                    if (inventoryEntity.getQty() >= quantity.get()) {
                        inventoryResultDTO.setQuantity(quantity.get());
                        result.add(inventoryResultDTO);
                        quantity.set(0);
                        return;
                    } else {
                        inventoryResultDTO.setQuantity(inventoryEntity.getQty());
                        quantity.set(quantity.get() - inventoryEntity.getQty());
                        result.add(inventoryResultDTO);
                    }
                }
            }
        }
    }

    private List<CfgRulePickingEntity> listOrderByPriority() {
        return list(Wrappers.<CfgRulePickingEntity>lambdaQuery()
                .eq(CfgRulePickingEntity::getDisabled, false)
                .orderByAsc(CfgRulePickingEntity::getPriority)
                .orderByDesc(CfgRulePickingEntity::getUpdateTime)
        );
    }
}
