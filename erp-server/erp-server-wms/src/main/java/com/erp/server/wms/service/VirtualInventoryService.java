package com.erp.server.wms.service;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.entity.VirtualInventoryEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 虚拟库存表 服务类
 * </p>
 *
 * @author will
 * @since 2024-06-03
 */
public interface VirtualInventoryService extends SuperService<VirtualInventoryEntity> {

    /**
     * 虚拟库存列表
     * @author will
     * @date 2024/6/3 14:58
     * @param dto PagingVO<ListDTO>
     */
    PagingVO<VirtualInventoryDTO.ListDTO> paging(PagingDTO<VirtualInventoryDTO.SearchParamDTO> dto);
    /**
     * 库存差异明细导出
     * @author will
     * @date 2024/6/3 17:17
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(VirtualInventoryDTO.SearchParamDTO dto, HttpServletResponse response);
    /**
     * 根据仓库、sku、库存状态查询
     * @author will
     * @date 2024/6/4 12:19
     * @param virtualWarehouseId
     * @param warehouseId
     * @param skuId
     * @param inventoryStatus
     * @return VirtualInventoryEntity
     */
    VirtualInventoryEntity findVirtualInventoryStock(String virtualWarehouseId,String warehouseId, String skuId, String inventoryStatus);
    /**
     * 保存虚拟库存数据
     * @author will
     * @date 2024/6/4 14:14
     * @param virtualWarehouseId
     * @param warehouseId
     * @param skuId
     * @param skuNo
     * @param inventoryStatus
     * @param qty
     * @return VirtualInventoryEntity
     */
    VirtualInventoryEntity addOrUpdate(String virtualWarehouseId,String warehouseId , String skuId, String skuNo, String inventoryStatus, Integer qty);

    /**
     * 更新库存数量
     * @author will
     * @date 2024/6/4 14:37
     * @param id
     * @param qty
     * @return boolean
     */
    boolean updateQtyById(String id, Integer qty);

    /**
     * 根据条件查询库存信息
     *
     * @param qtyTypeDTO
     * @author hyj
     * @date 2024/6/6
     */
    List<VirtualInventoryDTO.ViewQtyDTO> getQty(VirtualInventoryDTO.QtyTypeDTO qtyTypeDTO);
}
