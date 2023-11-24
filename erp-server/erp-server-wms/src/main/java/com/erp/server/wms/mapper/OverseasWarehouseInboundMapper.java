package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * 海外仓入库单 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Mapper
public interface OverseasWarehouseInboundMapper extends BaseMapper<OverseasWarehouseInboundEntity> {

    /**
     * 分页查询
     * @author Jim
     * @date: 2023-11-21
     */
    IPage<OverseasWarehouseInboundDTO.ListDTO> paging(Page<?> query, OverseasWarehouseInboundDTO.PagingParamDTO params);
}
