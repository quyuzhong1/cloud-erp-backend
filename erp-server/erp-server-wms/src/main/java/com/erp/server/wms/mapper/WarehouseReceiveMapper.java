package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.ReturnOrderExcelDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.dto.WarehouseReceiveExcelDTO;
import com.erp.model.wms.dto.excel.WarehouseReceiveExportExcelDTO;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  采购收货单Mapper 接口
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-06
 */
@Mapper
public interface WarehouseReceiveMapper extends BaseMapper<WarehouseReceiveEntity> {

    IPage<WarehouseReceiveDTO.PagingViewDTO> paging(Page query, @Param("params") WarehouseReceiveDTO.PagingParamDTO params);

    Integer listCount(@Param("params") WarehouseReceiveDTO.PagingParamDTO params);

    List<WarehouseReceiveDTO.GetReceiveDTO> getReceiveQty(@Param("purchaseOrderId") String purchaseOrderId);

    List<WarehouseReceiveDTO.OrderRefReceiveDTO> purchaseOrderRefReceive(@Param("purchaseOrderId") String purchaseOrderId);

    List<WarehouseReceiveDTO.GenerateStockInViewDTO> generateStockInView(@Param("ids") List<String> ids);

    List<WarehouseReceiveExcelDTO> warehouseReceiveExportExcel(@Param("params") WarehouseReceiveDTO.PagingParamDTO params);
}
