package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.dto.pickingstrategy.LocationInventoryResultDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.model.wms.entity.PickingListsEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 拣货单 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-06-07
 */
public interface PickingListsService extends SuperService<PickingListsEntity> {
    /**
     * 分页查询
     *
     * @param dto 分页查询条件
     */
    PagingVO<PickingListsDTO.PagingView> paging(PagingDTO<PickingListsDTO.PagingParam> dto);

    /**
     * 修改
     *
     * @param dto 编辑参数
     **/
    void add(PickingListsDTO.AddDTO dto);

    /**
     * 批量删除
     *
     * @param id id
     **/
    void delete(String id);
    /**
     * 查询详情
     *
     * @param id id
     **/
    PickingListsDTO.View view(String id);
    /**
     * 批量打印
     *
     * @param dto dto
     **/
    void export(PickingListsDTO.ExportDTO dto);
    /**
     * 批量打印
     *
     * @param ids ids
     */
    List<PickingListsDTO.PrintView> print(List<String> ids);
    /**
     * 修改
     *
     * @param dto 编辑参数
     **/
    void update(PickingListsDTO.UpdateDTO dto);

    /**
     * 删除拣货单
     * @param ids id
     */
    void deleteBySourceId(List<String> ids);

    /**
     * 根据来源id查询拣货单及明细
     */
    List<PickingListsDTO.SourceView> listBySourceIds(List<String> sourceIds);

    void exist(String id);
    void exist(List<String> id);

    /**
     * B2C发货单生成拣货单逻辑
     *
     * @param soB2cDeliveryEntity soB2c发货单
     * @param executionData       需要执行拣货规则的数据
     * @param warehouseMap        仓库id和仓库名映射
     * @param results 前置规则返回的仓位数据
     */
    List<String> generateSoB2cPicking(SoB2cDeliveryEntity soB2cDeliveryEntity, CfgRulePickingDTO.CfgExecutionDataDTO executionData, Map<String, String> warehouseMap, List<LocationInventoryResultDTO> results);
    /**
     * 根据来源单id汇总sku拣货数量
     * @param sourceIds
     * @return
     */
    List<PickingListsDTO.DetailPickDTO> listDetailBySourceIds(List<String> sourceIds);
    /**
     * 导出
     *
     * @param dto dto
     **/
    PagingVO<PickingListsDTO.ExportInfoDTO> exportPickingLists(PagingDTO<PickingListsDTO.ExportDTO> dto);

    void initDelivery(List<String> codes);
    /**
     * 处理组合sku
     */
    void generatePicking(PickingListsDTO.AddDTO dto);

    List<PickingDetailDTO.ChangeQtyView> generateRequisitionChange(PickingListsDTO.UpdateDTO dto);

    void updateByChange(List<PickingDetailEntity> updatePickingList, List<String> sourceDetailIds);
}
