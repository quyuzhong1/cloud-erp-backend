package com.erp.server.wms.service;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.ReportOrderSalesDTO;
import com.erp.model.wms.dto.VirtualTransFlowDTO;
import com.erp.model.wms.dto.VirtualTransFlowDetailDTO;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 虚拟库存交易流水表 服务类
 * </p>
 *
 * @author will
 * @since 2024-06-03
 */
public interface VirtualTransFlowService extends SuperService<VirtualTransFlowEntity> {
    /**
     * 虚拟库存流水分页查询
     * @author will
     * @date 2024/6/3 17:08
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<VirtualTransFlowDTO.ListDTO> paging(PagingDTO<VirtualTransFlowDTO.SearchParamDTO> dto);
    /**
     * 新增流水
     * @author will
     * @date 2024/6/4 18:47
     * @param addDTO
     * @param virtualTansRuleId
     * @param inventoryModeEnum
     */
    VirtualTransFlowEntity add(VirtualTransFlowDTO.AddDTO addDTO, String virtualTansRuleId, InventoryModeEnum inventoryModeEnum);

    /**
     * 根据已存在的流水新增新的流水
     * @author will
     * @date 2024/6/5 10:05
     * @param param
     * @param afterInventoryQty
     * @return Boolean
     */
    VirtualTransFlowEntity add(VirtualTransFlowEntity param, Integer afterInventoryQty);
    /**
     * 根据单据来源和单据id查询出库存交易流水
     * @author will
     * @date 2024/6/5 9:25
     * @param sourceType
     * @param sourceId
     * @return List<VirtualTransFlowEntity>
     */
    List<VirtualTransFlowEntity> getUnApprovedTxnFlows(String sourceType, String sourceId);
    /**
     * 更新流水状态为反审核
     * @author will
     * @date 2024/6/5 10:11
     * @param id
     * @param version
     * @return Boolean
     */
    Boolean updateUnapprovedById(String id, Integer version);
    /**
     * 虚拟库存明细列表
     * @author will
     * @date 2024/6/5 16:07
     * @param dto
     * @return PagingVO<InventoryDetailDTO>
     */
    PagingVO<VirtualTransFlowDTO.InventoryDetailDTO> detailPaging(PagingDTO<VirtualTransFlowDTO.InventoryDetailParamDTO> dto);
    /**
     * 虚拟库存交易流水导出
     *
     * @param dto
     * @return Boolean
     * @author will
     * @date 2024/6/6 15:31
     */
    Boolean exportExcel(VirtualTransFlowDTO.SearchParamDTO dto);

    PagingVO<VirtualTransFlowDTO.ListDTO> exportVirtualTransFlow(PagingDTO<VirtualTransFlowDTO.SearchParamDTO> dto);
    /**
     * 查询历史流水
     * @author will
     * @date 2024/11/8 16:11
     * @param sourceDetailIdList
     * @param sourceType
     * @return List<VirtualTransFlowEntity>
     */
    List<VirtualTransFlowEntity> listHistoryFlow(List<String> sourceDetailIdList,String sourceType);
    /**
     * 查询指定天数最后一条流水
     * @author will
     * @date 2024/11/20 16:05
     * @param skuIdList
     * @param warehouseIdList
     * @param virtualWarehouseIdList
     * @param localDate
     * @return List<LastVirtualQtyDTO>
     */
    List<ReportOrderSalesDTO.LastVirtualQtyDTO> listLastVirtualQty(List<String> skuIdList, List<String> warehouseIdList, List<String> virtualWarehouseIdList, LocalDate localDate);
    /**
     * 查询流水
     * @author will
     * @date 2024/12/20 9:50
     * @param deliveryDetailIdList
     * @return List<VirtualTransFlowEntity>
     */
    List<VirtualTransFlowEntity> listBySourceDetailIdList(List<String> deliveryDetailIdList);
    /**
     * 查询虚拟仓即时库存id
     * @author will
     * @date 2024/12/12 11:44
     * @param virtualInventoryId
     * @param virtualWarehouseId
     * @param warehouseId
     * @param skuId
     * @param fromTable
     * @return List<String>
     */
    List<String> listVirtualInventoryId(String virtualInventoryId, String virtualWarehouseId, String warehouseId, String skuId, Boolean fromTable);
    /**
     * 重算虚拟仓库存流水
     * @author will
     * @date 2024/12/12 12:06
     * @param startDate
     * @param virtualInvId
     */
    void overrideVirtualTransFlow(LocalDate startDate,String virtualInvId);
    /**
     * 查询历史虚拟仓流水数据
     * @author will
     * @date 2024/12/17 17:47
     * @param dto
     * @return List<VirtualTransFlowEntity>
     */
    List<VirtualTransFlowEntity> listHisVirtualTransFlow(VirtualTransFlowDetailDTO.HandleDTO dto);
    /**
     * 查询审核的流水
     * @author will
     * @date 2024/12/20 12:32
     * @param oldVirtualTransFlowIdList
     * @return List<VirtualTransFlowEntity>
     */
    List<VirtualTransFlowEntity> listApproveByIds(List<String> oldVirtualTransFlowIdList);
    /**
     * 查询
     * @author will
     * @date 2024/12/27 19:42
     * @param dto
     */
    List<VirtualTransFlowEntity> listApproveFlowDetail(VirtualTransFlowDetailDTO.HandleDTO dto);
    /**
     * 添加流水
     * @author will
     * @date 2024/12/27 20:10
     * @param dto
     */
    void handleAddDetail(VirtualTransFlowDetailDTO.HandleDTO dto);
    /**
     * 更新备注
     * @author will
     * @date 2024/12/27 20:44
     * @param id
     * @param remark
     */
    void updateRemark(String id, String remark);
    /**
     *
     * @Auther will
     * @Date 2025/2/12 09:40
     * @param sourceType
     * @param sourceDetailId
     * @return VirtualTransFlowEntity
     */
    VirtualTransFlowEntity getUnApprovedTxnFlowBySourceDetailId(String sourceType, String sourceDetailId, LocalDateTime dateTime);
}
