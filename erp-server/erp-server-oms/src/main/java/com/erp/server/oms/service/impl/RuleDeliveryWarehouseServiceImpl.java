package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.RuleConditionDTO;
import com.erp.model.oms.dto.RuleDeliveryWarehouseDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.RuleConditionEntity;
import com.erp.model.oms.entity.RuleDeliveryWarehouseEntity;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @Autowired
    private WmsTaskFeign wmsTaskFeign;

    @Autowired
    private RuleConditionService ruleConditionService;

    @Autowired
    private SpElServer spElServer;


    @Autowired
    private InventoryFeign inventoryFeign;

    @Autowired
    private SoB2cDetailService soB2cDetailService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

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
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR, expression);
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
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "发货仓库规则单", ruleDeliveryWarehouseEntity.getId());
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
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发货仓库规则单"));
        List<RuleConditionDTO.UpdateDTO> conditionList = updateDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(),"")).collect(Collectors.toList());
        SpElExpressionDTO sqElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = sqElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (!checkResult) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR, expression);
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
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), ruleDeliveryWarehouseEntity.getId(), "发货仓库规则单");
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
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
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
        Optional.ofNullable(ruleDeliveryWarehouse).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发货仓库规则单"));
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
        Optional.ofNullable(ruleDeliveryWarehouse).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发货仓库规则单"));
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
     *
     * @param map
     * @return
     */
    @Override
    public RuleDeliveryWarehouseDTO.RuleMatchResultDTO getRuleOrderMatchResult(Map<String,Object> map) {
        //根据优先级获取规则列表
        List<RuleDeliveryWarehouseEntity> ruleDeliveryWarehouselList = this.listOrderByPriority();
        List<String> ruleIdList = ruleDeliveryWarehouselList.stream().map(RuleDeliveryWarehouseEntity::getId).collect(Collectors.toList());
        List<Map<String, Object>> mapList = (List<Map<String, Object>>) map.get("detailList");
        //deliveryWarehouseId
        //这个表示有仓库id了就不用匹配了 返回成功
        mapList = mapList.stream().filter(m -> m.get("deliveryWarehouseId") == null ||
                        StringUtils.isBlank(m.getOrDefault("deliveryWarehouseId", "").toString())).
                collect(Collectors.toList());
        if(CollectionUtils.isEmpty(mapList)){
            return new RuleDeliveryWarehouseDTO.RuleMatchResultDTO();
        }
        map.put("detailList",mapList);
        //规则条件
        List<RuleConditionEntity> allRuleConditionList = ruleConditionService.listDbRuleIds(ruleIdList);
        for (RuleDeliveryWarehouseEntity item : ruleDeliveryWarehouselList) {
            String warehouseId = item.getWarehouseId();
            String ruleId = item.getId();
            List<RuleConditionEntity> ruleConditionList = allRuleConditionList.stream().
                    filter(r -> r.getRuleId().equals(ruleId)).
                    sorted(Comparator.comparing(RuleConditionEntity::getIndex)).collect(Collectors.toList());

            List<ConditionElement> conditionElementList = BeanMapper.copyList(ruleConditionList, ConditionElement.class);
            //是否缺货的字段
            long isLackCount = conditionElementList.stream().filter(c -> c.getField().equals("isOutStock")).count();
            //表示是有缺货的字段
            if (isLackCount > 0) {
                handleLackData(warehouseId,map);
            }
            //获取到表达式
            Boolean matchResult = spElServer.matchExpressionByConditionList(conditionElementList, map);
            if (matchResult) {
                RuleDeliveryWarehouseDTO.RuleMatchResultDTO ruleMatchResult = new RuleDeliveryWarehouseDTO.RuleMatchResultDTO();
                ruleMatchResult.setWarehouseId(warehouseId);
                ruleMatchResult.setMap(map);
                return ruleMatchResult;
            }
        }
        return null;

    }


    /**
     * 处理缺货数据的map
     * 将仓库id 放到该值中 在看是否缺货
     * @param warehouseId
     * @param map
     */
    private void handleLackData(String warehouseId, Map<String, Object> map) {

        // 忽略库存计算SKU
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = CollUtil.isNotEmpty(ignoreInventorySkuList) ?
                ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()): Lists.newArrayList();

        String usable = InventoryStatusEnum.USABLE.getCode();

        List<Map<String, Object>> mapList = (List<Map<String, Object>>) map.getOrDefault("detailList",new ArrayList<>(0));
        List<String> skuIdList = mapList.stream().filter(m -> m.get("skuId") != null && StringUtils.isNotBlank(m.get("skuId").toString())).
                map(m -> m.get("skuId").toString()).collect(Collectors.toList());

        List<String> detailIdList = mapList.stream().filter(m -> m.get("detailId") != null && StringUtils.isNotBlank(m.get("detailId").toString())).
                map(m -> m.get("detailId").toString()).collect(Collectors.toList());
        //即时库存数据
        InventoryQtyDTO.SkuInventoryStatusParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
        skuInventoryDTO.setInventoryStatusList(Arrays.asList(InventoryStatusEnum.USABLE.getCode()));
        List<String> warehouseIdList = Arrays.asList(warehouseId);
        skuInventoryDTO.setWarehouseIdList(warehouseIdList);
        skuInventoryDTO.setSkuIdList(skuIdList);
        //即时库存的数据
        List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList = inventoryFeign.listSkuInventoryStatusByParam(skuInventoryDTO);

        /**
         *  已付款且未提交发货且未作废的订单SKU的发货数量
         *  根据SKU、仓库、仓位查询SKU数量
         */
        SoB2cDetailDTO.WaitDeliveryParamDTO paramDTO = new SoB2cDetailDTO.WaitDeliveryParamDTO(skuIdList,warehouseIdList,detailIdList);
        List<SoB2cDetailDTO.WaitDeliveryQtyDTO> waitDeliveryQtyList = soB2cDetailService.listWaitDeliveryQty(paramDTO);
        for (Map<String, Object> item : mapList) {
            String skuId = item.get("skuId") != null ? item.get("skuId").toString() : "";
            //数量
            Integer qty = item.get("skuQty") != null ? Integer.valueOf(item.get("skuId").toString()) : 0;
            //可用库存
            Integer useableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(skuId)
                            && obj.getWarehouseId().equals(warehouseId)
                            && usable.equals(obj.getInventoryStatus()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal()))
                    .orElse(MathUtil.ZERO);
            //待发货数量
            Integer waitDeliveryQty = waitDeliveryQtyList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), skuId)
                            && StrUtil.equals(obj.getWarehouseId(), warehouseId))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getQty())).orElse(MathUtil.ZERO);

            Boolean isOutStock = (qty> useableQty - waitDeliveryQty) && !ignoreInventorySkuIds.contains(skuId) ;
            item.put("isOutStock", isOutStock);
        }
        long isOutStockCount = mapList.stream().filter(m-> (boolean) m.get("isOutStock")).count();
        map.put("isOutStock", isOutStockCount > 0);
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
