package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.WarehouseLocationSuggestAfterSalesDto;
import com.erp.model.wms.entity.WarehouseLocationSuggestAfterSalesEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 仓位售后推荐 Mapper 接口
 * </p>
 *
 * @author liuchao
 * @since 2026-04-30
 */
@Mapper
public interface WarehouseLocationSuggestAfterSalesMapper extends BaseMapper<WarehouseLocationSuggestAfterSalesEntity> {

    IPage<WarehouseLocationSuggestAfterSalesEntity> paging(@Param("page") Page<Object> page, @Param("params") WarehouseLocationSuggestAfterSalesDto.SearchParamDTO params);

    void upsertBatch(@Param("list") List<WarehouseLocationSuggestAfterSalesEntity> list);
}
