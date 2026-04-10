package com.erp.server.plm.mapper;
import com.erp.model.plm.entity.SkuStdRetailPriceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.plm.dto.SkuStdRetailPriceDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * sku标准零售价表 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2026-03-16
 */
@Mapper
public interface SkuStdRetailPriceMapper extends BaseMapper<SkuStdRetailPriceEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SkuStdRetailPriceDTO.ListDTO> paging(Page query, @Param("params") SkuStdRetailPriceDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SkuStdRetailPriceDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SkuStdRetailPriceDTO.ListDTO> listExport(@Param("params") SkuStdRetailPriceDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SkuStdRetailPriceDTO.TabListDTO> tabList(@Param("params") SkuStdRetailPriceDTO.PagingParamDTO searchParam);
}
