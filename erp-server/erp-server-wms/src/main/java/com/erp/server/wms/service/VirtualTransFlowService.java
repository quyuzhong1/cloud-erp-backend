package com.erp.server.wms.service;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualTransFlowDTO;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;

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
    Boolean add(VirtualTransFlowDTO.AddDTO addDTO, String virtualTansRuleId, InventoryModeEnum inventoryModeEnum);

    /**
     * 根据已存在的流水新增新的流水
     * @author will
     * @date 2024/6/5 10:05
     * @param param
     * @param afterInventoryQty
     * @return Boolean
     */
    Boolean add(VirtualTransFlowEntity param, Integer afterInventoryQty);
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
}
