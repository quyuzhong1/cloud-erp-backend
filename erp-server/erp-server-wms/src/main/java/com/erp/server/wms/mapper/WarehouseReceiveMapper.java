package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
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

    Integer getReceiveQty(@Param("PurchaseOrderId") String PurchaseOrderId, @Param("skuId") String skuId);

    List<WarehouseReceiveDTO.OrderRefReceiveDTO> purchaseOrderRefReceive(@Param("purchaseOrderId") String purchaseOrderId);

    List<WarehouseReceiveDTO.GenerateStockInViewDTO> generateStockInView(@Param("id") String id);
}
