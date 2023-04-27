package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.PurchaseStockInDTO;
import com.erp.model.wms.entity.PurchaseStockInEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 采购入库单 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
@Mapper
public interface PurchaseStorageMapper extends BaseMapper<PurchaseStockInEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/4/13 14:40
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<PurchaseStockInDTO.ListDTO> paging(Page query,@Param("params") PurchaseStockInDTO.SearchParamDTO params);
    /**
     * @description: 列表查询数量
     * @author Will
     * @date: 2023/4/13 15:10
     * @param searchParamDTO
     * @return Integer
     */
    Integer listCount(@Param("params") PurchaseStockInDTO.SearchParamDTO searchParamDTO);
    /**
     * @description: 导出数据查询
     * @author Will
     * @date: 2023/4/13 17:30
     * @param dto
     * @return List<PurchaseStockInDTO.ListDTO>
     */
    List<PurchaseStockInDTO.ListDTO> listExportExcel(@Param("params") PurchaseStockInDTO.SearchParamDTO dto);
    /**
     * @description: 查询退货单
     * @author Will
     * @date: 2023/4/14 14:23
     * @param ids
     * @return List<ViewGeneratePurchaseReturnOrderDTO>
     */
    List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(@Param("ids") List<String> ids);

    /**
     * 根据采购单获取入库数量
     * @Author Luo_WG
     * @Date 2023/4/18 19:36
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.PurchaseStockInDTO.GetStockInQty>
     **/
    List<PurchaseStockInDTO.GetStockInQty> getStockInQty(@Param("ids") List<String> ids);
    /**
     * @description: 查询关联单据
     * @author Will
     * @date: 2023/4/19 16:15
     * @param purchaseOrderId
     * @return List<OrderRefStockInDTO>
     */
    List<PurchaseStockInDTO.OrderRefStockInDTO> purchaseOrderRefStockIn(@Param("purchaseOrderId") String purchaseOrderId);
}
