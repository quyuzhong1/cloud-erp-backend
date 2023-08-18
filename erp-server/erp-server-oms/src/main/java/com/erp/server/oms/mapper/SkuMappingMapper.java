package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.SkuMappingEntity;
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
public interface SkuMappingMapper extends BaseMapper<SkuMappingEntity> {

    IPage<SkuMappingDTO.PagingViewDTO> paging(Page query, @Param("params") SkuMappingDTO.PagingParamDTO params, @Param("matchResult") Boolean matchResult);

    List<SkuMappingDTO.PagingViewDTO> listExport(@Param("params") SkuMappingDTO.ExportDTO params, @Param("matchResult")Boolean matchResult);
    /**
     * 获取到tab 统计数据
     * @author yl
     * @date 2023-08-18 11:17
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.MatchCountDTO>
     */
    List<SkuMappingDTO.MatchCountDTO> listMatchCount(@Param("params") SkuMappingDTO.FindTabDTO dto);

    IPage<SkuMappingDTO.ProductSkuInfoDTO> listPaging(Page query, @Param("params") SkuMappingDTO.ListParamDTO params);
}
