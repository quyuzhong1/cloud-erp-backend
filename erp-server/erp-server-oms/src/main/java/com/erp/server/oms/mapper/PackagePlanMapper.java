package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.PackagePlanDTO;
import com.erp.model.oms.entity.PackagePlanEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.formula.functions.T;


/**
 * <p>
 * 组包计划主表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-10-09
 */
@Mapper
public interface PackagePlanMapper extends BaseMapper<PackagePlanEntity> {

    /**
     * 分页查询组包计划主表
     * @param query 分页查询参数
     * @param params 分页查询参数
     * @return 分页查询结果
     */
    IPage<PackagePlanDTO.PagingViewDTO> paging(@Param("query") Page<T> query, @Param("params") PackagePlanDTO.PagingParamDTO params);

    /**
     * 导出分页查询组包计划主表
     * @param query 分页查询参数
     * @param params 分页查询参数
     * @return 分页查询结果
     */
    IPage<PackagePlanDTO.ExportDTO> exportPaging(@Param("query") Page<T> query, @Param("params") PackagePlanDTO.PagingParamDTO params);
}
