package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.dto.pickingstrategy.LocationInventoryResultDTO;
import com.erp.model.wms.entity.CfgRulePickingEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 拣货规则表 服务类
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
    void delete(List<String> ids);
    /**
     * 启用/禁用
     * @param dto dto
     */
    void updateStatus(UpdateStateDTO.BatchUpdateDTO dto);

    /**
     * 根据传入参数获取sku对应库位及拣货数量
     * @param map 参考
     *         <p>
     *         Map<String, Object> map = new HashMap<>();<br/>
     *         单据类型<br/>
     *         map.put("billType", PickingBillTypeEnum.B2C.getCode());<br/>
     *         仓库id<br/>
     *         map.put("deliveryWarehouseId", detailEntity.getWarehouseId());<br/>
     *         key skuId value sku数量<br/>
     *         map.put("sku", sku);<br/>
     *         单据号<br/>
     *         map.put("billCode",soB2cDeliveryEntity.getCode());<br/>
     *         key skuId value skuNo<br/>
     *         map.put("skuMap", skuMap);<br/>
     *         </p>
     */
    List<LocationInventoryResultDTO> getRuleOrderMatchResult(Map<String,Object> map);
}
