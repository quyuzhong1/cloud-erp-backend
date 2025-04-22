package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoPriceDTO;
import com.erp.model.oms.entity.SoPriceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 销售价目表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
@Mapper
public interface SoPriceMapper extends BaseMapper<SoPriceEntity> {
    /**
     * 分页查询
     * @param query
     * @param params
     * @return SoPriceDTO.PagingViewDTO
     */
    IPage<SoPriceDTO.PagingViewDTO> paging(Page query, @Param("params") SoPriceDTO.PagingParamDTO params);

    /**
     * @description: tab列表页查询
     * @author Will
     * @date: 2024/1/20 9:06
     * @param searchParamDTO
     * @return Integer
     */
    Integer tabList(@Param("params") SoPriceDTO.PagingParamDTO searchParamDTO);
}
