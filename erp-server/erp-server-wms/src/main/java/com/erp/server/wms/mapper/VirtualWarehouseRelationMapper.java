package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.VirtualWarehouseRelationDTO;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 虚拟仓实体仓关联关系 Mapper 接口
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
@Mapper
public interface VirtualWarehouseRelationMapper extends BaseMapper<VirtualWarehouseRelationEntity> {

    IPage<VirtualWarehouseRelationDTO.SelectResultDTO> warehousePagingSelect(Page query, @Param("params") VirtualWarehouseRelationDTO.SelectDTO params);
    IPage<VirtualWarehouseRelationDTO.SelectResultDTO> vmPagingSelect(Page query, @Param("params") VirtualWarehouseRelationDTO.SelectDTO params);
}
