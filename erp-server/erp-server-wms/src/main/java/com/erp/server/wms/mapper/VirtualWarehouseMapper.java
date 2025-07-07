package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 虚拟仓 Mapper 接口
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
@Mapper
public interface VirtualWarehouseMapper extends BaseMapper<VirtualWarehouseEntity> {

    IPage<VirtualWarehouseDTO.ListDTO> paging(Page query, @Param("params") VirtualWarehouseDTO.PagingParamDTO params);

    List<VirtualWarehouseDTO.VwDTO> getByNames(@Param("nameList")List<String> nameList);

    /**
     * 虚拟仓高级搜索
     * @param params
     * @return
     */
    IPage<VirtualWarehouseDTO.SelectDTO> warehousePagingSelect(@Param("params") PagingDTO<VirtualWarehouseDTO.WarehouseSelectDTO> params);

    /**
     * 虚拟仓列表
     * @param dto
     * @return
     */
    List<VirtualWarehouseDTO.SelectDTO> warehouseSelectList(PagingDTO<VirtualWarehouseDTO.WarehouseSelectDTO> dto);

    List<VirtualWarehouseDTO.SelectDTO> listByParam(@Param("params") VirtualWarehouseDTO.SearchDTO searchDTO);

    /**
     * 高级查询
     */
    List<String> listWarehouseBySql(@Param("compareCodeSplicingValueSql") String compareCodeSplicingValueSql);

    /**
     * 导出虚拟仓设置
     * @param query
     * @param params
     * @return
     */
    IPage<VirtualWarehouseDTO.ExportDTO> exportVirtualWarehouse(Page query,@Param("params") VirtualWarehouseDTO.PagingParamDTO params);

    List<VirtualWarehouseDTO.ViewWarehouseDTO> listWarehouseInfoByIds(@Param("virtualWarehouseIdList") List<String> virtualWarehouseIdList);
}
