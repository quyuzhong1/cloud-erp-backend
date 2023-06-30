package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SkuMapingDTO;
import com.erp.model.oms.entity.SkuMapingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * sku 对照表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Mapper
public interface SkuMapingMapper extends BaseMapper<SkuMapingEntity> {

    IPage<SkuMapingDTO.PagingViewDTO> paging(Page query, @Param("params") SkuMapingDTO.PagingParamDTO params, @Param("matchResult") Boolean matchResult, @Param("nowTime")LocalDateTime nowTime);

    List<SkuMapingDTO.PagingViewDTO> listExport( @Param("params") SkuMapingDTO.ExportDTO params, @Param("matchResult")Boolean matchResult);

    List<SkuMapingDTO.MatchCountDTO> listMatchCount(@Param("permissionSql") String permissionSql);
}
