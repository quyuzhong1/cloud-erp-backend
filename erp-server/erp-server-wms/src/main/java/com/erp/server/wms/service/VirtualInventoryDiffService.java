package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.VirtualInventoryDiffDTO;
import com.erp.model.wms.entity.VirtualInventoryEntity;

import java.util.List;

/**
 * 库存差异 服务类
 *
 * @author will
 * @since 2024-06-03
 */
public interface VirtualInventoryDiffService extends SuperService<VirtualInventoryEntity> {

    /**
     * 库存差异列表
     * @author will
     * @date 2024/6/3 16:34
     * @param dto 
     * @return PagingVO<ListDTO>
     */
    PagingVO<VirtualInventoryDiffDTO.ListDTO> diffPaging(PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto);
    /**
     * 库存差异明细列表
     * @author will
     * @date 2024/6/3 16:45
     * @param dto
     * @return PagingVO<ListDetailQtyDTO>
     */
    PagingVO<VirtualInventoryDiffDTO.ListDetailQtyDTO> diffDetailPaging(PagingDTO<VirtualInventoryDiffDTO.SearchParamDetailDTO> dto);
    /**
     * 库存差异列表导出
     * @author will
     * @date 2024/6/3 17:58
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(VirtualInventoryDiffDTO.SearchParamDTO dto);
    /**
     * 库存差异数量
     * @author will
     * @date 2024/6/11 10:03
     * @param dto
     * @return Integer
     */
    Integer diffPagingCount(PermissionsDTO dto);
    /**
     * 一键调整查询
     * @author will
     * @date 2024/7/17 16:22
     * @param dto
     * @return List<ListDetailQtyDTO>
     */
    List<VirtualInventoryDiffDTO.ListDetailQtyDTO> listDiffDetail(VirtualInventoryDiffDTO.SearchParamDetailDTO dto);
    /**
     * 一键调整保存
     * @author will
     * @date 2024/7/17 18:19
     * @param list
     */
    void updateVirtualInventory(List<VirtualInventoryDiffDTO.UpdateVirtualInventoryDTO> list);
    /**
     * 获取推荐仓位
     * @author will
     * @date 2024/7/17 19:01
     * @param list
     * @return List<ListSuggestQtyDTO>
     */
    List<VirtualInventoryDiffDTO.ListSuggestQtyDTO> listSuggestQty(List<VirtualInventoryDiffDTO.ListSuggestQtyParamDTO> list);

    /**
     * 差异数据
     */
    PagingVO<VirtualInventoryDiffDTO.ListDiffExportDataDTO> exportListDiffExportData(PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto);

    /**
     * 明细数据
     */
    PagingVO<VirtualInventoryDTO.WarehouseStatisticsExcelDTO> exportWarehouseStatisticsData(PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto);
}
