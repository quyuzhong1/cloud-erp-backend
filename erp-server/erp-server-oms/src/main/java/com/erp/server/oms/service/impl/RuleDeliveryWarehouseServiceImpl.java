package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.RuleConditionDTO;
import com.erp.model.oms.dto.RuleDeliveryWarehouseDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.RuleConditionEntity;
import com.erp.model.oms.entity.RuleDeliveryWarehouseEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.mapper.RuleDeliveryWarehouseMapper;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 发货仓库规则表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@Service
public class RuleDeliveryWarehouseServiceImpl extends SuperServiceImpl<RuleDeliveryWarehouseMapper, RuleDeliveryWarehouseEntity> implements RuleDeliveryWarehouseService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private RuleConditionService ruleConditionService;

    @Resource
    private SpElServer spElServer;


    @Resource
    private InventoryFeign inventoryFeign;

    @Resource
    private SoB2cDetailService soB2cDetailService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    @Lazy
    private SoB2cService soB2cService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(RuleDeliveryWarehouseDTO.AddDTO addDTO) {
        List<RuleConditionDTO.AddDTO> conditionList = addDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(),"")).collect(Collectors.toList());
        SpElExpressionDTO expressionDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = expressionDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (!checkResult) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
        }
        RuleDeliveryWarehouseEntity ruleDeliveryWarehouseEntity = new RuleDeliveryWarehouseEntity();
        BeanMapperUtils.copy(addDTO, ruleDeliveryWarehouseEntity);
        // 数据处理
        handleData(ruleDeliveryWarehouseEntity);
        boolean save = super.save(ruleDeliveryWarehouseEntity);
        if (!save) {
            throw new ServiceException("发货仓库规则单保存失败");
        }
        String id = ruleDeliveryWarehouseEntity.getId();
        //保存规则条件
        ruleConditionService.saveRuleCondition(id, conditionList);
        // 操作日志
        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "发货仓库规则单", ruleDeliveryWarehouseEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.RULE_DELIVERY_WAREHOUSE.getCode(), ruleDeliveryWarehouseEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return ruleDeliveryWarehouseEntity.getId();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RuleDeliveryWarehouseDTO.UpdateDTO updateDTO) {
        String id = updateDTO.getId();
        RuleDeliveryWarehouseEntity old = super.getById(id);
        if(null == old){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货仓库规则单");
        }
        List<RuleConditionDTO.UpdateDTO> conditionList = updateDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(),"")).collect(Collectors.toList());
        SpElExpressionDTO sqElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = sqElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (!checkResult) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
        }

        RuleDeliveryWarehouseEntity ruleDeliveryWarehouseEntity = BeanMapperUtils.map(RuleDeliveryWarehouseEntity.class, updateDTO);
        // 数据处理
        handleData(ruleDeliveryWarehouseEntity);
        boolean save = super.updateById(ruleDeliveryWarehouseEntity);
        if (!save) {
            throw new ServiceException("发货仓库规则单保存失败");
        }
        ruleConditionService.updateRuleCondition(id, conditionList);
        // 记录主单操作日志
        String msg =  CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), ruleDeliveryWarehouseEntity.getId(), "发货仓库规则单");
        operateLogService.addModuleOperateLogByObj(old, ruleDeliveryWarehouseEntity, ModuleTypeEnum.RULE_DELIVERY_WAREHOUSE.getCode(), ruleDeliveryWarehouseEntity.getId(), msg);
        return Boolean.TRUE;
    }

    /**
     * 分页查询
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.RuleDeliveryWarehouseDTO.PagingViewDTO>
     * @author yl
     * @date 2023-08-31 17:50
     */
    @Override
    public PagingVO<RuleDeliveryWarehouseDTO.PagingViewDTO> paging(PagingDTO<RuleDeliveryWarehouseDTO.PagingParamDTO> dto) {
        RuleDeliveryWarehouseDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        return new PagingVO<>(pageData);
    }


    /**
     * 详情
     *
     * @param id
     * @return com.erp.model.oms.dto.RuleDeliveryWarehouseDTO.ViewDTO
     * @author yl
     * @date 2023-08-31 18:31
     */
    @Override
    public RuleDeliveryWarehouseDTO.ViewDTO view(String id) {
        RuleDeliveryWarehouseEntity ruleDeliveryWarehouse = this.getById(id);
        if(null == ruleDeliveryWarehouse){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货仓库规则单");
        }
        RuleDeliveryWarehouseDTO.ViewDTO view = new RuleDeliveryWarehouseDTO.ViewDTO();
        BeanMapper.copy(ruleDeliveryWarehouse, view);
        String type = DictBasicTypeEnum.FIELD.getType();
        List<RuleConditionDTO.ViewDTO> conditionList = ruleConditionService.listByRuleId(id, type);
        view.setConditionList(conditionList);
        return view;
    }

    /**
     * 更改启用禁用状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-30 14:15
     */
    @Override
    public Boolean updateStatus(UpdateStateDTO dto) {
        String id = dto.getId();
        RuleDeliveryWarehouseEntity ruleDeliveryWarehouse = this.getById(id);
        if(null == ruleDeliveryWarehouse){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货仓库规则单");
        }
        Boolean disabled = ruleDeliveryWarehouse.getDisabled();
        if (disabled.equals(dto.getState())) {
            throw new ServiceException(ApiError.ERROR_98027);
        }
        String content = String.format("启用状态[%s]变更为[%s]", disabled ? "启用" : "停用", disabled ? "停用" : "启用");
        ruleDeliveryWarehouse.setDisabled(dto.getState());
        operateLogService.addModuleOperateLog(content, ModuleTypeEnum.RULE_DELIVERY_WAREHOUSE.getCode(), dto.getId(), "状态变更");
        return this.updateById(ruleDeliveryWarehouse);
    }


    /**
     * 获取到发货仓库匹配的结果
     * 存在明细中一个匹配成功即匹配结果成功
     * @param entity
     * @param detailList
     * @param map
     * @return
     */
    @Override
    public SoB2cDTO.RuleResultDTO getRuleOrderMatchResult(SoB2cEntity entity, List<SoB2cDetailEntity> detailList, Map<String, Object> map) {
        //根据优先级获取规则列表
        List<RuleDeliveryWarehouseEntity> ruleDeliveryWarehouselList = this.listOrderByPriority();
        List<String> ruleIdList = ruleDeliveryWarehouselList.stream().map(RuleDeliveryWarehouseEntity::getId).collect(Collectors.toList());
        List<Map<String, Object>> mapList = (List<Map<String, Object>>) map.get("detailList");
        //这个表示有仓库id了就不用匹配了 返回成功
        mapList = mapList.stream().filter(m -> m.get("deliveryWarehouseId") == null ||
                        StringUtils.isBlank(m.getOrDefault("deliveryWarehouseId", "").toString())).
                collect(Collectors.toList());
        if(CollectionUtils.isEmpty(mapList)){
            return SoB2cDTO.RuleResultDTO.builder().id((String) map.get("id")).isRuleMatch(Boolean.TRUE).map(map).build();
        }
        //规则条件
        List<RuleConditionEntity> allRuleConditionList = ruleConditionService.listDbRuleIds(ruleIdList);
        boolean isRuleMatch = Boolean.TRUE;

        //需要更新仓库id的明细
        List<Pair<SoB2cDetailEntity,String>> updateWarehouseList = new ArrayList<>();

        //明细规则匹配
        for(Map<String, Object> detailMap : mapList){
            String detailId = Objects.nonNull(detailMap.get("detailId")) ? detailMap.get("detailId").toString() : null;
            SoB2cDetailEntity soB2cDetail = detailList.stream().filter(e -> StringUtils.isNotBlank(detailId)
                    && e.getId().equals(detailId)).findFirst().orElse(null);
            List<String> detailIdList = StringUtils.isNotEmpty(detailId) ? Collections.singletonList(detailId) : null;
            RuleDeliveryWarehouseDTO.RuleMatchResultDTO ruleMatchResult = compareRules(detailMap, ruleDeliveryWarehouselList, allRuleConditionList);
            //配货规则是否通过
            boolean distributionSuccess = Objects.nonNull(ruleMatchResult);
            if (!distributionSuccess) {
                isRuleMatch = Boolean.FALSE;
                //未匹配到条件
                 soB2cDetailService.updateIsMatchWarehouseRule(entity.getId(),detailIdList);
            } else {
                if(CharSequenceUtil.isNotBlank(ruleMatchResult.getName())){
                    String msg =  CharSequenceUtil.format("自动匹配仓库规则成功，规则名称：{}", ruleMatchResult.getName());
                    operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "配货操作");
                }
                //更新明细仓库信息
                String warehouseId = ruleMatchResult.getWarehouseId();
                //返回了仓库则更新仓库为空的数据
                if (CharSequenceUtil.isNotBlank(warehouseId)) {
                    updateWarehouseList.add(new Pair<>(soB2cDetail,warehouseId));
                }
            }
        }
        //更新仓库
        if (CollectionUtils.isNotEmpty(updateWarehouseList)) {
            soB2cDetailService.updateWarehouse(entity, updateWarehouseList);
        }

        SoB2cDTO.RuleResultDTO ruleResult = new SoB2cDTO.RuleResultDTO();
        ruleResult.setId(entity.getId());
        ruleResult.setIsRuleMatch(isRuleMatch);
        ruleResult.setMap(map);
        return ruleResult;
    }

    /**
     * 匹配规则
     * @param detailMap
     * @param ruleDeliveryWarehouselList
     * @param allRuleConditionList
     * @return
     */
    private RuleDeliveryWarehouseDTO.RuleMatchResultDTO compareRules(Map<String, Object> detailMap, List<RuleDeliveryWarehouseEntity> ruleDeliveryWarehouselList, List<RuleConditionEntity> allRuleConditionList) {
        for (RuleDeliveryWarehouseEntity item : ruleDeliveryWarehouselList) {
            List<RuleConditionEntity> ruleConditionList = allRuleConditionList.stream().
                    filter(r -> r.getRuleId().equals(item.getId())).
                    sorted(Comparator.comparing(RuleConditionEntity::getIndex)).collect(Collectors.toList());

            List<ConditionElement> conditionElementList = BeanMapper.copyList(ruleConditionList, ConditionElement.class);
            //是否缺货的字段
            long isLackCount = conditionElementList.stream().filter(c -> c.getField().equals("isOutStock")).count();
            //表示是有缺货的字段
            if (isLackCount > 0) {
                handleLackData(item.getWarehouseId(),detailMap);
            }
            //获取到表达式
            Boolean matchResult = spElServer.matchDetailExpressionByConditionList(conditionElementList, detailMap);
            if (matchResult) {
                RuleDeliveryWarehouseDTO.RuleMatchResultDTO ruleMatchResult = new RuleDeliveryWarehouseDTO.RuleMatchResultDTO();
                ruleMatchResult.setWarehouseId(item.getWarehouseId());
                ruleMatchResult.setMap(detailMap);
                ruleMatchResult.setName(item.getName());
                return ruleMatchResult;
            }
        }
        return null;
    }


    /**
     * 处理缺货数据的map
     * 将仓库id 放到该值中 在看是否缺货
     * @param warehouseId
     * @param detailMap
     */
    private void handleLackData(String warehouseId, Map<String, Object> detailMap) {

        // 忽略库存计算SKU
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = CollUtil.isNotEmpty(ignoreInventorySkuList) ?
                ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()): Lists.newArrayList();

        String skuId = Objects.nonNull(detailMap.get("skuId")) ? detailMap.get("skuId").toString() : "";
        String detailId = Objects.nonNull(detailMap.get("detailId")) ? detailMap.get("detailId").toString() : "";
        Integer qty = Objects.nonNull(detailMap.get("skuQty")) ? (Integer) detailMap.get("skuQty") : 0;
        //整理传参
        List<String> warehouseIdList = StringUtils.isNotEmpty(warehouseId) ? Collections.singletonList(warehouseId) : null;
        List<String> skuIdList = StringUtils.isNotEmpty(skuId) ? Collections.singletonList(skuId) : null;
        List<String> detailIdList = StringUtils.isNotEmpty(detailId) ? Collections.singletonList(detailId) : null;
        //即时库存数据
        List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList = null;
        if (CollectionUtils.isNotEmpty(skuIdList) && CollectionUtils.isNotEmpty(warehouseIdList)){
            InventoryQtyDTO.SkuInventoryStatusParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
            skuInventoryDTO.setInventoryStatusList(Collections.singletonList(InventoryStatusEnum.USABLE.getCode()));
            skuInventoryDTO.setWarehouseIdList(warehouseIdList);
            skuInventoryDTO.setSkuIdList(skuIdList);
            //即时库存的数据
            inventoryList = inventoryFeign.listSkuInventoryStatusByParam(skuInventoryDTO);
        }


        /**
         *  已付款且未提交发货且未作废的订单SKU的发货数量
         *  根据SKU、仓库、仓位查询SKU数量
         */
        SoB2cDetailDTO.WaitDeliveryParamDTO paramDTO = new SoB2cDetailDTO.WaitDeliveryParamDTO(skuIdList, warehouseIdList, detailIdList);
        List<SoB2cDetailDTO.WaitDeliveryQtyDTO> waitDeliveryQtyList = soB2cDetailService.listWaitDeliveryQty(paramDTO);

        //是否缺货
        Boolean isOutStock = soB2cService.isChildOutStock(inventoryList, waitDeliveryQtyList, ignoreInventorySkuIds,
                skuId,warehouseId, qty);
        detailMap.put("isOutStock", isOutStock);
    }

    /**
     * 根据优先级获取规则列表
     *
     * @return
     */
    private List<RuleDeliveryWarehouseEntity> listOrderByPriority() {
        return this.lambdaQuery().eq(RuleDeliveryWarehouseEntity::getDisabled, Boolean.FALSE).
                orderByAsc(RuleDeliveryWarehouseEntity::getPriority).
                orderByDesc(RuleDeliveryWarehouseEntity::getUpdateTime).
                list();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(RuleDeliveryWarehouseEntity ruleDeliveryWarehouseEntity) {
        // TODO 验证数据 & 数据赋值
        String warehouseId = ruleDeliveryWarehouseEntity.getWarehouseId();
        if (StringUtils.isBlank(warehouseId)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        ruleDeliveryWarehouseEntity.setWarehouseName(warehouseList.get(0).getName());

    }

}
