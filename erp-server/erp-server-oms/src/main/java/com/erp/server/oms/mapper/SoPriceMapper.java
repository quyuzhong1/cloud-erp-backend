package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoPriceDTO;
import com.erp.model.oms.entity.SoPriceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
     * 获取导出数据
     * @author yl
     * @date 2023-03-27 17:57
     * @param dto
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDTO.PagingViewDTO>
     */
    List<SoPriceDTO.PagingViewDTO> getExport(@Param("params") SoPriceDTO.PagingParamDTO dto);
    Page<SoPriceDTO.PagingViewDTO> getExport(@Param("page") Page<SoPriceDTO.PagingViewDTO> page, @Param("params") SoPriceDTO.PagingParamDTO dto);

    /**
     * 获取供应商价格
     * @author yl
     * @date 2023-03-27 17:57
     * @param ids ids
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDTO.PagingViewDTO>
     */
    List<SoPriceDTO.SupplierSkuPrice> listSupplierSkuPrice(@Param("ids") List<String> ids);

    /**
     * @description: 获取所有的供应商价格
     * @author Will
     * @date: 2023/10/27 10:02
     * @param ids
     * @return List<SupplierSkuPrice>
     */
    List<SoPriceDTO.SupplierSkuPrice> listAllSupplierSkuPrice(@Param("ids") List<String> ids);
    /**
     * @description: tab列表页查询
     * @author Will
     * @date: 2024/1/20 9:06
     * @param searchParamDTO
     * @return Integer
     */
    Integer tabList(@Param("params") SoPriceDTO.PagingParamDTO searchParamDTO);
}
