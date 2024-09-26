package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.srm.dto.CfgSettingDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.PurchaseReturnStatisticsDTO;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.PoReturnEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 采购退货单 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-04-07
 */
@Mapper
public interface PoReturnMapper extends BaseMapper<PoReturnEntity> {

    IPage<PurchaseReturnOrderDTO.PagingViewDTO> paging(Page query, @Param("params") PurchaseReturnOrderDTO.PagingParamDTO params);

    List<PurchaseReturnOrderDTO.GetReturnQtyDTO> getReturnQty(@Param("purchaseOrderId") String purchaseOrderId);

    List<PurchaseReturnOrderDTO.OrderRefReceiveDTO> purchaseOrderRefReturn(@Param("purchaseOrderId") String purchaseOrderId);
    Integer listCount(@Param("params") PurchaseReturnOrderDTO.PagingParamDTO params);

    Integer pdaListCount(@Param("params") PurchaseReturnOrderDTO.PagingParamDTO params);

    /**
     * 根据供应商id集合、单据日期查询退货数量
     * @param params
     * @return
     */
    List<PurchaseReturnOrderDTO.SupplierReturnDTO> getReturnInfo(@Param("params") PurchaseReturnOrderDTO.SupplierReturnParamDTO params);


    /**
     * PDA:分页查询
     * @Author Luo_WG
     * @Date 2023/8/21 15:17
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.PurchaseReturnOrderDTO.PdaPagingViewDTO>
     **/
    IPage<PurchaseReturnOrderDTO.PdaPagingViewDTO> pdaPaging(Page query, @Param("params") PurchaseReturnOrderDTO.PdaPagingParamDTO params);

    /**
     * 供应商退货分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<PurchaseReturnOrderDTO.SupplierPagingViewDTO> supplierPaging(Page query, @Param("params") PurchaseReturnOrderDTO.SupplierPagingParamDTO params);

    /**
     * SRM供应商退货列表tab页
     * @param params
     * @return
     */
    List<PurchaseReturnOrderDTO.SupplierTabListDTO> supplierTabList( @Param("params") PurchaseReturnOrderDTO.SupplierPagingParamDTO params);

    List<PurchaseReturnStatisticsDTO.StatisticsMonthDTO> statisticsBySupplier(@Param("params") PurchaseReturnStatisticsDTO.RequestDTO returnRequestDTO);

    /**
     * 查询需要自动确认的退货单
     * @Author Luo_WG
     * @Date 2024/1/24 12:43
     * @param list
     * @return java.util.List<com.erp.model.scm.dto.PurchaseOrderDetailDTO.PurchaseOrderConfirmDTO>
     **/
    List<String> listPoReturnAutoConfirm(@Param("params") List<CfgSettingDTO.ViewDTO> list);

    List<PoReturnDetailEntity> listPoReturnByPoDetailIds(@Param("podIds") List<String> podIds,@Param("approveStatusList") List<String> approveStatusList);

    /**
     * 获取委外订单列表
     * @param poIds
     * @return
     */
    List<PurchaseReturnOrderDTO.SubcontractOrderDTO> listSubcontractOrder(@Param("poIds") List<String> poIds);

    /**
     * 批量查询报价
     * @param dto
     * @return
     */
    List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> batchGetTaxPrice(@Param("params") PurchasePriceDetailDTO.PurchaseTaxPriceBatchSearchDTO params);
}
