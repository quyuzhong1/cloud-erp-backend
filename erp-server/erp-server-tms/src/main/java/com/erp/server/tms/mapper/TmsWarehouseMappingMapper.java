package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.TmsWarehouseMappingDTO;
import com.erp.model.tms.entity.TmsWarehouseMappingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-03-19
 */
@Mapper
public interface TmsWarehouseMappingMapper extends BaseMapper<TmsWarehouseMappingEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/3/21 14:26
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<TmsWarehouseMappingDTO.ListDTO> paging(Page query,@Param("params") TmsWarehouseMappingDTO.PagingParamDTO params);
}
