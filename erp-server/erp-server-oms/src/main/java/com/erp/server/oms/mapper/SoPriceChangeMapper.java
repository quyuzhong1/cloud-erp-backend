package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoPriceChangeDTO;
import com.erp.model.oms.entity.SoPriceChangeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 销售价变更表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
@Mapper
public interface SoPriceChangeMapper extends BaseMapper<SoPriceChangeEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/10/18 17:21
     * @param query
     * @param params
     * @return IPage<PagingViewDTO>
     */
    IPage<SoPriceChangeDTO.PagingViewDTO> paging(Page query, @Param("params") SoPriceChangeDTO.PagingParamDTO params);
    /**
     * @description: 查询导出
     * @author Will
     * @date: 2023/10/18 17:21
     * @param dto
     * @return List<PagingViewDTO>
     */
    Page<SoPriceChangeDTO.PagingViewDTO> listExport(@Param("page") Page<SoPriceChangeDTO.PagingViewDTO> page, @Param("params") SoPriceChangeDTO.PagingParamDTO dto);

    /**
     * @description: tab集合
     * @author Will
     * @date: 2024/1/20 9:26
     * @param searchParamDTO
     * @return Integer
     */
    Integer tabList(@Param("params") SoPriceChangeDTO.PagingParamDTO searchParamDTO);
}
