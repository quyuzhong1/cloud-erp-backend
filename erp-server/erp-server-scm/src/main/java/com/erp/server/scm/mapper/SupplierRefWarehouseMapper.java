package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PermissionsDTO;
import com.erp.model.scm.dto.SupplierRefWarehouseDTO;
import com.erp.model.scm.entity.SupplierRefWarehouseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 供应商关联仓库表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-06-18
 */
@Mapper
public interface SupplierRefWarehouseMapper extends BaseMapper<SupplierRefWarehouseEntity> {
    /**
     * 分页查询
     * @author will
     * @date 2025/6/18 17:22
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<SupplierRefWarehouseDTO.ListDTO> paging(Page query, @Param("params") SupplierRefWarehouseDTO.PagingParamDTO params);
    /**
     * 查询tabList
     * @author will
     * @date 2025/6/18 17:36
     * @param params
     * @return List<TabListDTO>
     */
    List<SupplierRefWarehouseDTO.TabListDTO> tabList(@Param("params") PermissionsDTO params);
}
