package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;
import com.erp.model.mrp.entity.CalcSalesInfoDimEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 销量试算表 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Mapper
public interface CalcSalesInfoDimMapper extends BaseMapper<CalcSalesInfoDimEntity> {


    Page<CalcSalesInfoDimDTO.PagingView> paging(@Param("page") Page<CalcSalesInfoDimDTO.PagingView> objectPage, @Param("params") CalcSalesInfoDimDTO.PagingParamDTO params);

}
