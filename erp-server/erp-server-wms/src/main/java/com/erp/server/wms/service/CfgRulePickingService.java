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
    Pair<List<LocationInventoryResultDTO>, Map<String, Integer>> getSoB2CRuleOrderMatchResult(CfgRulePickingDTO.CfgExecutionDataDTO dto);

    Pair<List<LocationInventoryResultDTO>, Map<String, Integer>> getSoB2CRuleOrderMatchResult(CfgRulePickingDTO.CfgExecutionDataDTO dto,Pair<List<CfgRulePickingDTO.CfgRulePickingInventoryDTO>, List<WarehouseLocationEntity>> listListPair);
    /**
     * 拣货明细转换为规则执行数据明细
     */
    CfgRulePickingDTO.CfgExecutionDataDTO getPickingRuleExecutionData(PickingListsDTO.AddDTO dto);
    /**
     * 根据拣货策略条件，进行拣货策略的匹配
     * @param executionData 规则执行数据
     * @param determiningCondition 确定条件
     * @param ruleType 规则类型 RuleTypeEnum
     */
    Pair<List<CfgRulePickingDTO.CfgRulePickingInventoryDTO>, List<WarehouseLocationEntity>> matchRuleActionList(CfgRulePickingDTO.CfgExecutionDataDTO executionData,String determiningCondition, String ruleType);
}
