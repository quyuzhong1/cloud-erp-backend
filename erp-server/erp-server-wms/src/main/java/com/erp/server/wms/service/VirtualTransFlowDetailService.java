package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.VirtualTransFlowDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualTransFlowDetailDTO;
import com.erp.model.wms.entity.VirtualTransFlowEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 虚拟仓库存流水明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
public interface VirtualTransFlowDetailService extends SuperService<VirtualTransFlowDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-12-03
    * @param addDTOList
    * @return
    */
    Boolean batchAdd(List<VirtualTransFlowDetailDTO.AddDTO> addDTOList);


    /**
     * 分页查询
     * @author will
     * @date 2024/12/5 10:54
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<VirtualTransFlowDetailDTO.ListDTO> paging(PagingDTO<VirtualTransFlowDetailDTO.SearchParamDTO> dto);
    /**
     * 导出
     * @author will
     * @date 2024/12/5 11:00
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(VirtualTransFlowDetailDTO.SearchParamDTO dto);
    /**
     * 消费
     * @author will
     * @date 2024/12/10 12:28
     * @param virtualTransFlowEntity
     * @return Boolean
     */
    Boolean consumeMessage(VirtualTransFlowEntity virtualTransFlowEntity,String msgId);
    /**
     * 查询虚拟仓库存明细id集合
     * @author will
     * @date 2024/12/12 14:52
     * @param virtualInventoryId
     * @param virtualWarehouseId
     * @param warehouseId
     * @param skuId
     * @param fromTable
     * @return List<String>
     */
    List<String> listVirtualInventoryDetailIdList(String virtualInventoryId, String virtualWarehouseId, String warehouseId, String skuId, Boolean fromTable);
    /**
     * 重算虚拟仓流水
     * @author will
     * @date 2024/12/12 14:57
     * @param startDate
     * @param virtualInvDetailId
     */
    void overrideVirtualTransFlowDetail(LocalDate startDate, String virtualInvDetailId);
}
