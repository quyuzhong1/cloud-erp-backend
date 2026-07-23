package com.erp.server.wms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.dto.pickingstrategy.LocationInventoryResultDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.CfgRulePickingEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import org.apache.commons.math3.util.Pair;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 仓位推荐表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
public interface CfgRulePickingService extends SuperService<CfgRulePickingEntity> {
    /**
     * 分页查询
     * @param dto 分页查询条件
     */
    PagingVO<CfgRulePickingDTO.PagingView> paging(PagingDTO<CfgRulePickingDTO.PagingParam> dto);
    /**
     * 新增
     * @param dto 新增参数
     */
    void add(CfgRulePickingDTO.Add dto);
    /**
     * 修改
     * @param dto 编辑参数
     **/
    void update(CfgRulePickingDTO.Update dto);
    /**
     * 查询详情
     * @param id id
     **/
    CfgRulePickingDTO.View view(String id);
    /**
     * 批量删除
     * @param ids ids
     **/
    List<BatchResultDTO> delete(List<String> ids);
    /**
     * 启用/禁用
     * @param dto dto
     */
    void updateStatus(UpdateStateDTO.BatchUpdateDTO dto);

    /**
     * 限制来源单据只有一个,返回可能存在多个仓库id(来源单据sku对应拣货仓库可能不同), 需要根据warehouseId分组生成拣货单
     * 根据传入参数获取sku对应库位及拣货数量
     *
     * @param dto 参数
     */
    /**
     * @return first=拣货占用结果；second=缺货 Map，key=warehouseId#skuNo
     */
    Pair<List<LocationInventoryResultDTO>, Map<String, Integer>> getSoB2CRuleOrderMatchResult(CfgRulePickingDTO.CfgExecutionDataDTO dto);

    /**
     * @return first=拣货占用结果；second=缺货 Map，key=warehouseId#skuNo
     */
    Pair<List<LocationInventoryResultDTO>, Map<String, Integer>> getSoB2CRuleOrderMatchResult(CfgRulePickingDTO.CfgExecutionDataDTO dto,Pair<List<CfgRulePickingDTO.CfgRulePickingInventoryDTO>, List<WarehouseLocationEntity>> listListPair);
    /**
     * 拣货明细转换为规则执行数据明细
     */
    CfgRulePickingDTO.CfgExecutionDataDTO getPickingRuleExecutionData(PickingListsDTO.AddDTO dto);
    /**
     * 按单据条件命中仓位推荐规则，并查询候选仓位库存。
     * <p>
     * ruleType 决定动作类型与禁用标识过滤：拣货 / 补货 / 出库。
     *
     * @param executionData        规则执行数据（单据头 + SKU 明细）
     * @param determiningCondition 库存过滤条件，如 gt；传 null 表示不过滤数量
     * @param ruleType             规则类型，见 {@link com.erp.model.wms.enums.RuleTypeEnum}
     * @return first=仓位库存候选，second=相关仓位主数据
     */
    Pair<List<CfgRulePickingDTO.CfgRulePickingInventoryDTO>, List<WarehouseLocationEntity>> matchRuleActionList(CfgRulePickingDTO.CfgExecutionDataDTO executionData,String determiningCondition, String ruleType);

    /**
     * 按单据条件匹配命中的仓位推荐规则（仅规则，不含库存）。
     * <p>
     * 流程：启用规则 → 按 ruleType 过滤业务禁用标识 → SpEL 条件匹配 → 优先级排序。
     * 未命中返回空列表（不抛异常）。
     *
     * @param executionData 规则执行数据
     * @param ruleType      规则类型，见 {@link com.erp.model.wms.enums.RuleTypeEnum}
     * @return 命中规则列表（已按优先级、更新时间排序）
     */
    List<CfgRulePickingEntity> listMatchedRules(CfgRulePickingDTO.CfgExecutionDataDTO executionData, String ruleType);

    /**
     * 按补货仓位推荐解析缺货 SKU 的取货/上架仓位。
     * <p>
     * 取货：命中规则的补货动作库区优先级 + 单仓位可用量 ≥ 缺货数量；找不到抛 {@code WH_REPLENISH_FROM_LOCATION_NOT_FOUND}。<br>
     * 上架：按规则 {@code inWarehouseLocation} 解析：
     * <ul>
     *   <li>large / small → SKU 大件/小件推荐仓位</li>
     *   <li>recent → 优先产品小货区，否则拣货区最新出入库流水；无流水抛 {@code WH_REPLENISH_TO_LOCATION_NOT_CONFIGURED}</li>
     * </ul>
     * 无补货规则或本仓无补货动作时抛 {@code WH_LOCATION_SUGGEST_NOT_FOUND}。
     *
     * @param executionData 规则执行数据（用于命中补货规则）
     * @param warehouseId   仓库 ID
     * @param shortageItems 缺货 SKU 列表（含缺货数量）
     * @return 每个 SKU 的取货/上架仓位建议
     */
    List<CfgRulePickingDTO.ReplenishLocationSuggestDTO> resolveReplenishLocations(
            CfgRulePickingDTO.CfgExecutionDataDTO executionData,
            String warehouseId,
            List<CfgRulePickingDTO.ReplenishShortageItemDTO> shortageItems);

    /**
     * 按出库仓位推荐解析明细的出库仓位。
     * <p>
     * 命中 {@code WAREHOUSE_LOCATION_OUT_STOCK} 规则后，按出库动作库区优先级查找可用库存仓位：
     * <ul>
     *   <li>仓位在拣货区 → 明细直接赋该仓位（{@code needMove=false}）</li>
     *   <li>仓位非拣货区 → 需先移至空仓位 {@code ""}（{@code needMove=true}，{@code targetLocation=""}）</li>
     * </ul>
     * 无规则抛 {@code WH_OUT_STOCK_RULE_NOT_FOUND}；无足够可用库存抛 {@code WH_OUT_STOCK_LOCATION_NOT_FOUND}。
     *
     * @param executionData 规则执行数据（用于命中出库规则）
     * @param warehouseId   仓库 ID
     * @param items         出库明细（含数量）
     * @return 每个明细的出库仓位建议
     */
    List<CfgRulePickingDTO.OutStockLocationSuggestDTO> resolveOutStockLocations(
            CfgRulePickingDTO.CfgExecutionDataDTO executionData,
            String warehouseId,
            List<CfgRulePickingDTO.OutStockItemDTO> items);
}
