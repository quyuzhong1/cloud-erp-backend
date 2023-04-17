package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.dto.excel.WarehouseReceiveExportExcelDTO;
import com.erp.model.wms.entity.PurchaseReturnOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 采购退货单 Mapper 接口
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-07
 */
@Mapper
public interface PurchaseReturnOrderMapper extends BaseMapper<PurchaseReturnOrderEntity> {

    IPage<PurchaseReturnOrderDTO.PagingViewDTO> paging(Page query, @Param("params") PurchaseReturnOrderDTO.PagingParamDTO params);

    Integer getReceiveQty(@Param("PurchaseOrderId") String PurchaseOrderId, @Param("skuId") String skuId);

    List<PurchaseReturnOrderDTO.OrderRefReceiveDTO> purchaseOrderRefReceive(@Param("purchaseOrderId") String purchaseOrderId);

    List<WarehouseReceiveDTO.GenerateStockInViewDTO> generateStockInView(@Param("id") String id);

    //List<WarehouseReceiveExportExcelDTO> warehouseReceiveExportExcel(@Param("params") WarehouseReceiveDTO.PagingParamDTO params);
}
