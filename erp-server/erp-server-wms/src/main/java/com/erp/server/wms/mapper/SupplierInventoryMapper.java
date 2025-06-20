package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.entity.SupplierRefWarehouseEntity;
import com.erp.model.wms.dto.SupplierInventoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 即时库存
 * @author will
 * @date 2025/6/19 15:17
 */
@Mapper
public interface SupplierInventoryMapper {
    /**
     * 分页查询
     * @author will
     * @date 2025/6/19 15:18
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<SupplierInventoryDTO.ListDTO> paging(Page query, @Param("params") SupplierInventoryDTO.PagingParamDTO params, @Param("supplierRefWarehouseList") List<SupplierRefWarehouseEntity> supplierRefWarehouseList);
}
