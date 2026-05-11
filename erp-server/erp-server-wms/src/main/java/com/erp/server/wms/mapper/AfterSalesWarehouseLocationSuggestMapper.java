package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.model.wms.entity.AfterSalesWarehouseLocationSuggestEntity;
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
public interface AfterSalesWarehouseLocationSuggestMapper extends BaseMapper<AfterSalesWarehouseLocationSuggestEntity> {

    IPage<AfterSalesWarehouseLocationSuggestEntity> paging(@Param("page") Page<Object> page, @Param("params") AfterSalesWarehouseLocationSuggestDto.SearchParamDTO params);

    void upsertBatch(@Param("list") List<AfterSalesWarehouseLocationSuggestEntity> list);
}
