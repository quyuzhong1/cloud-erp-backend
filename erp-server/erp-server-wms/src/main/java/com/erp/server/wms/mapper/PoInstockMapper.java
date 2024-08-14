package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.FirstMassInstockDTO;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.entity.PoInstockEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
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
public interface PoInstockMapper extends BaseMapper<PoInstockEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/4/13 14:40
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<PoInstockDTO.ListDTO> paging(Page query, @Param("params") PoInstockDTO.SearchParamDTO params);
    /**
     * @description: 列表查询数量
     * @author Will
     * @date: 2023/4/13 15:10
     * @param searchParamDTO
     * @return Integer
     */
    Integer listCount(@Param("params") PoInstockDTO.SearchParamDTO searchParamDTO);

    Integer pdaListCount(@Param("params") PoInstockDTO.SearchParamDTO searchParamDTO);
    /**
     * @description: 导出数据查询
     * @author Will
     * @date: 2023/4/13 17:30
     * @param dto
     * @return List<PurchaseStockInDTO.ListDTO>
     */
    List<PoInstockDTO.ListDTO> listExportExcel(@Param("params") PoInstockDTO.ExportParamDTO dto);
    Page<PoInstockDTO.ListDTO> listExportExcel(@Param("page") Page<PoInstockDTO.ListDTO> page, @Param("params") PoInstockDTO.ExportParamDTO dto);
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
    List<PoInstockDTO.GetStockInQty> getStockInQty(@Param("ids") List<String> ids);
    /**
     * @description: 查询关联单据
     * @author Will
     * @date: 2023/4/19 16:15
     * @param purchaseOrderId
     * @return List<OrderRefStockInDTO>
     */
    List<PoInstockDTO.OrderRefStockInDTO> purchaseOrderRefStockIn(@Param("purchaseOrderId") String purchaseOrderId);

    /**
     * 根据供应商id集合获取入库单量和入库数量
     * @param supplierIds
     * @return
     */
    List<PoInstockDTO.SupplierInstockInfoDTO> getInstockInfoBySupplierIds(@Param("supplierIds") List<String> supplierIds,
                                                                          @Param("dateList") List<LocalDate> dateList);


    /**
     * PDA:分页查询
     * @Author Luo_WG
     * @Date 2023/8/16 16:12
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.PoInstockDTO.PdaPagingView>
     **/
    IPage<PoInstockDTO.PdaPagingView> pdaPaging(Page query, @Param("params") PoInstockDTO.PdaSearchParamDTO params);

    /**
     * 查询首批入库日期
     * @param ids
     * @return list
     */
    List<FirstMassInstockDTO> listFirstMassInstock(@Param("ids") List<String> ids);

    /**
     * 统计金额
     * @author yl
     * @date 2023-10-24 12:15
     * @param dto
     * @return com.erp.model.wms.dto.PoInstockDTO.PagingTotalDTO
     */
    PoInstockDTO.PagingTotalDTO pagingTotal(@Param("params") PoInstockDTO.SearchParamDTO  dto);
}
